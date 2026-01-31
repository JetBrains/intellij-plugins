// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.CompositePolySymbol
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolNameSegment
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.js.symbols.asJSSymbol
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
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

class VueBindingShorthandScope(attribute: XmlAttribute) :
  PolySymbolScopeWithCache<XmlAttribute, Unit>(attribute.project, attribute, Unit) {

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == VUE_BINDING_SHORTHANDS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val tag = dataHolder.context as? XmlTag ?: return
    VueAttributeNameParser.parse(dataHolder.name, tag)
      .asSafely<VueAttributeNameParser.VueDirectiveInfo>()
      ?.takeIf { it.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND } ?: return

    val executor = PolySymbolQueryExecutorFactory.create(dataHolder)
    val attributes = executor
      .listSymbolsQuery(HTML_ATTRIBUTES, expandPatterns = true)
      .exclude(PolySymbolModifier.ABSTRACT, PolySymbolModifier.VIRTUAL)
      .additionalScope(
        executor.nameMatchQuery(HTML_ELEMENTS, tag.name)
          .exclude(PolySymbolModifier.ABSTRACT)
          .run()
          .flatMap { it.queryScope }
      )
      .run()
      .associateBy { it.name }

    VueTemplateScopesResolver.resolve(tag, Processor { resolveResult ->
      val jsSymbol = resolveResult.element.asSafely<JSElement>()?.asJSSymbol() as? PsiSourcedPolySymbol
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

data class VueBindingShorthandSymbol(
  private val context: XmlAttribute,
  override val delegate: PsiSourcedPolySymbol,
  private val attrSymbol: PolySymbol,
) : PsiSourcedPolySymbolDelegate<PsiSourcedPolySymbol>,
    CompositePolySymbol {

  override val nameSegments: List<PolySymbolNameSegment>
    get() = listOf(PolySymbolNameSegment.create(0, delegate.name.length, delegate, attrSymbol))

  override val kind: PolySymbolKind
    get() = VUE_BINDING_SHORTHANDS

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(PolySymbolHtmlAttributeValue.create(kind = PolySymbolHtmlAttributeValue.Kind.NO_VALUE))
      else -> super<CompositePolySymbol>.get(property)
    }

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.HIGHEST

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    attrSymbol.getDocumentationTarget(location)

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    super<PsiSourcedPolySymbolDelegate>.getNavigationTargets(project) + attrSymbol.getNavigationTargets(project)

  override fun createPointer(): Pointer<out VueBindingShorthandSymbol> {
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
