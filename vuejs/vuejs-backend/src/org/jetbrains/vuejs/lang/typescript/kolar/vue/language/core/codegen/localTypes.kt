// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine

class LocalTypesGenerator(
  vueCompilerOptions: VueCompilerOptions,
) {
  private val used = linkedSetOf<String>()

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
    "type __VLS_PrettifyLocal<T> = (T extends any ? { [K in keyof T]: T[K]; } : { [K in keyof T as K]: T[K]; }) & {}$endOfLine"
  }

  private val withDefaults = Helper("__VLS_WithDefaults") {
    """
type __VLS_WithDefaults<P, D> = {
	[K in keyof Pick<P, keyof P>]: K extends keyof D
		? ${prettifyLocal.name}<P[K] & { default: D[K] }>
		: P[K]
};
""".trimStart()
  }

  private val withSlots = Helper("__VLS_WithSlots") {
    """
type __VLS_WithSlots<T, S> = T & {
	new(): {
		${'$'}slots: S;
	}
};
""".trimStart()
  }

  private val propsChildren = Helper("__VLS_PropsChildren") {
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
  }

  private val typePropsToOption = Helper("__VLS_TypePropsToOption") {
    """
type __VLS_TypePropsToOption<T> = {
	[K in keyof T]-?: {} extends Pick<T, K>
		? { type: import('${vueCompilerOptions.lib}').PropType<Required<T>[K]> }
		: { type: import('${vueCompilerOptions.lib}').PropType<T[K]>, required: true }
};
""".trimStart()
  }

  private val omitIndexSignature = Helper("__VLS_OmitIndexSignature") {
    "type __VLS_OmitIndexSignature<T> = { [K in keyof T as {} extends Record<K, unknown> ? never : K]: T[K]; }$endOfLine"
  }

  private val helpers: Map<String, Helper> = mapOf(
    prettifyLocal.helperName to prettifyLocal,
    withDefaults.helperName to withDefaults,
    withSlots.helperName to withSlots,
    propsChildren.helperName to propsChildren,
    typePropsToOption.helperName to typePropsToOption,
    omitIndexSignature.helperName to omitIndexSignature,
  )

  val PrettifyLocal: String get() = prettifyLocal.name
  val WithDefaults: String get() = withDefaults.name
  val WithSlots: String get() = withSlots.name
  val PropsChildren: String get() = propsChildren.name
  val TypePropsToOption: String get() = typePropsToOption.name
  val OmitIndexSignature: String get() = omitIndexSignature.name

  fun generate(): Sequence<Code> = sequence {
    while (used.isNotEmpty()) {
      val name = used.first()
      used.remove(name)
      yield(StringSegment(helpers[name]!!.generate()))
    }
  }
}
