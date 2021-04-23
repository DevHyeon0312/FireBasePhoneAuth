package com.devhyeon.phoneauth.activitys

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.devhyeon.phoneauth.databinding.ActivityPhoneAuthBinding
import com.devhyeon.phoneauth.utils.Status
import com.devhyeon.phoneauth.viewmodels.PhoneAuthViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

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
            reAuth()
        }

        binding.btnAuth.setOnClickListener {
            doLogin()
        }

        observeViewModel()
    }

    //인증번호 요청
    private fun doAuth() {
        phoneAuthViewModel.startPhoneNumberVerification("+82"+binding.etPhoneNumber.text.toString(),this@PhoneAuthActivity)
    }
    //인증번호 재요청
    private fun reAuth() {
        phoneAuthViewModel.resendVerificationCode("+82"+binding.etPhoneNumber.text.toString(),this@PhoneAuthActivity)
    }

    //인증번호 검사
    private fun doLogin() {
        //binding.etAuthNumber.text.toString()
        phoneAuthViewModel.doLogin(binding.etAuthNumber.text.toString(),this@PhoneAuthActivity)
    }

    private fun observeViewModel() {
        with(phoneAuthViewModel) {
            isRequestAuth.observe(this@PhoneAuthActivity, Observer {
                when(it) {
                    is Status.Run -> {
                        binding.loaderView.visibility = View.VISIBLE
                        println("Running")
                    }
                    is Status.Success -> {
                        binding.loaderView.visibility = View.GONE
                        println("Success")
                    }
                    is Status.Failure -> {
                        binding.loaderView.visibility = View.GONE
                        println("Failure")
                    }
                }
            })
        }

        with(phoneAuthViewModel) {
            isAuthCheck.observe(this@PhoneAuthActivity, Observer {
                when(it) {
                    is Status.Run -> {
                        binding.loaderView.visibility = View.VISIBLE
                        println("Running")
                    }
                    is Status.Success -> {
                        if(it.data!!) {
                            binding.loaderView.visibility = View.GONE
                            startMainActivity()
                        } else {
                            binding.loaderView.visibility = View.GONE
                            println("Failure")
                        }
                    }
                    is Status.Failure -> {
                        binding.loaderView.visibility = View.GONE
                        println("Failure")
                    }
                }
            })
        }
        with(phoneAuthViewModel) {
            isTimeOut.observe(this@PhoneAuthActivity, Observer {
                when(it) {
                    is Status.Run -> {
                        binding.btnAuth.visibility = View.VISIBLE
                        binding.resent.visibility = View.VISIBLE
                        binding.etAuthNumber.visibility = View.VISIBLE
                    }
                    is Status.Success -> {}
                    is Status.Failure -> {
                        binding.btnAuth.visibility = View.GONE
                        binding.resent.visibility = View.GONE
                        binding.etAuthNumber.visibility = View.GONE
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