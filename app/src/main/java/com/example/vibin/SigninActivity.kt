package com.example.vibin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import com.example.vibin.databinding.ActivitySigninBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SigninActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySigninBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val passwordLayout = binding.passwordLayout
            val emailLayout = binding.emailLayout
            passwordLayout.error = null
            emailLayout.error = null

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty() && password.isEmpty()){
                    passwordLayout.error = "Password required"
                    emailLayout.error = "This field cannot be empty"
                }else if(password.isEmpty()){
                    passwordLayout.error = "Password required"
                }else{
                    emailLayout.error = "This field cannot be empty"
                }
            }else if (password.length < 6) {
                passwordLayout.error = "Password must be at least 6 characters"
            }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Invalid format"
            } else {
                // First try to sign in the user
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            binding.Loadingscreen.visibility = View.VISIBLE
                            // User exists and signed in successfully
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // Check if user has completed profile setup
                                db.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        val profileset = document.get("profileset")
                                        if (profileset == "true") {
                                            // User has completed profile setup
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        } else {
                                            // User exists but hasn't completed profile setup
                                            startActivity(Intent(this, DetailsActivity::class.java))
                                            finish()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error checking user data: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            // Sign in failed, try to create a new account
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { createTask ->
                                    binding.Loadingscreen.visibility = View.VISIBLE
                                    if (createTask.isSuccessful) {
                                        // User successfully registered
                                        val userId = auth.currentUser?.uid

                                        // Create a user data map
                                        val userData = hashMapOf(
                                            "email" to email,
                                            "userId" to userId,
                                            "password" to password,
                                            "profileset" to "false"
                                        )

                                        // Store user data in Firestore under "users" collection
                                        if (userId != null) {
                                            db.collection("users").document(userId)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                                    startActivity(Intent(this, DetailsActivity::class.java))
                                                    finish()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(this, "Failed to store user data: ${it.message}", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    } else {
                                        binding.Loadingscreen.visibility = View.GONE
                                        // Both sign in and registration failed
                                        createTask.exception?.message?.let { errorMessage ->
                                            Toast.makeText(this, "Authentication failed: Invalid Password", Toast.LENGTH_LONG).show()
                                            Toast.makeText(this, "Authentication failed: $errorMessage", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                        }
                    }
            }
        }

        binding.logoImageView.setOnClickListener {
            val bottomSheet = MyBottomSheet()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    class MyBottomSheet : BottomSheetDialogFragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        }
    }

}