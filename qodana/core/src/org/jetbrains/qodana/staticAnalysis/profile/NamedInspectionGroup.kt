package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.EnabledInspectionsProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import java.util.concurrent.ConcurrentHashMap

abstract class NamedInspectionGroup(val name: String, val profile: QodanaInspectionProfile) {
  open fun createState(context: QodanaGlobalInspectionContext): GroupState = GroupState(context, this)

  open fun applyConfig(config: QodanaConfig, project: Project, addDefaultExclude: Boolean): NamedInspectionGroup {
    profile.tools.forEach { tool ->
      if (tool.tools.none { it.isEnabled }) {
        tool.isEnabled = false
      }
    }
    return this
  }

  protected fun calculateNewProfileForGroup(
    config: QodanaConfig,
    project: Project,
    addDefaultExclude: Boolean,
    group: String
  ): QodanaInspectionProfile? {
    val excludeModifiers = config.getExcludeModifiers(addDefaultExclude, project)
    if (excludeModifiers.isEmpty()) return null

    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    val newProfile = QodanaInspectionProfile.clone(profile, group, profileManager)

    excludeModifiers.forEach { it.updateProfileScopes(newProfile, project, config.projectPath) }
    return newProfile
  }
}

open class GroupState(val context: QodanaGlobalInspectionContext, val inspectionGroup: NamedInspectionGroup) {
  private val inspectionIds = ConcurrentHashMap<String, Boolean>()

  open fun onConsumeProblem(inspectionId: String, relativePath: String?, module: String?): Boolean {
    inspectionIds[inspectionId] = true
    return true
  }

  open fun onFinish() = Unit

  open fun shouldSkip(inspectionId: String, file: PsiFile, wrappers: EnabledInspectionsProvider.ToolWrappers) = false
}