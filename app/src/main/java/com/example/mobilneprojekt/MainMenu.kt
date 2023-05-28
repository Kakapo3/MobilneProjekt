package com.example.mobilneprojekt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobilneprojekt.arkanoid.ArkanoidActivity
import com.example.mobilneprojekt.minesweeper.ChooseMinesActivity
import com.example.mobilneprojekt.snake.SnakeActivity
import com.example.mobilneprojekt.theme.Typography
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Locale
import java.util.concurrent.Executors
import java.util.logging.Logger

@Composable
fun MakeGameRow(title: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Image(
            painter = painterResource(id = R.drawable.game_icon_temp),
            contentDescription = "Temp icon - change it when you deploy a game",
            Modifier.size(50.dp)
        )

        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 40.sp
        )

        Button(onClick = { /*TODO*/ }) {
            Text(text = "Play!")
        }
    }
}

@Composable
fun MakeGameColumn(imgSrc : Int, achievementSrc: List<Int>, achievementNames: List<String>, title: String, activity: Class<out ComponentActivity>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(id = imgSrc),
            contentDescription = "Temp icon - change it when you deploy a game",
            Modifier.size(250.dp).
                    clickable {
                        val intent = Intent(context, activity)
                        context.startActivity(intent)
                    }
        )

        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 60.sp
        )

        val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
        Row() {
            var achievementImages by remember { mutableStateOf(mutableListOf(0, 0, 0)) }
            val currUser = Firebase.auth.currentUser?.uid

            db.getReference("accounts/${currUser}/achievements").get().addOnSuccessListener { a ->
                var currAchiv = 0
                for (achievement in achievementNames) {
                    val newImages = achievementImages.toMutableList()
                    if (a.hasChild(achievement)) {
                        newImages[currAchiv] = currAchiv + 3
                    } else {
                        newImages[currAchiv] = currAchiv
                    }
                    achievementImages = newImages
                    currAchiv += 1
                }
            }

            val achievementDesc = mutableListOf("", "", "")

            db.getReference("${title.lowercase()}/achievements").get().addOnSuccessListener { a ->
                var currAchiv = 0
                for (achivement in a.children) {
                    achievementDesc[currAchiv] = achivement.child("description").value.toString()
                    currAchiv += 1
                }
            }



            Image(
                painter = painterResource(id = achievementSrc[achievementImages[0]]),
                contentDescription = "Achievement icon",
                Modifier
                    .size(80.dp)
                    .padding(10.dp)
                    .clickable {
                        Toast
                            .makeText(context, achievementDesc[0], Toast.LENGTH_SHORT)
                            .show()
                    }
            )

            Image(
                painter = painterResource(id = achievementSrc[achievementImages[1]]),
                contentDescription = "Achievement icon",
                Modifier
                    .size(80.dp)
                    .padding(10.dp)
                    .clickable {
                        Toast
                            .makeText(context, achievementDesc[1], Toast.LENGTH_SHORT)
                            .show()
                    }
            )

            Image(
                painter = painterResource(id = achievementSrc[achievementImages[2]]),
                contentDescription = "Achievement icon",
                Modifier
                    .size(80.dp)
                    .padding(10.dp)
                    .clickable {
                        Toast
                            .makeText(context, achievementDesc[2], Toast.LENGTH_SHORT)
                            .show()
                    }
            )
        }
    }
}

