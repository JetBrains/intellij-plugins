package com.intellij.openRewrite.yaml

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar


internal class OpenRewriteYamlReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(
      PlatformPatterns.psiElement(YAMLKeyValue::class.java)
        .with(RECIPE_YAML_CONDITION)
        .with(object : PatternCondition<YAMLKeyValue>("isAcceptableRecipeKeyValueReference") {
          override fun accepts(t: YAMLKeyValue, context: ProcessingContext?): Boolean {
            return getKeyValueType(t) != null
          }
        }),
      OpenRewriteYamlRecipeReferenceProvider())
    registrar.registerReferenceProvider(
      PlatformPatterns.psiElement(YAMLScalar::class.java)
        .with(RECIPE_YAML_CONDITION)
        .with(object : PatternCondition<YAMLScalar>("isAcceptableRecipeScalarReference") {
          override fun accepts(t: YAMLScalar, context: ProcessingContext?): Boolean {
            return getSequenceItemType(t.parent) != null
          }
        }),
      OpenRewriteYamlRecipeReferenceProvider())
    registrar.registerReferenceProvider(
      PlatformPatterns.psiElement(YAMLKeyValue::class.java)
        .with(RECIPE_YAML_CONDITION)
        .with(object : PatternCondition<YAMLKeyValue>("isAcceptableRecipeOptionReference") {
          override fun accepts(t: YAMLKeyValue, context: ProcessingContext?): Boolean {
            val parent = t.parent?.parent as? YAMLKeyValue ?: return false
            return getKeyValueType(parent) != null
          }
        }),
      OpenRewriteYamlRecipeOptionReferenceProvider())
    registrar.registerReferenceProvider(
      PlatformPatterns.psiElement(YAMLScalar::class.java)
        .with(RECIPE_YAML_CONDITION)
        .with(object : PatternCondition<YAMLScalar>("isAcceptableRecipeScalarReference") {
          override fun accepts(t: YAMLScalar, context: ProcessingContext?): Boolean {
            val parent = t.parent as? YAMLKeyValue ?: return false
            val grandParent = parent.parent?.parent as? YAMLKeyValue ?: return false
            return getKeyValueType(grandParent) != null
          }
        }),
      OpenRewriteYamlRecipeOptionValueReferenceProvider())
  }

}
