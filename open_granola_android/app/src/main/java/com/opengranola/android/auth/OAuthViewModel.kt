package com.opengranola.android.auth

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.opengranola.android.BuildConfig
import com.opengranola.android.ai.SecureSecretStore
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class OAuthProvider { GOOGLE, GITHUB }

data class ConnectedAccount(
    val provider: OAuthProvider,
    val displayName: String,
    val handle: String,
    val avatarUrl: String? = null
)

data class GitHubDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresInSeconds: Long,
    val pollingIntervalSeconds: Long
)

data class OAuthUiState(
    val google: ConnectedAccount? = null,
    val github: ConnectedAccount? = null,
    val busyProvider: OAuthProvider? = null,
    val githubDeviceCode: GitHubDeviceCode? = null,
    val browserUriToOpen: String? = null,
    val message: String? = null,
    val googleConfigured: Boolean = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank(),
    val githubConfigured: Boolean = BuildConfig.GITHUB_OAUTH_CLIENT_ID.isNotBlank()
)

/**
 * Owns account-linking state across configuration changes.
 *
 * Google uses Android Credential Manager and keeps only the selected profile locally. GitHub
 * uses OAuth device authorization so a public Android client never embeds a client secret.
 */
class OAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val accountStore = OAuthAccountStore(application)
    private val githubClient = GitHubDeviceOAuthClient()
    private var githubJob: Job? = null

    private val _uiState = MutableStateFlow(
        OAuthUiState(
            google = accountStore.account(OAuthProvider.GOOGLE),
            github = accountStore.account(OAuthProvider.GITHUB)
        )
    )
    val uiState: StateFlow<OAuthUiState> = _uiState.asStateFlow()

    fun connectGoogle(activity: Activity) {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Add GOOGLE_WEB_CLIENT_ID to local.properties first")
            return
        }
        if (_uiState.value.busyProvider != null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busyProvider = OAuthProvider.GOOGLE, message = "Opening Google account picker…")
            runCatching {
                val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val credential = CredentialManager.create(activity).getCredential(activity, request).credential
                check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    "Google returned an unsupported credential"
                }
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                ConnectedAccount(
                    provider = OAuthProvider.GOOGLE,
                    displayName = google.displayName?.takeIf { it.isNotBlank() } ?: google.id,
                    handle = google.id,
                    avatarUrl = google.profilePictureUri?.toString()
                )
            }.onSuccess { account ->
                accountStore.save(account)
                _uiState.value = _uiState.value.copy(
                    google = account,
                    busyProvider = null,
                    message = "Google account connected on this device"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    busyProvider = null,
                    message = googleErrorMessage(error)
                )
            }
        }
    }

    fun disconnectGoogle(activity: Activity) {
        if (_uiState.value.busyProvider != null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busyProvider = OAuthProvider.GOOGLE, message = "Disconnecting Google…")
            runCatching {
                CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
            }
            accountStore.clear(OAuthProvider.GOOGLE)
            _uiState.value = _uiState.value.copy(
                google = null,
                busyProvider = null,
                message = "Google disconnected from pa"
            )
        }
    }

    fun connectGitHub() {
        if (BuildConfig.GITHUB_OAUTH_CLIENT_ID.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "Add GITHUB_OAUTH_CLIENT_ID to local.properties first")
            return
        }
        if (_uiState.value.busyProvider != null) return
        githubJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busyProvider = OAuthProvider.GITHUB, message = "Requesting a GitHub sign-in code…")
            runCatching {
                val device = withContext(Dispatchers.IO) {
                    githubClient.requestDeviceCode(BuildConfig.GITHUB_OAUTH_CLIENT_ID)
                }
                _uiState.value = _uiState.value.copy(
                    githubDeviceCode = device,
                    browserUriToOpen = device.verificationUri,
                    message = "Enter ${device.userCode} in GitHub to continue"
                )
                val token = withContext(Dispatchers.IO) {
                    githubClient.pollForAccessToken(BuildConfig.GITHUB_OAUTH_CLIENT_ID, device)
                }
                val account = withContext(Dispatchers.IO) { githubClient.fetchAccount(token) }
                accountStore.save(account)
                accountStore.saveGitHubToken(token)
                account
            }.onSuccess { account ->
                _uiState.value = _uiState.value.copy(
                    github = account,
                    busyProvider = null,
                    githubDeviceCode = null,
                    browserUriToOpen = null,
                    message = "GitHub connected as ${account.handle}"
                )
            }.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) return@onFailure
                _uiState.value = _uiState.value.copy(
                    busyProvider = null,
                    githubDeviceCode = null,
                    browserUriToOpen = null,
                    message = "GitHub sign-in failed: ${error.message ?: "try again"}"
                )
            }
        }
    }

    fun cancelGitHub() {
        githubJob?.cancel()
        githubJob = null
        _uiState.value = _uiState.value.copy(
            busyProvider = null,
            githubDeviceCode = null,
            browserUriToOpen = null,
            message = "GitHub sign-in cancelled"
        )
    }

    fun disconnectGitHub() {
        githubJob?.cancel()
        accountStore.clear(OAuthProvider.GITHUB)
        accountStore.clearGitHubToken()
        _uiState.value = _uiState.value.copy(
            github = null,
            busyProvider = null,
            githubDeviceCode = null,
            browserUriToOpen = null,
            message = "GitHub disconnected from pa"
        )
    }

    fun browserOpened() {
        _uiState.value = _uiState.value.copy(browserUriToOpen = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun googleErrorMessage(error: Throwable): String {
        val type = error::class.simpleName.orEmpty()
        return when {
            "Cancellation" in type -> "Google sign-in cancelled"
            "NoCredential" in type -> "No Google account is available on this device"
            else -> "Google sign-in failed: ${error.message ?: type.ifBlank { "try again" }}"
        }
    }
}

private class OAuthAccountStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("oauth_accounts", Context.MODE_PRIVATE)
    private val secrets = SecureSecretStore(context)

    fun account(provider: OAuthProvider): ConnectedAccount? {
        val prefix = provider.name.lowercase()
        val handle = preferences.getString("$prefix.handle", null)?.takeIf { it.isNotBlank() } ?: return null
        return ConnectedAccount(
            provider = provider,
            displayName = preferences.getString("$prefix.name", handle).orEmpty(),
            handle = handle,
            avatarUrl = preferences.getString("$prefix.avatar", null)
        )
    }

    fun save(account: ConnectedAccount) {
        val prefix = account.provider.name.lowercase()
        preferences.edit()
            .putString("$prefix.name", account.displayName)
            .putString("$prefix.handle", account.handle)
            .putString("$prefix.avatar", account.avatarUrl)
            .apply()
    }

    fun clear(provider: OAuthProvider) {
        val prefix = provider.name.lowercase()
        preferences.edit()
            .remove("$prefix.name")
            .remove("$prefix.handle")
            .remove("$prefix.avatar")
            .apply()
    }

    fun saveGitHubToken(token: String) = secrets.put(GITHUB_TOKEN_KEY, token)
    fun clearGitHubToken() = secrets.clear(GITHUB_TOKEN_KEY)

    private companion object {
        const val GITHUB_TOKEN_KEY = "github_oauth_access_token"
    }
}

