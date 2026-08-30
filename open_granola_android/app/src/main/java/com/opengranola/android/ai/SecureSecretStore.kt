package com.opengranola.android.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Small encrypted preference store for user-provided credentials and OAuth tokens. */
class SecureSecretStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("secure_secrets", Context.MODE_PRIVATE)

    fun put(name: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        preferences.edit()
            .putString("$name.iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(name, Base64.encodeToString(cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8)), Base64.NO_WRAP))
            .apply()
    }

    fun get(name: String): String? = runCatching {
        val iv = Base64.decode(preferences.getString("$name.iv", null), Base64.NO_WRAP)
        val encrypted = Base64.decode(preferences.getString(name, null), Base64.NO_WRAP)
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        }.doFinal(encrypted).toString(StandardCharsets.UTF_8)
    }.getOrNull()

    fun clear(name: String) = preferences.edit().remove(name).remove("$name.iv").apply()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build())
        }.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "pa_frontier_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
