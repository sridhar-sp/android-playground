package com.gandiva.aidl.client.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gandiva.aidl.client.sensor.SensorDataLoggerScreen
import com.gandiva.aidl.client.sensor.SensorDataLoggerScreenV2
import com.gandiva.aidl.client.ui.theme.AppTheme
import com.gandiva.aidl.client.ui.theme.appDimens
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(themeMode = AppCompatDelegate.MODE_NIGHT_YES, dynamicColor = false) {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        BoxWithWeight {
                            SensorDataLoggerScreen()
                        }
                        BoxWithWeight {
                            SensorDataLoggerScreenV2()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.BoxWithWeight(
    weight: Float = 1f, content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(MaterialTheme.appDimens.smallContentPadding)
            .border(2.dp, MaterialTheme.colorScheme.onSurface, RectangleShape), content = content
    )
}
