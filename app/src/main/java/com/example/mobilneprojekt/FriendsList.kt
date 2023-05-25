package com.example.mobilneprojekt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.logging.Logger

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FriendsList(search: MutableState<Boolean>) {
    // A tutaj możemy tworzyć interfejs listy znajomych
    val viewModel: MainMenuViewModel = viewModel()
    LaunchedEffect(search.value){
        Logger.getLogger("FriendsList").warning("No pokaz się")
    }
    val search = remember { mutableStateOf("") }
    val active = remember { mutableStateOf(false) }
    val list = remember {
        mutableStateOf(listOf<User>())
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val showInvites = remember { mutableStateOf(false) }
        SearchBar(
            query = search.value,
            onQueryChange = {
                search.value = it
                list.value = viewModel.accountsList.filter {
                    it.name.contains(search.value) &&
                            !viewModel.friendsList.contains(it) &&
                            !viewModel.invites.contains(it) &&
                            it.uid != Firebase.auth.currentUser?.uid
                }
                            },
            onSearch = { active.value = false },
            active = active.value,
            onActiveChange = {
                active.value = it
            },
            placeholder = { Text("Search new ") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
        ) {
            FriendsList(
                list = list.value,
                columnModifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                elementModifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                imageModifier = Modifier.size(64.dp),
            ) {
                Button(onClick = {
                    viewModel.sendInvite(it.uid)
                    viewModel.search.value = false
                }) {
                    Text(text = "Send invite")
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(),
            shape = CardDefaults.elevatedShape,
            colors = CardDefaults.elevatedCardColors()
        ) {
            Button(
                onClick = {
                    showInvites.value = !showInvites.value
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Show invites")
            }
            AnimatedVisibility(visible = showInvites.value) {
                FriendsList(
                    list = viewModel.invites,
                    columnModifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    elementModifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    imageModifier = Modifier.size(64.dp),
                ) {
                    Button(onClick = {
                        viewModel.acceptInvite(it.uid)
                    }) {
                        Text(text = "Accept")
                    }
                }
            }


        }
        Spacer(modifier = Modifier.size(8.dp))
        Divider(modifier = Modifier.fillMaxWidth(0.8f))
        Spacer(modifier = Modifier.size(8.dp))
        FriendsList(
            list = viewModel.friendsList,
            columnModifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            elementModifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            imageModifier = Modifier.size(64.dp),
        ) {
        }

    }


}

@ExperimentalFoundationApi
@Composable
fun FriendsList(
    list: List<User>,
    columnModifier: Modifier = Modifier,
    elementModifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    action: @Composable (User) -> Unit
){
    LazyColumn(
        modifier = columnModifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally){
        items(list){ item ->
            ListItem(
                modifier = elementModifier
                    .animateItemPlacement(tween(500)),
                headlineContent = { Text(text = item.name) },
                leadingContent = {
                    AsyncImage(
                        model = item.request,
                        contentDescription = "User avatar",
                        modifier = imageModifier
                            .clip(shape = androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color.Blue, androidx.compose.foundation.shape.CircleShape)
                            .size(64.dp)
                            .animateItemPlacement(tween(500)),
                        contentScale = ContentScale.Crop)
                },
                trailingContent = {action.invoke(item)}
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}