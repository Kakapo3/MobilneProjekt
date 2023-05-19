package com.example.mobilneprojekt

import android.annotation.SuppressLint
import android.icu.text.CaseMap.Title
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilneprojekt.theme.MobilneProjektTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobilneProjektTheme(
                dynamicColor = true,
                darkTheme = true
            ) {
                // A surface container using the 'background' color from the theme
                MainMenuScaffold()
            }
        }
    }
}

@Composable
fun OldMenu() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)) {

            MakeTitle(
                title = "App title",
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    MakeGameRow(title = "Game 1")
                    MakeGameRow(title = "Game 2")
                    MakeGameRow(title = "Game 3")
                }
            }
        }
    }
}

@Composable
fun MakeTitle(title: String) {
    Text(
        text = title,
        textAlign = TextAlign.Center,
        fontSize = 50.sp
    )
}

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

@Composable
fun FriendsList(){
    // A tutaj możemy tworzyć interfejs listy znajomych
    Text(text = "Friends list")
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScaffold () {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopAppBar(
//            colors = TopAppBarDefaults.mediumTopAppBarColors(
//                containerColor = com.example.mobilneprojekt.theme.Purple80,
//            ),
            title = { Text(text = "Game hub", style = com.example.mobilneprojekt.theme.Typography.headlineMedium) }
        )},
        bottomBar = { NavigationBar {
            // Tworzymy dolny pasek nawigacji
            // selectedItem to stan, który będzie przechowywał informację o tym, który element jest zaznaczony
            val selectedItem = remember{ mutableStateOf(0) }

            NavigationBarItem(
                selected = selectedItem.value == 0,
                onClick = {
                    selectedItem.value = 0
                    navController.navigate("mainMenu")
                },
                label = { Text("Main menu") },
                icon = { Icon(imageVector  = Icons.Rounded.Home, contentDescription = "Home") }
            )

            NavigationBarItem(
                selected = selectedItem.value == 1,
                onClick = {
                    selectedItem.value = 1
                    navController.navigate("friends")
                },
                label = { Text("Friends") },
                icon = { Icon(imageVector  = Icons.Rounded.Face, contentDescription = "Home") }
            )

            }


        }
    // W Kotlinie ostatni parametr jako funkcja może być wyciągnięta poza nawiasy
    // Tutaj to jest paramter content
    ){ innerPadding ->
        // Tutaj jest kontent
        // Przekazywany padding jest po to, żeby nie nakładać elementów na siebie
        // Wpakowujemy nasz własny content do boxa, żeby nie nakładać elementów na siebie
        // i dajemy mu padding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            //Nasz navhost odpowiada za to, żeby się zmieniały ekrany
            //jak wyywołujemy funkcję navController.navigate("nazwa ekranu"), to zmieniamy ekran
            NavHost(navController = navController, startDestination = "mainMenu") {
                composable("mainMenu") { MainMenu()}
                composable("friends") { FriendsList() }
            }
        }
    }
}

// Można też ten duży plik podzielić na mniejsze, żeby było czytelniej


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainMenuScaffold()
}