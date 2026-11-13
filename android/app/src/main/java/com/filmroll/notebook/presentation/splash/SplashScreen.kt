package com.filmroll.notebook.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filmroll.notebook.R
import com.filmroll.notebook.core.di.ServiceLocator
import com.filmroll.notebook.core.navigation.Screen
import com.filmroll.notebook.core.theme.*
import com.filmroll.notebook.core.ui.ScreenBackdrop

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val model: SplashViewModel = viewModel(factory = ServiceLocator.factory(Screen.Splash))
    val finished by model.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished && model.consumeTransition()) onFinished() }
    SplashScreen()
}

@Composable
fun SplashScreen() {
    val loop = rememberInfiniteTransition(label = "safelight")
    val halo by loop.animateFloat(0.12f, 0.22f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "halo")
    val float by loop.animateFloat(-4f, 4f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "filmFloat")
    val opening = stringResource(R.string.opening_app)
    Box(Modifier.fillMaxSize()) {
        ScreenBackdrop(splash = true)
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(60.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.field_notebook), style = MaterialTheme.typography.labelSmall, color = SecondaryLavender, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            Box(Modifier.size(208.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(208.dp).background(Brush.radialGradient(listOf(SafelightAccent.copy(alpha = halo), SafelightAccent.copy(alpha = 0f))), CircleShape))
                Image(painterResource(R.drawable.sprite_1), null, Modifier.size(152.dp).graphicsLayer { translationY = float.dp.toPx() }, contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.splash_title), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center, color = NotebookBackground, modifier = Modifier.widthIn(max = 300.dp).semantics { heading() })
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.splash_subtitle), style = MaterialTheme.typography.bodyLarge, color = SecondaryLavender, textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
            LinearProgressIndicator(modifier = Modifier.width(120.dp).height(4.dp).semantics { contentDescription = opening }, color = SafelightAccent, trackColor = SecondaryLavender.copy(alpha = 0.2f))
            Spacer(Modifier.height(36.dp))
            Text(stringResource(R.string.notebook_edition), style = MaterialTheme.typography.labelSmall, color = SecondaryLavender.copy(alpha = 0.7f))
        }
    }
}
