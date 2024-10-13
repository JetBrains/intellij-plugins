package org.jetbrains.qodana.coroutines

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

/** General qodana plugin application and project scopes, use only if there are no more appropriate scope */
@Suppress("unused")
internal val qodanaApplicationScope: CoroutineScope get() = service<QodanaApplicationScopeService>().scope

@Suppress("unused")
internal val Project.qodanaProjectScope: CoroutineScope get() = this.service<QodanaProjectScopeService>().scope

internal val Project.qodanaProjectDisposable: Disposable get() = this.service<QodanaProjectScopeService>()

@Service(Service.Level.PROJECT)
private class QodanaProjectScopeService(
  @Suppress("unused") val project: Project,
  override val scope: CoroutineScope
) : EmptyScopeService()

@Service(Service.Level.APP)
private class QodanaApplicationScopeService(override val scope: CoroutineScope) : EmptyScopeService()

private abstract class EmptyScopeService: Disposable {
  abstract val scope: CoroutineScope

  override fun dispose() = Unit
}