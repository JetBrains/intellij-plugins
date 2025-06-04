package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.EnabledInspectionsProvider
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.license.isInspectionLicensed
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionProfileLoader
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile
import java.util.concurrent.ConcurrentHashMap

private const val EFFECTIVE_PROFILE_NAME = "qodana.effective.profile"

/**
 * The Qodana profile determines on which files the inspections are run.
 *
 * The profile mainly consists of a [MainInspectionGroup], which covers most of the inspections.
 * There may be additional inspection groups such as [PromoInspectionGroup] and [SanityInspectionGroup].
 *
 * Each of these inspection groups provides its own [InspectionProfileImpl].
 * The combination of these inspection profiles determines which inspections are run,
 * and on which files each inspection is run.
 */
class QodanaProfile(
  val mainGroup: MainInspectionGroup,
  private val additionalGroups: List<NamedInspectionGroup>,
  private val project: Project,
  private val licenseType: QodanaLicenseType
) {
  companion object {
    private val LOG = logger<QodanaProfile>()

    fun create(
      project: Project,
      mainInspectionProfile: QodanaInspectionProfile,
      inspectionProfileLoader: QodanaInspectionProfileLoader,
      config: QodanaConfig,
      sanity: Boolean,
      promo: Boolean
    ): QodanaProfile {
      val addDefaultExclude = mainInspectionProfile.isRecommendedOrStarter

      val inspectionGroups = mutableListOf<NamedInspectionGroup>()

      if (sanity) {
        val sanityProfileName = System.getProperty("qodana.sanity.profile.name", "qodana.sanity")
        val sanityProfile = inspectionProfileLoader.loadProfileByName(sanityProfileName)
        if (sanityProfile != null) {
          inspectionGroups.add(SanityInspectionGroup("sanity", sanityProfile).applyConfig(config, project, addDefaultExclude))
          LOG.info("The '$sanityProfileName' profile is configured for sanity checks")
        }
        else {
          LOG.warn("Can't load the '$sanityProfileName' sanity profile. Running sanity inspections is disabled.")
        }
      }

      if (promo) {
        val promoProfileName = System.getProperty("qodana.promo.profile.name", "qodana.recommended")
        val promoProfile = inspectionProfileLoader.loadProfileByName(promoProfileName)
        if (promoProfile != null) {
          inspectionGroups.add(PromoInspectionGroup("promo", promoProfile).applyConfig(config, project, addDefaultExclude))
          LOG.info("The '$promoProfileName' profile is configured for promo checks")
        }
        else {
          LOG.warn("Can't load the '$promoProfileName' promo profile. Running promo inspections is disabled.")
        }
      }

      val userGroup = MainInspectionGroup(mainInspectionProfile).applyConfig(config, project, addDefaultExclude)

      return QodanaProfile(userGroup, inspectionGroups, project, config.license.type)
    }

    private val QodanaInspectionProfile.isRecommendedOrStarter: Boolean
      get() {
        return sequenceOf(
          QodanaEmbeddedProfile.QODANA_RECOMMENDED_OLD,
          QodanaEmbeddedProfile.QODANA_STARTER_OLD
        ).any { it.matchesName(this.name) }
      }
  }

  val allGroups = listOf(mainGroup) + additionalGroups
  val idToEffectiveGroup: Map<String, NamedInspectionGroup> = mutableMapOf<String, NamedInspectionGroup>().apply {
    for (group in allGroups) {
      for (tool in group.profile.tools) {
        if (tool.isEnabled) putIfAbsent(tool.shortName, group)
      }
    }
  }

  val effectiveProfile : QodanaInspectionProfile by lazy {
    val effectiveProfile = QodanaInspectionProfile.newWithDisabledTools(EFFECTIVE_PROFILE_NAME, QodanaInspectionProfileManager.getInstance(project))
    for (group in allGroups) {
      for (tools in group.profile.tools) {
        if (tools.isEnabled && group == idToEffectiveGroup[tools.shortName]) {
          val to = effectiveProfile.getToolsOrNull(tools.shortName, project)
          if (to != null) {
            to.copyTool(tools, project)
            copyDependentTools(project, tools, group.profile, effectiveProfile)
          }
        }
      }
    }

    effectiveProfile.tools.forEach { tool ->
      if (!licenseType.isInspectionLicensed(tool)) {
        tool.isEnabled = false
      }
    }

    checkUnknownScopes(effectiveProfile)

    return@lazy effectiveProfile
  }

  private fun checkUnknownScopes(effectiveProfile: QodanaInspectionProfile) {
    val tools = effectiveProfile.getAllEnabledInspectionTools(project)
    val seen = mutableSetOf<String>()
    for (tool in tools) {
      for (scopeState in tool.tools) {
        if (scopeState.getScope(project) == null && seen.add(scopeState.scopeName)) {
          LOG.warn("Unknown scope in profile: '${scopeState.scopeName}'")
        }
      }
    }
  }

  fun createState(context: QodanaGlobalInspectionContext): QodanaProfileState {
    val mainState = mainGroup.createState(context)
    val states = additionalGroups.associateWith { it.createState(context) } + (mainGroup to mainState)
    val statesMap = idToEffectiveGroup.mapValues { (_, value) -> states[value]!! }

    return QodanaProfileState(mainState, statesMap, states.mapKeys { it.key.name })
  }

  class QodanaProfileState(val mainState: MainInspectionGroup.State,
                           val stateByInspectionId: Map<String, NamedInspectionGroup.State>,
                           val stateByGroupName: Map<String, NamedInspectionGroup.State>) {

    private val receivedCounters = ConcurrentHashMap<String, Int>()

    fun onReceive(inspectionId: String, size: Int) {
      receivedCounters.compute(inspectionId) { _, v ->
        if (v == null) size else v + size
      }
    }

    fun dump() {
      val receivedMessage = receivedCounters.entries.joinToString("\n") { (id, counter) ->
        "InspectionId:$id received: $counter"
      }
      LOG.debug("Received counters:\n $receivedMessage")
    }

    fun shouldSkip(inspectionId: String, file: PsiFile, wrappers: EnabledInspectionsProvider.ToolWrappers): Boolean {
      return stateByInspectionId[inspectionId]?.shouldSkip(inspectionId, file, wrappers) ?: true
    }

    fun onFinish() {
      stateByInspectionId.values.distinct().forEach { it.onFinish() }
    }
  }
}