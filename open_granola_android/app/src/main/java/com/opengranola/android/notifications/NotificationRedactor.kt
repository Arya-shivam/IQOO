package com.opengranola.android.notifications

/** Redacts common secrets before notification text reaches local storage. */
object NotificationRedactor {
    private val otp = Regex("(?i)\\b(?:otp|one[- ]time password|verification code)\\D{0,12}\\d{4,8}\\b")
    private val email = Regex("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", RegexOption.IGNORE_CASE)
    private val token = Regex("(?i)\\b(?:token|password|passcode|secret|api[-_ ]?key)\\s*[:=]?\\s*[^\\s]+")

    fun clean(value: CharSequence?): String = value?.toString()
        ?.replace(otp, "[code redacted]")
        ?.replace(email, "[email redacted]")
        ?.replace(token, "[secret redacted]")
        ?.trim()
        ?.take(500)
        .orEmpty()
}
