# FireBasePhoneAuth
FireBasePhoneAuth  ( 파이어베이스 문자인증)

------

### package com.devhyeon.phoneauth.define

#### 위 패키지 위치에 API_KEY.kt 파일을 생성해주세요.


```kotlin
package com.devhyeon.phoneauth.define

import com.devhyeon.phoneauth.utils.SafetyUtils

val FIREBASE_AUTH_NONCE = SafetyUtils().createNonce()
//TODO : Enter your API key.
const val FIREBASE_AUTH_KEY = "here"
```

------
질의응답에 따라 NONCE 생성 코드를 추가하였습니다.




------

## 기능

#### 1. 전화번호를 입력합니다.

#### 2. 전송을 누르면, 테스트 디바이스가 아닌경우 문자가전송되며, (가상기기가 아닌 Google Play Service 가 설치된 실기기라면, reCAPTCHA 과정 생략 ) 테스트 기기는 미전송됩니다.

#### 3. 전달받은 문자의 인증번호 or 테스트번호를 입력하면 인증이 가능합니다.

#### 4. 재전송도 가능합니다.


## 주요 코드


<details>
    <summary>PhoneAuthActivity</summary>

```
class PhoneAuthActivity : AppCompatActivity() {
    lateinit var binding : ActivityPhoneAuthBinding

    private val phoneAuthViewModel: PhoneAuthViewModel by viewModel()

    companion object {
        private val TAG = PhoneAuthActivity::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            doAuth()
        }
        binding.resent.setOnClickListener {
            doReAuth()
        }

        binding.btnAuth.setOnClickListener {
            doLogin()
        }

        observeViewModel()
    }

    //인증번호 요청
    private fun doAuth() {
        phoneAuthViewModel.sendVerificationCode("+82"+binding.etPhoneNumber.text.toString(),this@PhoneAuthActivity)
    }
    //인증번호 재요청
    private fun doReAuth() {
        phoneAuthViewModel.resendVerificationCode("+82"+binding.etPhoneNumber.text.toString(),this@PhoneAuthActivity)
    }

    //인증번호 검사
    private fun doLogin() {
        //binding.etAuthNumber.text.toString()
        phoneAuthViewModel.doLogin(binding.etAuthNumber.text.toString(),this@PhoneAuthActivity)
    }

    private fun observeViewModel() {
        //인증번호 요청/재요청에 따른 UI
        with(phoneAuthViewModel) {
            isRequestAuth.observe(this@PhoneAuthActivity, Observer {
                when(it) {
                    is Status.Run -> {
                        showView(binding.loaderView)
                        println("$TAG : isRequestAuth Running")
                    }
                    is Status.Success -> {
                        hideView(binding.loaderView)
                        println("$TAG : isRequestAuth Success")
                    }
                    is Status.Failure -> {
                        hideView(binding.loaderView)
                        println("$TAG : isRequestAuth Failure")
                    }
                }
            })
        }
        //인증번호 검사에 따른 UI
        with(phoneAuthViewModel) {
            isAuthCheck.observe(this@PhoneAuthActivity, Observer {
                when(it) {
                    is Status.Run -> {
                        showView(binding.loaderView)
                        println("$TAG : isAuthCheck Running")
                    }
                    is Status.Success -> {
                        hideView(binding.loaderView)
                        if(it.data!!) {
                            startMainActivity()
                        } else {
                            println("$TAG : isAuthCheck Failure")
                        }
                    }
                    is Status.Failure -> {
                        hideView(binding.loaderView)
                        println("$TAG : isAuthCheck Failure")
                    }
                }
            })
        }
        //제한시간에 따른 UI
        with(phoneAuthViewModel) {
            isTimeOut.observe(this@PhoneAuthActivity, Observer {
                when(it) {
                    is Status.Run -> {
                        showView(binding.btnAuth)
                        showView(binding.resent)
                        showView(binding.etAuthNumber)
                        println("$TAG : isTimeOut Running")
                    }
                    is Status.Success -> {}
                    is Status.Failure -> {
                        hideView(binding.btnAuth)
                        hideView(binding.resent)
                        hideView(binding.etAuthNumber)
                        println("$TAG : isTimeOut Failure")
                    }
                }
            })
        }
    }


    private fun startMainActivity() {
        startActivity(Intent(this@PhoneAuthActivity,MainActivity::class.java))
        finish()
    }
}
```

</details>


<details>
    <summary>PhoneAuthViewModel</summary>

```

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
```

</details>

## 실행 화면

<img src="https://user-images.githubusercontent.com/72678200/115906869-1393a600-a4a3-11eb-86a1-c6d21f18a3cd.png" width="30%" height="30%"> <img src="https://user-images.githubusercontent.com/72678200/115906891-18f0f080-a4a3-11eb-9de4-ffcf9711e2a3.png" width="30%" height="30%"> <img src="https://user-images.githubusercontent.com/72678200/115906911-1f7f6800-a4a3-11eb-9560-e6a90a61866b.png" width="30%" height="30%">
<img src="https://user-images.githubusercontent.com/72678200/115906929-273f0c80-a4a3-11eb-8918-c03a8a35816e.png" width="30%" height="30%"> <img src="https://user-images.githubusercontent.com/72678200/115906945-2dcd8400-a4a3-11eb-836a-d22fddf4e6f9.png" width="30%" height="30%">

