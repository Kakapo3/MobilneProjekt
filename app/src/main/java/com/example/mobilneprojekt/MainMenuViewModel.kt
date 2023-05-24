package com.example.mobilneprojekt

import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainMenuViewModel : ViewModel() {
    val addListener = mutableStateOf(false)

    fun createAccount(
        email: String,
        password: String,
        controller: NavHostController,
        visible: MutableState<Boolean>,
        snackbarDelegate: SnackbarDelegate,
        name: MutableState<String>
    ) {
        visible.value = true

        thread {
            val auth = Firebase.auth
            val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("TAG", "createUserWithEmail:success")
                        val user = auth.currentUser
                        val userRef = db.getReference("accounts/${user?.uid}")
                        userRef.child("name").setValue(name.value)
                        userRef.child("confirmed").setValue(false)
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    Log.d("TAG", "Email sent.")
                                    snackbarDelegate.showSnackbar(
                                        message = "Verification email sent",
                                        actionLabel = "OK",
                                        duration = SnackbarDuration.Short
                                    )

                                    Firebase.auth.signOut()
                                    visible.value = false

                                }
                            }?.exception?.let {
                                snackbarDelegate.showSnackbar(
                                    message = "Error: ${it.localizedMessage}",
                                    actionLabel = "OK",
                                    duration = SnackbarDuration.Short
                                )
                                visible.value = false
                                Firebase.auth.signOut()
                            }
                    } else {
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        snackbarDelegate.showSnackbar(
                            message = "Authentication failed: ${task.exception?.localizedMessage}",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                        visible.value = false
                    }
                }
        }


    }

    fun login(
        email: String,
        password: String,
        controller: NavHostController,
        visible: MutableState<Boolean>,
        snackbarDelegate: SnackbarDelegate
    ) {

        visible.value = true
        thread {
            val auth = Firebase.auth
            val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if(auth.currentUser?.isEmailVerified == false) {
                            snackbarDelegate.showSnackbar(
                                message = "Please verify your email",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                            visible.value = false
                            return@addOnCompleteListener
                        }
                        Log.d("TAG", "signInWithEmail:success")
                        db.getReference("accounts/${auth.currentUser?.uid}/confirmed").setValue(true)
                        Firebase.auth.addAuthStateListener ( object :
                            FirebaseAuth.AuthStateListener {
                            override fun onAuthStateChanged(auth: FirebaseAuth) {
                                Logger.getLogger("MainNav").warning("currentUser: ${auth.currentUser?.uid}")
                                if (auth.currentUser == null) {
                                    controller.navigate("login")
                                    Firebase.messaging.apply {
                                        unsubscribeFromTopic("/topics/${auth.currentUser?.uid}")
                                        Logger.getLogger("SnakeActivity").info("unsubscribed from topic: /topics/${auth.currentUser?.uid}")
                                    }
                                    auth.removeAuthStateListener(this)
                                } else {
                                    Firebase.messaging.apply {
                                        subscribeToTopic("/topics/${auth.currentUser?.uid}")
                                        Logger.getLogger("SnakeActivity").info("subscribed to topic: /topics/${auth.currentUser?.uid}")
                                        token.addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                Log.i("SnakeActivityToken", "token: ${it.result}")
                                            }
                                        }
                                    }
                                    controller.navigate("mainMenu")
                                }
                            }
                        })
                    } else {
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        snackbarDelegate.showSnackbar(
                            message = "Authentication failed: ${task.exception?.localizedMessage}",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                    }
                    visible.value = false
                }
        }

    }
}