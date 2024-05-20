package coinTurnstile

import ru.nsk.kstatemachine.*
import kotlinx.coroutines.*
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.Event

sealed class States : DefaultState() {
    object Locked : States()
    object Unlocked : States()
}
sealed class Events : Event {
    data object Coin : Events()
    data object Pass : Events()
}
//class Pass(override val data: Boolean) : DataEvent<Boolean>

class Customer(private var coins: Int = 0, private var passed: Boolean = false) {
    fun getState() {
        println("Customer state:")
        println("Coins: $coins")
        println("Passed: $passed")
    }
    private fun pass() {
        passed = true
    }
    private fun insertCoin() {
        coins--
    }
    private fun returnCoin() {
        coins++
    }
    fun pass(turnstile: StateMachine) = runBlocking {
        getState()
        println("PASS")
        println("---------")
        if (States.Unlocked.isActive) pass()
        turnstile.processEvent(Events.Pass)
    }

    fun coin(turnstile: StateMachine) = runBlocking {
        getState()
        println("COIN")
        insertCoin()
        println("---------")
        if (States.Unlocked.isActive) returnCoin()
        turnstile.processEvent(Events.Coin)
    }
}

fun main() = runBlocking {
    val customer = Customer(2)
    val turnstile = createStateMachine(this) {
        addInitialState(States.Locked) {
            onEntry { println("The turnstile is LOCKED. Please, enter a coin to pass.") }
            transition<Events.Coin> {
                targetState = States.Unlocked
            }
            transition<Events.Pass> {
                onTriggered { println("Please, enter the coin to pass first.") }
            }
        }
        addState(States.Unlocked) {
            onEntry {
                println("The turnstile is UNLOCKED.")
            }
            transition<Events.Coin> {
                onTriggered {
                    println("The turnstile is already UNLOCKED. Please, take your coin back.")
                    //customer.returnCoin()
                }
            }
            transition<Events.Pass> {
                //onTriggered { customer.pass() }
                targetState = States.Locked
            }
        }
    }
    customer.pass(turnstile)
    customer.coin(turnstile)
    customer.coin(turnstile)
    customer.pass(turnstile)
    customer.getState()
}