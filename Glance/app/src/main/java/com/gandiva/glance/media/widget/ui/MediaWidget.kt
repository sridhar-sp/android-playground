package com.gandiva.glance.media.widget.ui

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
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.gandiva.glance.MainActivity
import com.gandiva.glance.R
import com.gandiva.glance.media.widget.ui.MediaActionHandler.Companion.sendMediaAction
import com.gandiva.glance.media.widget.manager.MediaWidgetManagerImpl
import java.io.Serializable


/**
 * Implementation of App Widget functionality.
 */
class MediaWidget(private val widgetData: WidgetData) : GlanceAppWidget(errorUiLayout = R.layout.media_widget_error) {

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

        val PREF_IS_DARK_THEME_KEY = booleanPreferencesKey("isDarkTheme")
    }

    override val sizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<Preferences> = PreferencesGlanceStateDefinition

    @Composable
    override fun Content() {

        val isDarkTheme = currentState(key = PREF_IS_DARK_THEME_KEY) ?: false

        WidgetTheme(darkTheme = isDarkTheme) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .cornerRadius(MediaWidgetTheme.dimens.widgetCardRadius)
                    .background(MediaWidgetTheme.colors.background)
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
                            contentAlignment = Alignment.TopEnd,
                            modifier = GlanceModifier
                                .clickable(onClick = sendMediaAction(MediaActionHandler.Command.ToggleTheme))
                                .padding(MediaWidgetTheme.dimens.contentPadding)
                        ) {
                            ThemeToggleButton(isDarkTheme = isDarkTheme)
                        }

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
                                        .padding(MediaWidgetTheme.dimens.contentPadding)
                                ) {
                                    Image(
                                        provider = AndroidResourceImageProvider(widgetData.albumArt),
                                        contentDescription = "",
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .cornerRadius(MediaWidgetTheme.dimens.widgetCardRadius),
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
                                        size = MediaWidgetTheme.dimens.widgetLargeIconSize,
                                        iconResId = playOrPauseIconRes,
                                        contentDescription = "play/pause",
                                        action = sendMediaAction(MediaActionHandler.Command.MediaPlayOrPause)
                                    )
                                    Spacer(modifier = GlanceModifier.size(4.dp))
                                    MediaButton(
                                        size = MediaWidgetTheme.dimens.widgetMediumIconSize,
                                        iconResId = R.drawable.ic_baseline_skip_previous_24,
                                        contentDescription = "prev",
                                        action = sendMediaAction(MediaActionHandler.Command.MediaPrev)
                                    )
                                    Spacer(modifier = GlanceModifier.size(4.dp))
                                    MediaButton(
                                        size = MediaWidgetTheme.dimens.widgetMediumIconSize,
                                        iconResId = R.drawable.ic_baseline_skip_next_24,
                                        contentDescription = "next",
                                        action = sendMediaAction(MediaActionHandler.Command.MediaNext)
                                    )
                                }

                            }
                        }
                    }


                    Spacer(modifier = GlanceModifier.size(MediaWidgetTheme.dimens.contentPadding))
                    Box(
                        modifier = GlanceModifier.fillMaxWidth()
                            .padding(horizontal = MediaWidgetTheme.dimens.contentPadding)
                    ) {
                        ProgressBar(
                            width = LocalSize.current.width - MediaWidgetTheme.dimens.contentPadding.times(2),
                            height = 8.dp,
                            widgetData.progress,
                            trackColor = MediaWidgetTheme.colors.onPrimary,
                            progressColor = MediaWidgetTheme.colors.primary
                        )
                    }
                    Spacer(modifier = GlanceModifier.size(8.dp))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                            .padding(horizontal = MediaWidgetTheme.dimens.contentPadding).defaultWeight(),
                    ) {
                        Text(
                            text = widgetData.currentTime,
                            modifier = GlanceModifier.wrapContentSize().defaultWeight(),
                            style = TextStyle(color = ColorProvider(MediaWidgetTheme.colors.onBackground))
                        )
                        Text(
                            text = widgetData.totalTime,
                            modifier = GlanceModifier.wrapContentSize().defaultWeight(),
                            style = TextStyle(
                                textAlign = TextAlign.End,
                                color = ColorProvider(MediaWidgetTheme.colors.onBackground)
                            )
                        )
                    }
                }
            }
        }


    }
}


class MediaActionHandler : ActionCallback {
    sealed interface Command : Serializable {
        object MediaPlayOrPause : Command
        object MediaNext : Command
        object MediaPrev : Command
        object ToggleTheme : Command
    }

    companion object {
        private val KEY_MEDIA_COMMAND = ActionParameters.Key<Command>("media_command")

        fun sendMediaAction(command: Command) = actionRunCallback<MediaActionHandler>(
            actionParametersOf(KEY_MEDIA_COMMAND to command)
        )
    }

    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {

        val command = parameters[KEY_MEDIA_COMMAND]!!
        Log.d("MediaWidget", "OnRun $command")
        val mediaWidgetManager = MediaWidgetManagerImpl.mediaWidgetManager
        when (command) {
            is Command.MediaPlayOrPause -> mediaWidgetManager.playOrPause()
            is Command.MediaNext -> mediaWidgetManager.next()
            is Command.MediaPrev -> mediaWidgetManager.prev()
            is Command.ToggleTheme -> mediaWidgetManager.toggleTheme(glanceId)
        }
    }
}


@Composable
fun ProgressBar(width: Dp, height: Dp, progress: Float, trackColor: Color, progressColor: Color) {
    Box(
        modifier = GlanceModifier
            .width(width)
            .height(height)
            .cornerRadius(MediaWidgetTheme.dimens.widgetCardRadius)
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
            .background(MediaWidgetTheme.colors.secondary)
            .clickable(action)
    ) {
        Image(
            provider = IconImageProvider(
                IconCompat.createWithResource(LocalContext.current, iconResId)
                    .setTint(MediaWidgetTheme.colors.onSecondary.toArgb())
                    .toIcon(LocalContext.current)
            ),
            contentDescription = contentDescription,
            modifier = GlanceModifier.fillMaxSize()
        )
    }
}

@Composable
fun ThemeToggleButton(isDarkTheme: Boolean) {
    Image(
        provider = IconImageProvider(
            IconCompat.createWithResource(
                LocalContext.current,
                if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon
            )
                .setTint(MediaWidgetTheme.colors.onSecondary.toArgb())
                .toIcon(LocalContext.current)
        ),
        contentDescription = "toggle",
        modifier = GlanceModifier
            .background(MediaWidgetTheme.colors.background)
            .cornerRadius(MediaWidgetTheme.dimens.widgetCardRadius)
            .fillMaxSize()
            .width(MediaWidgetTheme.dimens.widgetLargeIconSize)
            .height(MediaWidgetTheme.dimens.widgetLargeIconSize)
            .padding(8.dp)
    )
}

class ThemeToggleActionCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Log.d("**** Widget", "ThemeToggleActionCallback glanceId $glanceId")
//        MediaWidgetService.safeSendCommand(context, MediaWidgetService.Command.ToggleTheme)
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