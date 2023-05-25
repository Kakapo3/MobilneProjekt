package com.example.mobilneprojekt

import android.content.Intent
import android.content.res.Configuration
import android.widget.Gallery
import android.widget.RatingBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.logging.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Gallery(navController: NavController){
    val viewModel: MainMenuViewModel = viewModel()
    val collectAsState = viewModel.uriItems.collectAsState().value
    val collectUris = viewModel.uriItems.collectAsState().value

    MaterialTheme() {
        Scaffold(
            containerColor = Color.Black,
            contentColor = Color.White,
            content = { padding ->
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = 0.dp,
                            start = 0.dp,
                            end = 0.dp
                        )
                ) {

                    items(collectAsState.size) {
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                        ) {

                            Image(
                                painter = rememberAsyncImagePainter(model = collectUris[it].uri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .aspectRatio(1f)
                                    .clickable {
                                        viewModel.updateImageRequest(collectUris[it].uri)
                                        navController.navigateUp()
                                    }
                            )
                        }

                    }
                }

            }
        )
    }
}
