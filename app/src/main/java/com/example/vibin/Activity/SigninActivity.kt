package com.example.vibin.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vibin.R
import com.example.vibin.databinding.ActivitySigninBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SigninActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivitySigninBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set click listener using function
        binding.signInButton.setOnClickListener {
            handleEmailPasswordAuth()
        }
        binding.googleSignInButton.setOnClickListener {
            Toast.makeText(this, "Not Available", Toast.LENGTH_SHORT).show()
        }

        binding.logoImageView.setOnClickListener {
            val bottomSheet = MyBottomSheet()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    private fun handleEmailPasswordAuth() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val passwordLayout = binding.passwordLayout
        val emailLayout = binding.emailLayout
        passwordLayout.error = null
        emailLayout.error = null

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty() && password.isEmpty()) {
                passwordLayout.error = "Password required"
                emailLayout.error = "This field cannot be empty"
            } else if (password.isEmpty()) {
                passwordLayout.error = "Password required"
            } else {
                emailLayout.error = "This field cannot be empty"
            }
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Invalid format"
        } else {
            // Try to sign in
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        binding.Loadingscreen.visibility = View.VISIBLE
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener { document ->
                                    val profileset = document.get("profileset")
                                    if (profileset == "true") {
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    } else {
                                        startActivity(Intent(this, DetailsActivity::class.java))
                                        finish()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error checking user data: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Try to register
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { createTask ->
                                binding.Loadingscreen.visibility = View.VISIBLE
                                if (createTask.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    val userData = hashMapOf(
                                        "email" to email,
                                        "userId" to userId,
                                        "password" to password,
                                        "profileset" to "false"
                                    )
                                    if (userId != null) {
                                        db.collection("users").document(userId)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        DetailsActivity::class.java
                                                    )
                                                )
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Failed to store user data: ${it.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                } else {
                                    binding.Loadingscreen.visibility = View.GONE
                                    Toast.makeText(this, "Authentication failed: Invalid Password", Toast.LENGTH_LONG).show()
                                    Toast.makeText(this, "Authentication failed: ${createTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
        }
    }

    class MyBottomSheet : BottomSheetDialogFragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.create_password_bottom_sheet, container, false)
        }
    }
}