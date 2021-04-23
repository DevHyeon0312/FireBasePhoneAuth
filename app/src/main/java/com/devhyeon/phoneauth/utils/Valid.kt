package com.devhyeon.phoneauth.utils

import org.jetbrains.annotations.NotNull
import java.util.regex.Pattern

fun isCheckPhoneNumber(@NotNull phoneNumber: String) : Boolean {
    val regex = "^\\s*(010|011|012|013|014|015|016|017|018|019)(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$";
    val regex2 = "^\\s*(10|11|12|13|14|15|16|17|18|19)(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$";
    val p = Pattern.compile(regex)
    val m = p.matcher(phoneNumber.substring(3))
    val p2 = Pattern.compile(regex2)
    val m2 = p2.matcher(phoneNumber.substring(3))
    if (m.matches() || m2.matches()) {
        return true
    }
    return false
}