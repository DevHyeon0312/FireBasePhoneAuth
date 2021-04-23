package com.devhyeon.phoneauth.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devhyeon.phoneauth.define.*
import com.devhyeon.phoneauth.utils.Status
import com.devhyeon.phoneauth.utils.isCheckPhoneNumber
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.jetbrains.annotations.NotNull
import java.util.concurrent.TimeUnit

class PhoneAuthViewModel : ViewModel() {
    private var auth: FirebaseAuth = Firebase.auth
    private val TIME_OUT : Long = 60  //인증제한시간
    lateinit var authId: String //인증ID
    private var forceResendingToken : PhoneAuthProvider.ForceResendingToken? = null //인증번호 재전송 토큰

    private val isResend = true //재전송 여부

    //인증코드 요청시 전송여부
    private val _isRequestAuth = MutableLiveData<Status<Boolean>>()
    //인증코드 입력시 결과
    private val _isAuthCheck = MutableLiveData<Status<Boolean>>()
    //타임아웃 여부
    private val _isTimeOut = MutableLiveData<Status<Boolean>>()

    val isRequestAuth : LiveData<Status<Boolean>> get() = _isRequestAuth
    val isAuthCheck : LiveData<Status<Boolean>> get() = _isAuthCheck
    val isTimeOut : LiveData<Status<Boolean>> get() = _isTimeOut

    //사용자가 인증번호 요청
    fun sendVerificationCode(@NotNull phoneNumber: String, @NotNull activity: Activity) {
        _isRequestAuth.value = Status.Run()
        if (isCheckPhoneNumber(phoneNumber)) {
            checkSafetyNet(phoneNumber, activity, !isResend)
        } else {
            _isRequestAuth.value = Status.Failure(PhoneNumberError)
        }
    }

    //사용자가 인증번호 재요청
    fun resendVerificationCode(@NotNull phoneNumber: String, @NotNull activity: Activity) {
        _isRequestAuth.value = Status.Run()
        if (isCheckPhoneNumber(phoneNumber)) {
            checkSafetyNet(phoneNumber, activity, isResend)
        } else {
            _isRequestAuth.value = Status.Failure(PhoneNumberError)
        }
    }

    //SafetyNet 기기검증 및 전송단계 진행
    private fun checkSafetyNet(@NotNull phoneNumber: String, @NotNull activity: Activity, isResend: Boolean) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS) {
            SafetyNet.getClient(activity).attest(FIREBASE_AUTH_NONCE, FIREBASE_AUTH_KEY)
                .addOnSuccessListener(activity) {
                    sendAuthMessage(phoneNumber,activity, isResend)
                }
                .addOnFailureListener(activity) { e ->
                    if (e is ApiException) {
                        _isRequestAuth.value = Status.Failure(SafetyNetApiError)
                    } else {
                        _isRequestAuth.value = Status.Failure(SafetyNetOtherError)
                    }
                }
        } else {
            _isRequestAuth.value = Status.Failure(GooglePlayServiceUpdateError)
        }
    }

    //인증번호 전송
    private fun sendAuthMessage(@NotNull phoneNumber: String, @NotNull activity: Activity, isResend:Boolean) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)      // Phone number to verify
            .setTimeout(TIME_OUT, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks

        if (isResend && forceResendingToken != null) {
            options.setForceResendingToken(forceResendingToken) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    //PhoneAuthCredential 객체 가져오기
    private fun verifyPhoneNumberWithCode(verificationId: String?, @NotNull code: String) : PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId!!, code)
    }

    //인증번호 검사
    fun doLogin(@NotNull authNumber:String, @NotNull activity: Activity) {
        _isAuthCheck.value = Status.Run()
        if (authNumber.isEmpty()) {
            _isAuthCheck.value = Status.Failure(AuthSameError)
        } else {
            val credential = verifyPhoneNumberWithCode(authId, authNumber)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        _isAuthCheck.value = Status.Success(true)
                    } else {
                        _isAuthCheck.value = Status.Failure(AuthSameError)
                    }
                }
        }
    }

    /** 인증번호 요청 콜백 */
    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        //1.즉시확인된 경우 : 확인 코드를 보내거나 입력 할 필요없이 확인된 경우
        //2.자동검색된 경우 : 일부기기에서 Google Play 서비스가 자동으로 SMS 감지 후 작업
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            _isRequestAuth.value = Status.Success(true)
        }

        //전화번호 검증 실패 등으로 전송이 실패한 경우
        override fun onVerificationFailed(e: FirebaseException) {
            _isRequestAuth.value = Status.Failure(AuthSendError)
        }

        //코드전송을 성공한 경우
        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            authId = verificationId
            forceResendingToken = token
            _isRequestAuth.value = Status.Success(true)
            _isTimeOut.value = Status.Run()
        }

        //입력시간 초과
        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            _isTimeOut.value = Status.Failure(AuthTimeOutError)
            super.onCodeAutoRetrievalTimeOut(p0)
        }
    }
}