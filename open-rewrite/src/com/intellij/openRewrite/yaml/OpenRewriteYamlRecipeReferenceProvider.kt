package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.YAML_KEY_NAME
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipePsiElement
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.NaturalComparator
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayUtilRt
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLPsiElement
import org.jetbrains.yaml.psi.YAMLScalar

internal class OpenRewriteYamlRecipeReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    return arrayOf(RecipeReference(element as YAMLPsiElement))
  }

  class RecipeReference(element: YAMLPsiElement) : PsiReferenceBase.Poly<YAMLPsiElement>(element) {
    private val text: String = if (element is YAMLKeyValue) element.keyText else if (element is YAMLScalar) element.textValue else element.text

    private fun getType(): OpenRewriteType? {
      val element = myElement
      return if (element is YAMLKeyValue) getKeyValueType(element) else getSequenceItemType(element.parent)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
      val keyText = text
      val type = getType() ?: return ResolveResult.EMPTY_ARRAY
      val descriptor =
        OpenRewriteRecipeService.getInstance(myElement.project).findDescriptor(keyText, myElement.containingFile, type)
        ?: return ResolveResult.EMPTY_ARRAY
      if (descriptor.declaration.retrieve() != null) {
        return PsiElementResolveResult.createResults(OpenRewriteRecipePsiElement(descriptor))
      }
      return ResolveResult.EMPTY_ARRAY
    }

    override fun getVariants(): Array<Any> {
      val type = getType() ?: return ArrayUtilRt.EMPTY_OBJECT_ARRAY
      var descriptors =
        OpenRewriteRecipeService.getInstance(myElement.project).getDescriptors(myElement.containingFile, type)
      val documentRecipeName = getDocumentRecipeName()
      if (documentRecipeName != null) {
        descriptors = descriptors.filter { it.name != documentRecipeName }
      }
      val project = myElement.project
      val sorted = ArrayList(descriptors)
      sorted.sortWith(Comparator.comparing({ it.name }, NaturalComparator.INSTANCE))
      return sorted.map { getLookupItem(it, project) }.toTypedArray()
    }

    private fun getDocumentRecipeName(): String? {
      val document = PsiTreeUtil.getParentOfType(myElement, YAMLDocument::class.java) ?: return null
      val value = (document.topLevelValue as? YAMLMapping)?.getKeyValueByKey(YAML_KEY_NAME)?.value ?: return null
      return if (value is YAMLScalar) value.textValue else null
    }

    private fun getLookupItem(descriptor: OpenRewriteRecipeDescriptor, project: Project): LookupElement {
      var builder = LookupElementBuilder.create(descriptor.name)
        .withIcon(OpenRewriteIcons.OpenRewrite)
        .withPsiElement(OpenRewriteRecipePsiElement(descriptor))
      if (descriptor.displayName != null) {
        builder = builder
          .withLookupString(descriptor.displayName)
          .withTypeText(descriptor.displayName)
      }
      if (descriptor.options.any { option -> option.required }) {
        builder = builder.withInsertHandler(OpenRewriteRecipeInsertHandler(descriptor, project))
      }
      return builder
    }
  }
}