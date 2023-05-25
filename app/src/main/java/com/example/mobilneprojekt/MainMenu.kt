package com.example.mobilneprojekt

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobilneprojekt.theme.Typography
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
fun MakeGameColumn(imgSrc : Int, title: String) {
    val context = LocalContext.current
    Column() {
        Image(
            painter = painterResource(id = R.drawable.game_icon_temp),
            contentDescription = "Temp icon - change it when you deploy a game",
            Modifier.size(250.dp)
        )

        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 60.sp
        )

        Row() {
            Image(
                painter = painterResource(id = R.drawable.game_icon_temp),
                contentDescription = "Temp icon - change it when you deploy a game",
                Modifier
                    .size(80.dp)
                    .padding(10.dp)
                    .clickable {
                        Toast
                            .makeText(context, "Achievement 1", Toast.LENGTH_SHORT)
                            .show()
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.game_icon_temp),
                contentDescription = "Temp icon - change it when you deploy a game",
                Modifier
                    .size(80.dp)
                    .padding(10.dp)
                    .clickable {
                        Toast
                            .makeText(context, "Achievement 2", Toast.LENGTH_SHORT)
                            .show()
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.game_icon_temp),
                contentDescription = "Temp icon - change it when you deploy a game",
                Modifier
                    .size(80.dp)
                    .padding(10.dp)
                    .clickable {
                        Toast
                            .makeText(context, "Achievement 3", Toast.LENGTH_SHORT)
                            .show()
                    }
            )
        }
    }
}

@Composable
@ExperimentalFoundationApi
fun GameScroll() {
    HorizontalPager(pageCount = 3) { page ->
        MakeGameColumn(imgSrc = R.drawable.game_icon_temp, title = "Game $page")
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
        topBar = { if (navController.currentBackStackEntryAsState().value?.destination?.route != "camera" || navController.currentBackStackEntryAsState().value?.destination?.route != "gallery") {
            TopAppBar(
//            colors = TopAppBarDefaults.mediumTopAppBarColors(
//                containerColor = com.example.mobilneprojekt.theme.Purple80,
//            ),
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
        }
        },
        bottomBar = {
            if (navController.currentBackStackEntryAsState().value?.destination?.route != "camera" || navController.currentBackStackEntryAsState().value?.destination?.route != "gallery") {
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
                composable("friends") { FriendsList() }
                composable("account") { Account(navController, snackbarDelegate) }
                composable("camera") { CameraView(
                    outputDirectory = outputDirectory,
                    executor = cameraExecutor,
                    onImageCaptured = {
                        viewModel.updateImageRequest(it)
                        (context as Activity).runOnUiThread {
                            navController.navigateUp()
                        }

                    },
                    onError = {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                ) }
                composable("gallery") { Gallery(navController) }
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