@Composable
@ExperimentalFoundationApi
fun GameScroll() {
    val images = listOf(
        R.drawable.snake_svgrepo_com,
        R.drawable.game_icon_arkanoid,
        R.drawable.game_icon_minesweeper
    )
    val titles = listOf(
        "Snake",
        "Arkanoid",
        "Minesweeper"
    )

    val achievements = listOf(
        listOf(R.drawable.baseline_star_24_not_completed, R.drawable.baseline_star_24_not_completed, R.drawable.baseline_star_24_not_completed, R.drawable.baseline_star_24, R.drawable.baseline_star_24, R.drawable.baseline_star_24),
        listOf(R.drawable.game_icon_temp, R.drawable.game_icon_temp, R.drawable.game_icon_temp, R.drawable.game_icon_temp, R.drawable.game_icon_temp, R.drawable.game_icon_temp),
        listOf(R.drawable.minesweeper_achievement_not_completed, R.drawable.minesweeper_achievement_not_completed, R.drawable.minesweeper_achievement_not_completed, R.drawable.minesweeper_achievement_1, R.drawable.minesweeper_achievement_2, R.drawable.minesweeper_achievement_3),
    )

    val achievementNames = listOf(
        listOf("snake1", "snake2", "snake3"),
        listOf("a1", "a2", "a3"),
        listOf("m1", "m2", "m3"),
    )

    val classes = listOf(
        SnakeActivity::class.java,
        ArkanoidActivity::class.java,
        ChooseMinesActivity::class.java
    )

    HorizontalPager(pageCount = 3
    ) { page ->
        MakeGameColumn(
            imgSrc = images[page],
            title = titles[page],
            achievementSrc = achievements[page],
            achievementNames = achievementNames[page],
            activity = classes[page]
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainMenu(){
    // Tutaj możemy tworzyć właściwy interfejs głównego menu
    GameScroll()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScaffold () {
    val navController = rememberNavController()
    val showDialogInfo = remember { mutableStateOf(false) }
    val showMenu = remember { mutableStateOf(false) }
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val scope = rememberCoroutineScope()
    val snackbarDelegate: SnackbarDelegate = remember {
        SnackbarDelegate(
            snackbarHostState = snackbarHostState,
            coroutineScope = scope
        )
    }
    val viewModel: MainMenuViewModel = viewModel()
    val search = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(navController.currentDestination){
        Logger.getLogger("Main menu").info("Route: ${navController.currentDestination?.route}")
        Logger.getLogger("Main menu").info("Route: ${navController.currentDestination?.route == "mainMenu"}")
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState){
                val backGroundColor = snackbarDelegate.snackbarBackgroundColor
                Snackbar(snackbarData = it, contentColor = backGroundColor)
            }
        },
        topBar = {
            val a = navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)
            TopAppBar(
                navigationIcon = {
                    AnimatedVisibility(
                        visible = a.value?.destination?.route == "camera" || a.value?.destination?.route == "gallery",
                        enter = slideInHorizontally(
                            initialOffsetX = {-it },
                            animationSpec = tween(500)),
                        exit = slideOutHorizontally(
                            targetOffsetX = { - it },
                            animationSpec = tween(500)),
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }

                    }
                },
                title = { Text(text = "Game hub", style = Typography.headlineMedium) },
                actions = {
                    IconButton(onClick = { showMenu.value = !showMenu.value }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu.value,
                        onDismissRequest = { showMenu.value = false }) {
                        DropdownMenuItem(onClick = { Firebase.auth.signOut() },
                            text = { Text(text = "Logout") },
                            leadingIcon = {

                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier
                                        .padding(10.dp)

                                )
                            }
                        )
                        DropdownMenuItem(onClick = { showDialogInfo.value = true },
                            text = { Text(text = "Info") },
                            leadingIcon = {

                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    modifier = Modifier
                                        .padding(10.dp)

                                )
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            val a = navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)
            AnimatedVisibility(
                visible = a.value?.destination?.route != "camera" && a.value?.destination?.route != "gallery",
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(500)
                )
            ) {
                NavigationBar {
                    // Tworzymy dolny pasek nawigacji
                    // selectedItem to stan, który będzie przechowywał informację o tym, który element jest zaznaczony
                    NavigationBarItem(
                        selected = navController.currentBackStackEntryAsState().value?.destination?.route == "mainMenu",
                        onClick = {
                            navController.navigate("mainMenu")
                        },
                        label = { Text("Main menu") },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = "Home"
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = navController.currentBackStackEntryAsState().value?.destination?.route == "friends",
                        onClick = {
                            navController.navigate("friends")
                        },
                        label = { Text("Friends") },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Face,
                                contentDescription = "Home"
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = navController.currentBackStackEntryAsState().value?.destination?.route == "account",
                        onClick = {
                            navController.navigate("account")
                        },
                        label = { Text("Account") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Home"
                            )
                        }
                    )
                }
            }
        }
        // W Kotlinie ostatni parametr jako funkcja może być wyciągnięta poza nawiasy
        // Tutaj to jest paramter content
    ) { innerPadding ->
        // Tutaj jest kontent
        // Przekazywany padding jest po to, żeby nie nakładać elementów na siebie
        // Wpakowujemy nasz własny content do boxa, żeby nie nakładać elementów na siebie
        // i dajemy mu padding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val context = LocalContext.current
            val outputDirectory = remember {
                getOutputDirectory(context)
            }
            val cameraExecutor = remember {
                Executors.newSingleThreadExecutor()
            }
            //Nasz navhost odpowiada za to, żeby się zmieniały ekrany
            //jak wyywołujemy funkcję navController.navigate("nazwa ekranu"), to zmieniamy ekran
            NavHost(navController = navController, startDestination = "mainMenu") {
                composable("mainMenu") { MainMenu() }
                composable("friends") { FriendsList(search) }
                composable("account") { Account(navController, snackbarDelegate) }
                composable("camera") {
                    EnterAnimation {
                        CameraView(
                            outputDirectory = outputDirectory,
                            executor = cameraExecutor,
                            onImageCaptured = {
                                updateImageRequest(it, viewModel.imageRequest, context)
                                (context as Activity).runOnUiThread {
                                    navController.navigateUp()
                                }

                            },
                            onError = {
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } }
                composable("gallery") { EnterAnimation {
                    Gallery(navController)
                }  }
            }
        }
        if(showDialogInfo.value){
            Dialog(onDismissRequest = {
                showDialogInfo.value = false
            }) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(20.dp)
                ) {
                    Text(text = "Info about the app")
                    Button(onClick = { showDialogInfo.value = false }) {
                        Text(text = "Close")
                    }
                }
            }
        }
    }
}

