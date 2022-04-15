package com.intellij.aws.cloudformation

import com.intellij.ide.IconProvider
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import javax.swing.Icon

internal class CloudFormationIconProvider : IconProvider() {
  override fun getIcon(element: PsiElement, flags: Int): Icon? {
    if (element is YAMLFile && isAwsYaml(element)
        || element is JsonFile && isAwsJson(element)) {
      return CloudFormationIcons.AwsFile
    }
    return null
  }

  private fun isAwsJson(element: JsonFile): Boolean {
    val topLevelValue = element.topLevelValue ?: return false
    return topLevelValue.children.any {
      it is JsonProperty && it.name == CloudFormationSection.FormatVersion.id
    }
  }

  private fun isAwsYaml(element: YAMLFile): Boolean {
    return element.documents.any { doc ->
      val topLevelValue = doc.topLevelValue ?: return false
      topLevelValue.children.any { it is YAMLKeyValue && it.keyText == CloudFormationSection.FormatVersion.id }
    }
  }
}