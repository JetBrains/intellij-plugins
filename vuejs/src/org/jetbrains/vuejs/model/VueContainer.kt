// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSParameterTypeDecorator
import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.context.isVue3
import org.jetbrains.vuejs.model.source.MODEL_VALUE_PROP

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
  val event: String? = null
) {
  companion object {

    private val DEFAULT_V2 = VueModelDirectiveProperties("value", "input")
    private val DEFAULT_V3 = VueModelDirectiveProperties(MODEL_VALUE_PROP, "update:$MODEL_VALUE_PROP")

    fun getDefault(context: PsiElement): VueModelDirectiveProperties =
      if (isVue3(context))
        DEFAULT_V3
      else
        DEFAULT_V2
  }
}

interface VueNamedSymbol : VueDocumentedItem {
  val name: String

  override val source: PsiElement?
    get() = null
}

interface VueSlot : VueNamedSymbol {
  val scope: JSType? get() = null
  val pattern: String? get() = null
}

@JvmDefaultWithCompatibility
interface VueEmitCall : VueNamedSymbol {
  /**
   * A type of event handler.
   */
  val eventJSType: JSType? get() = handlerSignature

  /**
   * Event parameters not including event type itself, e.g.
   * for `{(event: 'add', item: string): void}` contains only `item: string`.
   */
  val params: List<JSParameterTypeDecorator> get() = emptyList()

  /**
   * Is needed to distinguish between `['add']` and `{(event: 'add'): void}`.
   */
  val hasStrictSignature: Boolean get() = false
}

/**
 * Any property on the Vue instance, i.e. on `this` within Vue template
 *
 * @see VueInputProperty
 */
interface VueProperty : VueNamedSymbol {
  val jsType: JSType? get() = null
}

/**
 * Base interface for Vue component prop
 */
interface VueInputProperty : VueProperty {
  val required: Boolean
  val defaultValue: String? get() = null
}

interface VueDataProperty : VueProperty

interface VueComputedProperty : VueProperty

interface VueMethod : VueProperty

interface VueModelOwner {
  val modelDecl: VueModelDecl
}

interface VueModelDecl : VueNamedSymbol {
  /**
   * Type of a property outside of a component, e.g. a component attribute's type.
   */
  val jsType: JSType? get() = null

  /**
   * This type is typically referenced from within a component when the passed property is used, in contrast to [jsType].
   * For example, it could consider default values that eliminate "undefined" from a type declaration.
   */
  val referenceType: JSType? get() = jsType

  val required: Boolean

  val defaultValue: String? get() = null

  val local: Boolean? get() = null
}

interface VueProvide : VueNamedSymbol {
  val symbol: PsiNamedElement? get() = null

  val jsType: JSType? get() = null
}

interface VueInject : VueNamedSymbol {
  val symbol: PsiNamedElement? get() = null

  val from: String? get() = null

  val defaultValue: JSType? get() = null
}
