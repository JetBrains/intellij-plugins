// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_PROPERTIES
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createCompletionAutoPopup
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createComplexPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createPatternSequence
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createStringMatch
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory.createSymbolReferencePlaceholder
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver
import com.intellij.webSymbols.patterns.WebSymbolsPatternReferenceResolver.Reference
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_COMPONENT_COMPUTED_PROPERTIES
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_COMPONENT_DATA_PROPERTIES
import org.jetbrains.vuejs.web.symbols.VueComputedPropertySymbol
import org.jetbrains.vuejs.web.symbols.VueDataPropertySymbol
import org.jetbrains.vuejs.web.symbols.VueScopeElementOrigin

class VueWatchSymbolsScope(private val enclosingComponent: VueSourceComponent)
  : WebSymbolsScopeWithCache<VueSourceComponent, Unit>(VueFramework.ID, enclosingComponent.source.project, enclosingComponent, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val origin = VueScopeElementOrigin(enclosingComponent)
    enclosingComponent.acceptPropertiesAndMethods(object : VueModelVisitor() {
      override fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
        consumer(VueDataPropertySymbol(dataProperty, enclosingComponent, origin))
        return true
      }

      override fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
        consumer(VueComputedPropertySymbol(computedProperty, enclosingComponent, origin))
        return true
      }
    }, onlyPublic = false)
    consumer(VueWatchablePropertySymbol)
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun createPointer(): Pointer<VueWatchSymbolsScope> {
    val componentPointer = enclosingComponent.createPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueWatchSymbolsScope(it) }
    }
  }

  override fun getCodeCompletions(namespace: SymbolNamespace,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
    super.getCodeCompletions(namespace, kind, name, params, scope)
      .let { codeCompletions ->
        if (namespace == NAMESPACE_HTML && (kind == KIND_VUE_COMPONENT_COMPUTED_PROPERTIES || kind == KIND_VUE_COMPONENT_DATA_PROPERTIES))
          codeCompletions.filter { !it.name.startsWith("$") && it.name.length > 1 }
        else
          codeCompletions
      }



  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(enclosingComponent.source.project).modificationCount

  object VueWatchablePropertySymbol : WebSymbol {

    override val namespace: SymbolNamespace get() = NAMESPACE_JS

    override val kind: SymbolKind get() = KIND_JS_PROPERTIES

    override val name: String get() = "Vue Watchable Property"

    override val pattern: WebSymbolsPattern =
      createComplexPattern(
        ComplexPatternOptions(symbolsResolver = WebSymbolsPatternReferenceResolver(
          Reference(qualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, KIND_VUE_COMPONENT_DATA_PROPERTIES)),
          Reference(qualifiedKind = WebSymbolQualifiedKind(NAMESPACE_HTML, KIND_VUE_COMPONENT_COMPUTED_PROPERTIES)))
        ), false,
        createPatternSequence(
          createSymbolReferencePlaceholder(),
          createComplexPattern(
            ComplexPatternOptions(repeats = true, isRequired = false, symbolsResolver = WebSymbolsPatternReferenceResolver(
              Reference(qualifiedKind = WebSymbolQualifiedKind(NAMESPACE_JS, KIND_JS_PROPERTIES))
            )), false,
            createPatternSequence(
              createStringMatch("."),
              createCompletionAutoPopup(false),
              createSymbolReferencePlaceholder()
            )
          )
        )
      )

    override val origin: WebSymbolOrigin = object : WebSymbolOrigin {
      override val framework: FrameworkId
        get() = VueFramework.ID
    }

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)
  }

}