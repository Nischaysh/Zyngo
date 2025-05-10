package com.example.vibin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar
import com.example.vibin.databinding.UpdateProfileBottomSheetBinding

class UpdateProfileBottomSheet : BottomSheetDialogFragment() {

    private var _binding: UpdateProfileBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UpdateProfileBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load current user data
        loadCurrentUserData()

        // Set up update button click listener
        binding.updateButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadCurrentUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.usernameEditText.setText(document.getString("username"))
                        binding.nameEditText.setText(document.getString("firstName"))
                        binding.bioEditText.setText(document.getString("bio"))
                        val gender = document.getString("gender")
                        if(gender == "Male"){
                            binding.genderimg.visibility = View.VISIBLE
                            binding.genderimg.setImageResource(R.drawable.man)
                        }else{
                            binding.genderimg.visibility = View.VISIBLE
                            binding.genderimg.setImageResource(R.drawable.woman)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    showMessage("Error loading profile data: ${e.message}")
                }
        }
    }

    private fun updateProfile() {
        val username = binding.usernameEditText.text.toString().trim()
        val name = binding.nameEditText.text.toString().trim()
        val bio = binding.bioEditText.text.toString().trim()

        if (username.isEmpty() || name.isEmpty()) {
            showMessage("Username and name cannot be empty")
            return
        }

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val updates = hashMapOf(
                "username" to username,
                "firstName" to name,
                "bio" to bio
            )

            db.collection("users")
                .document(user.uid)
                .update(updates as Map<String, Any>)
                .addOnSuccessListener {
                    showMessage("Profile updated successfully")
                    dismiss()
                }
                .addOnFailureListener { e ->
                    showMessage("Error updating profile: ${e.message}")
                }
        }
    }

    private fun showMessage(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}