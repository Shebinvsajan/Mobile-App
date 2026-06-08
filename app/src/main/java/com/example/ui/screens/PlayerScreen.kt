package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun PlayerScreen(
    mediaId: Int,
    mediaType: String,
    season: Int? = null,
    episode: Int? = null,
    onClosePlayer: () -> Unit
) {
    val context = LocalContext.current
    var isPageLoading by remember { mutableStateOf(true) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var controlsTrigger by remember { mutableStateOf(0) }
    var aspectRatioMode by remember { mutableStateOf("fit") } // "fit", "fill", "stretch"
    var lastAppliedMode by remember { mutableStateOf("") }

    // Auto-hide player controls after 3 seconds of inactivity. Resets on state trigger update.
    LaunchedEffect(areControlsVisible, controlsTrigger) {
        if (areControlsVisible) {
            delay(3000)
            areControlsVisible = false
        }
    }

    // Force landscape mode for full theater experience during playback, restore on exit
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Build URL according to Cineverse specification
    val colorHex = "E50914"
    val playerUrl = if (mediaType == "tv") {
        val s = season ?: 1
        val ep = episode ?: 1
        "https://player.videasy.net/tv/$mediaId/$s/$ep?color=$colorHex&nextEpisode=true&autoplayNextEpisode=true&episodeSelector=true&overlay=true"
    } else {
        "https://player.videasy.net/movie/$mediaId?color=$colorHex&overlay=true"
    }

    // Back handler inside landscape player
    BackHandler {
        onClosePlayer()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // WebView container
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isPageLoading = false
                            applyAspectRatioJs(this@apply, aspectRatioMode)
                        }
                    }
                    webChromeClient = object : WebChromeClient() {}
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        textZoom = 100
                        databaseEnabled = true
                        
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                    }

                    // Listen to touches to wake up controls and reset auto-hide timer
                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            areControlsVisible = true
                            controlsTrigger++
                        }
                        false // Pass event so web-based player actions (play, speed, menus) work normally
                    }
                    
                    loadUrl(playerUrl)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { webView ->
                // Apply Javascript only when the ratio mode has actually changed to avoid disrupting page focus/reload
                if (aspectRatioMode != lastAppliedMode) {
                    lastAppliedMode = aspectRatioMode
                    applyAspectRatioJs(webView, aspectRatioMode)
                }
            }
        )

        // Loading Indicator Overlay
        if (isPageLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFE50914),
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        // Floating Back/Close button on Top-Left (Animated Auto-Hide)
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onClosePlayer,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Player",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Floating Aspect Ratio control bar on Top-Right (Animated Auto-Hide)
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.60f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Screen Ratio",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                val modes = listOf(
                    "fit" to "Original",
                    "fill" to "Fill",
                    "stretch" to "Stretch"
                )
                
                modes.forEach { (mode, label) ->
                    val isSelected = aspectRatioMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFFE50914) else Color.Transparent
                            )
                            .clickable {
                                aspectRatioMode = mode
                                controlsTrigger++ // Refresh the auto-hide timer on interaction
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

/**
 * Robustly injects Javascript inside the WebView container (including any matching iframes) 
 * to style video players using global element selectors. Utilizing a short-lived loop to ensure 
 * rendering captures dynamic DOM nodes perfectly.
 */
private fun applyAspectRatioJs(webView: WebView, mode: String) {
    val fitValue = when (mode) {
        "fill" -> "cover"
        "stretch" -> "fill"
        else -> "contain"
    }
    
    val js = """
        (function() {
            window.currentFitValue = '$fitValue';
            
            function injectStyle(doc) {
                if (!doc) return;
                var currentFit = window.currentFitValue || 'contain';
                var styleId = 'aspect-ratio-style';
                var style = doc.getElementById(styleId);
                if (!style) {
                    style = doc.createElement('style');
                    style.id = styleId;
                    doc.head.appendChild(style);
                }
                style.innerHTML = 'video { object-fit: ' + currentFit + ' !important; width: 100% !important; height: 100% !important; min-width: 100% !important; min-height: 100% !important; }';

                // For immediate update
                var videos = doc.getElementsByTagName('video');
                for (var i = 0; i < videos.length; i++) {
                    videos[i].style.setProperty('object-fit', currentFit, 'important');
                    videos[i].style.setProperty('width', '100%', 'important');
                    videos[i].style.setProperty('height', '100%', 'important');
                    videos[i].style.setProperty('min-width', '100%', 'important');
                    videos[i].style.setProperty('min-height', '100%', 'important');
                }
            }

            // Apply directly
            injectStyle(document);

            // Periodically check and enforce correct styling on prospective nested components or dynamic frames
            if (!window.aspectRatioInterval) {
                window.aspectRatioInterval = setInterval(function() {
                    injectStyle(document);
                    var iframes = document.getElementsByTagName('iframe');
                    for (var j = 0; j < iframes.length; j++) {
                        try {
                            var iframeDoc = iframes[j].contentDocument || iframes[j].contentWindow.document;
                            injectStyle(iframeDoc);
                        } catch(e) {}
                    }
                }, 500);
            } else {
                // Ensure immediate update in current interval loop
                var iframes = document.getElementsByTagName('iframe');
                for (var j = 0; j < iframes.length; j++) {
                    try {
                        var iframeDoc = iframes[j].contentDocument || iframes[j].contentWindow.document;
                        injectStyle(iframeDoc);
                    } catch(e) {}
                }
            }
        })()
    """.trimIndent()
    
    webView.evaluateJavascript(js, null)
}
