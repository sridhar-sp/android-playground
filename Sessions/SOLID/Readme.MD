# SOLID Design Principles

## Definition

SOLID stands for:

- **Single Responsibility Principle (SRP)**
- **Open-Closed Principle (OCP)**
- **Liskov Substitution Principle (LSP)**
- **Interface Segregation Principle (ISP)**
- **Dependency Inversion Principle (DIP)**

---

## **Single Responsibility Principle (SRP)**

### **Misconception:**

People often misinterpret SRP as _"A function should do one, and only one, thing."_

But SRP is much more than that—it applies at a system level.

### **Definition:**

A module should have one, and only one, reason to change.

### **What does this mean?**

We write software for users—let’s call them **actors**. A module should change only if its corresponding **actor**
requires a change.

If we deliver a bug-free software and developers have no reason to modify it, the only reason for change should come
from an **actor** who requests a feature or modification.

Rephrasing the definition:
> **"A module should be responsible to one, and only one, actor."**

A project can have multiple actors. For example, in an ERP system:

- HR Department (Actor)
- Board of Directors (Actor)

If the Board of Directors requests a change, we must ensure that it does not unintentionally impact the HR Department.

### **Example:**

#### ❌ **Bad Example: One class handling multiple responsibilities**

```kotlin
class EmployeeBad {
    fun calculatePay() { /* ... */
    }
    fun saveToDatabase() { /* ... */
    }
    fun generateReport() { /* ... */
    }
}
```

The above class handles multiple responsibilities, catering to multiple actors in a single module. This tight coupling
means that changes for one actor can inadvertently affect others.

For instance, if `getRegularHours()` is used both in `calculatePay()` and `generateReport()`, a change requested by the
Board of Directors (for reporting purposes) could unintentionally break payroll calculations.

```kotlin
class EmployeeBad {
    fun calculatePay() {
        val regularWorkingHours = getRegularHours()
    }

    fun generateReport() {
        val allWorkingHours = getRegularHours()
    }

    private fun getRegularHours() { /* ... */
    }
}
```

#### ✅ **Good Example: Separate responsibilities**

```kotlin
class Employee { /* Pure data class */ }

class EmployeePayCalculator {
    fun calculatePay(employee: Employee) { /* ... */
    }
}

class EmployeeRepository {
    fun saveToDatabase(employee: Employee) { /* ... */
    }
}

class ReportGenerator {
    fun generateReport(employee: Employee) { /* ... */
    }
}
```

Here, we have separated concerns by defining dedicated classes that handle different actor responsibilities. Each module
is now independent, preventing unintended changes from affecting unrelated functionalities.

---

## **Open-Closed Principle (OCP)**

> **Software artifacts should be open for extension but closed for modification.**

This does **not** mean that once deployed, a module should never change. Instead, it means that adding new functionality
should not require modifying existing code.

### **When is OCP violated?**

If a small requirement or feature request forces extensive modifications to the system, the architecture has likely not
followed OCP.

### **Example:**

#### **Contact Management System**

```kotlin
interface ContactDatabase {
    fun fetchContacts(): List<Cursor>
}
```

```kotlin
interface ContactRepository {
    fun fetchContacts(): List<Contacts>
}
```

```kotlin
class ContactViewModel(
    private val contactRepository: ContactRepository
) {
    var contacts: List<Contacts> = contactRepository.fetchContacts()
        private set
}
```

We display a **flat list of contacts**:

```kotlin
fun ListContactViewScreen(viewModel: ContactViewModel) {
    val contactList = viewModel.contacts
    /* UI logic for [contactList] */
}
```

Now, we want to group contacts by **organization**:

```kotlin
fun GroupContactViewScreen(viewModel: ContactViewModel) {
    val contactListByOrganization: Map<String, List<Contacts>> = viewModel.contacts.groupBy { it.organization }
    /* UI logic for [contactListByOrganization] */
}
```

### **Problem**

We now have **business logic inside the UI**, or we might move it into `ViewModel`. This requires modifying existing
code to accommodate the change.

Instead, let’s **refactor** using the Open-Closed Principle.

### **Solution: Introducing `ContactPresenter`**

A `ContactPresenter` determines how data should be transformed for the UI:

```kotlin
interface ContactPresenter<T> {
    fun asViewState(contacts: List<Contacts>): T
}
```

Now, we update the `ContactViewModel` to use it:

```kotlin
class ContactViewModel<T>(
    private val contactRepository: ContactRepository,
    private val presenter: ContactPresenter<T>
) {
    var contacts: T = presenter.asViewState(contactRepository.fetchContacts())
        private set
}
```

#### **Presenters for different views**

**✅ List Presenter**

```kotlin
class ListContactPresenter : ContactPresenter<List<String>> {
    override fun asViewState(contacts: List<Contacts>) = contacts.map { it.name }
}
```

**✅ Grouped Presenter**

```kotlin
class GroupContactPresenter : ContactPresenter<Map<String, List<Contacts>>> {
    override fun asViewState(contacts: List<Contacts>): Map<String, List<Contacts>> {
        return contacts.groupBy { it.organization }
    }
}
```

