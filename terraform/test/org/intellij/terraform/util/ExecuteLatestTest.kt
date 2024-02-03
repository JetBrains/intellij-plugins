package org.intellij.terraform.util

import com.intellij.testFramework.common.timeoutRunBlocking
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import org.intellij.terraform.executeLatest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger


class ExecuteLatestTest {


  @Test
  fun executedOnce() {
    timeoutRunBlocking {
      val waiter = CompletableDeferred<Unit>()
      val counter = AtomicInteger(0)
      val testFun = executeLatest {
        waiter.await()
        counter.incrementAndGet()
      }

      val jobs = (1..10).map {
        async(start = CoroutineStart.UNDISPATCHED) { testFun() }
      }

      waiter.complete(Unit)
      val results = jobs.awaitAll()
      Assertions.assertEquals(generateSequence { 1 }.take(10).toList(), results)
    }

  }

  @Test
  fun redoFirstIfSecondCancelld() {
    timeoutRunBlocking {
      val waiter = CompletableDeferred<Unit>()
      val startSemaphore = Semaphore(1, 1)
      val endCounter = AtomicInteger(0)

      val testFun = executeLatest {
        startSemaphore.release()
        waiter.await()
        endCounter.incrementAndGet()
      }

      val job1 = async(CoroutineName("Job1"), start = CoroutineStart.UNDISPATCHED) { testFun() }
      startSemaphore.acquire()
      val job2 = async(CoroutineName("Job2"), start = CoroutineStart.UNDISPATCHED) { testFun() }
      startSemaphore.acquire()
      job2.cancel()
      waiter.complete(Unit)
      job1.await()
      Assertions.assertEquals(1, startSemaphore.availablePermits)
      Assertions.assertEquals(1, endCounter.get())
    }

  }

  @Test
  fun noInfiniteRestarts() {
    timeoutRunBlocking {
      val testFun = executeLatest {
        delay(10) // increases chance of live-lock
      }

      val jobs = (1..100).map {
        async(Dispatchers.Default) {
          testFun()
        }
      }

      jobs.awaitAll()
      // if the test finished - it is successful
    }

  }

}