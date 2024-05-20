package paymentTurnstile

import ru.nsk.kstatemachine.*
import kotlinx.coroutines.*

//class State(val price: Int = 0) : DefaultDataState<Int>(dataExtractor = defaultDataExtractor<Int>()) {
//    var balance = 0
//}
//sealed class States : State() {
//    object Locked : States()
//    object Unlocked : States()
//}
sealed class Events : Event {
    class Coin(val coins: Int) : Events()
    data object Pass : Events()
    data object Unlock: Events()
}

object Locked : DefaultState() {
    private val price: Int = 10
    private var balance = 0
    fun resetBalance() {
        balance = 0
    }

    fun processPayment(payment: Int) {
        balance += payment
    }

    fun change(): Int = if (balance - price > 0) balance - price else 0
    fun displayBalance() = println("Current balance: $balance coins")
    fun displayPrice() = println("Entry price: $price coins")
    fun getBalance(): Int = balance
    fun getPrice(): Int = price
}

object Unlocked : DefaultState()

class Customer(private var coins: Int = 0) {
    private var passed: Boolean = false
    fun getState() {
        println("Customer state:")
        println("Coins: $coins")
        println("Passed: $passed")
    }

    private fun pass() {
        passed = true
    }

    private fun payCoins(payment: Int) {
        coins -= payment
    }

    private fun returnCoins(change: Int) {
        coins += change
    }

    fun pass(turnstile: StateMachine) = runBlocking {
        getState()
        println("PASS")
        println("---------")
        if (Unlocked.isActive)
            pass()
        turnstile.processEvent(Events.Pass)
    }

    fun coin(turnstile: StateMachine, payment: Int) = runBlocking {
        getState()
        println("COIN")
        println("---------")
        payCoins(payment)
        turnstile.processEvent(Events.Coin(payment))
        turnstile.processEvent(Events.Unlock)
        if (Unlocked.isActive)
            returnCoins(Locked.change())
    }
}

fun main() = runBlocking {
    val customer = Customer(20)
    val turnstile = createStateMachine(this) {
        addState(Unlocked) {
            onEntry {
                println("The turnstile is UNLOCKED.")
            }
            transition<Events.Pass> {
                Locked.resetBalance()
                targetState = Locked
            }
        }
        addInitialState(Locked) {
            onEntry {
                println("The turnstile is LOCKED.")
                displayPrice()
                displayBalance()
            }
            transition<Events.Coin> {
                onTriggered {
                    Locked.processPayment(it.event.coins)
                    Locked.displayPrice()
                    Locked.displayBalance()
                }
            }
            transition<Events.Unlock> {
                guard = {Locked.getBalance() >= Locked.getPrice()}
                targetState = Unlocked
            }
            transition<Events.Pass> {
                onTriggered { println("Please, pay to pass first.") }
            }
        }
    }
    customer.pass(turnstile)
    customer.coin(turnstile, 4)
    customer.coin(turnstile, 8)
    customer.pass(turnstile)
    customer.getState()
}