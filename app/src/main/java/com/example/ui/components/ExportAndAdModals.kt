package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.VideoEditorViewModel

@Composable
fun ExportModalDialog(
    viewModel: VideoEditorViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedRes by remember { mutableStateOf(state.exportConfig.resolution) }
    var selectedFps by remember { mutableStateOf(state.exportConfig.fps) }
    var removeWatermark by remember { mutableStateOf(state.exportConfig.removeWatermark || !state.showWatermark) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Export", tint = CyberCyan)
                        Text(
                            text = "Export Studio 2050",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.isExporting) {
                    // Export Progress View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RENDERING VIDEO PIPELINE",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { state.exportProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = CyberMagenta,
                            trackColor = DarkSurfaceHighlight
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(state.exportProgress * 100).toInt()}% Rendered",
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ETA: ${state.exportEtaSeconds}s",
                                color = CyberGold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Encoding ${selectedRes.label} @ ${selectedFps.label} (Hardware Accelerated)",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                } else if (state.exportedVideoReady) {
                    // Export Complete Screen
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyberGreen.copy(alpha = 0.2f))
                                .border(2.dp, CyberGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = CyberGreen, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Export Complete!", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Saved to DCIM/VideoEditorPro2050/${selectedRes.width}x${selectedRes.height}_60fps.mp4",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.dismissExport() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Done", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Export Settings Selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Resolution:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ExportResolution.values().forEach { res ->
                                val isSel = selectedRes == res
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isSel) CyberCyan else GlassBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedRes = res }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = res.label.split(" ").first(),
                                            color = if (isSel) CyberCyan else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (res.isProOnly && !state.isProUser) {
                                            Text("PRO", color = CyberGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Frame Rate:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ExportFps.values().forEach { fps ->
                                val isSel = selectedFps == fps
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) CyberMagenta.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isSel) CyberMagenta else GlassBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedFps = fps }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = fps.label.split(" ").first() + " " + fps.label.split(" ")[1],
                                        color = if (isSel) CyberMagenta else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        // Watermark Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Remove Watermark", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (state.isProUser) "Unlocked (PRO VIP)" else "Requires PRO or Rewarded Ad",
                                    color = if (state.isProUser) CyberGreen else CyberGold,
                                    fontSize = 9.sp
                                )
                            }
                            Switch(
                                checked = removeWatermark,
                                onCheckedChange = {
                                    if (state.isProUser) {
                                        removeWatermark = it
                                    } else {
                                        viewModel.watchRewardedAdFor4K()
                                        removeWatermark = true
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Start Render Button
                        Button(
                            onClick = {
                                viewModel.updateExportConfig(
                                    ExportConfig(
                                        resolution = selectedRes,
                                        fps = selectedFps,
                                        removeWatermark = removeWatermark
                                    )
                                )
                                viewModel.startExport()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_export_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "RENDER & EXPORT 4K 60FPS",
                                color = DarkBackground,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdMobBannerBar(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        color = Color(0xFF0F1522),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberGold)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("Ad", color = DarkBackground, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Text(
                    text = "Google AdMob · CapCut 2050 Pro Tools · Free",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Remove Ads ($4.99)",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onUpgrade() }
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Ad",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onDismiss() }
                )
            }
        }
    }
}

@Composable
fun AdMobInterstitialDialog(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    var countdown by remember { mutableStateOf(3) }
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown -= 1
        }
    }

    Dialog(onDismissRequest = { if (countdown == 0) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberMagenta)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberGold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("AdMob Interstitial", color = DarkBackground, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }

                    if (countdown == 0) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                        }
                    } else {
                        Text("Skip in ${countdown}s", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(CyberCyan.copy(alpha = 0.3f), CyberMagenta.copy(alpha = 0.3f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🚀", fontSize = 36.sp)
                        Text("UPGRADE TO VIDEO EDITOR PRO", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text("Never see ads again. Unlock full 4K 60FPS.", color = CyberCyan, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onUpgrade()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Unlock Lifetime PRO for $4.99", color = DarkBackground, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
