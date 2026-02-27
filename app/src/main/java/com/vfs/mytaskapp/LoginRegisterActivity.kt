package com.vfs.mytaskapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var subtitleTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var nameFieldContainer: TextInputLayout
    private lateinit var loginToggleButton: Button
    private lateinit var registerToggleButton: Button

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Cloud.auth.currentUser != null) {
            goToGroupsActivity()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.login_register_layout)
        initializeViews()
        setupClickListeners()
        updateUI()
    }

    private fun initializeViews() {
        titleTextView = findViewById(R.id.tv_title)
        subtitleTextView = findViewById(R.id.tv_subtitle)
        nameEditText = findViewById(R.id.et_name)
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.et_password)
        submitButton = findViewById(R.id.btn_submit)
        nameFieldContainer = findViewById(R.id.name_field_container)
        loginToggleButton = findViewById(R.id.btn_login_toggle)
        registerToggleButton = findViewById(R.id.btn_register_toggle)
    }

    private fun setupClickListeners() {
        submitButton.setOnClickListener { loginOrRegister() }
        loginToggleButton.setOnClickListener { isLoginMode = true; updateUI() }
        registerToggleButton.setOnClickListener { isLoginMode = false; updateUI() }
    }

    private fun updateUI() {
        if (isLoginMode) {
            titleTextView.text = "Sign in with email"
            subtitleTextView.text = "Enter your email and password to sign in."
            submitButton.text = "Sign In"
            nameFieldContainer.visibility = View.GONE
            loginToggleButton.isEnabled = false
            registerToggleButton.isEnabled = true
        } else {
            titleTextView.text = "Create an account"
            subtitleTextView.text = "Enter your details to create a new account."
            submitButton.text = "Sign Up"
            nameFieldContainer.visibility = View.VISIBLE
            loginToggleButton.isEnabled = true
            registerToggleButton.isEnabled = false
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun loginOrRegister() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all required fields")
            return
        }

        if (isLoginMode) {
            loginUser(email, password)
        } else {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                showMessage("Please enter your name")
                return
            }
            registerUser(name, email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        Cloud.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goToGroupsActivity()
                } else {
                    showMessage("Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun registerUser(name: String, email: String, password: String) {
        Cloud.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user ?: return@addOnCompleteListener
                    val sanitizedEmail = Cloud.sanitizeEmail(email)
                    
                    // 1. Store email-to-uid lookup
                    Cloud.db.child("users").child(sanitizedEmail).setValue(user.uid)
                    
                    // 2. Store user profile
                    Cloud.db.child(user.uid).child("email").setValue(email)

                    updateUserProfile(user, name) {
                        createInitialGroup(user.uid) {
                            goToGroupsActivity()
                        }
                    }
                } else {
                    showMessage("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun updateUserProfile(firebaseUser: FirebaseUser, name: String, onFinished: () -> Unit) {
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener {
            onFinished()
        }
    }

    private fun createInitialGroup(userId: String, onFinished: () -> Unit) {
        val groupRef = Cloud.db.child("groups").child(userId).child("groups").push()
        val groupId = groupRef.key ?: return onFinished()

        val newGroup = Group(name = "My Tasks", owner = userId)
        
        groupRef.setValue(newGroup).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Link group to user
                Cloud.db.child(userId).child("groups").child(groupId).setValue(userId)
                    .addOnCompleteListener {
                        val tasks = listOf(
                            Task(name = "Welcome to My Task App!", content = "This is a sample task."),
                            Task(name = "Long-press a group to share it."),
                            Task(name = "Check for new invitations in the menu.")
                        )
                        val itemsRef = groupRef.child("items")
                        var count = tasks.size
                        tasks.forEach { t ->
                            itemsRef.push().setValue(t).addOnCompleteListener {
                                count--
                                if (count == 0) onFinished()
                            }
                        }
                    }
            } else {
                onFinished()
            }
        }
    }

    private fun goToGroupsActivity() {
        val intent = Intent(this, GroupsActivity::class.java)
        startActivity(intent)
        finish()
    }
}