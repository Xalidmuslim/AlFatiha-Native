package app.xalidmuslim.azkar.ui.designsystem

import android.graphics.Paint as FrameworkPaint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

internal fun Modifier.azkarShadow(
    layers: List<AzkarShadowLayer>,
    cornerRadius: Dp,
): Modifier = drawBehind {
    if (layers.isEmpty()) return@drawBehind
    val radiusPx = cornerRadius.toPx()
    drawIntoCanvas { canvas ->
        val paint = FrameworkPaint()
        layers.forEach { layer ->
            paint.reset()
            paint.style = FrameworkPaint.Style.FILL
            paint.color = layer.color.toArgb()
            paint.setShadowLayer(
                layer.blurRadius.toPx(),
                0f,
                layer.offsetY.toPx(),
                layer.color.toArgb(),
            )
            canvas.nativeCanvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                radiusPx, radiusPx, paint,
            )
            paint.clearShadowLayer()
        }
    }
}

@Composable
fun AzkarSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.background(AzkarThemeValues.colors.background),
        content = content,
    )
}

@Composable
fun AzkarCardSurface(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    completed: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val top = if (compact) AzkarSpacing.compactCard else AzkarSpacing.cardTop
    val horizontal = if (compact) AzkarSpacing.compactCard else AzkarSpacing.cardHorizontal
    val bottom = if (compact) AzkarSpacing.compactCard else AzkarSpacing.cardBottom
    val shape = RoundedCornerShape(AzkarRadius.dhikrCard)
    val cardBackground = if (completed) colors.doneMarkerBackground else colors.card
    val cardBorder = if (completed) colors.doneCardBorder else colors.border

    Box(
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = shape,
                clip = false,
                ambientColor = colors.border.copy(alpha = 0.18f),
                spotColor = colors.border.copy(alpha = 0.18f),
            )
            .clip(shape)
            .background(cardBackground)
            .drawBehind {
                if (completed) {
                    drawRect(
                        color = colors.success,
                        size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height),
                    )
                }
            }
            .border(
                AzkarBorders.thin,
                cardBorder,
                shape,
            )
            .padding(start = horizontal, end = horizontal, top = top, bottom = bottom),
        content = content,
    )
}

@Composable
fun AzkarPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AzkarThemeValues.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(AzkarMotion.toggleDurationMillis),
        label = "primary-press",
    )
    val background = if (enabled) colors.primary else colors.surface
    val foreground = if (enabled) colors.countButtonText else colors.muted

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .defaultMinSize(
                minWidth = AzkarDimensions.countButtonMinWidth,
                minHeight = AzkarDimensions.countButtonMinHeight,
            )
            .clip(RoundedCornerShape(AzkarRadius.countButton))
            .background(background)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = AzkarSpacing.countButtonHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.countButton.copy(color = foreground),
        )
    }
}

@Composable
fun AzkarOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AzkarThemeValues.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(AzkarMotion.toggleDurationMillis),
        label = "outline-press",
    )
    val shape = RoundedCornerShape(AzkarRadius.explainButton)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .fillMaxWidth()
            .defaultMinSize(minHeight = AzkarDimensions.explainButtonMinHeight)
            .clip(shape)
            .background(colors.surface)
            .border(AzkarBorders.thin, colors.border, shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.explainButton.copy(
                color = if (enabled) colors.foreground else colors.muted,
            ),
        )
    }
}

enum class AzkarIconButtonSize { Standard, Compact }

@Composable
fun AzkarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: AzkarIconButtonSize = AzkarIconButtonSize.Standard,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(AzkarMotion.toggleDurationMillis),
        label = "icon-press",
    )
    val dimension = when (size) {
        AzkarIconButtonSize.Standard -> AzkarDimensions.settingsIconButton
        AzkarIconButtonSize.Compact -> AzkarDimensions.compactIconButton
    }
    val radius = when (size) {
        AzkarIconButtonSize.Standard -> AzkarRadius.settingsIcon
        AzkarIconButtonSize.Compact -> AzkarRadius.toolbarButton
    }
    val shape = RoundedCornerShape(radius)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .size(dimension)
            .clip(shape)
            .background(colors.card)
            .border(AzkarBorders.thin, colors.border, shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun AzkarBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(AzkarRadius.pill)

    Box(
        modifier = modifier
            .clip(shape)
            .border(AzkarBorders.thin, colors.warningBadgeBorder, shape)
            .padding(
                horizontal = AzkarSpacing.badgeHorizontal,
                vertical = AzkarSpacing.badgeVertical,
            ),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = AzkarThemeValues.typography.disputeBadge.copy(color = colors.warning),
        )
    }
}

@Composable
fun AzkarProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val colors = AzkarThemeValues.colors
    val target = progress.coerceIn(0f, 1f)
    val displayed = if (animate) {
        animateFloatAsState(
            targetValue = target,
            animationSpec = tween(
                durationMillis = AzkarMotion.progressDurationMillis,
                easing = AzkarMotion.progressEasing,
            ),
            label = "AzkarProgress",
        ).value
    } else {
        target
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = displayed,
                    range = 0f..1f,
                )
            }
            .height(AzkarDimensions.progressTrackHeight)
            .clip(RoundedCornerShape(AzkarRadius.pill))
            .background(colors.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(displayed)
                .height(AzkarDimensions.progressTrackHeight)
                .clip(RoundedCornerShape(AzkarRadius.pill))
                .background(colors.primary),
        )
    }
}


@Composable
fun AzkarConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AzkarThemeValues.colors
    val shape = RoundedCornerShape(18.dp)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.card)
                .border(AzkarBorders.thin, colors.border, shape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText(
                text = title,
                style = AzkarThemeValues.typography.sheetTitle.copy(color = colors.foreground),
            )
            BasicText(
                text = message,
                style = AzkarThemeValues.typography.sheetSubtitle.copy(color = colors.muted),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AzkarOutlineButton(
                    text = "Отмена",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                AzkarPrimaryButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
