package com.gandiva.glance

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

/**
 * Sample AppWidget that showcase the Responsive SizeMode changing its content to Row, Column or Box
 * based on the available space. In addition to shows how alignment and default weight works
 * on these components.
 */
class ResponsiveAppWidget : GlanceAppWidget {

    constructor() {
        Log.d("**** Widget", "ResponsiveAppWidget ()")
    }

    companion object {
        private val SMALL_BOX = DpSize(90.dp, 90.dp)
        private val BIG_BOX = DpSize(180.dp, 180.dp)
        private val VERY_BIG_BOX = DpSize(300.dp, 300.dp)
        private val ROW = DpSize(180.dp, 48.dp)
        private val LARGE_ROW = DpSize(300.dp, 48.dp)
        private val COLUMN = DpSize(48.dp, 180.dp)
        private val LARGE_COLUMN = DpSize(48.dp, 300.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_BOX, BIG_BOX, ROW, LARGE_ROW, COLUMN, LARGE_COLUMN)
    )

//    override val sizeMode: SizeMode
//        get() = SizeMode.Exact

    @Composable
    override fun Content() {
        Log.d("**** Responsive Widget", "Responsive Content()")

        // Content will be called for each of the provided sizes
//        when (LocalSize.current) {
//            COLUMN -> ResponsiveColumn(numItems = 3)
//            ROW -> ResponsiveRow(numItems = 3)
//            LARGE_COLUMN -> ResponsiveColumn(numItems = 5)
//            LARGE_ROW -> ResponsiveRow(numItems = 5)
//            SMALL_BOX -> ResponsiveBox(numItems = 1)
//            BIG_BOX -> ResponsiveBox(numItems = 3)
//            VERY_BIG_BOX -> ResponsiveBox(numItems = 5)
//            else -> throw IllegalArgumentException("Invalid size not matching the provided ones")
//        }

        when (LocalSize.current) {
            COLUMN -> SimpleText(LocalSize.current, "COLUMN")
            ROW -> SimpleText(LocalSize.current, "ROW")
            LARGE_COLUMN -> SimpleText(LocalSize.current, "LARGE_COLUMN")
            LARGE_ROW -> SimpleText(LocalSize.current, "LARGE_ROW")
            SMALL_BOX -> SimpleText(LocalSize.current, "SMALL_BOX")
            BIG_BOX -> SimpleText(LocalSize.current, "BIG_BOX")
            VERY_BIG_BOX -> SimpleText(LocalSize.current, "VERY_BIG_BOX")
            else -> SimpleText(LocalSize.current, "ELSE ")
        }
    }
}

@Composable
fun SimpleText(dpSize: DpSize, text: String) {
    Text(
        text = text + " ${dpSize.width} x ${dpSize.height}", style = TextStyle(
            fontSize = 24.sp
        )
    )
}

private val ItemClickedKey = ActionParameters.Key<String>("name")

private val parentModifier = GlanceModifier
    .fillMaxSize()
    .padding(8.dp)
    .background(android.R.color.white)

private val columnColors = listOf(Color(0xff70D689), Color(0xffB2E5BF))
private val rowColors = listOf(Color(0xff5087EF), Color(0xffA2BDF2))
private val boxColors = listOf(Color(0xffF7A998), Color(0xffFA5F3D))

/**
 * Displays a column with three items that share evenly the available space
 */
@Composable
private fun ResponsiveColumn(numItems: Int) {
    Column(parentModifier) {
        val modifier = GlanceModifier.fillMaxSize().padding(4.dp).defaultWeight()
        (1..numItems).forEach {
            val color = columnColors[(it - 1) % columnColors.size]
            ContentItem("$it", color, modifier)
        }
    }
}

/**
 * Displays a row with three items that share evenly the available space
 */
@Composable
private fun ResponsiveRow(numItems: Int) {
    Row(parentModifier) {
        val modifier = GlanceModifier.fillMaxSize().padding(4.dp).defaultWeight()
        (1..numItems).forEach {
            val color = rowColors[(it - 1) % rowColors.size]
            ContentItem("$it", color, modifier)
        }
    }
}

/**
 * Displays a Box with three items on top of each other
 */
@Composable
private fun ResponsiveBox(numItems: Int) {
    val size = LocalSize.current
    Box(modifier = parentModifier, contentAlignment = Alignment.Center) {
        (1..numItems).forEach {
            val index = numItems - it + 1
            val color = boxColors[(index - 1) % boxColors.size]
            val boxSize = (size.width * index) / numItems
            ContentItem("$index",
                color,
                GlanceModifier.size(boxSize),
                textStyle = TextStyle(textAlign = TextAlign.End).takeIf { numItems != 1 }
            )
        }
    }
}

@Composable
private fun ContentItem(
    text: String,
    color: Color,
    modifier: GlanceModifier,
    textStyle: TextStyle? = null
) {
    Box(modifier = modifier) {
        Button(
            text = text,
            modifier = GlanceModifier.fillMaxSize().padding(8.dp).background(color),
            style = textStyle ?: TextStyle(textAlign = TextAlign.Center),
            onClick = actionRunCallback<ResponsiveAction>(
                actionParametersOf(
                    ItemClickedKey to text
                )
            )
        )
    }
}

class ResponsiveAction : ActionCallback {
    override suspend fun onRun(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Handler(context.mainLooper).post {
            Toast.makeText(
                context,
                "Item clicked: ${parameters[ItemClickedKey]}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

class ResponsiveAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ResponsiveAppWidget()
}