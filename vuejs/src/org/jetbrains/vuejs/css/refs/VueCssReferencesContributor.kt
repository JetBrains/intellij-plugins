// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.css.refs

import com.intellij.javascript.web.css.CssClassInJSLiteralOrIdentifierReferenceProvider
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSTypeOwner
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
import org.apache.commons.lang.StringUtils
import org.jetbrains.vuejs.codeInsight.attributes.VueCustomAttributeValueProvider.Companion.isVBindClassAttribute
import org.jetbrains.vuejs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent

class VueCssReferencesContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    CssClassInJSLiteralOrIdentifierReferenceProvider.register(registrar, VueJSLanguage.INSTANCE,
                                                              VueJSEmbeddedExpressionContent::class.java, ::isVBindClassAttribute)
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(CssElementTypes.CSS_IDENT).withParent(CssTerm::class.java),
                                        VBindIdentifierReferenceProvider())
  }

  private class VBindIdentifierReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
      val term = element.parent as? CssTerm ?: return PsiReference.EMPTY_ARRAY
      var termList = term.parent as? CssTermList ?: return PsiReference.EMPTY_ARRAY
      (termList.parent as? CssTermList)?.let { parent ->
        termList = parent // in SCSS CssTermList exists twice in AST
      }
      val function = termList.parent as? CssFunction ?: return PsiReference.EMPTY_ARRAY

      return if (function.name == "v-bind")
        arrayOf(VBindIdentifierReference(element))
      else PsiReference.EMPTY_ARRAY
    }

  }

  class VBindIdentifierReference(private val myElement: PsiElement) : PsiPolyVariantCachingReference(), CssReference {

    override fun getElement(): PsiElement = myElement

    override fun getRangeInElement(): TextRange = TextRange(0, myElement.textLength)

    override fun resolveInner(incompleteCode: Boolean, containingFile: PsiFile): Array<ResolveResult> {
      val result = mutableListOf<ResolveResult>()
      val name = canonicalText
      VueTemplateScopesResolver.resolve(myElement) {
        val element = it.element as? JSPsiNamedElementBase
        if (element != null && name == StringUtils.uncapitalize(element.name)) {
          result.add(it)
          return@resolve false
        }
        true
      }
      return result.toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val result = mutableListOf<Any>()
        VueTemplateScopesResolver.resolve(myElement) { resolveResult ->
          (resolveResult.element as? JSPsiNamedElementBase)
            .takeIf { it !is JSFunctionItem
                      && it?.name?.startsWith("$") == false
                      && (it as? JSTypeOwner)?.jsType?.substitute() !is JSFunctionType}
            ?.let {
              result.add(JSLookupUtilImpl.createLookupElement(it, it.name!!))
            }
          true
        }
        return result.toTypedArray()
    }

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
