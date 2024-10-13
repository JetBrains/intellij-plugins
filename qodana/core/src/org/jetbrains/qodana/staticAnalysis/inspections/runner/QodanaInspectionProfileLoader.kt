package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.InspectionProfileLoaderBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaToolRegistrar
import java.nio.file.Files
import java.nio.file.Paths

class QodanaInspectionProfileLoader(project: Project) : InspectionProfileLoaderBase<QodanaInspectionProfile>(project) {
  override fun loadProfileByName(profileName: String): QodanaInspectionProfile? {
    return QodanaInspectionProfileManager.getInstance(project).getQodanaProfile(profileName)
  }

  override fun loadProfileByPath(profilePath: String): QodanaInspectionProfile? {
    val profileManager = QodanaInspectionProfileManager.getInstance(project)

    val profileFromYaml = tryLoadProfileFromYaml(profilePath, QodanaToolRegistrar.getInstance(project), profileManager)
    if (profileFromYaml != null) return QodanaInspectionProfile.clone(profileFromYaml, profileFromYaml.name, profileManager)

    val file = Paths.get(profilePath)
    if (!Files.isRegularFile(file)) return null

    val element = JDOMUtil.load(file)
    return QodanaInspectionProfile.newFromXml(element, name = null, profileManager = profileManager)
  }
}
