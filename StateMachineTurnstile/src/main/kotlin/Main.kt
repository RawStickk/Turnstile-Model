package turnstile

import ru.nsk.kstatemachine.*
import kotlinx.coroutines.*
import ru.nsk.kstatemachine.Event

sealed class States : DefaultState() {
    object Locked : States()
    object Unlocked : States()
}

sealed class Events : Event {
    data object Coin : Events()
    data object Pass : Events()
}

fun main(): Unit = runBlocking {
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
            onEntry { println("The turnstile is UNLOCKED.") }
            transition<Events.Coin> {
                onTriggered { println("The turnstile is already UNLOCKED. Please, take your coin back.") }
            }
            transition<Events.Pass> {
                targetState = States.Locked
            }
        }
    }

    fun pass() = runBlocking {
        println("PASS")
        turnstile.processEvent(Events.Pass)
    }

    fun coin() = runBlocking {
        println("COIN")
        turnstile.processEvent(Events.Coin)
    }

    pass()
    coin()
    coin()
    pass()
}