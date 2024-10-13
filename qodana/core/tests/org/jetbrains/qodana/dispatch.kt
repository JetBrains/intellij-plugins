package org.jetbrains.qodana

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.runInEdtAndWait
import kotlinx.coroutines.*

/**
 * In tests, we override all dispatchers to [Dispatchers.EDT], and the tests themselves are run on EDT.
 * So, in order to test any coroutine which schedules to [Dispatchers.EDT],
 * we need to dispatch all tasks on EDT
 *
 * You can't just do `runBlocking` on UI thread since any `withContext(Dispatchers.EDT)` inside it will block it
 */
fun runDispatchingOnUi(action: suspend CoroutineScope.() -> Unit): Unit = runBlocking {
  val result = async(Dispatchers.EDT) {
    action()
  }
  runInEdtAndWait {
    dispatchAllTasksOnUi()
  }
  result.await()
}

/**
 * In tests, we override all dispatchers to [Dispatchers.EDT], so in order to dispatch tasks on EDT, you need to call this function
 */
fun dispatchAllTasksOnUi() = PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()