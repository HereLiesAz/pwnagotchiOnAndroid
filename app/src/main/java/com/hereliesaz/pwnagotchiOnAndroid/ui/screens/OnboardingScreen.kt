package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hereliesaz.pwnagotchiOnAndroid.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit, onNavigateToSettings: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val postNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    // Request permission when the user reaches the final page
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 3) {
            if (postNotificationPermission != null) {
                if (!postNotificationPermission.status.isGranted) {
                    postNotificationPermission.launchPermissionRequest()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "Remote Client Mode",
                    description = "Connect to your existing Pwnagotchi device over the network. Monitor its status, manage plugins, and view handshakes in real-time.",
                    imageResId = R.drawable.ic_remote
                )

                1 -> OnboardingPage(
                    title = "Standalone Mode",
                    description = "Turn your rooted Android device into a Pwnagotchi. (Requires a compatible Nexmon Wi-Fi chipset).",
                    imageResId = R.drawable.ic_standalone
                )

                2 -> OnboardingPage(
                    title = "Hybrid Mode",
                    description = "Use your phone as the 'brain' and a USB-connected Raspberry Pi as the wireless interface. The best of both worlds.",
                    imageResId = R.drawable.ic_hybrid
                )

                3 -> OnboardingPage(
                    title = "Permissions & Setup",
                    description = "To get started, we need a few permissions. After that, head to the settings screen to configure your Pwnagotchi's address."
                ) {
                    if (postNotificationPermission != null) {
                        if (!postNotificationPermission.status.isGranted) {
                            Button(onClick = { postNotificationPermission.launchPermissionRequest() }) {
                                Text("Grant Notification Permission")
                            }
                        } else {
                            Text("Notification permission granted!")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateToSettings) {
                        Text("Go to Settings")
                    }
                }
            }
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip Button - hides on the last page
            if (pagerState.currentPage < pagerState.pageCount - 1) {
                TextButton(onClick = onOnboardingComplete) {
                    Text("Skip")
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp)) // Placeholder to keep layout consistent
            }

            // Polished dot indicator
            Row(
                Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(12.dp)
                    )
                }
            }

            // Next/Get Started Button
            Button(onClick = {
                if (pagerState.currentPage < pagerState.pageCount - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onOnboardingComplete()
                }
            }) {
                Text(if (pagerState.currentPage < pagerState.pageCount - 1) "Next" else "Get Started")
            }
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    imageResId: Int? = null,
    content: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (imageResId != null) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = title,
                modifier = Modifier.size(128.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        content()
    }
}