Now, different views can use different presenters **without modifying the existing `ViewModel`**:

```kotlin
fun ListContactViewScreen(viewModel: ContactViewModel<List<String>>) {
    val contactList = viewModel.contacts
    /* UI logic for [contactList] */
}
```

```kotlin
fun GroupContactViewScreen(viewModel: ContactViewModel<Map<String, List<Contacts>>>) {
    val contactListByOrganization: Map<String, List<Contacts>> = viewModel.contacts
    /* UI logic for [contactListByOrganization] */
}
```

This approach follows OCP because **adding a new presenter (e.g., sorting by last contacted) does not modify existing
classes**.

---

## **Liskov Substitution Principle (LSP)**

> **Derived classes must be substitutable for their base classes without altering the correctness of the program.**

### **Example:**

```kotlin
open class Bird {
    open fun fly() {}
}

class Sparrow : Bird() {
    override fun fly() {
        println("Sparrow is flying")
    }
}

// Penguins are a group of aquatic flightless birds
class Penguin : Bird() {
    /* Penguins can't fly */
    override fun fly() {
        throw UnReasonableAskException("Boss! I can't fly.")
    }
}
```

Here, `Penguin` violates LSP since it cannot fly, yet it inherits from `Bird`. A better approach is:

```kotlin
interface Flyable {
    fun fly()
}

open class Bird

class Sparrow : Bird(), Flyable {
    override fun fly() {
        println("Sparrow is flying")
    }
}

class Penguin : Bird()
```

Now, we enforce correct behavior without breaking substitutability.

### **Another Example:**

```kotlin
interface LogExporter {
    suspend fun export(logs: List<String>)
}
```

```kotlin
class NetworkLogExporter : LogExporter {
    override suspend fun export(logs: List<String>) {
        // Send the report to server using HTTP
    }
}
```

```kotlin
class DiskLogExporter : LogExporter {
    override suspend fun export(logs: List<String>) {
        // Save the report in disk
    }
}
```

```kotlin
// Takes LogExporter base interface.
fun processLogs(exporter: LogExporter, logs: List<String>) {
    runBlocking { exporter.export(logs) }
}

val networkExporter: LogExporter = NetworkLogExporter()
val diskExporter: LogExporter = DiskLogExporter()

val logs = listOf("Error 1", "Warning 2")

// LogExporter can be substituted with NetworkLogExporter
processLogs(networkExporter, logs)

// LogExporter can be substituted with DiskLogExporter
processLogs(diskExporter, logs)

```

---

# **Interface Segregation Principle (ISP)**

> **Clients should not be forced to depend on interfaces they do not use.**

The **Interface Segregation Principle (ISP)** suggests that instead of creating large, monolithic interfaces, we should
create smaller, more specific ones. This prevents classes from depending on methods they do not need.

### **Why is ISP important?**

- Reduces unnecessary dependencies.
- Makes the system more maintainable and scalable.
- Improves code readability by ensuring that interfaces are focused on specific behaviors.

### **Example: Violating ISP**

Consider an interface that represents a multifunctional printer:

```kotlin
interface Printer {
    fun printDocument()
    fun scanDocument()
    fun faxDocument()
}
```

Now, assume we have a **BasicPrinter** that only supports printing:

```kotlin
class BasicPrinter : Printer {
    override fun printDocument() {
        println("Printing document...")
    }

    override fun scanDocument() {
        throw UnsupportedOperationException("Scan not supported")
    }

    override fun faxDocument() {
        throw UnsupportedOperationException("Fax not supported")
    }
}
```

Here, `BasicPrinter` is **forced** to implement `scanDocument()` and `faxDocument()`, even though it does not support
those
functionalities. This violates ISP because clients relying on `BasicPrinter` may accidentally call unsupported methods.

### **Solution: Apply ISP by Splitting Interfaces**

We should separate concerns by creating multiple smaller interfaces:

```kotlin
interface Printable {
    fun printDocument()
}

interface Scannable {
    fun scanDocument()
}

interface Faxable {
    fun faxDocument()
}
```

Now, different classes can implement only the interfaces they need:

```kotlin
class BasicPrinter : Printable {
    override fun printDocument() {
        println("Printing document...")
    }
}

class AdvancedPrinter : Printable, Scannable, Faxable {
    override fun printDocument() {
        println("Printing document...")
    }

    override fun scanDocument() {
        println("Scanning document...")
    }

    override fun faxDocument() {
        println("Faxing document...")
    }
}
```

### **Another Example:**

`TokenReader` contains only read operations (`getAccessToken()`, `getRefreshToken()`), making it ideal for components
that only need to read tokens.

```kotlin
interface TokenReader {
    fun getAccessToken(): String?

    fun getRefreshToken(): String?

    companion object {
        internal const val KEY_ACCESS_TOKEN = "access_token"
        internal const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
```

`TokenRepository` extends `TokenReader` and adds write operations (`saveAccessToken()`, `saveRefreshToken()`,
`clearAllTokens()`), ensuring that clients needing full token management can use it.

