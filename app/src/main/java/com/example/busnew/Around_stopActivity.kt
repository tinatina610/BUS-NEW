package com.example.busnew

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.busnew.ui.theme.BUSNEWTheme
import com.example.myapplication.TDXApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Around_stopActivity : ComponentActivity() {
    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BUSNEWTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tdxResult = remember { mutableStateOf("Loading TDX data...") }
                    val aroundStopResult = remember { mutableStateOf("Loading Around Stop data...") }

                    // Launch Coroutine for TDXApi
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val tdxResultJson = TDXApi.main()
                            withContext(Dispatchers.Main) {
                                tdxResult.value = tdxResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error fetching TDX data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                tdxResult.value = "Error fetching TDX data: ${e.message}"
                            }
                        }
                    }

                    // Launch Coroutine for Around_stop
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val aroundStopResultJson = Around_stop.main()
                            withContext(Dispatchers.Main) {
                                aroundStopResult.value = aroundStopResultJson
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error fetching Around Stop data: ${e.message}", e)
                            withContext(Dispatchers.Main) {
                                aroundStopResult.value = "Error fetching Around Stop data: ${e.message}"
                            }
                        }
                    }
                    ScrollableContent1( aroundStopResult.value){
                        val intent = Intent(this@Around_stopActivity, NewsActivity::class.java)
                        startActivity(intent)
                    }

                }
            }
        }
    }
}
@Composable
fun ScrollableContent1( aroundStopResult: String,onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "附近站牌 :\n")
        Text(text = aroundStopResult)
        Button(
            onClick = onButtonClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "前往最新消息頁")
        }
    }
}