private class GitHubDeviceOAuthClient {
    fun requestDeviceCode(clientId: String): GitHubDeviceCode {
        val json = postForm(
            "https://github.com/login/device/code",
            mapOf("client_id" to clientId, "scope" to "read:user")
        )
        val error = json.optString("error")
        check(error.isBlank()) { json.optString("error_description", error) }
        return GitHubDeviceCode(
            deviceCode = json.getString("device_code"),
            userCode = json.getString("user_code"),
            verificationUri = json.getString("verification_uri"),
            expiresInSeconds = json.optLong("expires_in", 900L),
            pollingIntervalSeconds = json.optLong("interval", 5L).coerceAtLeast(5L)
        )
    }

    suspend fun pollForAccessToken(clientId: String, device: GitHubDeviceCode): String {
        val deadline = System.currentTimeMillis() + device.expiresInSeconds * 1_000L
        var interval = device.pollingIntervalSeconds
        while (System.currentTimeMillis() < deadline) {
            delay(interval * 1_000L)
            val json = postForm(
                "https://github.com/login/oauth/access_token",
                mapOf(
                    "client_id" to clientId,
                    "device_code" to device.deviceCode,
                    "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"
                )
            )
            json.optString("access_token").takeIf { it.isNotBlank() }?.let { return it }
            when (val error = json.optString("error")) {
                "authorization_pending" -> Unit
                "slow_down" -> interval += 5L
                "access_denied" -> error("Access was denied in GitHub")
                "expired_token" -> error("The GitHub code expired")
                else -> error(json.optString("error_description", error.ifBlank { "Unknown GitHub response" }))
            }
        }
        error("The GitHub code expired")
    }

    fun fetchAccount(token: String): ConnectedAccount {
        val connection = (URL("https://api.github.com/user").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        }
        return try {
            val response = connection.readResponse()
            check(connection.responseCode in 200..299) { githubHttpError(connection.responseCode, response) }
            val json = JSONObject(response)
            val login = json.getString("login")
            ConnectedAccount(
                provider = OAuthProvider.GITHUB,
                displayName = json.optString("name").takeIf { it.isNotBlank() && it != "null" } ?: login,
                handle = "@$login",
                avatarUrl = json.optString("avatar_url").takeIf { it.isNotBlank() }
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun postForm(endpoint: String, values: Map<String, String>): JSONObject {
        val body = values.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, Charsets.UTF_8.name())}=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        return try {
            connection.outputStream.use { it.write(body.toByteArray()) }
            val response = connection.readResponse()
            check(connection.responseCode in 200..299) { githubHttpError(connection.responseCode, response) }
            JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }

    private fun HttpURLConnection.readResponse(): String =
        (if (responseCode in 200..299) inputStream else errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()

    private fun githubHttpError(code: Int, body: String): String =
        runCatching { JSONObject(body).optString("message") }.getOrNull()?.takeIf { it.isNotBlank() }
            ?.let { "GitHub returned $code: $it" }
            ?: "GitHub returned HTTP $code"
}
