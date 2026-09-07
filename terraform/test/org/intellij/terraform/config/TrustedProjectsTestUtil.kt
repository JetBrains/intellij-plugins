package org.intellij.terraform.config

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.util.setSystemProperty
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.util.ThrowableRunnable
import org.jetbrains.annotations.TestOnly

/**
 * this class was added because of lack of the TrustedProjectsTestUtil class in the 262 branch
 */
internal object TfTrustedProjectsTestUtil {

  @TestOnly
  @JvmStatic
  fun withTrustedProjectsCheckEnabled(action: ThrowableRunnable<out Throwable>) {
    @Suppress("UNCHECKED_CAST")
    PlatformTestUtil.withSystemProperty("idea.trust.headless.disabled", "false", action as ThrowableRunnable<Throwable>)
  }

  @TestOnly
  @JvmStatic
  fun enableTrustedProjectsCheck(parentDisposable: Disposable) {
    setSystemProperty("idea.trust.headless.disabled", "false", parentDisposable)
  }

}