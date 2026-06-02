// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.config

import kotlinx.serialization.Serializable

/**
 * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options)
 */
@Serializable
data class VueCompilerOptions(

  /**
   * Default: Automatically detected from the installed vue version, otherwise 3.3.
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#target)
   */
  val target: Double = 99.0,

  /**
   * Default: "vue"
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#lib)
   */
  val lib: String = "vue",

  /**
   * Since v3.2.0
   *
   * Default: "@vue/language-core/types"
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#typesRoot)
   */
  val typesRoot: String = "@vue/language-core/types",

  /**
   * Default: [".vue"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#extensions)
   */
  val extensions: List<String> = listOf(".vue"),

  /**
   * Since v2.0.15
   *
   * Default: []
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#vitePressExtensions)
   */
  val vitePressExtensions: List<String> = emptyList(),

  /**
   * Since v2.0.15
   *
   * Default: []
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#petiteVueExtensions)
   */
  val petiteVueExtensions: List<String> = emptyList(),

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#jsxSlots)
   */
  val jsxSlots: Boolean = false,

  /**
   * Since v3.0.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictCssModules)
   */
  val strictCssModules: Boolean = false,

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictTemplates)
   */
  val strictTemplates: Boolean = false,

  /**
   * Since v3.0.0
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictVModel)
   */
  val strictVModel: Boolean = strictTemplates,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownProps)
   */
  val checkUnknownProps: Boolean = strictTemplates,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownEvents)
   */
  val checkUnknownEvents: Boolean = strictTemplates,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownDirectives)
   */
  val checkUnknownDirectives: Boolean = strictTemplates,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownComponents)
   */
  val checkUnknownComponents: Boolean = strictTemplates,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferComponentDollarEl)
   */
  val inferComponentDollarEl: Boolean = false,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferComponentDollarRefs)
   */
  val inferComponentDollarRefs: Boolean = false,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarAttrs)
   */
  val inferTemplateDollarAttrs: Boolean = false,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarEl)
   */
  val inferTemplateDollarEl: Boolean = false,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarRefs)
   */
  val inferTemplateDollarRefs: Boolean = false,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarSlots)
   */
  val inferTemplateDollarSlots: Boolean = false,

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#skipTemplateCodegen)
   */
  val skipTemplateCodegen: Boolean = false,

  /**
   * Since v2.1.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#fallthroughAttributes)
   */
  val fallthroughAttributes: Boolean = false,

  /**
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkRequiredFallthroughAttributes)
   */
  val checkRequiredFallthroughAttributes: Boolean = false,

  /**
   * Since v3.0.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#resolveStyleImports)
   */
  val resolveStyleImports: Boolean = false,

  /**
   * Since v3.0.0
   *
   * Default: "scoped"
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#resolveStyleClassNames)
   */
  val resolveStyleClassNames: String = "scoped",
  /* boolean | "scoped" */

  /**
   * Since v2.2.4
   *
   * Default: ["Transition", "KeepAlive", "Teleport", "Suspense"]
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#fallthroughComponentNames)
   */
  val fallthroughComponentNames: List<String> = listOf(
    "Transition",
    "KeepAlive",
    "Teleport",
    "Suspense",
  ),

  /**
   * Default: []
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#dataAttributes)
   */
  val dataAttributes: List<String> = emptyList(),

  /**
   * Default: ["aria-*"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#htmlAttributes)
   */
  val htmlAttributes: List<String> = listOf("aria-*"),

  /**
   * Default: ["(await import('${lib}')).defineComponent(", ")"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#optionsWrapper)
   */
  val optionsWrapper: List<String> = listOf("(await import('$lib')).defineComponent(", ")"),
  /* [string, string] | [] */

  /**
   * Default: { defineProps: ['defineProps'], defineSlots: ['defineSlots'], defineEmits: ['defineEmits'], defineExpose: ['defineExpose'], defineModel: ['defineModel'], defineOptions: ['defineOptions'], withDefaults: ['withDefaults'] }
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#macros)
   */
  val macros: Macros = Macros(),

  /**
   * Since v2.2.0
   *
   * Default: { useAttrs: ['useAttrs'], useCssModule: ['useCssModule'], useSlots: ['useSlots'], useTemplateRef: ['useTemplateRef', 'templateRef'] }
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#composables)
   */
  val composables: Composables = Composables(),

  /**
   * Default: []
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#plugins)
   */
  val plugins: List<String> = emptyList(),

  /**
   * Default: { '': { input: true }, value: { input: { type: 'text' }, textarea: true, select: true } }
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#experimentalModelPropName)
   */
  val experimentalModelPropName: Map<String, Map<String, String>>? = null,
  /* Record<string, Record<string, boolean | Record<string, string> | Record<string, string>[]>> */
) {

  @Serializable
  data class Macros(
    val defineProps: List<String> = listOf("defineProps"),
    val defineSlots: List<String> = listOf("defineSlots"),
    val defineEmits: List<String> = listOf("defineEmits"),
    val defineExpose: List<String> = listOf("defineExpose"),
    val defineModel: List<String> = listOf("defineModel"),
    val defineOptions: List<String> = listOf("defineOptions"),
    val withDefaults: List<String> = listOf("withDefaults"),
  )

  @Serializable
  data class Composables(
    val useAttrs: List<String> = listOf("useAttrs"),
    val useCssModule: List<String> = listOf("useCssModule"),
    val useSlots: List<String> = listOf("useSlots"),
    val useTemplateRef: List<String> = listOf("useTemplateRef", "templateRef"),
  )
}
