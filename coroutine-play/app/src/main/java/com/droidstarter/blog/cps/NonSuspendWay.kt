package com.droidstarter.blog.cps

import java.util.concurrent.Executors


fun main() {

    var result = ""

    // getToken or getUser may fail, exactly communicating which one failed to caller requires extra effort.
    getToken(onResult = { token ->
        // if getToken failed, then retrying getToken is not easy, makes the code harder to read
        getUser(token, onResult = { user ->
            println("Main Output : $user")
            result = user
        })
    })

    while (result == "") {
        println("Wait for result")
        Thread.sleep(1000L)
    }

}

fun getUser(token: String, onResult: (String) -> Unit) {
    dummyNetworkCall(input = token.hashCode().toString(), onResult = onResult)
}

fun getToken(onResult: (String) -> Unit) {
    dummyNetworkCall(input = "token", onResult = onResult)
}

private fun dummyNetworkCall(input: String, onResult: (String) -> Unit) {
    Executors.newFixedThreadPool(10).execute {
        val url = java.net.URL("https://jsonplaceholder.typicode.com/todos/1")
        val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            useCaches = true
        }

        val response = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        onResult(input + response)
    }
}
