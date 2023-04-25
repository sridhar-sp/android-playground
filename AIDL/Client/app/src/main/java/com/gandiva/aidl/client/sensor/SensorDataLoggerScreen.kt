package com.gandiva.aidl.client.sensor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gandiva.aidl.client.R
import com.gandiva.aidl.client.ui.theme.appDimens

@Composable
fun SensorDataLoggerScreen(viewModel: SensorDataLoggerViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize()) {
        Text(text = "SensorDataLoggerScreen", style = MaterialTheme.typography.titleMedium)

        DefaultButton(
            text = if (viewModel.isServiceConnected) stringResource(id = R.string.disconnect) else stringResource(
                id = R.string.connect_to_sensor_service
            ), onClick = {
                if (viewModel.isServiceConnected) viewModel.disconnectService()
                else viewModel.connectToService()
            }
        )

        if (viewModel.isServiceConnected) {
            Text(
                text = stringResource(id = R.string.available_apis), modifier = Modifier.padding(
                    top = MaterialTheme.appDimens.largeContentPadding,
                    bottom = MaterialTheme.appDimens.mediumContentPadding
                )
            )
            DefaultButton(text = stringResource(id = R.string.show_speed), onClick = viewModel::showSpeed)
            DefaultButton(text = stringResource(id = R.string.show_rpm), onClick = viewModel::showRpm)
            DefaultButton(text = stringResource(id = R.string.attach_listener), onClick = viewModel::listenForChanges)
            DefaultButton(text = stringResource(id = R.string.remove_listener), onClick = viewModel::removeChangeListener)
        }


    }
}

@Composable
private fun DefaultButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}