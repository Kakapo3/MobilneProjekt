package com.example.mobilneprojekt

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilneprojekt.theme.MobilneProjektTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import java.util.logging.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobilneProjektTheme(
                dynamicColor = true,
            ) {
                val viewModel: MainMenuViewModel = viewModel()
                // A surface container using the 'background' color from the theme
                MainNav()
            }
        }
    }
}

@Composable
fun MainNav(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = if (Firebase.auth.currentUser == null) "login" else "mainMenu") {
        composable("mainMenu") {
            EnterAnimation {
                MainMenuScaffold()
            }

        }
        composable("login") {
            EnterAnimation {
                LoginScreen(controller = navController)
            }
        }
        composable("register") {
            EnterAnimation {
                RegisterScreen(controller = navController)
            }
        }
    }
    LaunchedEffect(Unit){
//        Firebase.auth.signOut()

        navController.addOnDestinationChangedListener{ controller , destination, _ ->
            if (destination.route == "mainMenu") {
                controller.enableOnBackPressed(false)
            } else {
                controller.enableOnBackPressed(true)
            }
        }
        if (Firebase.auth.currentUser != null){
            Firebase.auth.addAuthStateListener ( object : AuthStateListener {
                override fun onAuthStateChanged(auth: FirebaseAuth) {
                    Logger.getLogger("MainNav").warning("currentUser: ${auth.currentUser?.uid}")
                    if (auth.currentUser == null) {
                        navController.navigate("login")
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
                    }
                }
            })
        }

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnterAnimation(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { 150 }
        ) + expandVertically(
            expandFrom = Alignment.CenterVertically
        ) + fadeIn(initialAlpha = 0.7f),
        exit = slideOutHorizontally() + shrinkHorizontally() + fadeOut(),
        content = {
            content()
        },
        initiallyVisible = false
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainMenuScaffold()
}