package com.example.mobilneprojekt

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.concurrent.thread

data class UriItem(
    val id: Int,
    val description: String,
    val uri: Uri,
    val stars: Float = 0f
)

class MainMenuViewModel(val app: Application) : AndroidViewModel(app) {
    val name = mutableStateOf("")
    val friendsList = mutableStateListOf<Pair<String, ImageRequest>>()
    val imageRequest : MutableState<ImageRequest?> = mutableStateOf(null)
    val invites = mutableStateListOf<Pair<String, ImageRequest>>()

    val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    val auth = Firebase.auth
    private val storage = Firebase.storage("gs://projekt-mobilki-aa7ab.appspot.com")

    private val _uriItems = MutableStateFlow(listOf<UriItem>())
    val uriItems = _uriItems.asStateFlow()

    private val _currentIdItem = MutableStateFlow(0)
    val currentIdItem = _currentIdItem.asStateFlow()

    val currentItemSelect: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(1)
    }

    private var maxCount = 20
    init{
        val listenerFriends = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                thread {
                    friendsList.add(Pair(snapshot.getValue(String::class.java)!!,getImageRequest(snapshot.getValue(String::class.java)!!)))
                }
            }
            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                friendsList.removeAll { it.first == snapshot.getValue(String::class.java) }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        val inviteListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                thread {
                    invites.add(Pair(snapshot.getValue(String::class.java)!!,getImageRequest(snapshot.getValue(String::class.java)!!)))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("invites", "onChildChanged: ${snapshot.value}")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                invites.removeAll { it.first == snapshot.getValue(String::class.java) }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("invites", "onChildMoved: ${snapshot.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("invites", "onCancelled: ${error.message}")
            }
        }
        if (auth.currentUser != null) {
            db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").get().addOnSuccessListener {
                name.value = it.getValue(String::class.java) ?: ""
            }
            db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends").get().addOnSuccessListener {
                val list = it.getValue(object : GenericTypeIndicator<List<String>>(){}) ?: listOf()
                for (i in list){
                    thread {
                        friendsList.add(Pair(i,getImageRequest(i)))
                    }
                }
            }
            db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends").addChildEventListener(listenerFriends)
            db.getReference("accounts/${Firebase.auth.currentUser?.uid}/invites").addChildEventListener(inviteListener)
        }
        Firebase.auth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                friendsList.removeAll { true }
                name.value = ""
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends").removeEventListener(listenerFriends)
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/invites").removeEventListener(inviteListener)
            } else{
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").get().addOnSuccessListener {
                    name.value = it.getValue(String::class.java) ?: ""
                }
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends").get().addOnSuccessListener {
                    val list = it.getValue(object : GenericTypeIndicator<List<String>>(){}) ?: listOf()
                    for (i in list){
                        thread {
                            friendsList.add(Pair(i,getImageRequest(i)))
                        }
                    }
                    db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends").addChildEventListener(listenerFriends)
                    db.getReference("accounts/${Firebase.auth.currentUser?.uid}/invites").addChildEventListener(inviteListener)
                }
                updateImageRequest()
            }
        }
        updateImageRequest()
        updateUriItems()
    }

    fun updateName(newName: String){
        db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").setValue(newName)
        name.value = newName
    }



    fun updateImageRequest(uri: Uri? = null) = thread {
        val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
        if (uri != null){
            val newImageRequest = ImageRequest.Builder(app.applicationContext)
                .data(uri)
                .memoryCacheKey(uri.toString())
                .build()
            imageRequest.value = newImageRequest
            db.getReference("accounts/${Firebase.auth.currentUser?.uid}/avatar").setValue("profilowe.jpg")
            Firebase.storage.reference.child("${Firebase.auth.currentUser?.uid}/profilowe.jpg").putFile(uri)
        } else {
            val res = Tasks.await(db.getReference("accounts/${Firebase.auth.currentUser?.uid}/avatar").get()).getValue(String::class.java)
            if(res != null){
                Firebase.storage.getReference("${Firebase.auth.currentUser?.uid}/$res").downloadUrl.addOnSuccessListener {
                    val newImageRequest = ImageRequest.Builder(app.applicationContext)
                        .data(it)
                        .memoryCacheKey(it.toString())
                        .build()
                    imageRequest.value = newImageRequest
                }
            } else {
                Firebase.storage.getReference("53254.png").downloadUrl.addOnSuccessListener {
                    val newImageRequest = ImageRequest.Builder(app.applicationContext)
                        .data(it)
                        .memoryCacheKey(it.toString())
                        .build()
                    imageRequest.value = newImageRequest
                }
            }
        }
    }

    fun getImageRequest(id: String): ImageRequest {
        val db =
            Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
        val res =
            Tasks.await(db.getReference("accounts/$id/avatar").get()).getValue(String::class.java)
        if (res != null) {
            val url = Tasks.await(Firebase.storage.getReference("$id/$res").downloadUrl)
            val newImageRequest = ImageRequest.Builder(app.applicationContext)
                .data(url)
                .build()
            return newImageRequest
        } else {
            val url = Tasks.await(Firebase.storage.getReference("53254.png").downloadUrl)
            val newImageRequest = ImageRequest.Builder(app.applicationContext)
                .data(url)
                .memoryCacheKey(url.toString())
                .build()
            return newImageRequest

        }

    }



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




    fun updateUriItems(){
        @SuppressLint("ObsoleteSdkInt")
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val contentResolver: ContentResolver = app.contentResolver
        contentResolver.query(
            collection,
            null,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor -> var count = 0
            val test = mutableListOf<UriItem>()
            while(cursor.moveToNext() && count < maxCount){
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                )
                Logger.getLogger("TAG").warning(contentUri.toString())
                test.add(UriItem(count, "Item 1", contentUri))
                count++
            }
            _uriItems.value = test
            maxCount += 1
        }
    }

    fun setCurrentIdItem(id: Int) {
        _currentIdItem.value = id
    }


    fun setRating(stars: Float) {
        _uriItems.value = _uriItems.value.map {
            if (currentIdItem.value == it.id){
                Logger.getLogger("TAG").warning("setRating: $stars, id: ${it.id}")
                it.copy(stars = stars)
            } else it
        }.sortedByDescending {
            it.stars
        }
        Logger.getLogger("TAG").warning("setRating: ${_uriItems.value}")
    }

    fun setDescription(description: String) {
        _uriItems.value = _uriItems.value.map {
            if (currentIdItem.value == it.id) it.copy(description = description) else it
        }
    }
}