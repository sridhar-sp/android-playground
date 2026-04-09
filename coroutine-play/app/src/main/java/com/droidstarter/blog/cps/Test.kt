package com.droidstarter.blog.cps

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

private val client = HttpClient(CIO)

private suspend fun dummyNetworkCall(input: String): String {
    val response = client.get("https://jsonplaceholder.typicode.com/todos/1")
    return input + response.bodyAsText()
}

suspend fun main() {
    val response = dummyNetworkCall("hello")
    println("dummyNetworkCall Response $response")
}