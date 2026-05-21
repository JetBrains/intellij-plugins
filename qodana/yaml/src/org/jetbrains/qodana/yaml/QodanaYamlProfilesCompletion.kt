package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.openapi.project.Project
import icons.QodanaIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProjectInspectionProfileManager
import org.jetbrains.yaml.psi.YAMLFile


private val DEFAULT_PROFILES = listOf(
  QodanaLookupElement("Default", "Default profile")
)

class QodanaYamlProfilesCompletion : QodanaYamlCompletionContributorBase() {
  override suspend fun variantsForKey(key: String, file: YAMLFile, prefix: String): List<QodanaLookupElement> = when (key) {
    QODANA_PROFILE_NAME -> DEFAULT_PROFILES + getAllProfileAsync(file.project).map(::QodanaProfileLookupElement)
    else -> emptyList()
  }

  private suspend fun getAllProfileAsync(project: Project): List<QodanaInspectionProfile> {
    return project.qodanaProjectScope.async(Dispatchers.IO) {
      runInterruptible {
        // not cancellable, blocking, so we need to call this on independent coroutine to achieve completion's cancellability
        QodanaProjectInspectionProfileManager.getInstance(project).getAllProfiles()
      }
    }.await()
  }
}

internal class QodanaProfileLookupElement(val profile: QodanaInspectionProfile) : QodanaLookupElement(
  profile.displayName,
  profile.description ?: PROFILE_DESCRIPTIONS[profile.displayName] ?: ""
) {
  companion object {
    private val PROFILE_DESCRIPTIONS = mapOf(
      "qodana.recommended" to "Default profile including needed inspections",
      "qodana.starter" to "Lighter version of qodana.recommended profile that does not include heavy inspections",
      "qodana.sanity" to "Only sanity checks are included",
      "empty" to "No inspections included"
    )
  }

  override fun renderElement(presentation: LookupElementPresentation) {
    super.renderElement(presentation)
    presentation.icon = QodanaIcons.Icons.Qodana
  }
}