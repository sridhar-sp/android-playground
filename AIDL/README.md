# Android IPC (using AIDL)

### Quick guide to AIDL.

## What's AIDL

AIDL (Android Interface Definition Language) is used to create a common interface for client-server communication in
Android. Both client and server can agree on the common interface to communicate with each other using IPC.

AIDL supports all Java primitive data types and a handful of wrapper data types, such as String, List, Map, Parcelable.

## Android IPC

* Note: Android apps can communicate using several methods; this guide focuses on IPC via AIDL.

## What we are going to build

To demonstrate AIDL in action, we'll build a practical example: a sensor data logging system consisting of two
applications:

### Server App: A service that reads various sensors and provides APIs to:

* Fetch real-time sensor values
* Register callbacks for sensor value changes
* Manage logging sessions

### Client App:

* Connects to the server app and consumes the sensor data
* The client app will connect to the server app by binding with the service; once binding is complete, we get a binder
  object, which is the implementation of the AIDL interface we both agreed on. using this binder, we can call the API
  exposed from Server app

### Service Connector

* We will also discuss a small utility class that I created to take care of the boiler plate code when binding with a
  service.
* ServiceConnector handles binding to services and manages retries when the server crashes or stops. It provides a
  `getService` method that `suspends` until a connection is established or a timeout occurs.
* This will come in handy when the server app crashes/stops due to some reason. The next time we call the getService
  method, it takes care of binding with the service and returns the binder interface.

## Let's see some code
### Step 1

* Create an AIDL interface
  * The files should have .aidl extension and should be placed inside a `src/main/aidl` folder
  * Example
    ```aidl
    package com.gandiva.aidl.remoteservices;
    import com.gandiva.aidl.remoteservices.SensorDataCallback;
    
    interface SensorDataLoggerService {
        String getSpeedInKm();
        int getRPM();
        void startLogging(in SensorDataCallback callback);
        void stopLogging(in SensorDataCallback callback);
    }
    ```
    ```aidl
    package com.gandiva.aidl.remoteservices;
    import com.gandiva.aidl.remoteservices.model.SensorData;
    
    interface SensorDataCallback {
        // Create a SensorData class, which should implement android.os.Parcelable interface, and placed in java/kotlin package.
        void onEvent(in SensorData data);
    }
    ```
* Create a SensorData class, which should implement the `android.os.Parcelable` interface and place it in the
  Java/Kotlin
  package.
  ```kotlin
  package com.gandiva.aidl.remoteservices.model
    
  import android.os.Parcelable
  import kotlinx.parcelize.Parcelize
    
  @Parcelize
  data class SensorData(val sensorID: Int, val value: Int) : Parcelable
  ```
