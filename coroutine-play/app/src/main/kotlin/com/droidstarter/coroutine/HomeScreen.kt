package com.droidstarter.coroutine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Single Core to Coroutine",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium
        )

        val text = if (viewModel.useCoroutine) "Use Coroutine" else "Use Thread"
        DefaultSwitch(
            text = text, isChecked = viewModel.useCoroutine, onCheckedChange = viewModel::setShouldUseCoroutine
        )

        ColumnWithBorder {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DefaultButton(
                    modifier = Modifier.padding(end = 8.dp),
                    text = "Show thread count",
                    onClick = viewModel::readThreadCount
                )
                DefaultText(text = "Count ${viewModel.threadCount}")
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DefaultButton(modifier = Modifier.padding(end = 8.dp),
                    text = "Run GC",
                    onClick = { Runtime.getRuntime().gc() })
                DefaultButton(text = "Clear cache & restart", onClick = {
                    viewModel.clearAppCacheAndRestart(context)
                })
            }

        }

        ColumnWithBorder {
            DefaultSlider(
                titlePrefix = "Parallel task count",
                value = viewModel.parallelTaskCount.toFloat(),
                max = HomeViewModel.MAX_TASK_COUNT.toFloat(),
                onValueChange = viewModel::updateTaskCount
            )

            if (viewModel.useCoroutine) {
                DefaultSwitch(
                    text = "Limit Parallelism",
                    isChecked = viewModel.shouldLimitParallelism,
                    onCheckedChange = viewModel::updateShouldLimitParallelism
                )
                if (viewModel.shouldLimitParallelism) {
                    DefaultSlider(
                        titlePrefix = "Max coroutine parallel task",
                        value = viewModel.coroutineParallelismLimit.toFloat(),
                        max = HomeViewModel.MAX_CO_ROUTINE_PARALLEL_TASK.toFloat(),
                        onValueChange = viewModel::updateCoroutineParallelismLimit
                    )
                }
            }

            LogText(text = viewModel.taskStatus)

            if (viewModel.exeuctedTaskCountStatus > 0) {
                DefaultText(text = "Ran ${viewModel.exeuctedTaskCountStatus} tasks")
            }

            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DefaultButton(text = "Run all task", onClick = viewModel::runAllTask)
                DefaultButton(text = "Fetch status", onClick = viewModel::updateCurrentTaskExecutionStatus)
            }
        }

        ColumnWithBorder {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DefaultButton(text = "Download Image", onClick = viewModel::downloadImage)
                if (viewModel.isDownloadInProgress) CircularProgressIndicator()
            }

            LogText(text = viewModel.downloadImageLogs)

            viewModel.downloadedImage?.let { image ->
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 8.dp)
                )
            }
        }

    }
}

@Composable
private fun ColumnWithBorder(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(
                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary), shape = RectangleShape
            )
            .padding(8.dp),
    ) {
        content()
    }
}

@Composable
private fun DefaultSlider(
    titlePrefix: String, value: Float, min: Float = 1f, max: Float = 1f, onValueChange: (Float) -> Unit
) {
    Text(text = "$titlePrefix ${value.toInt()}")
    Slider(value = value, onValueChange = onValueChange, valueRange = min..max, steps = max.toInt())
}

@Composable
private fun DefaultSwitch(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal)
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DefaultButton(
    modifier: Modifier = Modifier, text: String, onClick: () -> Unit
) {
    Button(modifier = modifier, onClick = onClick) {
        Text(text = text)
    }
}

@Composable
private fun DefaultText(text: String) {
    Text(text = text)
}

@Composable
private fun LogText(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, fontSize = 12.sp, lineHeight = 16.sp)
}

@Composable
private fun DefaultSpacer() {
    Spacer(modifier = Modifier.height(8.dp))
}