package org.jetbrains.qodana

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.UiInterceptors
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

inline fun <reified T : DialogWrapper> registerDialogInterceptor(): Deferred<T> {
  val intercepted = CompletableDeferred<T>()
  val interceptor = object : UiInterceptors.UiInterceptor<T>(T::class.java) {
    override fun doIntercept(component: T) {
      intercepted.complete(component)
    }
  }
  UiInterceptors.register(interceptor)
  return intercepted
}