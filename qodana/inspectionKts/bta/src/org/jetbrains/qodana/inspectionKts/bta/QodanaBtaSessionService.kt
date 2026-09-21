package org.jetbrains.qodana.inspectionKts.bta

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import org.jetbrains.kotlin.buildToolsRuntime.KotlinBuildToolsRuntime
import org.jetbrains.kotlin.buildtools.api.BuildOperation
import org.jetbrains.kotlin.buildtools.api.ExecutionPolicy
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.buildtools.api.KotlinLogger
import org.jetbrains.kotlin.buildtools.api.KotlinToolchains

/**
 * Owns a single long-lived BTA build session shared across all `.inspection.kts` compilations. Reuse
 * retains the compiler's jar caches (closing a session runs `clearJarCaches()`), so a
 * fresh-session-per-compile would re-open the large toolchain classpath every time.
 *
 * The toolchain and daemon execution policy come from the reusable [KotlinBuildToolsRuntime] (in the
 * standalone Kotlin Build Tools plugin); this qodana-side service only adds the session reuse +
 * serialization.
 */
@OptIn(ExperimentalBuildToolsApi::class)
@Service(Service.Level.APP)
internal class QodanaBtaSessionService : Disposable {
  companion object {
    fun getInstance(): QodanaBtaSessionService = service()
  }

  private val runtime: KotlinBuildToolsRuntime get() = KotlinBuildToolsRuntime.getInstance()

  private val executionPolicy: ExecutionPolicy by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { runtime.daemonExecutionPolicy() }

  private val session: Lazy<KotlinToolchains.BuildSession> =
    lazy(LazyThreadSafetyMode.SYNCHRONIZED) { runtime.toolchains().createBuildSession() }

  private val sessionLock = Any()

  fun toolchains(): KotlinToolchains = runtime.toolchains()

  /**
   * Runs [operation] on the shared session under the daemon policy. Calls are serialized: the session
   * is not documented as safe for concurrent operations and they would all share its single
   * `projectId`. The compile still runs on the daemon; the lock only guards session reuse.
   *
   * The very first call may block while the compiler dist is downloaded (see [KotlinBuildToolsRuntime]),
   * so callers must stay off the EDT.
   */
  fun <R> execute(operation: BuildOperation<R>, logger: KotlinLogger?): R =
    synchronized(sessionLock) { session.value.executeOperation(operation, executionPolicy, logger) }

  override fun dispose() {
    synchronized(sessionLock) {
      if (session.isInitialized()) session.value.close()
    }
  }
}
