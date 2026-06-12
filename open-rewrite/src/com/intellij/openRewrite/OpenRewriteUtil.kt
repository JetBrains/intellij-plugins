package com.intellij.openRewrite

import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

internal fun isRecipe(psiFile: PsiFile): Boolean {
  if (psiFile !is YAMLFile) return false

  return CachedValuesManager.getManager(psiFile.project).getCachedValue(psiFile) {
    Result.create(matchesType(psiFile), psiFile)
  }
}

private fun matchesType(file: YAMLFile): Boolean {
  val document = file.documents.firstOrNull() ?: return false
  val value = (document.topLevelValue as? YAMLMapping)?.keyValues?.find { it.name == YAML_KEY_TYPE }?.value ?: return false
  val type = (value as? YAMLScalar)?.textValue ?: return false
  return REWRITE_TYPE_REGEX.matches(type)
}