package com.project17.tourbooking.helper.zalo_pay_helper.hmac

object HexStringUtil {
    private val HEX_CHAR_TABLE = charArrayOf(
        '0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f'
    )

    fun byteArrayToHexString(raw: ByteArray): String {
        val hex = CharArray(2 * raw.size)
        var index = 0

        for (b in raw) {
            val v = b.toInt() and 0xFF
            hex[index++] = HEX_CHAR_TABLE[v ushr 4]
            hex[index++] = HEX_CHAR_TABLE[v and 0xF]
        }
        return String(hex)
    }

}