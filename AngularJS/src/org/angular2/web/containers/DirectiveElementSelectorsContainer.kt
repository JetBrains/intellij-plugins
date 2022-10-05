// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsContainerWithCache
import org.angular2.Angular2Framework
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.web.Angular2DirectiveSymbolWrapper
import org.angular2.web.Angular2WebSymbolsRegistryExtension.Companion.KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.Angular2WebSymbolsRegistryExtension.Companion.KIND_NG_DIRECTIVE_ELEMENT_SELECTORS

internal class DirectiveElementSelectorsContainer(project: Project)
  : WebSymbolsContainerWithCache<Project, Unit>(Angular2Framework.ID, project, project, Unit) {

  override fun provides(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
    namespace == WebSymbol.NAMESPACE_JS
    && (kind == KIND_NG_DIRECTIVE_ELEMENT_SELECTORS || kind == KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS)

  override fun createPointer(): Pointer<DirectiveElementSelectorsContainer> =
    Pointer.hardPointer(this)

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    Angular2EntitiesProvider.getAllElementDirectives(project)
      .asSequence()
      .filter { (name, list) -> list.isNotEmpty() && name.isNotEmpty() }
      .forEach { (name, list) ->
        list.forEach { directive ->
          consumer(Angular2DirectiveSymbolWrapper.create(directive, directive.selector.getSymbolForElement(name)))
        }
      }
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

}