package org.jetbrains.qodana.run

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.annotations.TestOnly

interface QodanaRunInIdeService {
  companion object {
    fun getInstance(project: Project): QodanaRunInIdeService = project.service()

    @TestOnly
    fun setTestInstance(project: Project, instance: QodanaRunInIdeService, disposable: Disposable)  {
      (getInstance(project) as QodanaRunInIdeServiceTestImpl).setInstance(instance, disposable)
    }
  }

  val runState: StateFlow<QodanaRunState>


  val runsResults: StateFlow<Set<QodanaInIdeOutput>>
}