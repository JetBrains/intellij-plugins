package org.jetbrains.idea.perforce.application

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class PerforceDisposable(private val cs: CoroutineScope) : Disposable {
  override fun dispose() {
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): PerforceDisposable {
      return project.service()
    }

    @JvmStatic
    fun getCoroutineScope(project: Project): CoroutineScope {
      return getInstance(project).cs
    }
  }
}