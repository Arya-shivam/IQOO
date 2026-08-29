package com.opengranola.android.sync

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.opengranola.android.calendar.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

data class SyncStatus(
    val label: String = "Not configured",
    val lastSync: Long = 0L,
    val itemCount: Int = 0
)

class SyncStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("sync", Context.MODE_PRIVATE)
    private val secrets = SecretStore(prefs)

    fun save(gmailClientId: String, gmailClientSecret: String, gmailRefreshToken: String, githubToken: String, githubRepos: String) {
        if (gmailClientId.isNotBlank()) secrets.put("gmail_client_id", gmailClientId)
        if (gmailClientSecret.isNotBlank()) secrets.put("gmail_client_secret", gmailClientSecret)
        if (gmailRefreshToken.isNotBlank()) secrets.put("gmail_refresh_token", gmailRefreshToken)
        if (githubToken.isNotBlank()) secrets.put("github_token", githubToken)
        if (githubRepos.isNotBlank()) prefs.edit().putString("github_repos", githubRepos).apply()
    }

    fun configured(): Boolean = gmailReady() || githubReady()
    fun gmailReady(): Boolean = secrets.get("gmail_refresh_token").isNotBlank() && secrets.get("gmail_client_id").isNotBlank() && secrets.get("gmail_client_secret").isNotBlank()
    fun githubReady(): Boolean = secrets.get("github_token").isNotBlank() && repos().isNotEmpty()
    fun repos(): List<String> = prefs.getString("github_repos", "").orEmpty().split(',', '\n').map(String::trim).filter(String::isNotBlank)
    fun status(): SyncStatus = SyncStatus(
        label = prefs.getString("status", if (configured()) "Ready" else "Not configured").orEmpty(),
        lastSync = prefs.getLong("last_sync", 0L),
        itemCount = prefs.getInt("item_count", 0)
    )
    fun setStatus(label: String, count: Int = runCatching { JSONArray(context().ifBlank { "[]" }).length() }.getOrDefault(0)) = prefs.edit()
        .putString("status", label).putLong("last_sync", System.currentTimeMillis()).putInt("item_count", count).apply()
    fun gmailHistoryId(): String? = prefs.getString("gmail_history_id", null)
    fun setGmailHistoryId(value: String) = prefs.edit().putString("gmail_history_id", value).apply()
    fun githubSha(repo: String): String? = prefs.getString("github_sha_$repo", null)
    fun setGithubSha(repo: String, sha: String) = prefs.edit().putString("github_sha_$repo", sha).apply()
    fun secret(name: String): String = secrets.get(name)
    fun context(): String = secrets.get("context")
    fun appendContext(items: List<JSONObject>) {
        val existing = JSONArray(context().ifBlank { "[]" })
        val byId = linkedMapOf<String, JSONObject>()
        for (i in 0 until existing.length()) {
            val item = existing.optJSONObject(i) ?: continue
            byId[item.optString("id")] = item
        }
        items.forEach { byId[it.optString("id")] = it }
        val result = JSONArray()
        byId.values.takeLast(100).forEach(result::put)
        secrets.put("context", result.toString())
    }

    private class SecretStore(private val prefs: android.content.SharedPreferences) {
        private val keyAlias = "open_granola_sync"
        private fun key() = KeyStore.getInstance("AndroidKeyStore").run {
            load(null)
            if (!containsAlias(keyAlias)) {
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
                    init(KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
                    generateKey()
                }
            }
            getKey(keyAlias, null)
        }
        fun put(name: String, value: String) {
            if (value.isBlank()) { prefs.edit().remove("secret_$name").apply(); return }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
            val payload = cipher.iv + cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
            prefs.edit().putString("secret_$name", Base64.encodeToString(payload, Base64.NO_WRAP)).apply()
        }
        fun get(name: String): String = runCatching {
            val payload = Base64.decode(prefs.getString("secret_$name", null), Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, payload.copyOfRange(0, 12)))
            String(cipher.doFinal(payload.copyOfRange(12, payload.size)), StandardCharsets.UTF_8)
        }.getOrDefault("")
    }
}

object SyncScheduler {
    private const val PERIODIC_NAME = "open_granola_sync"
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(12, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
    fun syncNow(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "${PERIODIC_NAME}_manual", ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            ).build()
        )
    }
}

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val store = SyncStore(applicationContext)
        if (!store.configured() && !CalendarRepository.isEnabled(applicationContext)) return@withContext Result.success()
        store.setStatus("Syncing…")
        runCatching {
            val items = buildList {
                if (store.gmailReady()) addAll(syncGmail(store))
                if (store.githubReady()) addAll(syncGithub(store))
                addAll(CalendarRepository.readUpcoming(applicationContext))
            }
            store.appendContext(items)
            store.setStatus("Synced ${items.size} new item${if (items.size == 1) "" else "s"}")
            Result.success()
        }.getOrElse {
            store.setStatus("Sync failed: ${it.message ?: "check credentials"}")
            Result.retry()
        }
    }
}

