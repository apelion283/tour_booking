package com.project17.tourbooking.helper.zalo_pay_helper.hmac

import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HMacUtil {
    const val HMACSHA256 = "HmacSHA256"

    private fun hmacEncode(algorithm: String, key: String, data: String): ByteArray? {
        var macGenerator: Mac? = null
        try {
            macGenerator = Mac.getInstance(algorithm)
            val signingKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), algorithm)
            macGenerator.init(signingKey)
        } catch (ex: Exception) {
            // Handle exception
        }

        if (macGenerator == null) {
            return null
        }

        var dataByte: ByteArray? = null
        try {
            dataByte = data.toByteArray(Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            // Handle exception
        }

        return macGenerator.doFinal(dataByte)
    }

    fun hmacHexStringEncode(algorithm: String, key: String, data: String): String? {
        val hmacEncodeBytes = hmacEncode(algorithm, key, data)
        return hmacEncodeBytes?.let { HexStringUtil.byteArrayToHexString(it) }
    }
}