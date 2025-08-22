# What is kotlin `suspend` function

A Kotlin suspend function is a special type of function that can be `paused` and `resumed` **without blocking the thread
** it's
running on. It's a key feature of Kotlin's coroutines system for asynchronous programming.

#How Suspension Works
When a suspend function encounters a `suspension point` (like a network call (async io) or delay), it doesn't block the
current thread. Instead, it saves its state and allows the thread to do other work. When the operation completes,
the function resumes from where it left off.

```kotlin
suspend fun performTasks() {
    println("Starting task 1")
    delay(1000) // Suspension point - thread is free to do other work
    println("Task 1 completed")

    val result = networkCall() // Another suspension point
    println("Got result: $result")
}
```

# Common Use Cases

Suspend functions are typically used for operations that would otherwise block a thread, such as network requests, file
I/O, database operations, or delays. They enable you to write asynchronous code that looks and feels like synchronous
code, making it much more readable than callback-based approaches.

The beauty of suspend functions is that they provide the performance benefits of asynchronous programming while
maintaining the simplicity and readability of sequential code.

# The Blocking Chain

`connection.inputStream` is a deeply blocking call that goes through multiple layers.

The call stack goes from your Kotlin code all the way down to the OS kernel and network hardware. At each level, the
thread can be blocked waiting for network operations to complete

**Critical Blocking Points:**

* **DNS Resolution** - If the hostname isn't cached
* **TCP Connection** - The 3-way handshake (SYN → SYN-ACK → ACK)
* **HTTP Request** - Sending headers and waiting for response
* **Response Reading** - Each byte read from the network

**Why It Matters**

When your thread hits `connection.inputStream`, it doesn't just **pause** - the operating system actually puts that
thread to **sleep** and removes it from the CPU scheduler. The thread consumes memory but does zero useful work until
network data arrives.

This is the fundamental problem that coroutines solve. Instead of blocking expensive threads during I/O waits, suspend
functions allow the thread to be freed up for other work while the I/O operation continues in the background.

Before coroutines: 1000 concurrent downloads = 1000 blocked threads
With coroutines: 1000 concurrent downloads = maybe 10-20 active threads doing actual work

When you call: `connection.inputStream`
This triggers a complex call stack that goes from Java all the way down to the OS kernel:

1. APPLICATION LAYER (Your Kotlin/Java Code)
    * └── connection.inputStream
    * └── HttpURLConnection.getInputStream()

2. JAVA HTTP LAYER

    * └── sun.net.www.protocol.http.HttpURLConnection.getInputStream()
    * ├── connect() // If not already connected
    * ├── writeRequests() // Send HTTP headers
    * └── getInputStream0() // Get the actual stream

3. JAVA NETWORKING LAYER

    * └── sun.net.www.http.HttpClient.getInputStream()
    * ├── openServer() // Open socket connection if needed
    * ├── writeRequests() // Write HTTP request headers
    * └── parseHTTP() // Parse HTTP response headers
    * └── Returns: sun.net.www.http.HttpClient$HttpInputStream

4. SOCKET LAYER

    * └── java.net.Socket.getInputStream()
    * └── java.net.SocketInputStream
    * └── socketRead0() // Native method call

5. JVM NATIVE LAYER (C/C++)

    * └── Java_java_net_SocketInputStream_socketRead0() // JNI call
    * ├── NET_Read() // Platform-specific network read
    * └── Calls OS-specific socket functions

6. OPERATING SYSTEM LAYER

    * ├── POSIX/Linux: recv() system call
    * ├── Windows: WSARecv() system call
    * └── macOS: recv() system call

7. KERNEL NETWORK STACK
    * ├── Socket buffer management
    * ├── TCP state machine
    * ├── IP packet processing
    * ├── Network interface driver
    * └── Hardware network adapter

# Non-Blocking IO

QNS:

* Does java uses blocking IO or Non-Blocking IO (Async IO)
* If Java uses blocking IO, how co-routine still helps

# Commands

After adb root (when using in android)

``` 
cat /proc/sys/kernel/threads-max
```

# Links

HttpURLOpenConnection
https://cr.openjdk.org/~arieber/6563286/webrev.01/src/share/classes/sun/net/www/protocol/http/HttpURLConnection.java.html
https://cr.openjdk.org/~robm/8009251/webrev.01/src/share/classes/sun/net/www/http/HttpClient.java.html
https://cr.openjdk.org/~malenkov/8022746.8.1/jdk/src/share/classes/sun/net/NetworkClient.java.html
https://developer.classpath.org/doc/java/net/Socket-source.html
https://gitlab.redox-os.org/hasheddan/gcc/-/blob/0ab2e446c2937480c34d2c5f90dbcac19a3eebbe/libjava/gnu/java/net/PlainSocketImpl.java
