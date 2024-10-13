package org.jetbrains.qodana.yaml

import com.intellij.codeInspection.ex.EditInspectionToolsSettingsAction
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.ScopeToolState
import com.intellij.openapi.project.Project
import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProjectInspectionProfileManager
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLPsiElement
import org.jetbrains.yaml.psi.YAMLScalar

internal const val QODANA_CONFIG_NAME = "qodana"
internal const val QODANA_CONFIG_EXTENSION = "yaml"
internal const val QODANA_INSPECTION_INCLUDE_NAME = "include.name"
internal const val QODANA_INSPECTION_EXCLUDE_NAME = "exclude.name"
internal const val QODANA_PROFILE_NAME = "profile.name"
internal const val QODANA_INSPECTION_INCLUDE_PATHS = "include.paths"
internal const val QODANA_INSPECTION_EXCLUDE_PATHS = "exclude.paths"

internal fun isQodanaYaml(originalFile: PsiFile): Boolean {
  val virtualFile = originalFile.virtualFile ?: return false
  return originalFile is YAMLFile
         && virtualFile.nameWithoutExtension == QODANA_CONFIG_NAME
         && virtualFile.extension == QODANA_CONFIG_EXTENSION
}

internal val QODANA_YAML_PATTERN: PatternCondition<PsiElement> = object : PatternCondition<PsiElement>("isQodanaYaml") {
  override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
    return isQodanaYaml(element.containingFile)
  }
}

internal fun yamlKeysPattern(vararg keys: String): PatternCondition<PsiElement> = object : PatternCondition<PsiElement>("yamlKeysPattern") {
  override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
    return element is YAMLScalar && fullYamlKey(element) in keys
  }
}

internal fun fullYamlKey(element: YAMLPsiElement?): String = element
  ?.let(YAMLUtil::getConfigFullName)
  ?.replace("\\[\\d+\\]".toRegex(), "")
  .orEmpty()

internal fun getAllInspections(project: Project): List<InspectionToolWrapper<*, *>> {
  // All possible tools for project and for application
  val allTools = QodanaProjectInspectionProfileManager
    .getInstance(project)
    .getAllProfiles()
    .flatMap { it.allTools }

  return allTools
    .distinctBy { it.tool.shortName }
    .map(ScopeToolState::getTool)
}

internal fun getInspectionFromElement(element: PsiElement, fromParent: Boolean = true): InspectionToolWrapper<*, *>? {
  val yamlValue = retrieveYamlValue(element.parent.takeIf { fromParent } ?: element) ?: return null

  val key = fullYamlKey(yamlValue)
  if (key != QODANA_INSPECTION_INCLUDE_NAME && key != QODANA_INSPECTION_EXCLUDE_NAME) return null

  return getAllInspections(yamlValue.project)
    .find { it.id == yamlValue.textValue }
}

private fun retrieveYamlValue(element: PsiElement): YAMLScalar? {
  if (element !is YAMLScalar || !element.isValid) return null
  return element
}

internal fun showInspection(project: Project, inspectionName: String) {
  val projectProfileManager = QodanaInspectionProfileManager.getInstance(project)
  val inspectionProfile = projectProfileManager.currentProfile
  EditInspectionToolsSettingsAction.editToolSettings(project, inspectionProfile, inspectionName)
}

internal fun InspectionToolWrapper<*, *>.groupDisplayPath(): String = groupPath.joinToString("/")