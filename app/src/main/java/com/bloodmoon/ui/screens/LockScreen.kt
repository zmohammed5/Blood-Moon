package com.bloodmoon.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.bloodmoon.ui.components.Moony
import com.bloodmoon.ui.components.MoonyAnimation
import com.bloodmoon.ui.components.MoonyVariant
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    isBiometricEnabled: Boolean,
    onPinEntered: suspend (String) -> Boolean,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var isShaking by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Shake animation for wrong PIN
    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 10f else 0f,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(50),
            repeatMode = RepeatMode.Reverse
        ),
        finishedListener = { isShaking = false },
        label = "shake"
    )

    // Show biometric prompt on mount if enabled
    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled && context is FragmentActivity) {
            showBiometricPrompt(context, onUnlocked)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Moony with shield
            Moony(
                variant = MoonyVariant.DEFAULT,
                animation = if (isShaking) MoonyAnimation.ANGRY_SHAKE else MoonyAnimation.FLOAT,
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = shakeOffset.dp)
            )

            // Lock title
            Text(
                text = "Enter your sacred sigil",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // PIN Display
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                if (index < pin.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }
            }

            // Error message
            if (showError) {
                Text(
                    text = "The sigil rejects you…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Number pad (glyph-styled)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (col in 0..2) {
                            val number = row * 3 + col + 1
                            PinButton(
                                text = number.toString(),
                                onClick = {
                                    if (pin.length < 4) {
                                        pin += number
                                        if (pin.length == 4) {
                                            // Check PIN
                                            coroutineScope.launch {
                                                if (onPinEntered(pin)) {
                                                    onUnlocked()
                                                } else {
                                                    isShaking = true
                                                    showError = true
                                                    pin = ""
                                                    kotlinx.coroutines.delay(2000)
                                                    showError = false
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Bottom row: empty, 0, backspace
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.size(64.dp))
                    PinButton(
                        text = "0",
                        onClick = {
                            if (pin.length < 4) {
                                pin += "0"
                                if (pin.length == 4) {
                                    coroutineScope.launch {
                                        if (onPinEntered(pin)) {
                                            onUnlocked()
                                        } else {
                                            isShaking = true
                                            showError = true
                                            pin = ""
                                            kotlinx.coroutines.delay(2000)
                                            showError = false
                                        }
                                    }
                                }
                            }
                        }
                    )
                    PinButton(
                        text = "⌫",
                        onClick = {
                            if (pin.isNotEmpty()) {
                                pin = pin.dropLast(1)
                                showError = false
                            }
                        }
                    )
                }
            }

            // Biometric button
            if (isBiometricEnabled && context is FragmentActivity) {
                TextButton(
                    onClick = {
                        showBiometricPrompt(context, onUnlocked)
                    }
                ) {
                    Text(
                        text = "Use Biometric",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PinButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit
) {
    val biometricManager = BiometricManager.from(activity)
    val canAuthenticate = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )

    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Blood Moon")
            .setSubtitle("Unlock with your biometric credential")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
