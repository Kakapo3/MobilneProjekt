package com.example.mobilneprojekt

import android.widget.Toast
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.google.firebase.storage.ktx.storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.*
import androidx.navigation.NavController
import coil.request.ImageRequest
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.ktx.database
import java.util.logging.Logger
import kotlin.concurrent.thread

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account(navController: NavController, snackbarDelegate: SnackbarDelegate) {

    SideEffect {

    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingValues(8.dp, 20.dp, 8.dp, 16.dp))
            .verticalScroll(rememberScrollState())
    ) {
        val showDialog = remember { mutableStateOf(true) }
        val viewModel: MainMenuViewModel = viewModel()
        val url = remember{mutableStateOf("")}
        val context = LocalContext.current
        val db = remember {
            Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
        }
        if (showDialog.value) {
            Dialog(
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                onDismissRequest = { /*TODO*/ }) {
                CircularProgressIndicator()
            }
        }
        val storage = Firebase.storage("gs://projekt-mobilki-aa7ab.appspot.com")
        var gsReference = storage.reference.child("53254.png")

        LaunchedEffect(Unit) {
            gsReference.downloadUrl.addOnSuccessListener { a->
                url.value = a.toString()
            }
        }


        val transitionState = remember {
            MutableTransitionState(false)
        }
        key(viewModel.imageRequest.value) {
            AsyncImage(
                model = viewModel.imageRequest.value,
                contentDescription = "essa",
                modifier = Modifier
                    .size(150.dp)
                    .border(
                        3.dp,
                        androidx.compose.ui.graphics.Color.Blue,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .clip(shape = androidx.compose.foundation.shape.CircleShape)
                    .background(androidx.compose.ui.graphics.Color.White)
                    .clickable {
                        transitionState.targetState = true
                    },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Success) {
                        showDialog.value = false
                    } else if (state is AsyncImagePainter.State.Empty) {
                        Toast.makeText(
                            context,
                            "Empty image",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (state is AsyncImagePainter.State.Loading) {
                        showDialog.value = true
                    }
                })
        }


        Spacer(modifier = Modifier.size(16.dp))

        val editName = remember { mutableStateOf(false) }

        Text(
            text = viewModel.name.value,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
                .padding(8.dp)
                .clickable {
                    editName.value = true
                }
        )

        if(editName.value){
            ModalBottomSheet(onDismissRequest = { editName.value = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    TextField(value = viewModel.name.value, onValueChange = {
                        updateName(it, viewModel.name)
                    }, label = {
                        Text(text = "Name")
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                    )
                    Spacer(modifier =
                    Modifier.size(8.dp))
                    Button(onClick = {
                        editName.value = false
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)) {
                        Text(text = "Save")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        Divider(modifier = Modifier.fillMaxWidth(0.8f))

        Spacer(modifier = Modifier.size(8.dp))

        val resetPassword = remember { mutableStateOf(false) }

        Button(onClick = {
            resetPassword.value = true
        },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.9f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Change password")

                Icon(Icons.Default.Lock, contentDescription = "Change password")
            }
        }

        val showLoading = remember { mutableStateOf(false) }

        if (showLoading.value) {
            Dialog(
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                onDismissRequest = { /*TODO*/ }) {
                CircularProgressIndicator()
            }
        }

        if (resetPassword.value){
            ModalBottomSheet(onDismissRequest = { resetPassword.value = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val oldemail = remember { mutableStateOf("") }
                    val oldpassword = remember { mutableStateOf("") }
                    val newpassword = remember { mutableStateOf("") }
                    val oldpasswordVisible = remember { mutableStateOf(false) }
                    val newpasswordVisible = remember { mutableStateOf(false) }
                    TextField(
                        value = oldemail.value,
                        onValueChange = { oldemail.value = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        label = { Text("Email") },
                        singleLine = true
                    )
                    TextField(
                        value = oldpassword.value,
                        onValueChange = { oldpassword.value = it },
                        visualTransformation = if (oldpasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        label = { Text("Password") },
                        singleLine = true,
                        trailingIcon = {
                            val image = if (oldpasswordVisible.value)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            // Please provide localized description for accessibility services
                            val description =
                                if (oldpasswordVisible.value) "Hide password" else "Show password"

                            IconButton(onClick = { oldpasswordVisible.value = !oldpasswordVisible.value }) {
                                Icon(imageVector = image, description)
                            }
                        }
                    )
                    TextField(
                        value = newpassword.value,
                        onValueChange = { newpassword.value = it },
                        visualTransformation = if (newpasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        label = { Text("Password") },
                        singleLine = true,
                        trailingIcon = {
                            val image = if (newpasswordVisible.value)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            // Please provide localized description for accessibility services
                            val description =
                                if (newpasswordVisible.value) "Hide password" else "Show password"

                            IconButton(onClick = { newpasswordVisible.value = !newpasswordVisible.value }) {
                                Icon(imageVector = image, description)
                            }
                        }
                    )
                    Spacer(modifier =
                    Modifier.size(8.dp))
                    Button(onClick = {
                        resetPassword.value = false
                        showLoading.value = true


                        thread{
                            val user = Firebase.auth.currentUser
                            val credential = EmailAuthProvider
                                .getCredential(oldemail.value, oldpassword.value)
                            try{
                                user?.reauthenticate(credential)?.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        user.updatePassword(newpassword.value).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                snackbarDelegate.showSnackbar(
                                                    "Password updated",
                                                    "Ok",
                                                    SnackbarDuration.Short
                                                )
                                            } else {
                                                Logger.getLogger("ChangePassword").warning("Error password not updated: ${task.exception?.localizedMessage}")
                                                snackbarDelegate.showSnackbar(
                                                    "Error password not updated",
                                                    "Retry",
                                                    SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    } else {
                                        snackbarDelegate.showSnackbar(
                                            "Error auth failed",
                                            "Retry",
                                            SnackbarDuration.Short
                                        )
                                    }
                                }
                            } catch (e: Exception){
                                snackbarDelegate.showSnackbar(
                                    "Error auth failed",
                                    "Retry",
                                    SnackbarDuration.Short
                                )
                            } finally {
                                showLoading.value = false
                            }
                        }



                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)) {
                        Text(text = "Save")
                    }
                }
            }
        }

        Button(
            onClick = {
                Firebase.auth.signOut()
            },
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(0.9f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Logout")

                Icon(Icons.Default.Logout, contentDescription = "Logout")
            }

        }



        // Visibility state for the dialog which will trigger it only once when called


        if (transitionState.targetState) {
            ModalBottomSheet(onDismissRequest = {
                transitionState.targetState = false
            }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Your layout

                    // This can be any user interraction that closes the dialog
                    Button(
                        onClick = {
                            navController.navigate("gallery")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Icon(Icons.Default.BrowseGallery, contentDescription = "From gallery",
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("From Gallery")

                        }
                    }

                    Button(
                        onClick = {
                            navController.navigate("camera")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Icon(
                                    Icons.Default.Camera, contentDescription = "From gallery",
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("From Camera")

                            }

                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))

                }
            }
        }
    }

}