package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.configurationStore.SchemeDataHolder
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.ProjectBasedInspectionProfileManager
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException

/**
 * Use only [QodanaInspectionProfile] in Qodana plugin.
 * Avoid [InspectionProfileImpl]: all profiles used by Qodana must have [QodanaToolRegistrar] and [QodanaInspectionProfileManager].
 *
 * Also do not use constructor (unless you know what you're doing), prefer factory methods instead
 * (due to complicated logic related to [baseProfile]).
 *
 * Don't use [QodanaApplicationInspectionProfileManager.qodanaBaseProfile] or [QodanaApplicationInspectionProfileManager.qodanaEmptyProfile]
 * directly since they have `null` base profile, which leads to incorrect serialization:
 * if you modify a profile which has a `null` as a base, it won't serialize any of your changes.
 *
 * See [org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileTest] for examples.
 */
class QodanaInspectionProfile(
  profileName: String,
  profileManager: QodanaInspectionProfileManager,
  baseProfile: QodanaInspectionProfile?,
  dataHolder: SchemeDataHolder<InspectionProfileImpl>? = null
) : InspectionProfileImpl(
  profileName,
  QodanaToolRegistrar.getInstance(profileManager.managerProject),
  profileManager,
  baseProfile,
  dataHolder
) {
  companion object {
    fun newWithEnabledByDefaultTools(
      name: String,
      profileManager: QodanaInspectionProfileManager,
      dataHolder: SchemeDataHolder<InspectionProfileImpl>? = null
    ): QodanaInspectionProfile {
      return QodanaInspectionProfile(name, profileManager, profileManager.qodanaBaseProfile, dataHolder)
    }

    fun newWithDisabledTools(name: String, profileManager: QodanaInspectionProfileManager): QodanaInspectionProfile {
      return QodanaInspectionProfile(name, profileManager, profileManager.qodanaEmptyProfile)
    }

    fun newFromXml(element: Element, name: String?, profileManager: QodanaInspectionProfileManager): QodanaInspectionProfile {
      return newWithEnabledByDefaultTools(name ?: "unknown", profileManager).apply {
        readExternal(element.profileElement)
        if (name != null) {
          this.name = name
        }
      }
    }

    fun clone(
      source: InspectionProfileImpl,
      name: String,
      profileManager: QodanaInspectionProfileManager
    ): QodanaInspectionProfile {
      return newWithDisabledTools(name, profileManager).apply {
        copyTools(source, (profileManager as? ProjectBasedInspectionProfileManager)?.project)
      }
    }
  }

  private fun copyTools(base: InspectionProfileImpl, project: Project?) {
    if (base.inspectionToolsSupplier !is QodanaToolRegistrar) throw QodanaException("Provided profile doesn't have Qodana tool registrar")
    if (base.profileManager !is QodanaInspectionProfileManager) throw QodanaException("Provided profile doesn't have Qodana profile manager")

    tools.forEach {
      val tools = base.getToolsOrNull(it.shortName, project)
      if (tools != null) {
        it.copyTool(tools, project)
        copyDependentTools(project, tools, base, this)
      }
    }
  }
}

val Element.profileElement: Element
  get() = getChild("profile") ?: this