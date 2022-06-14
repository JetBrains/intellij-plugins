package com.intellij.aws.cloudformation

import com.intellij.ide.IconProvider
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import javax.swing.Icon

internal class CloudFormationIconProvider : IconProvider() {
  override fun getIcon(element: PsiElement, flags: Int): Icon? {
    if (element is YAMLFile && isAwsCloudFormationFile(element)) {
      return CloudFormationIcons.AwsFile
    }

    return null
  }
}

private fun isAwsCloudFormationFile(element: YAMLFile): Boolean {
  return CachedValuesManager.getManager(element.project).getCachedValue(element, CachedValueProvider {
    val text = element.text ?: element.virtualFile?.let { LoadTextUtil.loadText(it, 200) }

    if (!CloudFormationFileTypeDetector.isYaml(text ?: "")) {
      return@CachedValueProvider Result.create(false, element)
    }

    Result.create(isCloudFormationYaml(element), element)
  })
}

internal fun isCloudFormationYaml(element: PsiFile): Boolean {
  if (element !is YAMLFile) return false

  for (child in element.children) {
    if (child is YAMLDocument) {
      val topLevelValue = child.topLevelValue
      if (topLevelValue is YAMLMapping) {
        return topLevelValue.keyValues.firstOrNull()?.keyText == CloudFormationSection.FormatVersion.id
      }
    }
  }
  return false
}