package com.devhyeon.phoneauth.define

//전화번호 검증 실패 등으로 전송이 실패한 경우
const val AuthSendError     : Int = -1
//전화번호 유효성 검사 실패
const val PhoneNumberError  : Int = -2
//제한시간 내에 인증진행 안함
const val AuthTimeOutError       : Int = -3
//인증번호 불일치
const val AuthSameError : Int = -10
//SafetyNet 관련 에러
const val SafetyNetApiError  : Int = -4
const val SafetyNetOtherError  : Int = -5
const val GooglePlayServiceUpdateError  : Int = -6