```kotlin
interface TokenRepository : TokenReader {
    fun saveAccessToken(token: String?)
    fun saveRefreshToken(token: String?)

    fun clearAllTokens() {
        saveAccessToken(null)
        saveRefreshToken(null)
    }
}
```

### 🚀 Example Usage

AuthValidator, a Read-Only Client (Uses `TokenReader`), which has no usage of mutating the `accessToken` or
`refreshToken`

```kotlin
class AuthValidator(private val tokenReader: TokenReader) {
    fun isUserLoggedIn(): Boolean {
        return tokenReader.getAccessToken() != null
    }
}
```

A Full Token Management Class (Uses `TokenRepository`)

```kotlin

class SecureTokenStorage : TokenRepository {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override fun getAccessToken(): String? = accessToken
    override fun getRefreshToken(): String? = refreshToken

    override fun saveAccessToken(token: String?) {
        accessToken = token
    }

    override fun saveRefreshToken(token: String?) {
        refreshToken = token
    }
}

```

### **Benefits of ISP Implementation**

- **Flexibility**: Clients depend only on the methods they use.
- **Scalability**: Adding new functionalities without modifying existing interfaces.
- **Better Code Organization**: Smaller, focused interfaces improve readability and maintainability.

By following ISP, we design interfaces that are more adaptable, reducing unnecessary dependencies and improving overall
system architecture.

# **Dependency Inversion Principle (DIP)**

> The dependency inversion principle (DIP) is a software design principle that states that high-level modules should
> depend on **abstractions**, not low-level modules

## **Violating DIP: Tight Coupling**

In the following example, `OrderService` directly depends on `MySQLDatabase`, making it tightly coupled:

```kotlin
class MySQLDatabase {
    fun save(order: String) {
        println("Order saved in MySQL database: $order")
    }
}

class OrderService {
    private val database = MySQLDatabase() // ❌ Direct dependency

    fun placeOrder(order: String) {
        database.save(order)
    }
}
```

### **Problems with this approach:**

- If we need to switch to a different database (e.g., `MongoDBDatabase`), we must modify `OrderService`.
- It violates **OCP (Open-Closed Principle)** since modifying the database type requires changes in existing code.

---

## **Applying DIP: Using Abstractions**

We solve this by introducing an **abstraction (`Database`)** that both `MySQLDatabase` and `MongoDBDatabase` will
implement.

```kotlin
interface Database {
    fun save(order: String)
}
```

### **Concrete Implementations:**

```kotlin
class MySQLDatabase : Database {
    override fun save(order: String) {
        println("Order saved in MySQL database: $order")
    }
}

class MongoDBDatabase : Database {
    override fun save(order: String) {
        println("Order saved in MongoDB database: $order")
    }
}
```

Now, `OrderService` depends on the **abstraction (`Database`)**, not a specific implementation.

```kotlin
class OrderService(private val database: Database) {
    fun placeOrder(order: String) {
        database.save(order)
    }
}
```

### **Usage:**

```kotlin
fun main() {
    val mySQLService = OrderService(MySQLDatabase())
    mySQLService.placeOrder("Laptop")

    val mongoDBService = OrderService(MongoDBDatabase())
    mongoDBService.placeOrder("Smartphone")
}
```

### **Advantages of this approach:**

✅ **Loosely Coupled** - `OrderService` doesn’t care about the concrete database type.  
✅ **Easily Extendable** - We can add more database implementations without modifying `OrderService`.  
✅ **Testable** - We can easily mock `Database` for unit tests.

---

## **DIP in Android Development (Example: Repository Pattern)**

In Android, we often use **DIP** in Repository and ViewModel layers.

### **Without DIP: ViewModel directly depends on a specific Repository**

```kotlin
class UserRepository {
    fun getUser(): String = "John Doe"
}

class UserViewModel {
    private val repository = UserRepository() // ❌ Direct dependency

    fun fetchUser(): String {
        return repository.getUser()
    }
}
```

If we want to switch to `RemoteUserRepository`, we have to modify `UserViewModel`.

### **With DIP: Injecting an Abstraction**

```kotlin
interface UserRepository {
    fun getUser(): String
}

class LocalUserRepository : UserRepository {
    override fun getUser(): String = "John Doe from Local DB"
}

class RemoteUserRepository : UserRepository {
    override fun getUser(): String = "John Doe from API"
}

class UserViewModel(private val repository: UserRepository) {
    fun fetchUser(): String {
        return repository.getUser()
    }
}
```

### **Usage with Dependency Injection (DI):**

```kotlin
fun main() {
    val localViewModel = UserViewModel(LocalUserRepository())
    println(localViewModel.fetchUser())

    val remoteViewModel = UserViewModel(RemoteUserRepository())
    println(remoteViewModel.fetchUser())
}
```

---

## **Key Takeaways**

✅ **High-level modules (`OrderService`, `UserViewModel`) depend on abstractions (`Database`, `UserRepository`), not
concrete implementations.**  
✅ **Loosely coupled architecture allows easy modifications and unit testing.**  
✅ **DIP aligns well with Dependency Injection (DI) frameworks like Dagger/Hilt in Android.**

---

## **References**

- _Clean Architecture_ by Robert C. Martin (Uncle Bob)