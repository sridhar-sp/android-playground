package com.gandiva.glance.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.FixedColorProvider

class ResponsiveSizeGlanceAppWidget : GlanceAppWidget() {

    companion object {
        val TINY_BOX = DpSize(110.dp, 110.dp)
        val SMALL_BOX = DpSize(210.dp, 110.dp)
        val SMALL_RECTANGLE = DpSize(330.dp, 110.dp)
        val TINY_TALL_RECTANGLE = DpSize(110.dp, 310.dp)
        val TALL_RECTANGLE = DpSize(210.dp, 310.dp)
        val MEDIUM_BOX = DpSize(330.dp, 310.dp)
        val BIG_RECTANGLE = DpSize(330.dp, 410.dp)

        fun getSizeString(width: Dp, height: Dp): String {
            return when (DpSize(width, height)) {
                TINY_BOX -> "Tiny box"
                SMALL_BOX -> "Small box"
                SMALL_RECTANGLE -> "Small rectangle"
                MEDIUM_BOX -> "Medium box"
                BIG_RECTANGLE -> "Big rectangle"
                TALL_RECTANGLE -> "Tall rectangle"
                TINY_TALL_RECTANGLE -> "Tint tall rectangle"
                else -> "Unknown size"
            }
        }
    }

    override val sizeMode: SizeMode
        get() = SizeMode.Responsive(
            setOf(
                TINY_BOX, SMALL_BOX, SMALL_RECTANGLE, TINY_TALL_RECTANGLE,
                TALL_RECTANGLE, MEDIUM_BOX, BIG_RECTANGLE
            )
        )

    @Composable
    override fun Content() {
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                style = TextStyle(
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = FixedColorProvider(Color.White)
                ),

                text = "Size mode is Responsive" +
                        "\n\n" +
                        "Width ${LocalSize.current.width.value.toInt()}" +
                        "\n" +
                        " Height ${LocalSize.current.height.value.toInt()}" +
                        "\n\n" +
                        "${getSizeText()}"
            )
        }
    }

    @Composable
    private fun getSizeText() = getSizeString(LocalSize.current.width, LocalSize.current.height)
}

class ResponsiveSizeGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = ResponsiveSizeGlanceAppWidget()
}