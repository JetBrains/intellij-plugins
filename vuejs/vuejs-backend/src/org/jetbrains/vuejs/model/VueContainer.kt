// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSParameterTypeDecorator
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.symbols.getMatchingJSPropertySymbols
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.jetbrains.vuejs.context.isVue3
import org.jetbrains.vuejs.model.source.MODEL_VALUE_PROP
import org.jetbrains.vuejs.web.*
import org.jetbrains.vuejs.web.symbols.VueSourceElementSymbol
import org.jetbrains.vuejs.web.symbols.VueSymbol

const val EMIT_CALL_UPDATE_PREFIX: String = "update:"

interface VueContainer : VueEntitiesContainer {
  val data: List<VueDataProperty>
  val computed: List<VueComputedProperty>
  val methods: List<VueMethod>
  val props: List<VueInputProperty>
  val emits: List<VueEmitCall>
  val slots: List<VueSlot>
  val provides: List<VueProvide>
  val injects: List<VueInject>

  val template: VueTemplate<*>? get() = null
  val element: String? get() = null
  val extends: List<VueContainer>
  val delimiters: Pair<String, String>? get() = null
  val model: VueModelDirectiveProperties?
}

data class VueModelDirectiveProperties(
  val prop: String? = null,
  val event: String? = null,
) : VueSymbol {

  override val name: String
    get() = "Vue Model"

  override val kind: PolySymbolKind
    get() = VUE_MODEL

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_VUE_MODEL_PROP -> prop as T?
      PROP_VUE_MODEL_EVENT -> event as T?
      else -> null
    }

  override fun createPointer(): Pointer<VueModelDirectiveProperties> =
    Pointer.hardPointer(this)

  companion object {

    private val DEFAULT_V2 = VueModelDirectiveProperties("value", "input")
    private val DEFAULT_V3 = VueModelDirectiveProperties(MODEL_VALUE_PROP, "$EMIT_CALL_UPDATE_PREFIX$MODEL_VALUE_PROP")

    fun getDefault(context: PsiElement): VueModelDirectiveProperties =
      if (isVue3(context))
        DEFAULT_V3
      else
        DEFAULT_V2
  }
}

interface VueNamedSymbol : VueSourceElementSymbol {

  abstract override fun createPointer(): Pointer<out VueNamedSymbol>
}

interface VueSlot : VueNamedSymbol, PolySymbolScope {

  override val kind: PolySymbolKind
    get() = HTML_SLOTS

  override val searchTarget: PolySymbolSearchTarget?
    get() = null

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> {
    return getJSPropertySymbols(kind)
  }

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> {
    return getMatchingJSPropertySymbols(qualifiedName, params.queryExecutor.namesProvider)
  }

  override fun createPointer(): Pointer<out VueSlot>

  override fun getModificationCount(): Long = -1
}

@JvmDefaultWithCompatibility
interface VueEmitCall : VueNamedSymbol {
  /**
   * Event parameters not including event type itself, e.g.
   * for `{(event: 'add', item: string): void}` contains only `item: string`.
   */
  val params: List<JSParameterTypeDecorator> get() = emptyList()

  /**
   * Is needed to distinguish between `['add']` and `{(event: 'add'): void}`.
   */
  val hasStrictSignature: Boolean get() = false

  override val kind: PolySymbolKind
    get() = JS_EVENTS

  override val type: JSType?
    get() = handlerSignature

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.HIGHEST

  override val attributeValue: PolySymbolHtmlAttributeValue
    get() =
      PolySymbolHtmlAttributeValue.create(PolySymbolHtmlAttributeValue.Kind.EXPRESSION, PolySymbolHtmlAttributeValue.Type.OF_MATCH)

  override fun adjustNameForRefactoring(
    queryExecutor: PolySymbolQueryExecutor,
    oldName: PolySymbolQualifiedName,
    newName: String,
    occurence: String,
  ): String {
    if (this is VueModelOwner && occurence.startsWith(EMIT_CALL_UPDATE_PREFIX) && !newName.startsWith(EMIT_CALL_UPDATE_PREFIX)) {
      return "$EMIT_CALL_UPDATE_PREFIX$newName"
    }
    return super.adjustNameForRefactoring(queryExecutor, oldName, newName, occurence)
  }

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  override fun createPointer(): Pointer<out VueEmitCall>
}

/**
 * Any property on the Vue instance, i.e. on `this` within Vue template
 *
 * @see VueInputProperty
 */
interface VueProperty : VueNamedSymbol, PolySymbolScope {

  override val type: JSType?

  abstract override fun createPointer(): Pointer<out VueProperty>

  override fun getModificationCount(): Long = -1

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    getMatchingJSPropertySymbols(qualifiedName, params.queryExecutor.namesProvider)

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    getJSPropertySymbols(kind)

}

/**
 * Base interface for Vue component prop
 */
interface VueInputProperty : VueProperty {
  val required: Boolean

  val defaultValue: String? get() = null

  override val kind: PolySymbolKind
    get() = VUE_COMPONENT_PROPS

  override val modifiers: Set<PolySymbolModifier>
    get() = when (required) {
      true -> setOf(PolySymbolModifier.REQUIRED)
      false -> setOf(PolySymbolModifier.OPTIONAL)
    }

  override val attributeValue: PolySymbolHtmlAttributeValue
    get() =
      object : PolySymbolHtmlAttributeValue {
        override val default: String?
          get() = defaultValue
      }

  override fun createPointer(): Pointer<out VueInputProperty>

}

interface VueDataProperty : VueProperty {

  override val kind: PolySymbolKind
    get() = VUE_COMPONENT_DATA_PROPERTIES

  override fun createPointer(): Pointer<out VueDataProperty>

}

interface VueComputedProperty : VueProperty {

  override val kind: PolySymbolKind
    get() = VUE_COMPONENT_COMPUTED_PROPERTIES

  override fun createPointer(): Pointer<out VueComputedProperty>
}

interface VueMethod : VueProperty {

  override val kind: PolySymbolKind
    get() = VUE_METHODS

  override fun createPointer(): Pointer<out VueMethod>
}

interface VueModelOwner {
  val modelDecl: VueModelDecl
}

interface VueModelDecl : VueNamedSymbol {

  override val kind: PolySymbolKind
    get() = VUE_MODEL_DECL

  /**
   * Type of a property outside of a component, e.g. a component attribute's type.
   */
  override val type: JSType? get() = null

  /**
   * This type is typically referenced from within a component when the passed property is used, in contrast to [type].
   * For example, it could consider default values that eliminate "undefined" from a type declaration.
   */
  val referenceType: JSType? get() = type

  val required: Boolean

  val defaultValue: String? get() = null

  val local: Boolean? get() = null

  override fun createPointer(): Pointer<out VueModelDecl>
}

interface VueProvide : VueNamedSymbol {
  val injectionKey: PsiNamedElement? get() = null

  override val kind: PolySymbolKind
    get() = VUE_PROVIDES

  abstract override fun createPointer(): Pointer<out VueProvide>
}

interface VueInject : VueNamedSymbol {
  val injectionKey: PsiNamedElement? get() = null

  val from: String? get() = null

  val defaultValue: JSType? get() = null

  override val kind: PolySymbolKind
    get() = VUE_INJECTS

  abstract override fun createPointer(): Pointer<out VueInject>
}
