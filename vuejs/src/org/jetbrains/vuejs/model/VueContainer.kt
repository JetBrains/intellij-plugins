// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.context.VUE_3_0_0
import org.jetbrains.vuejs.context.detectVueVersion

interface VueContainer : VueEntitiesContainer {
  val data: List<VueDataProperty>
  val computed: List<VueComputedProperty>
  val methods: List<VueMethod>
  val props: List<VueInputProperty>
  val emits: List<VueEmitCall>
  val slots: List<VueSlot>

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
    private val DEFAULT_V3 = VueModelDirectiveProperties("modelValue", "update:modelValue")

    fun getDefault(version: SemVer?): VueModelDirectiveProperties =
      if (version == null || version >= VUE_3_0_0)
        DEFAULT_V3
      else
        DEFAULT_V2

    fun getDefault(context: PsiElement): VueModelDirectiveProperties =
      getDefault(detectVueVersion(context))
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

interface VueEmitCall : VueNamedSymbol {
  val eventJSType: JSType? get() = null
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
