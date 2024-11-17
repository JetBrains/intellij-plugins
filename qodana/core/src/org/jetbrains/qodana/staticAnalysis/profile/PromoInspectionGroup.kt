package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.EnabledInspectionsProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import kotlin.random.Random

internal const val QODANA_PROMO_ANALYZE_EACH_N_FILE_KEY = "qodana.promo.analyze.each"

/**
 * The promotion inspections run on a random subset of the files,
 * to give the user an impression about Qodana's abilities beyond the starter profile.
 */
internal class PromoInspectionGroup(name: String, profile: QodanaInspectionProfile) : NamedInspectionGroup(name, profile) {

  override fun createState(context: QodanaGlobalInspectionContext): State = PromoState(context)

  inner class PromoState(context: QodanaGlobalInspectionContext) : State(context) {
    private val analyzeEachNth: Int = System.getProperty(QODANA_PROMO_ANALYZE_EACH_N_FILE_KEY, "10").toInt()

    override fun shouldSkip(inspectionId: String, file: PsiFile, wrappers: EnabledInspectionsProvider.ToolWrappers): Boolean {
      val profile = context.profile
      if (wrappers.allWrappers.none { profile.idToEffectiveGroup[it.shortName] == profile.mainGroup }) return true
      return Random.nextInt(analyzeEachNth) != 0
    }
  }

  override fun applyConfig(config: QodanaConfig, project: Project, addDefaultExclude: Boolean): PromoInspectionGroup {
    super.applyConfig(config, project, addDefaultExclude)
    val excludeModifiers = config.getExcludeModifiers(addDefaultExclude, project)
    if (excludeModifiers.isEmpty()) return this

    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    val newProfile = QodanaInspectionProfile.clone(profile, "qodana.promo.profile(base:${profile.name})", profileManager)

    excludeModifiers.forEach { it.updateProfileScopes(newProfile, project, config.projectPath) }
    return PromoInspectionGroup(name, newProfile)
  }
}
