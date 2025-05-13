package com.example.vibin.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.vibin.Activity.MainActivity
import com.example.vibin.R
import com.example.vibin.databinding.ActivityDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class DetailsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivityDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser

        if (currentUser != null) {
            // Load existing user data
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    var email = doc.getString("email")
                    var password = doc.getString("password")
                    binding.emailEditText.setText(email)
                    binding.passwordEditText.setText(password)
                    binding.confirmPasswordEditText.setText(password)
                }

            // Handle save button click
            binding.saveButton.setOnClickListener {
                binding.Loadingscreen.visibility = View.VISIBLE
                val username = binding.usernameEditText.text.toString().trim()
                val firstName = binding.firstNameEditText.text.toString().trim()
                val lastName = binding.lastNameEditText.text.toString().trim()
                val gender = when (binding.genderToggleGroup.checkedButtonId) {
                    R.id.maleButton -> "Male"
                    R.id.femaleButton -> "Female"
                    else -> ""
                }

                // Validate inputs
                if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create user data map
                val userData = hashMapOf(
                    "username" to username,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "gender" to gender,
                    "profileset" to "true",
                    "followerCount" to 0,
                    "followingCount" to 0
                )

                // Update user data in Firestore
                db.collection("users").document(currentUser.uid)
                    .update(userData as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.Loadingscreen.visibility = View.GONE
                        Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}