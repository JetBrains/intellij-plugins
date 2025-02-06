// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.HTML_ATTRIBUTES
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2LibrariesHacks
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import org.angular2.web.*

private val providedKinds: Set<WebSymbolQualifiedKind> = setOf(
  NG_DIRECTIVE_INPUTS,
  NG_DIRECTIVE_OUTPUTS,
  NG_DIRECTIVE_IN_OUTS,
  NG_DIRECTIVE_ATTRIBUTES,
  NG_DIRECTIVE_EXPORTS_AS,
  HTML_ATTRIBUTES
)

abstract class MatchedDirectivesScope<T : PsiElement>(dataHolder: T)
  : WebSymbolsScopeWithCache<T, Unit>(Angular2Framework.ID, dataHolder.project, dataHolder, Unit) {

  companion object {
    fun createFor(tag: XmlTag): MatchedDirectivesScope<XmlTag> =
      MatchedDirectivesScopeOnTag(tag)

    fun createFor(location: PsiElement, tagName: String): MatchedDirectivesScope<PsiElement> =
      MatchedDirectivesScopeForTagName(location, tagName)
  }

  abstract val isTemplateTagContext: Boolean

  abstract fun matchDirectives(): List<Angular2Directive>

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind in providedKinds

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(dataHolder) {
      matchDirectives().forEach { directive ->
        directive.exportAs.forEach { consumer(it.value) }
        collectSymbols(directive, isTemplateTagContext) { symbol ->
          consumer(Angular2DirectiveSymbolWrapper.create(directive, symbol, dataHolder.containingFile, WebSymbol.Priority.HIGHEST))
        }
      }
    }
  }

  protected fun collectSymbols(directive: Angular2Directive, isTemplateTagContext: Boolean, consumer: (Angular2Symbol) -> Unit) {
    if (!directive.directiveKind.isRegular && !isTemplateTagContext) {
      return
    }

    directive.inOuts.forEach(consumer)
    directive.inputs.forEach(consumer)
    directive.outputs.forEach(consumer)
    directive.attributes.forEach(consumer)

    Angular2LibrariesHacks.hackIonicComponentAttributeNames(directive).forEach(consumer)
  }

  private class MatchedDirectivesScopeOnTag(tag: XmlTag) : MatchedDirectivesScope<XmlTag>(tag) {

    override val isTemplateTagContext: Boolean
      get() = isTemplateTag(dataHolder)

    override fun matchDirectives(): List<Angular2Directive> =
      Angular2ApplicableDirectivesProvider(dataHolder)
        .matched

    override fun createPointer(): Pointer<MatchedDirectivesScopeOnTag> {
      val tag = dataHolder.createSmartPointer()
      return Pointer {
        tag.dereference()?.let { MatchedDirectivesScopeOnTag(it) }
      }
    }
  }

  private class MatchedDirectivesScopeForTagName(location: PsiElement, private val tagName: String) : MatchedDirectivesScope<PsiElement>(location) {
    override val isTemplateTagContext: Boolean
      get() = isTemplateTag(tagName)

    override fun matchDirectives(): List<Angular2Directive> =
      Angular2ApplicableDirectivesProvider(dataHolder.project, dataHolder.containingFile, tagName, false, Angular2DirectiveSimpleSelector(tagName), null)
        .matched

    override fun createPointer(): Pointer<MatchedDirectivesScopeForTagName> {
      val location = dataHolder.createSmartPointer()
      val tagName = this.tagName
      return Pointer {
        location.dereference()?.let { MatchedDirectivesScopeForTagName(it, tagName) }
      }
    }
  }

}