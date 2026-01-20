// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.patterns.ComplexPatternOptions
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createCompletionAutoPopup
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createComplexPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createPatternSequence
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createStringMatch
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createSymbolReferencePlaceholder
import com.intellij.polySymbols.patterns.PolySymbolPatternReferenceResolver
import com.intellij.polySymbols.patterns.PolySymbolPatternReferenceResolver.Reference
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueSymbol
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.web.VUE_COMPONENT_COMPUTED_PROPERTIES
import org.jetbrains.vuejs.web.VUE_COMPONENT_DATA_PROPERTIES
import org.jetbrains.vuejs.web.symbols.VueAnySymbol

class VueWatchSymbolScope(private val enclosingComponent: VueSourceComponent) :
  PolySymbolScopeWithCache<VueSourceComponent, Unit>(enclosingComponent.source.project, enclosingComponent, Unit) {

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES
    || kind == VUE_COMPONENT_DATA_PROPERTIES
    || kind == VUE_COMPONENT_COMPUTED_PROPERTIES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    enclosingComponent.acceptPropertiesAndMethods(object : VueModelVisitor() {
      override fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
        consumer(dataProperty)
        return true
      }

      override fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
        consumer(computedProperty)
        return true
      }
    }, onlyPublic = false)
    if (!DialectDetector.isTypeScript(enclosingComponent.source)) {
      consumer(anyJsDataSymbol)
    }
    consumer(VueWatchablePropertySymbol)
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun createPointer(): Pointer<VueWatchSymbolScope> {
    val componentPointer = enclosingComponent.createPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueWatchSymbolScope(it) }
    }
  }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> =
    super.getCodeCompletions(qualifiedName, params, stack)
      .let { codeCompletions ->
        if (qualifiedName.matches(VUE_COMPONENT_COMPUTED_PROPERTIES, VUE_COMPONENT_DATA_PROPERTIES))
          codeCompletions.filter { !it.name.startsWith("$") && it.name.length > 1 }
        else
          codeCompletions
      }


  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(enclosingComponent.source.project).modificationCount

  companion object {

    private val anyJsDataSymbol = VueAnySymbol(
      VUE_COMPONENT_DATA_PROPERTIES,
      "Unknown data property",
      JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.JS)
    )

  }

  object VueWatchablePropertySymbol : PolySymbolWithPattern, VueSymbol {

    override val kind: PolySymbolKind
      get() = JS_PROPERTIES

    override val name: String get() = "Vue Watchable Property"

    override val pattern: PolySymbolPattern =
      createComplexPattern(
        ComplexPatternOptions(symbolsResolver = PolySymbolPatternReferenceResolver(
          Reference(kind = VUE_COMPONENT_DATA_PROPERTIES),
          Reference(kind = VUE_COMPONENT_COMPUTED_PROPERTIES))
        ), false,
        createPatternSequence(
          createSymbolReferencePlaceholder(),
          createComplexPattern(
            ComplexPatternOptions(repeats = true, isRequired = false, symbolsResolver = PolySymbolPatternReferenceResolver(
              Reference(kind = JS_PROPERTIES)
            )), false,
            createPatternSequence(
              createStringMatch("."),
              createCompletionAutoPopup(false),
              createSymbolReferencePlaceholder()
            )
          )
        )
      )

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)
  }

}