package com.example.mobilneprojekt

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun Gallery(navController: NavController) {
    val viewModel: MainMenuViewModel = viewModel()
    val collectAsState = viewModel.uriItems.collectAsState().value
    val collectUris = viewModel.uriItems.collectAsState().value

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
                        val context = LocalContext.current
                        Image(
                            painter = rememberAsyncImagePainter(model = collectUris[it].uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .aspectRatio(1f)
                                .clickable {
                                    updateImageRequest(collectUris[it].uri, viewModel.imageRequest, context)
                                    navController.navigateUp()
                                }
                        )
                    }

                }
            }

        }
    )
}

