package com.gandiva.glance

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gandiva.glance.media.widget.MediaPlayerService
import com.gandiva.glance.media.widget.MediaWidgetService
import com.gandiva.glance.ui.theme.GlanceTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlanceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column() {
                        Greeting("Android")
                        Button(onClick = {


//                            MediaWidgetService.start(this@MainActivity)

                            bindService(
                                Intent(this@MainActivity, MediaPlayerService::class.java),
                                object : ServiceConnection {
                                    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                                        Log.d("MWS", "IMediaPlayerService onServiceConnected")
//                                        mediaPlayerService = service as IMediaPlayerService
//                                        c.resumeWith(Result.success(mediaPlayerService!!))
                                    }

                                    override fun onServiceDisconnected(p0: ComponentName?) {
                                        Log.d("MWS", "IMediaPlayerService onServiceDisconnected")
                                        //Nothing here
                                    }
                                },
                                Context.BIND_AUTO_CREATE
                            )
                        }) {
                            Text(text = "Start Service")
                        }
                        Button(onClick = { MediaWidgetService.stop(this@MainActivity) }) {
                            Text(text = "Stop Service")
                        }
                    }


                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GlanceTheme {
        Greeting("Android")
    }
}