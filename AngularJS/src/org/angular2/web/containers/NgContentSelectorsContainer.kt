// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsContainerWithCache
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2CodeInsightUtils
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.web.Angular2WebSymbolsRegistryExtension

class NgContentSelectorsContainer(tag: XmlTag)
  : WebSymbolsContainerWithCache<XmlTag, Unit>(Angular2Framework.ID, tag.project, tag, Unit) {

  override fun provides(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
    namespace == WebSymbol.NAMESPACE_JS
    && (kind == Angular2WebSymbolsRegistryExtension.KIND_NG_DIRECTIVE_ELEMENT_SELECTORS || kind == Angular2WebSymbolsRegistryExtension.KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS)

  override fun createPointer(): Pointer<NgContentSelectorsContainer> {
    val tag = dataHolder.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { NgContentSelectorsContainer(it) }
    }
  }

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val tag = dataHolder
    val tagName = tag.name
    Angular2CodeInsightUtils.getAvailableNgContentSelectorsStream(tag, Angular2DeclarationsScope(tag))
      .forEach { selector ->
        val elementSelector = selector.element
        elementSelector?.let(consumer)
        if (elementSelector == null || elementSelector.name.equals(tagName, true)) {
          selector.notSelectors.flatMap { it.attributes }
            .plus(selector.attributes)
            .forEach(consumer)
        }
      }
  }

}