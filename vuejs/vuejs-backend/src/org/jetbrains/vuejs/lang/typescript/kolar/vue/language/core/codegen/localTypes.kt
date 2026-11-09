// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.isTsLang
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

class LocalTypesGenerator(
  vueCompilerOptions: VueCompilerOptions,
  lang: String,
) {
  private val used = linkedSetOf<String>()
  private val isTs = isTsLang(lang)

  private inner class Helper(
    val helperName: String,
    private val helperGenerate: () -> String,
  ) {
    val name: String
      get() {
        used.add(helperName)
        return helperName
      }

    fun generate(): String = helperGenerate()
  }

  private val prettifyLocal = Helper("__VLS_PrettifyLocal") {
    if (isTs)
      "type __VLS_PrettifyLocal<T> = (T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {}$endOfLine"
    else
      jsTypedef(
        "T",
        "__VLS_PrettifyLocal",
        "(T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {}",
      )
  }

  private val withDefaults = Helper("__VLS_WithDefaults") {
    if (isTs)
      """
type __VLS_WithDefaults<P, D> = {
	[K in keyof Pick<P, keyof P>]: K extends keyof D
		? ${prettifyLocal.name}<P[K] & { default: D[K] }>
		: P[K]
};
""".trimStart()
    else
      jsTypedef(
        "P, D",
        "__VLS_WithDefaults",
        "{ [K in keyof Pick<P, keyof P>]: K extends keyof D ? ${prettifyLocal.name}<P[K] & { default: D[K] }> : P[K] }",
      )
  }

  private val withSlots = Helper("__VLS_WithSlots") {
    if (isTs)
      """
type __VLS_WithSlots<T, S> = T & {
	new(): {
		${'$'}slots: S;
	}
};
""".trimStart()
    else
      jsTypedef("T, S", "__VLS_WithSlots", "T & { new(): { ${'$'}slots: S; } }")
  }

  private val propsChildren = Helper("__VLS_PropsChildren") {
    if (isTs)
      """
type __VLS_PropsChildren<S> = {
	[K in keyof (
		boolean extends (
			// @ts-ignore
			JSX.ElementChildrenAttribute extends never
				? true
				: false
		)
			? never
			// @ts-ignore
			: JSX.ElementChildrenAttribute
	)]?: S;
};
""".trimStart()
    // The single-line form lets the preceding @ts-ignore cover the whole typedef,
    // mirroring the inline @ts-ignore guards of the TS form.
    else
      "// @ts-ignore$newLine/** @template S @typedef {{ [K in keyof (boolean extends (JSX.ElementChildrenAttribute extends never ? true : false) ? never : JSX.ElementChildrenAttribute)]?: S; }} __VLS_PropsChildren */$newLine"
  }

  private val typePropsToOption = Helper("__VLS_TypePropsToOption") {
    if (isTs)
      """
type __VLS_TypePropsToOption<T> = {
	[K in keyof T]-?: {} extends Pick<T, K>
		? { type: import('${vueCompilerOptions.lib}').PropType<Required<T>[K]> }
		: { type: import('${vueCompilerOptions.lib}').PropType<T[K]>, required: true }
};
""".trimStart()
    else
      jsTypedef(
        "T",
        "__VLS_TypePropsToOption",
        "{ [K in keyof T]-?: {} extends Pick<T, K> ? { type: import('${vueCompilerOptions.lib}').PropType<Required<T>[K]> } : { type: import('${vueCompilerOptions.lib}').PropType<T[K]>, required: true } }",
      )
  }

  private val omitIndexSignature = Helper("__VLS_OmitIndexSignature") {
    if (isTs)
      "type __VLS_OmitIndexSignature<T> = { [K in keyof T as {} extends Record<K, unknown> ? never : K]: T[K]; }$endOfLine"
    else
      jsTypedef(
        "T",
        "__VLS_OmitIndexSignature",
        "{ [K in keyof T as {} extends Record<K, unknown> ? never : K]: T[K] }",
      )
  }

  private val helpers: Map<String, Helper> =
    sequenceOf(
      prettifyLocal,
      withDefaults,
      withSlots,
      propsChildren,
      typePropsToOption,
      omitIndexSignature,
    ).associateBy { it.helperName }

  val PrettifyLocal: String get() = prettifyLocal.name
  val WithDefaults: String get() = withDefaults.name
  val WithSlots: String get() = withSlots.name
  val PropsChildren: String get() = propsChildren.name
  val TypePropsToOption: String get() = typePropsToOption.name
  val OmitIndexSignature: String get() = omitIndexSignature.name

  fun generate(): Sequence<Code> = sequence {
    for (name in used) {
      yield(helpers.getValue(name).generate())
    }

    used.clear()
  }

  private fun jsTypedef(params: String, name: String, body: String): String =
    "/**$newLine * @template $params$newLine * @typedef {$body} $name$newLine */$newLine"
}
