package com.devhyeon.phoneauth.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Nonce : 안드로이드에서 SafetyNet 에서 사용
 * SafetyNet Attestation API를 호출할 때 nonce를 전달해야 합니다.
 * SafetyNet 요청에 사용되는 nonce는 길이가 16바이트 이상이어야 합니다.
 * */
class SafetyUtils {
    //랜덤한 문자열을 생성하기 위한 BASE String
    private fun loadBaseString(): String {
        return "abcdefghijklmnopqrstuvwxyz0123456789_-"
    }

    //현재 년월일시분초밀리초를 반환
    private fun createDateNow(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
        return current.format(formatter)
    }

    //결합하여 고유하면서 랜덤한 nonce 생성 (Size : 34)
    fun createNonce() : ByteArray {
        val dateStr = createDateNow()
        val baseStr = loadBaseString()
        val nonce = StringBuilder()

        for (i in dateStr.toCharArray()) {
            //date 로 BASE 참조 (고유) : 0~9 를 참조하기 위해 i - ASCII
            nonce.append(baseStr[i.toInt()-48])
            //랜덤하게 BASE 참조 (랜덤)
            nonce.append(baseStr[Random().nextInt(baseStr.length - 1)])
        }

        return nonce.toString().toByteArray()
    }
}