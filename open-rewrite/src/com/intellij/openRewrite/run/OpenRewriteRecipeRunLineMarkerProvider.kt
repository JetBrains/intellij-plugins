package com.intellij.openRewrite.run

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openRewrite.RECIPE_TYPE_REGEX
import com.intellij.openRewrite.YAML_KEY_NAME
import com.intellij.openRewrite.YAML_KEY_TYPE
import com.intellij.openRewrite.isRecipe
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

internal class OpenRewriteRecipeRunLineMarkerProvider : RunLineMarkerContributor() {
  override fun isDumbAware(): Boolean = true

  override fun getInfo(element: PsiElement): Info? {
    val psiFile = element.containingFile ?: return null
    if (!isRecipe(psiFile)) return null

    val yamlKeyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java) ?: return null
    if (yamlKeyValue.key != element) return null

    if (yamlKeyValue.keyText != YAML_KEY_NAME) return null

    val parent = yamlKeyValue.parent as? YAMLMapping ?: return null
    if (parent.parent !is YAMLDocument) return null

    val type = (parent.getKeyValueByKey(YAML_KEY_TYPE)?.value as? YAMLScalar)?.textValue ?: return null
    if (!RECIPE_TYPE_REGEX.matches(type)) return null

    val recipeName = (yamlKeyValue.value as? YAMLScalar)?.textValue ?: return null
    if (recipeName.isBlank()) return null

    return Info(AllIcons.RunConfigurations.TestState.Run, ExecutorAction.getActions(), null)
  }
}