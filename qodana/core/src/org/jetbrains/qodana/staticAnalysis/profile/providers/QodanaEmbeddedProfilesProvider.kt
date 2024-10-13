package org.jetbrains.qodana.staticAnalysis.profile.providers

import com.intellij.codeInspection.inspectionProfile.YamlInspectionProfileImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.PlatformUtils
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider
import org.jetbrains.qodana.staticAnalysis.profile.QodanaToolRegistrar
import java.io.InputStream
import java.io.InputStreamReader

private const val QODANA_PROFILES_DIR = "/qodana-profiles/.idea/inspectionProfiles"

private const val QODANA_RECOMMENDED_OTHER = "qodana.recommended.yaml"
private const val QODANA_RECOMMENDED_WITH_JS = "qodana-js.recommended.yaml"
private const val QODANA_RECOMMENDED_WITH_DOTNET = "qodana-dotnet.recommended.yaml"

private const val QODANA_STARTER_OTHER = "qodana.starter.yaml"
private const val QODANA_STARTER_WITH_JS = "qodana-js.starter.yaml"
private const val QODANA_STARTER_WITH_DOTNET = "qodana-dotnet.starter.yaml"

class QodanaEmbeddedProfilesProvider : QodanaInspectionProfileProvider {

  override fun provideProfile(profileName: String, project: Project?): QodanaInspectionProfile? {
    val embeddedProfile = QodanaEmbeddedProfile.entries.firstOrNull { it.matchesName(profileName) } ?: return null
    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    getInputStreamForProfile(embeddedProfile).use { profileStream ->

      if (embeddedProfile.isYaml) {
        val yamlProfile = InputStreamReader(profileStream).use { reader ->
          YamlInspectionProfileImpl.loadFrom(
            reader,
            { path -> InputStreamReader(createResourceStream(path.toString())) },
            QodanaToolRegistrar.getInstance(project),
            profileManager,
          ).buildEffectiveProfile()
        }
        return QodanaInspectionProfile.clone(yamlProfile, yamlProfile.name, profileManager)
      }
      else {
        val element = JDOMUtil.load(profileStream)
        return QodanaInspectionProfile.newFromXml(element, embeddedProfile.profileName, profileManager)
      }
    }
  }

  override fun getAllProfileNames(project: Project?): List<String> = QodanaEmbeddedProfile.entries.filter { it.isYaml }.map { it.profileName }

  @VisibleForTesting
  fun getInputStreamForProfile(embeddedProfile: QodanaEmbeddedProfile): InputStream {
    val profileFilename = getProfileResourceName(embeddedProfile)
    return createResourceStream(profileFilename)
  }

  private fun getProfileResourceName(embeddedProfile: QodanaEmbeddedProfile): String {
    return when (embeddedProfile) {
      QodanaEmbeddedProfile.QODANA_RECOMMENDED_OLD -> {
        "qodana.recommended.full.xml"
      }
      QodanaEmbeddedProfile.QODANA_STARTER_OLD -> {
        "qodana.starter.full.xml"
      }
      QodanaEmbeddedProfile.QODANA_SANITY -> {
        "qodana.sanity.yaml"
      }
      QodanaEmbeddedProfile.QODANA_STARTER -> when {
        PlatformUtils.isWebStorm() || PlatformUtils.isPhpStorm() -> QODANA_STARTER_WITH_JS
        PlatformUtils.isRider() -> QODANA_STARTER_WITH_DOTNET
        else -> QODANA_STARTER_OTHER
      }
      QodanaEmbeddedProfile.QODANA_RECOMMENDED -> when {
        PlatformUtils.isWebStorm() || PlatformUtils.isPhpStorm()  -> QODANA_RECOMMENDED_WITH_JS
        PlatformUtils.isRider() -> QODANA_RECOMMENDED_WITH_DOTNET
        else -> QODANA_RECOMMENDED_OTHER
      }
    }
  }

  private fun createResourceStream(profileFilename: String): InputStream {
    return javaClass.getResourceAsStream("$QODANA_PROFILES_DIR/$profileFilename")
           ?: throw QodanaException("Cannot find file profile file $profileFilename in resources")
  }
}