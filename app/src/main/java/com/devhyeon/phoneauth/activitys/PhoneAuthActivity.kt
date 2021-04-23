package com.devhyeon.phoneauth.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.devhyeon.phoneauth.databinding.ActivityPhoneAuthBinding
import com.devhyeon.phoneauth.utils.Status
import com.devhyeon.phoneauth.utils.hideView
import com.devhyeon.phoneauth.utils.showView
import com.devhyeon.phoneauth.viewmodels.PhoneAuthViewModel
import org.koin.android.viewmodel.ext.android.viewModel

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