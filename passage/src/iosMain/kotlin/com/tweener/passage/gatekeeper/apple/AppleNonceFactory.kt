package com.tweener.passage.gatekeeper.apple

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */
class AppleNonceFactory {

    companion object {

        private const val CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"

        fun createRandomNonceString(length: Int = 32): String {
            require(length > 0) { "Parameter 'length' must be greater than 0." }

            return iosSecureRandomBytes(length)
                .map { byte -> CHARSET[(byte.toInt() and 0xFF) % CHARSET.length] }
                .joinToString("")
        }

        @OptIn(ExperimentalForeignApi::class, ExperimentalStdlibApi::class)
        fun sha256(input: String): String {
            val inputData = input.encodeToByteArray()
            val hashedData = UByteArray(CC_SHA256_DIGEST_LENGTH)
            inputData.usePinned { CC_SHA256(data = it.addressOf(0), len = inputData.size.convert(), md = hashedData.refTo(0)) }
            return hashedData.toByteArray().toHexString(HexFormat.Default)
        }

        @OptIn(ExperimentalForeignApi::class)
        private fun iosSecureRandomBytes(length: Int): ByteArray =
            memScoped {
                val randomBytes = allocArray<UByteVar>(length)

                val errorCode = SecRandomCopyBytes(rnd = kSecRandomDefault, count = length.convert(), bytes = randomBytes)
                require(errorCode == errSecSuccess) { "Unable to generate nonce. SecRandomCopyBytes failed with OSStatus $errorCode" }

                randomBytes.readBytes(length)
            }
    }
}
