package com.gandiva.glance.media.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.IconCompat
import androidx.glance.*
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.gandiva.glance.MainActivity
import com.gandiva.glance.R
import com.gandiva.glance.ui.theme.DefaultDimens

/**
 * Implementation of App Widget functionality.
 */
class MediaWidget(private val widgetData: WidgetData) : GlanceAppWidget() {

    init {
        Log.d("MediaWidget ", "MediaWidget constructor arg $widgetData")
    }

    companion object {
        private val SMALL_BOX = DpSize(90.dp, 90.dp)
        private val BIG_BOX = DpSize(180.dp, 180.dp)
        private val VERY_BIG_BOX = DpSize(300.dp, 210.dp)
        private val ROW = DpSize(180.dp, 48.dp)
        private val LARGE_ROW = DpSize(300.dp, 48.dp)
        private val COLUMN = DpSize(48.dp, 180.dp)
        private val LARGE_COLUMN = DpSize(48.dp, 300.dp)
        private val SMALL = DpSize(90.dp, 90.dp)
        private val MEDIUM = DpSize(180.dp, 180.dp)
        private val BIG = DpSize(250.dp, 250.dp)
    }

    override val sizeMode = SizeMode.Exact

    class MaterialTheme {
        class Colors {
            val background = Color.White;
            val primary = Color(0xFF6200EE);
            val secondary = Color(0xFF03DAC5);
        }

        companion object {
            val colors = Colors()
        }

    }

    @Composable
    override fun Content() {
        Log.d("**** Widget", "Content()")
        val appDimens = DefaultDimens

//        GlanceTheme(){
//
//        }
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .fillMaxHeight()
                .cornerRadius(16.dp)
                .background(MaterialTheme.colors.background)
                .clickable(
                    onClick = actionRunCallback<LaunchAppActionCallback>()
                )
        ) {
            Column {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(LocalSize.current.height * .70f),
                ) {
                    Image(
                        provider = AndroidResourceImageProvider(widgetData.albumArt),
                        contentDescription = "large_album_art",
                        modifier = GlanceModifier.fillMaxWidth()
                            .height(LocalSize.current.height * .55f),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        contentAlignment = Alignment.BottomStart,
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        Row(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .width((LocalSize.current.width * .4f))
                                    .height((LocalSize.current.height * .4f))
                                    .padding(8.dp)
                            ) {
                                Image(
                                    provider = AndroidResourceImageProvider(widgetData.albumArt),
                                    contentDescription = "",
                                    modifier = GlanceModifier
                                        .fillMaxSize()
                                        .cornerRadius(16.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Row(
                                modifier = GlanceModifier.wrapContentSize()
                                    .height(LocalSize.current.height * .30f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val playOrPauseIconRes =
                                    if (widgetData.isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24
                                MediaButton(
                                    size = 40.dp,
                                    iconResId = playOrPauseIconRes,
                                    contentDescription = "play/pause",
                                    action = sendMediaAction(MediaWidgetService.Command.MediaPlayOrPause)
                                )
                                Spacer(modifier = GlanceModifier.size(4.dp))
                                MediaButton(
                                    size = 32.dp,
                                    iconResId = R.drawable.ic_baseline_skip_previous_24,
                                    contentDescription = "prev",
                                    action = sendMediaAction(MediaWidgetService.Command.MediaPrev)
                                )
                                Spacer(modifier = GlanceModifier.size(4.dp))
                                MediaButton(
                                    size = 32.dp,
                                    iconResId = R.drawable.ic_baseline_skip_next_24,
                                    contentDescription = "next",
                                    action = sendMediaAction(MediaWidgetService.Command.MediaNext)
                                )
                            }

                        }

                    }
                }



                Spacer(modifier = GlanceModifier.size(16.dp))
                Box(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    ProgressBar(
                        width = LocalSize.current.width - 32.dp,
                        height = 8.dp,
                        widgetData.progress,
                        trackColor = MaterialTheme.colors.secondary,
                        progressColor = MaterialTheme.colors.primary
                    )
                }
                Spacer(modifier = GlanceModifier.size(8.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 18.dp).defaultWeight(),
                ) {
                    Text(text = widgetData.currentTime, modifier = GlanceModifier.wrapContentSize().defaultWeight())
                    Text(
                        text = widgetData.totalTime,
                        modifier = GlanceModifier.wrapContentSize().defaultWeight(),
                        style = TextStyle(textAlign = TextAlign.End)
                    )
                }
            }
        }

    }
}

fun sendMediaAction(command: MediaWidgetService.Command) = actionRunCallback<StartServiceAction>(
    actionParametersOf(
        StartServiceAction.KEY_MEDIA_COMMAND to command
    )
)

class StartServiceAction : ActionCallback {

    companion object {
        val KEY_MEDIA_COMMAND = ActionParameters.Key<MediaWidgetService.Command>("command")
    }

    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        MediaWidgetService.safeSendCommand(context, parameters[KEY_MEDIA_COMMAND]!!)
    }

}

@Composable
fun ProgressBar(width: Dp, height: Dp, progress: Float, trackColor: Color, progressColor: Color) {
    Box(
        modifier = GlanceModifier
            .width(width)
            .height(height)
            .cornerRadius(16.dp)
            .background(trackColor),
        contentAlignment = Alignment.BottomStart

    ) {

        Box(
            modifier = GlanceModifier
                .width(width * progress)
                .height(height)
                .background(progressColor),
            contentAlignment = Alignment.BottomStart
        ) {}
    }
}

@Composable
fun MediaButton(size: Dp, @DrawableRes iconResId: Int, contentDescription: String, action: Action) {
    Box(
        modifier = GlanceModifier
            .cornerRadius(size / 2)
            .width(size)
            .height(size)
            .padding(size / 6)
            .background(Color.Yellow)
            .clickable(action)
    ) {
        Image(
            provider = IconImageProvider(
                IconCompat.createWithResource(LocalContext.current, iconResId)
                    .setTint(Color.Red.toArgb())
                    .toIcon(LocalContext.current)
            ),
            contentDescription = contentDescription,
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}


class LaunchAppActionCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        context.startActivity(
            Intent(
                context,
                MainActivity::class.java
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

}

data class WidgetData(
    val totalTime: String,
    val currentTime: String,
    val progress: Float,
    val isPlaying: Boolean,
    @DrawableRes val albumArt: Int
)