private fun syncGmail(store: SyncStore): List<JSONObject> {
    val accessToken = refreshGmailToken(store)
    val historyId = store.gmailHistoryId()
    val ids = if (historyId == null) gmailLatestIds(accessToken) else runCatching {
        getJson("https://gmail.googleapis.com/gmail/v1/users/me/history?startHistoryId=${encode(historyId)}&historyTypes=messageAdded", accessToken)
            .optJSONArray("history").messageIdsFromHistory()
    }.getOrElse { gmailLatestIds(accessToken) }
    val profile = getJson("https://gmail.googleapis.com/gmail/v1/users/me/profile", accessToken)
    store.setGmailHistoryId(profile.optString("historyId"))
    return ids.distinct().take(20).map { id ->
        val message = getJson("https://gmail.googleapis.com/gmail/v1/users/me/messages/${encode(id)}?format=full", accessToken)
        JSONObject().put("id", "gmail:$id").put("source", "Gmail").put("title", message.headers().optString("Subject", "(no subject)"))
            .put("detail", message.headers().optString("From") + "\n" + message.bodyText().take(20_000))
            .put("timestamp", message.headers().optString("Date")).put("url", "https://mail.google.com/mail/u/0/#all/$id")
    }
}

private fun gmailLatestIds(accessToken: String): List<String> = getJson(
    "https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=20", accessToken
).optJSONArray("messages").messageIds()

private fun syncGithub(store: SyncStore): List<JSONObject> = buildList {
    val token = store.secret("github_token")
    store.repos().forEach { repo ->
        val commits = getArray("https://api.github.com/repos/${repo.trim('/')}/commits?per_page=10", token)
        val latestSha = commits.optJSONObject(0)?.optString("sha").orEmpty()
        if (latestSha.isBlank()) return@forEach
        val oldSha = store.githubSha(repo)
        val fresh = commits.objects().takeWhile { it.optString("sha") != oldSha }
        fresh.forEach { commit ->
            val data = commit.optJSONObject("commit") ?: JSONObject()
            add(JSONObject().put("id", "github:${repo}:${commit.optString("sha")}").put("source", "GitHub")
                .put("title", "${repo}: ${data.optString("message").lineSequence().firstOrNull().orEmpty()}")
                .put("detail", data.optString("message")).put("timestamp", data.optJSONObject("author")?.optString("date"))
                .put("url", commit.optString("html_url")))
        }
        store.setGithubSha(repo, latestSha)
    }
}

private fun refreshGmailToken(store: SyncStore): String {
    val body = postForm("https://oauth2.googleapis.com/token", mapOf(
        "client_id" to store.secret("gmail_client_id"), "client_secret" to store.secret("gmail_client_secret"),
        "refresh_token" to store.secret("gmail_refresh_token"), "grant_type" to "refresh_token"
    ))
    return body.optString("access_token").ifBlank { error("Gmail token refresh failed") }
}

private fun getJson(url: String, token: String): JSONObject = request(url, "GET", token, null)
private fun getArray(url: String, token: String): JSONArray = requestText(url, "GET", token, null).let(::JSONArray)
private fun postForm(url: String, values: Map<String, String>): JSONObject = request(url, "POST", null, values.entries.joinToString("&") { "${encode(it.key)}=${encode(it.value)}" })
private fun request(url: String, method: String, token: String?, body: String?): JSONObject = JSONObject(requestText(url, method, token, body))
private fun requestText(url: String, method: String, token: String?, body: String?): String {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = method
    connection.connectTimeout = 15_000
    connection.readTimeout = 30_000
    connection.setRequestProperty("Accept", "application/json")
    token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
    if (body != null) {
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
    }
    val text = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use { it.readText() }
    check(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}: ${text.take(160)}" }
    return text
}
private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
private fun JSONArray?.messageIds(): List<String> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.optString("id")?.takeIf(String::isNotBlank) }
private fun JSONArray?.messageIdsFromHistory(): List<String> = if (this == null) emptyList() else (0 until length()).flatMap { optJSONObject(it)?.optJSONArray("messagesAdded").messageIds() }
private fun JSONArray?.objects(): List<JSONObject> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
private fun JSONObject.headers(): JSONObject = optJSONObject("payload")?.optJSONArray("headers").objects().fold(JSONObject()) { result, header -> result.put(header.optString("name"), header.optString("value")) }
private fun JSONObject.bodyText(): String {
    val payload = optJSONObject("payload") ?: return ""
    val parts = payload.optJSONArray("parts")
    val data = payload.optJSONObject("body")?.optString("data").orEmpty()
    if (data.isNotBlank()) return decode(data)
    return parts.objects().joinToString("\n") { JSONObject().put("payload", it).bodyText() }
}
private fun decode(value: String): String = String(Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP), StandardCharsets.UTF_8)
