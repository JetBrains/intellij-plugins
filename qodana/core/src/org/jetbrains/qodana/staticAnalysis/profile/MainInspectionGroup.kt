package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.openapi.project.Project
import kotlinx.coroutines.cancel
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaCancellationException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import java.util.concurrent.atomic.AtomicInteger

class MainInspectionGroup(profile: QodanaInspectionProfile) : NamedInspectionGroup("", profile) {
  override fun createState(context: QodanaGlobalInspectionContext) = State(context)

  inner class State(context: QodanaGlobalInspectionContext) : NamedInspectionGroup.State(context) {
    private val problemsCounter = AtomicInteger()

    override fun onConsumeProblem(inspectionId: String, relativePath: String?, module: String?): Boolean {
      if (context.config.isAboveStopThreshold(problemsCounter.get())) {
        val errorMessage = "Analysis cancelled since stopThreshold ${context.config.stopThreshold} is surpassed."

        context.qodanaRunScope.cancel(QodanaCancellationException(errorMessage))
        return false
      }
      problemsCounter.incrementAndGet()
      return super.onConsumeProblem(inspectionId, relativePath, module)
    }

    fun getCount() = problemsCounter.get()
  }

  override fun applyConfig(config: QodanaConfig, project: Project, addDefaultExclude: Boolean): MainInspectionGroup {
    super.applyConfig(config, project, addDefaultExclude)
    val excludeModifiers = config.getExcludeModifiers(addDefaultExclude, project)
    val includeModifiers = config.getIncludeModifiers()
    if (excludeModifiers.isEmpty() && includeModifiers.isEmpty()) return this

    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    val newProfile = QodanaInspectionProfile.clone(profile, "qodana.main.profile(base:${profile.name})", profileManager)

    excludeModifiers.forEach { it.updateProfileScopes(newProfile, project, config.projectPath) }
    includeModifiers.forEach { it.updateProfileScopes(newProfile, project, config.projectPath) }

    return MainInspectionGroup(newProfile)
  }
}