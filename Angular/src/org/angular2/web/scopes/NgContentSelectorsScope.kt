// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2CodeInsightUtils
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.NG_DIRECTIVE_ELEMENT_SELECTORS

class NgContentSelectorsScope(tag: XmlTag)
  : WebSymbolsScopeWithCache<XmlTag, Unit>(Angular2Framework.ID, tag.project, tag, Unit) {

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == NG_DIRECTIVE_ATTRIBUTE_SELECTORS

  override fun createPointer(): Pointer<NgContentSelectorsScope> {
    val tag = dataHolder.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { NgContentSelectorsScope(it) }
    }
  }

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    val tag = dataHolder
    val tagName = tag.name
    Angular2CodeInsightUtils.getAvailableNgContentSelectorsSequence(tag, Angular2DeclarationsScope(tag))
      .forEach { selector ->
        val elementSelector = selector.element
        if (elementSelector == null || elementSelector.name.equals(tagName, true)) {
          selector.notSelectors.flatMap { it.attributes }
            .plus(selector.attributes)
            .forEach(consumer)
        }
      }
  }

}