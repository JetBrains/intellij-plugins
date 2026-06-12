package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.recipe.OpenRewriteOptionDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.ResolveResult
import com.intellij.util.ArrayUtilRt
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

internal class OpenRewriteYamlRecipeOptionValueReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    val scalar = element as YAMLScalar
    if (scalar.isMultiline) return PsiReference.EMPTY_ARRAY

    return arrayOf(RecipeOptionValueReference(scalar))
  }

  class RecipeOptionValueReference(element: YAMLScalar) : PsiReferenceBase.Poly<YAMLScalar>(element) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
      return ResolveResult.EMPTY_ARRAY
    }

    override fun getVariants(): Array<Any> {
      val descriptor = findRecipeOptionDescriptor() ?: return ArrayUtilRt.EMPTY_OBJECT_ARRAY
      val type = descriptor.typePointer.type?.canonicalText
      if (type == JvmPrimitiveTypeKind.BOOLEAN.name ||
          type == JvmPrimitiveTypeKind.BOOLEAN.boxedFqn) {
        return arrayOf(LookupElementBuilder.create("true"),
                       LookupElementBuilder.create("false"))
      }
      return descriptor.valid.map { LookupElementBuilder.create(it).withIcon(OpenRewriteIcons.OpenRewrite) }.toTypedArray()
    }

    fun findRecipeOptionDescriptor(): OpenRewriteOptionDescriptor? {
      val parent = element.parent as? YAMLKeyValue ?: return null
      val grandParent = parent.parent?.parent as? YAMLKeyValue ?: return null
      val type = getKeyValueType(grandParent) ?: return null

      val descriptor =
        OpenRewriteRecipeService.getInstance(element.project).findDescriptor(grandParent.keyText, myElement.containingFile, type)
        ?: return null
      val keyText = parent.keyText
      return descriptor.options.find { it.name == keyText }
    }
  }
}