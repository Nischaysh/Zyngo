package com.example.vibin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vibin.databinding.ActivitySigninBinding
import com.example.vibin.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser : FirebaseUser? = auth.currentUser
        if(currentUser != null){
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
                        } else if(profileset == "false") {
                            // User exists but hasn't completed profile setup
                            startActivity(Intent(this, DetailsActivity::class.java))
                            finish()
                        }else {
                            startActivity(Intent(this, SigninActivity::class.java))
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error checking user data: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }else{
            startActivity(Intent(this, SigninActivity::class.java))
            finish()

        }

    }
}
