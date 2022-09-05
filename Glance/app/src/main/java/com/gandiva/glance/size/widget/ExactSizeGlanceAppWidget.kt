package com.gandiva.glance.size.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

class ExactSizeGlanceAppWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    @Composable
    override fun Content() {
        WidgetSizeInfoText("Exact")
    }
}

@Composable
fun WidgetSizeInfoText(size: String) {
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

            text = "Size mode is $size" +
                    "\n\n" +
                    "Width ${LocalSize.current.width.value.toInt()}" +
                    "\n" +
                    " Height ${LocalSize.current.height.value.toInt()}"
        )
    }
}


class ExactSizeGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = ExactSizeGlanceAppWidget()
}