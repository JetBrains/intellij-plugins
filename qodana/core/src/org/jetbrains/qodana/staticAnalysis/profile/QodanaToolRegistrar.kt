package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalInspectionToolWrapper
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsProvider


abstract class QodanaToolRegistrar(
  val platformInspectionToolRegistrar: InspectionToolsSupplier
) : InspectionToolsSupplier() {
  companion object {
    fun getInstance(project: Project? = null): QodanaToolRegistrar {
      return project?.service<QodanaToolProjectRegistrar>() ?: service<QodanaToolApplicationRegistrar>()
    }
  }

  protected open fun createExternalTools(): List<ExternalInspectionToolWrapper> {
    return ExternalToolsProvider.runAnnounceToolProvider(null)
  }

  override fun addListener(listener: Listener, parentDisposable: Disposable?) {
    // Don't do anything here, since we don't care about inspection changes during the execution.
    // The rest is being handled by InspectionToolRegistrar already
  }

  override fun createTools(): List<InspectionToolWrapper<*, *>> {
    return platformInspectionToolRegistrar.createTools() + createExternalTools()
  }
}

@VisibleForTesting
@Service(Service.Level.APP)
class QodanaToolApplicationRegistrar : QodanaToolRegistrar(InspectionToolRegistrar.getInstance())

@VisibleForTesting
@Service(Service.Level.PROJECT)
class QodanaToolProjectRegistrar(val project: Project) : QodanaToolRegistrar(ProjectInspectionToolRegistrar.getInstance(project)) {
  override fun createExternalTools(): List<ExternalInspectionToolWrapper> {
    return ExternalToolsProvider.runAnnounceToolProvider(project)
  }
}
