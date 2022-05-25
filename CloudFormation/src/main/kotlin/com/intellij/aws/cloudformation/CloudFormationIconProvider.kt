package com.intellij.aws.cloudformation

import com.intellij.ide.IconProvider
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.yaml.psi.YAMLFile
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
    Result.create(CloudFormationFileTypeDetector.isYaml(text ?: ""), element)
  })
}
