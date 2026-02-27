package com.vfs.mytaskapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Cloud {
    companion object {
        val auth: FirebaseAuth by lazy {
            FirebaseAuth.getInstance()
        }
        val db: DatabaseReference by lazy {
            FirebaseDatabase.getInstance().reference
        }

        fun sanitizeEmail(email: String): String {
            return email.replace(".", ",")
        }
    }
}