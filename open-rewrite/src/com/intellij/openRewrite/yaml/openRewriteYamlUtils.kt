package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.TailTypeDecorator
import com.intellij.microservices.jvm.config.yaml.ConfigYamlUtils
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.isRecipe
import com.intellij.openRewrite.recipe.OpenRewriteOptionDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteOptionPsiElement
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequenceItem

internal val RECIPE_YAML_CONDITION: PatternCondition<PsiElement> = object : PatternCondition<PsiElement>("isOpenRewriteRecipe") {
  override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
    val containingFile = element.getContainingFile() ?: return false
    return isRecipe(containingFile.originalFile)
  }
}

internal fun getKeyValueType(yamlKeyValue: YAMLKeyValue): OpenRewriteType? = getSequenceItemType(yamlKeyValue.parent?.parent)

internal fun getSequenceItemType(element: PsiElement?): OpenRewriteType? {
  if (element !is YAMLSequenceItem) return null

  val parentKeyValue = element.parent?.parent
  if (parentKeyValue !is YAMLKeyValue) return null

  if (parentKeyValue.parent?.parent !is YAMLDocument) return null

  val keyText = parentKeyValue.keyText
  for (type in OpenRewriteType.entries) {
    if (type.listKey == keyText || type.additionalListKeys.contains(keyText)) {
      return type
    }
  }
  return null
}

internal fun getOptionLookupElement(descriptor: OpenRewriteOptionDescriptor): LookupElement {
  var builder = LookupElementBuilder.create(descriptor.name)
    .withIcon(OpenRewriteIcons.OpenRewrite)
    .withPsiElement(OpenRewriteOptionPsiElement(descriptor))
  if (descriptor.displayName != null) {
    builder = builder
      .withLookupString(descriptor.displayName)
      .withTypeText(descriptor.displayName)
  }
  return TailTypeDecorator.withTail(builder, ConfigYamlUtils.getValueTailType())
}