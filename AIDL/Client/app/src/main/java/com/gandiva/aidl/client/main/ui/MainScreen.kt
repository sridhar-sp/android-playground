package com.gandiva.aidl.client.main.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gandiva.aidl.client.messaging.MessagingScreen
import com.gandiva.aidl.client.sensor.SensorDataLoggerScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    val pagerState = rememberPagerState()
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            pageCount = mainViewModel.tabs.size, state = pagerState
        ) { tab: Int ->
            when (tab) {
                MainViewModel.Tabs.SensorScreenTab.position -> SensorDataLoggerScreen()
                MainViewModel.Tabs.MessageScreenTab.position -> MessagingScreen()
            }
        }
        PagerIndicator(count = mainViewModel.tabs.size, selectedIndex = pagerState.currentPage)
    }
}

@Composable
private fun BoxScope.PagerIndicator(count: Int, selectedIndex: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { index ->
            val color = if (selectedIndex == index) MaterialTheme.colorScheme.primary else
                MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .padding(2.dp)

                    .background(color, RoundedCornerShape(2.dp))
                    .width(48.dp)
                    .height(4.dp)
            )
        }
    }
}