* We need to share this interface with both applications, so it would be advisable to create a library module and
  share the interface with the two apps. (Note as general software practise we don't share interfaces which client app
  has no usage, so we can create finer level libraries to expose particular aidl for particular client,
  see [Interface segregation principle](https://en.wikipedia.org/wiki/Interface_segregation_principle))
* Extras
  * `in`, `out`, and `inout` are direction specifiers used in AIDL (Android Interface Definition Language) to
    indicate the direction of data transfer for method parameters.

  * `in`: The parameter is an input parameter and data is transferred from the client to the server. This means
    that the value of the parameter is passed from the client to the server, but any modifications to the parameter's
    value on the server are not passed back to the client.

  * `out`: The parameter is an output parameter and data is transferred from the server to the client. This means
    that the value of the parameter is initially undefined on the client, but the server sets a value, which is then
    passed back to the client.

  * `inout`: The parameter is both an input and output parameter, and data is transferred in both directions. This
    means that the value of the parameter is initially passed from the client to the server, and the server may
    modify the value and pass it back to the client.

### Step 2

* Create an Android Service to expose the Binder interface implementation from onBind method

```kotlin
class SensorDataLoggerServiceImpl : Service() {

  companion object {
    const val SENSOR_DATA_LOGGER_BIND_ACTION = "com.gandiva.aidl.server.action.BIND_SENSOR_DATA_LOGGER"
  }

  override fun onBind(intent: Intent): IBinder? {
    if (intent.action != SENSOR_DATA_LOGGER_BIND_ACTION) return null

    val binder = object : SensorDataLoggerService.Stub() {
      override fun getSpeedInKm(): String {
        TODO("Not yet implemented")
      }

      override fun getRPM(): Int {
        TODO("Not yet implemented")
      }

      override fun startLogging(callback: SensorDataCallback?) {
        TODO("Not yet implemented")
      }

      override fun stopLogging(callback: SensorDataCallback?) {
        TODO("Not yet implemented")
      }
    }
    binder.linkToDeath(DeathRecipient { Log.d("SensorDataLoggerService", "**** Service died") }, 0)
    return binder
  }
}
```

* For brevity, the methods just have TODO; actual implementation can be
  found [SensorDataLoggerServiceAidl.kt](server%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fgandiva%2Faidl%2Fserver%2Fsensor%2FSensorDataLoggerServiceAidl.kt)

### Step 3

* The client can try to bind the exposed service from the server app using explicit intent.
* Once the service is connected, we can typecast the IBinder instance to the AIDL interface type, which both apps agreed
  on step 1.

```kotlin
@HiltViewModel
class SensorDataLoggerViewModel @Inject constructor(val appContext: Application) : AndroidViewModel(appContext) {

  companion object {
    const val SENSOR_DATA_LOGGER_PKG_NAME = "com.gandiva.aidl.server"
    const val SENSOR_DATA_LOGGER_SERVICE_NAME = "com.gandiva.aidl.server.sensor.SensorDataLoggerServiceImpl"
    const val SENSOR_DATA_LOGGER_BIND_ACTION = "com.gandiva.aidl.server.action.BIND_SENSOR_DATA_LOGGER"
  }

  private var sensorDataLoggerService: SensorDataLoggerService? = null

  var isServiceConnected by mutableStateOf(false)
    private set

  private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      isServiceConnected = true
      sensorDataLoggerService = SensorDataLoggerService.Stub.asInterface(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
      isServiceConnected = false
    }
  }

  fun disconnectService() {
    appContext.applicationContext.unbindService(serviceConnection)
    isServiceConnected = false
  }

  // Should call this method before accessing [sensorDataLoggerService]
  fun connectToService(appContext: Context = this.getApplication()) {
    val bindIntent = Intent().apply {
      component = ComponentName(SENSOR_DATA_LOGGER_PKG_NAME, SENSOR_DATA_LOGGER_SERVICE_NAME)
      action = SENSOR_DATA_LOGGER_BIND_ACTION
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      appContext.bindService(bindIntent, Context.BIND_AUTO_CREATE, appContext.mainExecutor, serviceConnection)
    } else {
      appContext.applicationContext.bindService(bindIntent, serviceConnection, Context.BIND_NOT_FOREGROUND)
    }
  }

  fun showSpeed() {
    val speedInKm = sensorDataLoggerService?.speedInKm // Assume service is connected
    Toast.makeText(appContext, "Speed $speedInKm", Toast.LENGTH_SHORT).show()
  }

  fun showRpm() {
    val rpm = sensorDataLoggerService?.rpm // Assume service is connected
    Toast.makeText(appContext, "RPM $rpm", Toast.LENGTH_SHORT).show()
  }
}
```

* In the above code, we have to explicitly call the `connectToService` method to initialize the connection, and post
  connection only, we should call any method from the (AIDL) binder instance.
* So our code works under the assumption that when a `showSpeed` method is called, it tries to call the AIDL API
  irrespective of the connection. This works in the best-case scenario, but the real world will be far from the
  best-case scenario. The server app can crash or stop the post-initial connection, leaving the last obtained AIDL
  binder instance as obsolete.
* So, we should have a way to call the API only when the service is connected; we can modify the showSpeed method like
  below to call the API when the service is connected, or call `connectToService` and wait for the service connection;
  post we are eligible to call any API.

```kotlin
 fun showSpeed() {
  if (isServiceConnected) {
    val speedInKm = sensorDataLoggerService?.speedInKm
    Toast.makeText(appContext, "Speed $speedInKm", Toast.LENGTH_SHORT).show()
  } else {
    connectToService() // Async operation
    // Wait for service to connect then call API
    val speedInKm = sensorDataLoggerService?.speedInKm
    Toast.makeText(appContext, "Speed $speedInKm", Toast.LENGTH_SHORT).show()
  }
}
```

* But we have to follow the same approach everywhere before calling the AIDL API to handle the worst-case scenario. But
  if we do that, it will introduce a lot of boilerplate code.
* The best way to deal with this is to create a utility class that takes care of this complexity. Following is one
  example (i.e., ServiceConnector)

## Service connector

* ServiceConnector exposes a getService suspend function, which will suspend until a connection is made or
  timeOutInMillis expires.
* This handles the service connection and retry logic internally, so clients don't have to worry about the service
  connection or retry in case the server died or crashed.

```kotlin
interface IServiceConnector<T> {
  suspend fun getService(timeOutInMillis: Long = -1): T?

  suspend fun unbindService()

  fun onServiceConnected() {}
}

open class ServiceConnector<T>(
  private val context: Context,
  private val intent: Intent,
  val transformBinderToService: (service: IBinder?) -> T?,
  private val allowNullBinding: Boolean = false
) : IServiceConnector<T> {

  private var serviceConnected = false

  private var service: T? = null

  private val mutex = Mutex()

  private var lastServiceConnection: ServiceConnection? = null

  private val logTag = "Service :: ${this.javaClass}"

  override suspend fun getService(timeOutInMillis: Long): T? {
    // If allowNullBinding is true don't care what service object is
    if (serviceConnected && (allowNullBinding || service != null)) {
      return service
    }

    if (timeOutInMillis < 0)
      return mutex.withLock { bindAndGetService() }
    return mutex.withLock { withTimeoutOrNull(timeOutInMillis) { bindAndGetService() } }
  }

  private suspend fun bindAndGetService() = suspendCancellableCoroutine { continuation ->
    val serviceConnection = object : ServiceConnection {
      override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        resumeWithServiceInstance(binder)
      }

      override fun onServiceDisconnected(name: ComponentName?) {
        cleanUpAndResumeIfRequired()
      }

      override fun onBindingDied(name: ComponentName?) {
        cleanUpAndResumeIfRequired()
      }

      override fun onNullBinding(name: ComponentName?) {
        if (allowNullBinding) resumeWithServiceInstance(null)
        else cleanUpAndResumeIfRequired()
      }

      private fun resumeWithServiceInstance(binder: IBinder?) {
        service = transformBinderToService(binder)
        serviceConnected = true
        if (continuation.isActive) continuation.resume(service)
        onServiceConnected()
      }

      private fun cleanUpAndResumeIfRequired() {
        service = null
        serviceConnected = false
        if (continuation.isActive) continuation.resume(null)
      }

    }

    context.bindService(
      intent, serviceConnection, Context.BIND_AUTO_CREATE
    )

    lastServiceConnection = serviceConnection
  }

  @Throws(Exception::class)
  override suspend fun unbindService() {
    lastServiceConnection?.let(context::unbindService)
    serviceConnected = false
    service = null
  }
}
```

* Extend the ServiceConnector and provide necessary information about the service to which we want to connect and the
  type of the binder interface.
* `@param context` Context used to bind the service.
* `@param intent` Explicit intent describing the service to connect.
* `@param transformBinderToService` callback function called to transform the generic IBinder instance to the
  client-specific AIDL interface.
* `@param allowNullBinding` Pass true to indicate to keep the server connected even if the server returns a null IBinder
  instance from the onBind method.

```kotlin
class SensorDataLoggerServiceCoordinator(context: Context) : ServiceConnector<SensorDataLoggerService>(
  context = context,
  intent = bindIntent(),
  transformBinderToService = { binder: IBinder? -> binder?.let { SensorDataLoggerService.Stub.asInterface(it) } },
  allowNullBinding = false
) {

  companion object {
    private const val SENSOR_DATA_LOGGER_PKG_NAME = "com.gandiva.aidl.server"
    private const val SENSOR_DATA_LOGGER_SERVICE_NAME = "com.gandiva.aidl.server.sensor.SensorDataLoggerServiceImpl"
    private const val SENSOR_DATA_LOGGER_BIND_ACTION = "com.gandiva.aidl.server.action.BIND_SENSOR_DATA_LOGGER"

    fun bindIntent(): Intent {
      return Intent().apply {
        component = ComponentName(SENSOR_DATA_LOGGER_PKG_NAME, SENSOR_DATA_LOGGER_SERVICE_NAME)
        action = SENSOR_DATA_LOGGER_BIND_ACTION
      }
    }
  }
}
```

* To use create an instance of SensorDataLoggerServiceCoordinator and use the getService method to obtain the binder
  instance

```kotlin
@HiltViewModel
class SensorDataLoggerViewModelV2 @Inject constructor(val appContext: Application) : AndroidViewModel(appContext) {

  private val sensorDataLoggerServiceCoordinator: SensorDataLoggerServiceCoordinator by lazy {
    SensorDataLoggerServiceCoordinator(context = appContext)
  }

  fun showSpeed() {
    viewModelScope.launch {
      // Suspend till service gets connected.
      val speedInKm = sensorDataLoggerServiceCoordinator.getService()?.speedInKm
      Toast.makeText(appContext, "Speed $speedInKm", Toast.LENGTH_SHORT).show()
    }
  }

  fun showRPM() {
    viewModelScope.launch {
      // Suspend till service gets connected. or at max 1500 ms. which ever comes first.
      val rpm = sensorDataLoggerServiceCoordinator.getService(1500L)?.rpm
      Toast.makeText(appContext, "RMP $rpm", Toast.LENGTH_SHORT).show()
    }
  }

  fun disconnectService() {
    viewModelScope.launch { sensorDataLoggerServiceCoordinator.unbindService() }
  }
}
```