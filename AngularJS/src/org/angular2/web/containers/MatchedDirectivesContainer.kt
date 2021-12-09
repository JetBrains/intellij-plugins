// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.javascript.web.symbols.*
import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2LibrariesHacks
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.web.Angular2DirectiveSymbolWrapper
import org.angular2.web.Angular2Symbol

class MatchedDirectivesContainer(tag: XmlTag)
  : WebSymbolsContainerWithCache<XmlTag, Unit>(Angular2Framework.ID, tag.project, tag, Unit) {

  override fun createPointer(): Pointer<MatchedDirectivesContainer> {
    val tag = dataHolder.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { MatchedDirectivesContainer(it) }
    }
  }

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    Angular2ApplicableDirectivesProvider(dataHolder)
      .matched.forEach { directive ->
        collectSymbols(directive) { symbol ->
          consumer(Angular2DirectiveSymbolWrapper.create(directive, symbol, WebSymbol.Priority.HIGHEST))
        }
      }
  }

  private fun collectSymbols(directive: Angular2Directive, consumer: (Angular2Symbol) -> Unit) {
    if (!directive.directiveKind.isRegular && !isTemplateTag(dataHolder)) {
      return
    }

    directive.inOuts.forEach(consumer)
    directive.inputs.forEach(consumer)
    directive.outputs.forEach(consumer)
    directive.attributes.forEach(consumer)

    Angular2LibrariesHacks.hackIonicComponentAttributeNames(directive).forEach(consumer)
  }

}