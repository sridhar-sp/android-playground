package com.gandiva.glance.widget.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.FixedColorProvider

private interface WidgetColor {
    val backgroundColor: Color
    val onBackgroundColor: Color
}

private object LightColor : WidgetColor {
    override val backgroundColor = Color.White
    override val onBackgroundColor = Color.Black
};

private object DarkColor : WidgetColor {
    override val backgroundColor = Color(0xff1b1b1b)
    override val onBackgroundColor = Color(0xffe5e5e5)
};

class StatefulGlanceAppWidget : GlanceAppWidget() {

    companion object {
        val PREF_KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val PARAM_KEY_IS_DARK_THEME = ActionParameters.Key<Boolean>("is_dark_theme")
    }

    @Composable
    override fun Content() {
        val isDarkTheme = currentState(key = PREF_KEY_IS_DARK_THEME) ?: false
        val color = if (isDarkTheme) DarkColor else LightColor
        Box(modifier = GlanceModifier.fillMaxSize().background(color.backgroundColor)) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isDarkTheme) "Dark theme" else "Light theme",
                    style = TextStyle(color = FixedColorProvider(color.onBackgroundColor), fontSize = 16.sp)
                )
                Button(
                    text = "Toggle theme", onClick = actionRunCallback<ToggleThemeActionCallback>(
                        actionParametersOf(
                            PARAM_KEY_IS_DARK_THEME to isDarkTheme
                        )
                    )
                )
            }
        }
    }
}

class ToggleThemeActionCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val isDarkTheme = parameters[StatefulGlanceAppWidget.PARAM_KEY_IS_DARK_THEME] ?: false
        updateAppWidgetState(context, glanceId) {
            it[StatefulGlanceAppWidget.PREF_KEY_IS_DARK_THEME] = !isDarkTheme
        }
        StatefulGlanceAppWidget().update(context, glanceId)
    }
}

class StatefulGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = StatefulGlanceAppWidget()
}