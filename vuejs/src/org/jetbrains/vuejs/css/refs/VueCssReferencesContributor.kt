// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.css.refs

import com.intellij.javascript.web.codeInsight.css.refs.CssClassInJSLiteralOrIdentifierReferenceProvider
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.css.CssElementFactory
import com.intellij.psi.css.CssFunction
import com.intellij.psi.css.CssTerm
import com.intellij.psi.css.CssTermList
import com.intellij.psi.css.impl.CssElementTypes
import com.intellij.psi.css.reference.CssReference
import com.intellij.psi.impl.source.resolve.reference.impl.PsiPolyVariantCachingReference
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.codeInsight.attributes.VueCustomAttributeValueProvider.Companion.isVBindClassAttribute
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression
import org.jetbrains.vuejs.model.VueModelManager

class VueCssReferencesContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    CssClassInJSLiteralOrIdentifierReferenceProvider.register(registrar, VueJSLanguage.INSTANCE,
                                                              VueJSEmbeddedExpression::class.java, ::isVBindClassAttribute)
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(CssElementTypes.CSS_IDENT).withParent(CssTerm::class.java),
                                        VBindIdentifierReferenceProvider())
  }

  private class VBindIdentifierReferenceProvider() : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
      val term = element.parent as? CssTerm ?: return PsiReference.EMPTY_ARRAY
      val termList = term.parent as? CssTermList ?: return PsiReference.EMPTY_ARRAY
      val function = termList.parent as? CssFunction ?: return PsiReference.EMPTY_ARRAY

      return if (function.name == "v-bind")
        arrayOf(VBindIdentifierReference(element))
      else PsiReference.EMPTY_ARRAY
    }

  }

  class VBindIdentifierReference(private val myElement: PsiElement) : PsiPolyVariantCachingReference(), CssReference {

    override fun getElement(): PsiElement = myElement

    override fun getRangeInElement(): TextRange = TextRange(0, myElement.textLength)

    override fun resolveInner(incompleteCode: Boolean, containingFile: PsiFile): Array<ResolveResult> =
      VueModelManager.findEnclosingContainer(myElement)
        .thisType
        .asRecordType()
        .findPropertySignature(canonicalText)
        ?.memberSource
        ?.allSourceElements
        ?.map { PsiElementResolveResult(it) }
        ?.toTypedArray()
      ?: ResolveResult.EMPTY_ARRAY

    override fun getVariants(): Array<Any> =
      VueModelManager.findEnclosingContainer(myElement)
        .thisType
        .asRecordType()
        .properties
        .mapNotNull { property ->
          property
            ?.takeIf { !it.memberName.startsWith("$") }
            ?.memberSource
            ?.singleElement
            ?.takeIf { it !is JSFunctionItem }
            ?.let { JSLookupUtilImpl.createLookupElement(it, property.memberName) }
        }
        .toTypedArray()

    override fun getCanonicalText(): String = myElement.text

    override fun handleElementRename(newElementName: String): PsiElement? =
      CssElementFactory.getInstance(myElement.project)
        .createTerm(newElementName, myElement.language)
        .firstChild
        ?.takeIf { it.node.elementType == CssElementTypes.CSS_IDENT }
        ?.let { myElement.replace(it) }

    override fun bindToElement(element: PsiElement): PsiElement? = null

    override fun isReferenceTo(element: PsiElement): Boolean {
      val results = multiResolve(false)
      for (result in results) {
        if (getElement().manager.areElementsEquivalent(element, result.element)) return true
      }
      return false
    }

    override fun getUnresolvedMessagePattern(): String {
      return ""
    }

  }

}
