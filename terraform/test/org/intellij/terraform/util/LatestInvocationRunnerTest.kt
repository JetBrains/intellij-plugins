package org.intellij.terraform.util

import com.intellij.testFramework.common.timeoutRunBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import org.intellij.terraform.LatestInvocationRunner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger


class LatestInvocationRunnerTest {


  @Test
  fun executedOnce() {
    timeoutRunBlocking {
      val waiter = CompletableDeferred<Unit>()
      val counter = AtomicInteger(0)
      val latestExecutor = LatestInvocationRunner {
        waiter.await()
        counter.incrementAndGet()
      }

      val jobs = (1..10).map {
        async(start = CoroutineStart.UNDISPATCHED) { latestExecutor.cancelPreviousAndRun() }
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

      val latestExecutor = LatestInvocationRunner {
        startSemaphore.release()
        waiter.await()
        endCounter.incrementAndGet()
      }

      val job1 = async(CoroutineName("Job1"), start = CoroutineStart.UNDISPATCHED) { latestExecutor.cancelPreviousAndRun() }
      startSemaphore.acquire()
      val job2 = async(CoroutineName("Job2"), start = CoroutineStart.UNDISPATCHED) { latestExecutor.cancelPreviousAndRun() }
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
      val latestExecutor = LatestInvocationRunner {
        delay(10) // increases chance of live-lock
      }

      val jobs = (1..100).map {
        async(Dispatchers.Default) {
          latestExecutor.cancelPreviousAndRun()
        }
      }

      jobs.awaitAll()
      // if the test finished - it is successful
    }

  }

}