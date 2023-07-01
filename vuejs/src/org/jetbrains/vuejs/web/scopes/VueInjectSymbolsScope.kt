// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_PROPERTIES
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_STRING_LITERALS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createComplexPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createPatternSequence
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createSymbolReferencePlaceholder
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver.Reference
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import org.jetbrains.vuejs.model.provides
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_PROVIDES
import org.jetbrains.vuejs.web.symbols.VueProvideSymbol
import org.jetbrains.vuejs.web.symbols.VueScopeElementOrigin

class VueInjectSymbolsScope(private val enclosingComponent: VueSourceComponent)
  : WebSymbolsScopeWithCache<VueSourceComponent, Unit>(VueFramework.ID, enclosingComponent.source.project, enclosingComponent, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val origin = VueScopeElementOrigin(enclosingComponent)
    val provides = enclosingComponent.global.provides

    provides.forEach { (provide, container) ->
      consumer(VueProvideSymbol(provide, container, origin))
    }

    consumer(VueInjectStringSymbol)
    consumer(VueInjectPropertySymbol)

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun getCodeCompletions(namespace: SymbolNamespace,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> {
    return super.getCodeCompletions(namespace, kind, name, params, scope).filter {
      it.symbol.asSafely<VueProvideSymbol>()?.injectionKey == null
    }
  }

  override fun createPointer(): Pointer<VueInjectSymbolsScope> {
    val componentPointer = enclosingComponent.createPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueInjectSymbolsScope(it) }
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(enclosingComponent.source.project).modificationCount

  private abstract class VueInjectSymbol : WebSymbol {

    override val namespace: SymbolNamespace
      get() = NAMESPACE_JS

    override val pattern: WebSymbolsPattern =
      createComplexPattern(
        ComplexPatternOptions(symbolsResolver = WebSymbolsPatternReferenceResolver(
          Reference(qualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, KIND_VUE_PROVIDES))
        )), false,
        createPatternSequence(
          createSymbolReferencePlaceholder(),
        )
      )

    override val origin: WebSymbolOrigin = object : WebSymbolOrigin {
      override val framework: FrameworkId
        get() = VueFramework.ID
    }

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)
  }

  private object VueInjectStringSymbol : VueInjectSymbol() {
    override val kind: SymbolKind
      get() = KIND_JS_STRING_LITERALS

    override val name: String
      get() = "Vue Inject String"
  }

  private object VueInjectPropertySymbol : VueInjectSymbol() {

    override val kind: SymbolKind
      get() = KIND_JS_PROPERTIES

    override val name: String
      get() = "Vue Inject Property"
  }

}