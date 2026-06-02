// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

/**
 * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options)
 */
class VueCompilerOptions(

  /**
   * Default: Automatically detected from the installed vue version, otherwise 3.3.
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#target)
   */
  val target: String? = null,

  /**
   * Default: "vue"
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#lib)
   */
  val lib: String? = null,

  /**
   * Since v3.2.0
   *
   * Default: "@vue/language-core/types"
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#typesRoot)
   */
  val typesRoot: String? = null,

  /**
   * Default: [".vue"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#extensions)
   */
  val extensions: List<String>? = null,

  /**
   * Since v2.0.15
   *
   * Default: []
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#vitePressExtensions)
   */
  val vitePressExtensions: List<String>? = null,

  /**
   * Since v2.0.15
   *
   * Default: []
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#petiteVueExtensions)
   */
  val petiteVueExtensions: List<String>? = null,

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictTemplates)
   */
  val strictTemplates: Boolean? = null,

  /**
   * Since v3.0.0
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictVModel)
   */
  val strictVModel: Boolean? = null,

  /**
   * Since v3.0.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictCssModules)
   */
  val strictCssModules: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownProps)
   */
  val checkUnknownProps: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownEvents)
   */
  val checkUnknownEvents: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownDirectives)
   */
  val checkUnknownDirectives: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownComponents)
   */
  val checkUnknownComponents: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferComponentDollarEl)
   */
  val inferComponentDollarEl: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferComponentDollarRefs)
   */
  val inferComponentDollarRefs: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarAttrs)
   */
  val inferTemplateDollarAttrs: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarEl)
   */
  val inferTemplateDollarEl: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarRefs)
   */
  val inferTemplateDollarRefs: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarSlots)
   */
  val inferTemplateDollarSlots: Boolean? = null,

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#skipTemplateCodegen)
   */
  val skipTemplateCodegen: Boolean? = null,

  /**
   * Since v2.1.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#fallthroughAttributes)
   */
  val fallthroughAttributes: Boolean? = null,

  /**
   * Default: []
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#dataAttributes)
   */
  val dataAttributes: List<String>? = null,

  /**
   * Default: ["aria-*"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#htmlAttributes)
   */
  val htmlAttributes: List<String>? = null,

  /**
   * Default: ["(await import('${lib}')).defineComponent(", ")"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#optionsWrapper)
   */
  val optionsWrapper: List<String>? = null,
  /* [string, string] | [] */

  /**
   * Since v2.2.4
   *
   * Default: ["Transition", "KeepAlive", "Teleport", "Suspense"]
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#fallthroughComponentNames)
   */
  val fallthroughComponentNames: List<String>? = null,

  /**
   * Since v3.0.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#resolveStyleImports)
   */
  val resolveStyleImports: Boolean? = null,

  /**
   * Since v3.0.0
   *
   * Default: "scoped"
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#resolveStyleClassNames)
   */
  val resolveStyleClassNames: Any? = null,
  /* boolean | "scoped" */

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#jsxSlots)
   */
  val jsxSlots: Boolean? = null,

  /**
   * Default: { defineProps: ['defineProps'], defineSlots: ['defineSlots'], defineEmits: ['defineEmits'], defineExpose: ['defineExpose'], defineModel: ['defineModel'], defineOptions: ['defineOptions'], withDefaults: ['withDefaults'] }
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#macros)
   */
  val macros: Map<String, List<String>>? = null,

  /**
   * Since v2.2.0
   *
   * Default: { useAttrs: ['useAttrs'], useCssModule: ['useCssModule'], useSlots: ['useSlots'], useTemplateRef: ['useTemplateRef', 'templateRef'] }
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#composables)
   */
  val composables: Map<String, List<String>>? = null,

  /**
   * Default: []
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#plugins)
   */
  val plugins: List<String>? = null,

  /**
   * Default: { '': { input: true }, value: { input: { type: 'text' }, textarea: true, select: true } }
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#experimentalModelPropName)
   */
  val experimentalModelPropName: Map<String, Map<String, String>>? = null,
  /* Record<string, Record<string, boolean | Record<string, string> | Record<string, string>[]>> */
)
