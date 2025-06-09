// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.javascript.polySymbols.symbols.asPolySymbol
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.*
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.query.PolySymbolsQueryExecutorFactory
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolsScopeWithCache
import com.intellij.polySymbols.utils.PsiSourcedPolySymbolDelegate
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.Processor
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.template.VueTemplateScopesResolver
import org.jetbrains.vuejs.web.VUE_BINDING_SHORTHANDS

class VueBindingShorthandScope(attribute: XmlAttribute)
  : PolySymbolsScopeWithCache<XmlAttribute, Unit>(null, attribute.project, attribute, Unit) {

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == VUE_BINDING_SHORTHANDS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val tag = dataHolder.context as? XmlTag ?: return
    VueAttributeNameParser.parse(dataHolder.name, tag)
      .asSafely<VueAttributeNameParser.VueDirectiveInfo>()
      ?.takeIf { it.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND } ?: return

    val executor = PolySymbolsQueryExecutorFactory.create(dataHolder)
    val attributes = executor
      .runListSymbolsQuery(HTML_ATTRIBUTES, virtualSymbols = false, expandPatterns = true,
                           additionalScope = executor.runNameMatchQuery(HTML_ELEMENTS, tag.name))
      .associateBy { it.name }

    VueTemplateScopesResolver.resolve(tag, Processor { resolveResult ->
      val jsSymbol = resolveResult.element.asSafely<JSElement>()?.asPolySymbol() as? PsiSourcedPolySymbol
      if (jsSymbol != null) {
        attributes[fromAsset(jsSymbol.name)]?.let {
          consumer(VueBindingShorthandSymbol(dataHolder, jsSymbol, it))
        }
      }
      true
    })
  }

  override fun createPointer(): Pointer<VueBindingShorthandScope> {
    val pointer = dataHolder.createSmartPointer()
    return Pointer {
      pointer.dereference()?.let { VueBindingShorthandScope(it) }
    }
  }
}

class VueBindingShorthandSymbol(
  private val context: XmlAttribute,
  override val delegate: PsiSourcedPolySymbol,
  private val attrSymbol: PolySymbol,
) : PsiSourcedPolySymbolDelegate<PsiSourcedPolySymbol>,
    CompositePolySymbol {

  override val nameSegments: List<PolySymbolNameSegment>
    get() = listOf(PolySymbolNameSegment.create(0, delegate.name.length, delegate, attrSymbol))

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_BINDING_SHORTHANDS

  override val attributeValue: PolySymbolHtmlAttributeValue
    get() = PolySymbolHtmlAttributeValue.create(kind = PolySymbolHtmlAttributeValue.Kind.NO_VALUE)

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.HIGHEST

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    attrSymbol.getDocumentationTarget(location)

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    super<PsiSourcedPolySymbolDelegate>.getNavigationTargets(project) + attrSymbol.getNavigationTargets(project)

  override fun createPointer(): Pointer<out PsiSourcedPolySymbol> {
    val contextPtr = context.createSmartPointer()
    val psiSourcedSymbolPtr = delegate.createPointer()
    val attrSymbolPtr = attrSymbol.createPointer()
    return Pointer {
      val context = contextPtr.dereference() ?: return@Pointer null
      val psiSourcedSymbol = psiSourcedSymbolPtr.dereference() ?: return@Pointer null
      val attrSymbol = attrSymbolPtr.dereference() ?: return@Pointer null
      VueBindingShorthandSymbol(context, psiSourcedSymbol, attrSymbol)
    }
  }
}
