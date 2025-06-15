package com.example.vibin.Activity

import Post
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vibin.R
import com.example.vibin.databinding.ActivityCreatePostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import java.io.File
import java.util.*

class CreatePostActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreatePostBinding
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val sourceUri = result.data?.data
                sourceUri?.let { startCrop(it) }
            }
        }

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnPost.setOnClickListener {
            if (imageUri != null) {
                uploadImageThenPost()
            } else {
                postTextOnly()
            }
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${UUID.randomUUID()}.jpg"))

        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(false)
            setToolbarTitle("Crop Image")
            setShowCropFrame(false)
            setShowCropGrid(false)
            setHideBottomControls(true)
            setTheme(R.style.UcropFixTheme)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        }

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f) // Square crop
            .withMaxResultSize(1080, 1080)
            .withOptions(options)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                imageUri = it
                binding.ivPreview.setImageURI(it)
                binding.ivPreview.visibility = View.VISIBLE
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showMessage("Crop error: ${cropError?.message}")
        }
    }

    private fun uploadImageThenPost() {
        val fileName = "posts/${UUID.randomUUID()}.jpg"
        val ref = FirebaseStorage.getInstance().reference.child(fileName)
        binding.Loadingscreen.visibility = View.VISIBLE
        ref.putFile(imageUri!!).continueWithTask {
            ref.downloadUrl
        }.addOnSuccessListener { uri ->
            savePost(uri.toString())
        }
    }

    private fun postTextOnly() {
        savePost(null)
    }

    private fun savePost(imageUrl: String?) {
        val uid = FirebaseAuth.getInstance().uid ?: return

        val userDocRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)

        userDocRef.get()
            .addOnSuccessListener { document ->
                val userName = document.getString("username") ?: "Unknown"

                val post = Post(
                    userId = uid,
                    userName = userName,
                    text = binding.etCaption.text.toString(),
                    imageUrl = imageUrl,
                    timestamp = Timestamp.now()
                )

                FirebaseFirestore.getInstance().collection("posts")
                    .add(post)
                    .addOnSuccessListener {
                        showMessage("Posted")
                        finish()
                    }
            }
            .addOnFailureListener {
                showMessage("Failed to fetch user data.")
            }
    }

    private fun showMessage(message: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }
}
