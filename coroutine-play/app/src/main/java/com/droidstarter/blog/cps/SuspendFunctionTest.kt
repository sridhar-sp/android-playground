package com.droidstarter.blog.cps

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val client = HttpClient(CIO)

suspend fun main() {
    GlobalScope.launch {
        val token = getToken()
        val user = getUser(token)
        println("Main Output : $user")
    }

    delay(2500)
    client.close()
}

suspend fun getUser(token: String): String {
    val user = dummyNetworkCall(input = token.hashCode().toString())
    return user
}

suspend fun getToken(): String {
    val token = dummyNetworkCall(input = "token")
    return token
}

private suspend fun dummyNetworkCall(input: String): String {
    val response = client.get("https://jsonplaceholder.typicode.com/todos/1")
    return input + response.bodyAsText()
}
