package com.example.mobilneprojekt



import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(controller: NavHostController) {
    val viewModel: MainMenuViewModel = viewModel()
    val auth = Firebase.auth
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarDelegate: SnackbarDelegate = remember {
        SnackbarDelegate(
            snackbarHostState = snackbarHostState,
            coroutineScope = scope
        )
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState){
                val backGroundColor = snackbarDelegate.snackbarBackgroundColor
                Snackbar(snackbarData = it, contentColor = backGroundColor)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game Hub",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

            )
        }
    ) { value ->
        Column(
            modifier = Modifier
                .padding(value)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val email = rememberSaveable { mutableStateOf("") }
            val password = rememberSaveable { mutableStateOf("") }
            val passwordVisible = rememberSaveable { mutableStateOf(false) }
            Text(
                text = "Login",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.padding(16.dp))
            TextField(
                value = email.value,
                onValueChange = { email.value = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                label = { Text("Username") },
                singleLine = true
            )
            TextField(
                value = password.value,
                onValueChange = { password.value = it },
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Password") },
                singleLine = true,
                trailingIcon = {
                    val image = if (passwordVisible.value)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description =
                        if (passwordVisible.value) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            val visible = rememberSaveable { mutableStateOf(false) }
            Button(
                onClick = {
                    if (email.value.isEmpty() || password.value.isEmpty()) {
                        snackbarDelegate.showSnackbar(
                            message = "Please fill in all fields",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                    } else {
                        viewModel.login(email.value, password.value, controller, visible, snackbarDelegate)
                    }
                },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            if (visible.value) {
                Dialog(
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    ),
                    onDismissRequest = { /*TODO*/ }
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.padding(16.dp))
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                onClick = { controller.navigate("register") },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Sign up")
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Text(
                text = "Forgot password?",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.padding(8.dp))
            val emailReset = rememberSaveable { mutableStateOf(false) }
            Button(
                onClick = { emailReset.value = true },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Reset password")
            }
            if (emailReset.value){
                Dialog(onDismissRequest = { emailReset.value = false}){
                    val emailRestore = rememberSaveable { mutableStateOf("") }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp)
                    ){
                        Text(text = "Reset password", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.padding(8.dp))
                        TextField(
                            value = emailRestore.value,
                            onValueChange = { emailRestore.value = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            label = { Text("Email") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Button(onClick = {
                            auth.sendPasswordResetEmail(emailRestore.value)
                            emailReset.value = false
                            snackbarDelegate.showSnackbar(
                                message = "Reset email sent",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Short
                            )
                        }) {
                            Text(text = "Send reset email")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(controller: NavHostController) {
    val viewModel: MainMenuViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarDelegate: SnackbarDelegate = remember {
        SnackbarDelegate(
            snackbarHostState = snackbarHostState,
            coroutineScope = scope
        )
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState){b ->
                val backGroundColor = snackbarDelegate.snackbarBackgroundColor
                Snackbar(snackbarData = b, contentColor = backGroundColor)
            }
        },
        topBar = {
            TopAppBar (
                title = {
                    Text(
                        text = "Game hub",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        controller.popBackStack()
                    }
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { value ->
        Column(
            modifier = Modifier
                .padding(value)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val email = rememberSaveable { mutableStateOf("") }
            val password = rememberSaveable { mutableStateOf("") }
            val passwordVisible = rememberSaveable { mutableStateOf(false) }
            val name = rememberSaveable { mutableStateOf("") }
            Text(
                text = "Register",
                style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.padding(16.dp))
            TextField(
                value = email.value,
                onValueChange = { email.value = it},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                label = { Text("Username") },
                singleLine = true
            )
            TextField(
                value = password.value,
                onValueChange = { password.value = it},
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text("Password") },
                singleLine = true,
                trailingIcon = {
                    val image = if (passwordVisible.value)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (passwordVisible.value) "Hide password" else "Show password"

                    IconButton(onClick = {passwordVisible.value = !passwordVisible.value}){
                        Icon(imageVector  = image, description)
                    }
                }
            )
            TextField(
                value = name.value,
                onValueChange = { name.value = it},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                label = { Text("Name") },
                singleLine = true
            )
            Spacer(modifier = Modifier.padding(8.dp))
            val visible = rememberSaveable { mutableStateOf(false) }
            Button(
                onClick = {
                    if (email.value.isEmpty() || password.value.isEmpty() || name.value.isEmpty()) {
                        snackbarDelegate.showSnackbar(
                            message = "Please fill in all fields",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Short
                        )
                    } else {
                        viewModel.createAccount(
                            email.value,
                            password.value,
                            controller,
                            visible,
                            snackbarDelegate,
                            name
                        )
                    }

                },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Register")
            }
            Spacer(modifier = Modifier.padding(8.dp))

            if (visible.value) {
                Dialog(
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    ),
                    onDismissRequest = { /*TODO*/ }
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

