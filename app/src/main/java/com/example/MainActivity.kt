package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: MediaViewModel = viewModel()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "splash"
                val showBars = !currentRoute.startsWith("player") && currentRoute != "splash"

                Scaffold(
                    bottomBar = {
                        if (showBars) {
                            AppBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    if (currentRoute != route) {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0B0B0C) // CineBlack
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B0B0C))
                            // Avoid bottom innerPadding.calculateBottomPadding() so scroll view renders under the bottom bar
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        NavigationGraph(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Beautiful pill-shaped floating Dock container from layout specifications
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // safety insets overlay
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.90f) // Take exactly 90% width
                .background(Color(0xFF161618).copy(alpha = 0.75f), RoundedCornerShape(28.dp)) // slightly transparent background
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("home", "Home", Icons.Default.Home),
                Triple("movies", "Movies", Icons.Default.Movie),
                Triple("series", "Series", Icons.Default.Tv),
                Triple("profile", "Profile", Icons.Default.Person)
            )

            tabs.forEach { (route, label, icon) ->
                val isSelected = currentRoute == route
                
                // Extremely sleek color transitions for elegant UI feel and active circular/rounded-rectangle mapping
                val bgRectColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    animationSpec = tween(durationMillis = 350)
                )
                
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                    animationSpec = tween(durationMillis = 350)
                )

                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(20.dp)) // rectangle background with some more curve
                            .background(bgRectColor)
                            .clickable { onNavigate(route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    viewModel: MediaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetails = { id, type ->
                    navController.navigate("details/$id/$type")
                },
                onNavigateToPlayer = { id, type ->
                    navController.navigate("player/$id/$type/-1/-1")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                }
            )
        }

        composable("movies") {
            MoviesScreen(
                viewModel = viewModel,
                onNavigateToDetails = { id, type ->
                    navController.navigate("details/$id/$type")
                },
                onNavigateToPlayer = { id, type ->
                    navController.navigate("player/$id/$type/-1/-1")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                }
            )
        }

        composable("series") {
            SeriesScreen(
                viewModel = viewModel,
                onNavigateToDetails = { id, type ->
                    navController.navigate("details/$id/$type")
                },
                onNavigateToPlayer = { id, type ->
                    navController.navigate("player/$id/$type/-1/-1")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                userEmail = "",
                onNavigateToDetails = { id, type ->
                    navController.navigate("details/$id/$type")
                },
                onNavigateToPlayer = { id, type, s, ep ->
                    val season = s ?: -1
                    val episode = ep ?: -1
                    navController.navigate("player/$id/$type/$season/$episode")
                }
            )
        }

        composable("search") {
            SearchScreen(
                viewModel = viewModel,
                onNavigateToDetails = { id, type ->
                    navController.navigate("details/$id/$type")
                }
            )
        }

        composable(
            route = "details/{id}/{type}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val type = backStackEntry.arguments?.getString("type") ?: "movie"
            DetailScreen(
                mediaId = id,
                mediaType = type,
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
                onPlayMedia = { playId, playType, season, episode ->
                    val s = season ?: -1
                    val ep = episode ?: -1
                    navController.navigate("player/$playId/$playType/$s/$ep")
                },
                onNavigateToDetails = { similarId, similarType ->
                    navController.navigate("details/$similarId/$similarType")
                }
            )
        }

        composable(
            route = "player/{id}/{type}/{season}/{episode}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("type") { type = NavType.StringType },
                navArgument("season") { type = NavType.IntType },
                navArgument("episode") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val type = backStackEntry.arguments?.getString("type") ?: "movie"
            val seasonVal = backStackEntry.arguments?.getInt("season") ?: -1
            val episodeVal = backStackEntry.arguments?.getInt("episode") ?: -1

            val season = if (seasonVal == -1) null else seasonVal
            val episode = if (episodeVal == -1) null else episodeVal

            PlayerScreen(
                mediaId = id,
                mediaType = type,
                season = season,
                episode = episode,
                onClosePlayer = { navController.navigateUp() }
            )
        }
    }
}
