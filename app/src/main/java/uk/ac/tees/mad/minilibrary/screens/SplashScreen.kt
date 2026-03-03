package uk.ac.tees.mad.minilibrary.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    var isLoading by mutableStateOf(true)
        private set

    var isAuthenticated by mutableStateOf(false)
        private set

    fun checkAuthStatus(
        onNavigateToAuth: () -> Unit,
        onNavigateToHome: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Simulate splash delay
                delay(2000)

                // Check if user is logged in
                //val session = SupabaseClient.auth.currentSessionOrNull()
                isAuthenticated = false

                isLoading = false

                // Navigate based on auth status
                delay(500)
                if (isAuthenticated) {
                    onNavigateToHome()
                } else {
                    onNavigateToAuth()
                }

            } catch (e: Exception) {
                isLoading = false
                onNavigateToAuth()
            }
        }
    }
}

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    // Start auth check
    LaunchedEffect(Unit) {
        viewModel.checkAuthStatus(onNavigateToAuth, onNavigateToHome)
    }

    // Animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1), // Indigo
                        Color(0xFF8B5CF6)  // Purple
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Book Icon
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "MiniLibrary Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "MiniLibrary",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Personal Digital Library",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading Indicator
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "MiniLibrary – Splash Screen")
@Composable
fun SplashPreview() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1), // Indigo
                        Color(0xFF8B5CF6)  // Purple
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Book Icon
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "MiniLibrary Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "MiniLibrary",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Personal Digital Library",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading Indicator (static in preview)
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}