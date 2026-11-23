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

  override fun createState(context: QodanaGlobalInspectionContext): GroupState = PromoState(context)

  inner class PromoState(context: QodanaGlobalInspectionContext) : GroupState(context, this) {
    private val analyzeEachNth: Int = System.getProperty(QODANA_PROMO_ANALYZE_EACH_N_FILE_KEY, "10").toInt()

    override fun shouldSkip(inspectionId: String, file: PsiFile, wrappers: EnabledInspectionsProvider.ToolWrappers): Boolean {
      val profile = context.profile
      if (wrappers.allWrappers.none { profile.idToEffectiveGroup[it.shortName] == profile.mainGroup }) return true
      return Random.nextInt(analyzeEachNth) != 0
    }
  }

  override fun applyConfig(config: QodanaConfig, project: Project, addDefaultExclude: Boolean): PromoInspectionGroup {
    super.applyConfig(config, project, addDefaultExclude)
    val newProfile = calculateNewProfileForGroup(config, project, addDefaultExclude, "qodana.promo.profile(base:${profile.name})") ?: return this
    return PromoInspectionGroup(name, newProfile)
  }
}
