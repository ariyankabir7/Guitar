package com.mosquito.hincash.extrazz

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object videoplayyer {
    val encorder: Base64.Encoder = Base64.getEncoder()
    val decorder: Base64.Decoder = Base64.getDecoder()
    @Throws(Exception::class)
    private fun cipher(opmode: Int, secretKey: String): Cipher {
        if (secretKey.length != 32) throw RuntimeException("SecretKey length is not 32 chars")
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val sk = SecretKeySpec(secretKey.toByteArray(), "AES")
        val iv = IvParameterSpec(secretKey.substring(0, 16).toByteArray()) //0~16은 서버와 합의!
        c.init(opmode, sk, iv)
        return c
    }

    fun encrypt(str: String, secretKey: String): String? {
        return try {
            val encrypted =
                cipher(Cipher.ENCRYPT_MODE, secretKey).doFinal(str.toByteArray(charset("UTF-8")))
            String(encorder.encode(encrypted))
        } catch (e: Exception) {
            null
        }
    }

    fun decrypt(str: String, secretKey: String): String? {
        return try {
            val byteStr: ByteArray = decorder.decode(str.toByteArray())
            String(cipher(Cipher.DECRYPT_MODE, secretKey).doFinal(byteStr), charset("UTF-8"))
        } catch (e: Exception) {
            null
        }
    }
}