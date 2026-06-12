package com.intellij.openRewrite.yaml

import com.intellij.openRewrite.recipe.OpenRewriteOptionPsiElement
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.ResolveResult
import com.intellij.util.ArrayUtilRt
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue

internal class OpenRewriteYamlRecipeOptionReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    return arrayOf(RecipeOptionReference(element as YAMLKeyValue))
  }

  class RecipeOptionReference(element: YAMLKeyValue) : PsiReferenceBase.Poly<YAMLKeyValue>(element) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
      val descriptor = findRecipeDescriptor() ?: return ResolveResult.EMPTY_ARRAY
      val keyText = element.keyText
      val option = descriptor.options.find { it.name == keyText } ?: return ResolveResult.EMPTY_ARRAY
      if (option.declaration.retrieve() != null) {
        return PsiElementResolveResult.createResults(OpenRewriteOptionPsiElement(option))
      }
      return ResolveResult.EMPTY_ARRAY
    }

    override fun getVariants(): Array<Any> {
      val descriptor = findRecipeDescriptor() ?: return ArrayUtilRt.EMPTY_OBJECT_ARRAY
      return descriptor.options.map { getOptionLookupElement(it) }.toTypedArray()
    }

    private fun findRecipeDescriptor(): OpenRewriteRecipeDescriptor? {
      val keyValue = (element.parent?.parent as? YAMLKeyValue) ?: return null
      val type = getKeyValueType(keyValue) ?: return null
      val text = keyValue.keyText
      return OpenRewriteRecipeService.getInstance(myElement.project).findDescriptor(text, myElement.containingFile, type)
    }
  }
}