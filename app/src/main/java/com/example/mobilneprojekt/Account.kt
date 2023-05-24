package com.example.mobilneprojekt

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun Account() {
    Button(onClick = {
        Firebase.auth.signOut()
    }) {
        Text("Logout")
    }
}