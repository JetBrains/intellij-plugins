package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.EnabledInspectionsProvider
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaAnalysisCancellationService
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import java.util.concurrent.atomic.AtomicInteger

class MainInspectionGroup(profile: QodanaInspectionProfile) : NamedInspectionGroup("", profile) {
  override fun createState(context: QodanaGlobalInspectionContext): State = State(context)

  private companion object {
    val LOG = logger<MainInspectionGroup>()
  }

  inner class State(context: QodanaGlobalInspectionContext) : GroupState(context, this) {
    val thresholdState: StateWithThreshold? =
      if (context.config.forceThresholdsOnMainProfile) StateWithThreshold(context, this@MainInspectionGroup) else null

    private val problemsCounter = AtomicInteger()

    override fun onConsumeProblem(inspectionId: String, relativePath: String?, module: String?): Boolean {
      val keepConsuming = thresholdState?.onConsumeProblem(inspectionId, relativePath, module)
      if (keepConsuming == false) return false
      if (context.config.isAboveStopThreshold(problemsCounter.get())) {
        val errorMessage = "Analysis cancelled since stopThreshold ${context.config.stopThreshold} is surpassed."
        if (!context.project.service<QodanaAnalysisCancellationService>().requestCancel(errorMessage)) {
          LOG.warn("Qodana analysis cancellation requested not in Qodana flow")
        }
        return false
      }
      problemsCounter.incrementAndGet()
      return super.onConsumeProblem(inspectionId, relativePath, module)
    }


    override fun onFinish() {
      thresholdState?.onFinish()
      super.onFinish()
    }

    override fun shouldSkip(inspectionId: String, file: PsiFile, wrappers: EnabledInspectionsProvider.ToolWrappers): Boolean {
      return thresholdState?.shouldSkip(inspectionId, file, wrappers) ?: false
    }

    fun getCount(): Int = problemsCounter.get()
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