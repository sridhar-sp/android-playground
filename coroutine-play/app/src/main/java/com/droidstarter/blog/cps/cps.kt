package com.droidstarter.blog.cps
private val COROUTINE_SUSPENDED = Any()

// ─────────────────────────────────────────
// original code
// ─────────────────────────────────────────
// suspend fun getToken(): String {
//     val token = dummyNetworkCall("token")
//     return token
// }
//
// suspend fun getUser(token: String): String {
//     val user = dummyNetworkCall(token)
//     return user
// }
//
// suspend fun main() {
//     val token = getToken()
//     val user  = getUser(token)
//     println("User: $user")
// }
// ─────────────────────────────────────────
// compiler generated state machine (simplified)
// ─────────────────────────────────────────

fun main() {
    mainStateMachine(label = 0, result = null)
}

fun mainStateMachine(label: Int, result: Any?) {
    var currentLabel = label
    var token: String? = null

    when (currentLabel) {
        0 -> {
            // suspend point 1: waiting for getToken
            getTokenStateMachine(0, null) { tokenResult ->
                mainStateMachine(1, tokenResult)   // resume main at label 1
            }
        }
        1 -> {
            // getToken done, now call getUser
            token = result as String
            getUserStateMachine(0, token) { userResult ->
                mainStateMachine(2, userResult)    // resume main at label 2
            }
        }
        2 -> {
            // getUser done
            println("User: $result")
        }
    }
}

fun getTokenStateMachine(label: Int, result: Any?, resume: (String) -> Unit) {
    when (label) {
        0 -> {
            // suspend point: waiting for dummyNetworkCall
            dummyNetworkCallStateMachine(0, "token") { networkResult ->
                getTokenStateMachine(1, networkResult, resume)  // resume at label 1
            }
        }
        1 -> {
            // dummyNetworkCall done, return token to caller
            resume(result as String)
        }
    }
}

fun getUserStateMachine(label: Int, token: String?, resume: (String) -> Unit) {
    when (label) {
        0 -> {
            // suspend point: waiting for dummyNetworkCall
            dummyNetworkCallStateMachine(0, token ?: "") { networkResult ->
                getUserStateMachine(1, networkResult, resume)   // resume at label 1
            }
        }
        1 -> {
            // dummyNetworkCall done, return user to caller
            resume(token as String)
        }
    }
}

fun dummyNetworkCallStateMachine(label: Int, input: String, resume: (String) -> Unit) {
    when (label) {
        0 -> {
            // actual work — in real coroutines thread suspends here
            val response = "{ response for $input }"
            resume(response)                                     // resume caller
        }
    }
}