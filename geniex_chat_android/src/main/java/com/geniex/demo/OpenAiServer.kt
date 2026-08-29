package com.geniex.demo

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Minimal OpenAI-compatible HTTP server for local USB/LAN clients. */
class OpenAiServer(
    private val port: Int,
    private val modelName: () -> String,
    private val complete: suspend (List<ApiMessage>) -> String,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val requestLock = Mutex()
    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var acceptJob: Job? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return
        acceptJob = scope.launch {
            try {
                ServerSocket(port, 50, InetAddress.getByName("0.0.0.0")).use { server ->
                    serverSocket = server
                    while (running.get()) {
                        runCatching { server.accept() }
                            .onSuccess { socket -> scope.launch { handle(socket) } }
                            .onFailure { if (running.get()) it.printStackTrace() }
                    }
                }
            } finally {
                serverSocket = null
            }
        }
    }

    fun stop() {
        running.set(false)
        runCatching { serverSocket?.close() }
        acceptJob?.cancel()
        acceptJob = null
    }

    private suspend fun handle(socket: Socket) {
        socket.use { client ->
            client.soTimeout = 30_000
            val reader = BufferedReader(InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
            val writer = BufferedWriter(OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))
            val requestLine = reader.readLine() ?: return
            val headers = mutableMapOf<String, String>()
            while (true) {
                val line = reader.readLine() ?: return
                if (line.isEmpty()) break
                line.substringBefore(':').trim().lowercase().let { key ->
                    headers[key] = line.substringAfter(':', "").trim()
                }
            }
            val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
            if (contentLength > MAX_BODY_BYTES) {
                respond(writer, 413, error("request body too large"))
                return
            }
            val body = CharArray(contentLength)
            reader.read(body, 0, contentLength)
            val parts = requestLine.split(' ')
            if (parts.size < 2) {
                respond(writer, 400, error("invalid HTTP request"))
                return
            }
            val method = parts[0]
            val path = parts[1].substringBefore('?')
            when {
                method == "GET" && path == "/v1/models" -> respond(writer, 200, models())
                method == "POST" && path == "/v1/chat/completions" -> chat(writer, String(body))
                else -> respond(writer, 404, error("not found"))
            }
        }
    }

    private suspend fun chat(writer: BufferedWriter, body: String) {
        try {
            val root = Json.parseToJsonElement(body).jsonObject
            val messages = root["messages"]?.jsonArray?.mapNotNull { element ->
                val message = element.jsonObject
                val content = message["content"]?.jsonPrimitive?.contentOrNull
                val role = message["role"]?.jsonPrimitive?.contentOrNull
                if (role != null && content != null) ApiMessage(role, content) else null
            }.orEmpty()
            if (messages.isEmpty()) {
                respond(writer, 400, error("messages must contain at least one text message"))
                return
            }
            val text = requestLock.withLock { complete(messages) }
            val response = buildJsonObject {
                put("id", "chatcmpl-${UUID.randomUUID()}")
                put("object", "chat.completion")
                put("created", System.currentTimeMillis() / 1000)
                put("model", modelName())
                put("choices", JsonArray(listOf(buildJsonObject {
                    put("index", 0)
                    put("message", buildJsonObject {
                        put("role", "assistant")
                        put("content", text)
                    })
                    put("finish_reason", "stop")
                })))
            }
            respond(writer, 200, response)
        } catch (error: Exception) {
            respond(writer, 400, error(error.message ?: "invalid request"))
        }
    }

    private fun models(): JsonObject = buildJsonObject {
        put("object", "list")
        put("data", JsonArray(listOf(buildJsonObject {
            put("id", modelName())
            put("object", "model")
            put("owned_by", "geniex")
        })))
    }

    private fun error(message: String): JsonObject = buildJsonObject {
        put("error", buildJsonObject {
            put("message", message)
            put("type", "invalid_request_error")
        })
    }

    private fun respond(writer: BufferedWriter, status: Int, payload: JsonObject) {
        val body = Json.encodeToString(JsonObject.serializer(), payload)
        writer.write("HTTP/1.1 $status ${STATUS_TEXT[status] ?: "OK"}\r\n")
        writer.write("Content-Type: application/json\r\n")
        writer.write("Content-Length: ${body.toByteArray(StandardCharsets.UTF_8).size}\r\n")
        writer.write("Connection: close\r\n\r\n")
        writer.write(body)
        writer.flush()
    }

    companion object {
        private const val MAX_BODY_BYTES = 1_048_576
        private val STATUS_TEXT = mapOf(200 to "OK", 400 to "Bad Request", 404 to "Not Found", 413 to "Payload Too Large")
    }
}

data class ApiMessage(val role: String, val content: String)
