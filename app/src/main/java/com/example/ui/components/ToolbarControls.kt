package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ToolPanelType

data class StudioToolItem(
    val panel: ToolPanelType,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isProBadge: Boolean = false
)

@Composable
fun ToolbarControls(
    activePanel: ToolPanelType,
    onSelectPanel: (ToolPanelType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val tools = listOf(
        StudioToolItem(ToolPanelType.EDIT_TOOLS, "Edit Tools", Icons.Default.ContentCut, CyberCyan),
        StudioToolItem(ToolPanelType.SPEED, "Speed & Curve", Icons.Default.Speed, CyberGold),
        StudioToolItem(ToolPanelType.FILTERS, "100+ Filters", Icons.Default.FilterVintage, CyberMagenta),
        StudioToolItem(ToolPanelType.COLOR_GRADE, "Color Grading", Icons.Default.Tune, CyberGreen),
        StudioToolItem(ToolPanelType.EFFECTS, "VFX & Keyframe", Icons.Default.AutoFixHigh, CyberPurple),
        StudioToolItem(ToolPanelType.TEXT_CAPTIONS, "AI Captions", Icons.Default.Subtitles, CyberCyan),
        StudioToolItem(ToolPanelType.AUDIO_STUDIO, "Audio Studio", Icons.Default.Headphones, CyberGreen),
        StudioToolItem(ToolPanelType.OVERLAY_PIP, "PIP & Stickers", Icons.Default.Layers, CyberMagenta),
        StudioToolItem(ToolPanelType.TRANSITIONS, "Transitions", Icons.Default.Transform, CyberGold),
        StudioToolItem(ToolPanelType.CHROMA_KEY, "Chroma Key", Icons.Default.ColorLens, CyberGreen),
        StudioToolItem(ToolPanelType.PRO_STORE, "PRO $4.99", Icons.Default.WorkspacePremium, CyberGold, isProBadge = true),
        StudioToolItem(ToolPanelType.FLUTTER_CODE, "Flutter Code", Icons.Default.Code, CyberCyan)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, GlassBorder)
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tools.forEach { tool ->
            val isSelected = activePanel == tool.panel
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) tool.accentColor.copy(alpha = 0.25f)
                        else DarkSurfaceElevated
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) tool.accentColor else GlassBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectPanel(tool.panel) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("tool_${tool.label.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.label,
                        tint = if (isSelected) tool.accentColor else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = tool.label,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (tool.isProBadge) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberGold)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("VIP", color = DarkBackground, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
