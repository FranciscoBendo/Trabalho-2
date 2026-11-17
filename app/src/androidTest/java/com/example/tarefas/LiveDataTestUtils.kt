package com.example.tarefas

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Observa o LiveData (na thread principal) até que o valor emitido satisfaça [predicate]
 * e o devolve para a thread do teste. Lança TimeoutException se isso não acontecer a tempo.
 */
fun <T> LiveData<T>.awaitValue(
    timeoutSeconds: Long = 5,
    predicate: (T) -> Boolean = { true }
): T {
    var resultado: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            if (predicate(value)) {
                resultado = value
                latch.countDown()
            }
        }
    }

    val instrumentation = InstrumentationRegistry.getInstrumentation()
    instrumentation.runOnMainSync { observeForever(observer) }
    try {
        if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
            throw TimeoutException("O LiveData não emitiu o valor esperado em $timeoutSeconds s")
        }
    } finally {
        instrumentation.runOnMainSync { removeObserver(observer) }
    }

    @Suppress("UNCHECKED_CAST")
    return resultado as T
}

/** Devolve o primeiro valor emitido (inclusive null). */
fun <T> LiveData<T>.getOrAwaitValue(): T = awaitValue()
