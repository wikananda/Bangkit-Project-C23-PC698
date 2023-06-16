package com.example.projectcapstone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.projectcapstone.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.tvRegistHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btRegist.setOnClickListener {
            val name = binding.edRegistName.text.toString()
            val email = binding.edRegistEmail.text.toString()
            val pass = binding.edRegistPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && name.isNotEmpty()){
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}