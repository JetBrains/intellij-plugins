package org.jetbrains.qodana.staticAnalysis.testFramework

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path

class QodanaEmbeddedProfilesTestProvider(private val getTestDataPath: (String) -> Path) : QodanaInspectionProfileProvider {
  override fun provideProfile(profileName: String, project: Project?): QodanaInspectionProfile? {
    val embeddedProfile = QodanaEmbeddedProfile.entries.firstOrNull { it.matchesName(profileName) } ?: return null

    val element = JDOMUtil.load(getInputStreamForProfile(embeddedProfile))
    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    return QodanaInspectionProfile.newFromXml(element, embeddedProfile.profileName, profileManager)
  }

  override fun getAllProfileNames(project: Project?): List<String> = QodanaEmbeddedProfile.entries.map { it.name }

  private fun getInputStreamForProfile(embeddedProfile: QodanaEmbeddedProfile): InputStream {
    val profileFilename = when(embeddedProfile) {
      QodanaEmbeddedProfile.QODANA_RECOMMENDED_OLD -> "qodana.recommended.full.xml"
      QodanaEmbeddedProfile.QODANA_RECOMMENDED -> "qodana.recommended.full.xml"
      QodanaEmbeddedProfile.QODANA_STARTER_OLD -> "qodana.starter.full.xml"
      QodanaEmbeddedProfile.QODANA_STARTER -> "qodana.starter.full.xml"
      QodanaEmbeddedProfile.QODANA_SANITY -> "qodana.sanity.xml"
      else -> ""
    }
    return FileInputStream(getTestDataPath.invoke(profileFilename).toFile())
  }
}