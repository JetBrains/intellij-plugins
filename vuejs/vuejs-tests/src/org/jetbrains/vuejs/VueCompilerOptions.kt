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
  private val target: String? = null,

  /**
   * Default: "vue"
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#lib)
   */
  private val lib: String? = null,

  /**
   * Since v3.2.0
   *
   * Default: "@vue/language-core/types"
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#typesRoot)
   */
  private val typesRoot: String? = null,

  /**
   * Default: [".vue"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#extensions)
   */
  private val extensions: List<String>? = null,

  /**
   * Since v2.0.15
   *
   * Default: []
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#vitePressExtensions)
   */
  private val vitePressExtensions: List<String>? = null,

  /**
   * Since v2.0.15
   *
   * Default: []
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#petiteVueExtensions)
   */
  private val petiteVueExtensions: List<String>? = null,

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictTemplates)
   */
  private val strictTemplates: Boolean? = null,

  /**
   * Since v3.0.0
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictVModel)
   */
  private val strictVModel: Boolean? = null,

  /**
   * Since v3.0.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#strictCssModules)
   */
  private val strictCssModules: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownProps)
   */
  private val checkUnknownProps: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownEvents)
   */
  private val checkUnknownEvents: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownDirectives)
   */
  private val checkUnknownDirectives: Boolean? = null,

  /**
   * Since v2.2.2
   *
   * Default: false (or the value of strictTemplates if set)
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#checkUnknownComponents)
   */
  private val checkUnknownComponents: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferComponentDollarEl)
   */
  private val inferComponentDollarEl: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferComponentDollarRefs)
   */
  private val inferComponentDollarRefs: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarAttrs)
   */
  private val inferTemplateDollarAttrs: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarEl)
   */
  private val inferTemplateDollarEl: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarRefs)
   */
  private val inferTemplateDollarRefs: Boolean? = null,

  /**
   * Since v2.2.4
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#inferTemplateDollarSlots)
   */
  private val inferTemplateDollarSlots: Boolean? = null,

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#skipTemplateCodegen)
   */
  private val skipTemplateCodegen: Boolean? = null,

  /**
   * Since v2.1.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#fallthroughAttributes)
   */
  private val fallthroughAttributes: Boolean? = null,

  /**
   * Default: []
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#dataAttributes)
   */
  private val dataAttributes: List<String>? = null,

  /**
   * Default: ["aria-*"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#htmlAttributes)
   */
  private val htmlAttributes: List<String>? = null,

  /**
   * Default: ["(await import('${lib}')).defineComponent(", ")"]
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#optionsWrapper)
   */
  private val optionsWrapper: List<String>? = null,
  /* [string, string] | [] */

  /**
   * Since v2.2.4
   *
   * Default: ["Transition", "KeepAlive", "Teleport", "Suspense"]
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#fallthroughComponentNames)
   */
  private val fallthroughComponentNames: List<String>? = null,

  /**
   * Since v3.0.0
   *
   * Default: false
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#resolveStyleImports)
   */
  private val resolveStyleImports: Boolean? = null,

  /**
   * Since v3.0.0
   *
   * Default: "scoped"
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#resolveStyleClassNames)
   */
  private val resolveStyleClassNames: Any? = null,
  /* boolean | "scoped" */

  /**
   * Default: false
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#jsxSlots)
   */
  private val jsxSlots: Boolean? = null,

  /**
   * Default: { defineProps: ['defineProps'], defineSlots: ['defineSlots'], defineEmits: ['defineEmits'], defineExpose: ['defineExpose'], defineModel: ['defineModel'], defineOptions: ['defineOptions'], withDefaults: ['withDefaults'] }
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#macros)
   */
  private val macros: Map<String, List<String>>? = null,

  /**
   * Since v2.2.0
   *
   * Default: { useAttrs: ['useAttrs'], useCssModule: ['useCssModule'], useSlots: ['useSlots'], useTemplateRef: ['useTemplateRef', 'templateRef'] }
   *
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#composables)
   */
  private val composables: Map<String, List<String>>? = null,

  /**
   * Default: []
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#plugins)
   */
  private val plugins: List<String>? = null,

  /**
   * Default: { '': { input: true }, value: { input: { type: 'text' }, textarea: true, select: true } }
   * 
   * [Online Documentation](https://github.com/vuejs/language-tools/wiki/Vue-Compiler-Options#experimentalModelPropName)
   */
  private val experimentalModelPropName: Map<String, Map<String, String>>? = null,
  /* Record<string, Record<string, boolean | Record<string, string> | Record<string, string>[]>> */
) {

  private fun escape(
    value: String,
  ): String =
    """"$value""""

  private fun <T : Any> option(
    name: String,
    value: T?,
    toString: (T) -> String,
  ): String? {
    value ?: return null

    return """"$name": ${toString(value)},"""
  }

  private fun option(
    name: String,
    value: Boolean?,
  ): String? =
    option(
      name = name,
      value = value,
      toString = { it.toString() },
    )

  private fun option(
    name: String,
    value: String?,
  ): String? =
    option(
      name = name,
      value = value,
      toString = ::escape,
    )

  private fun option(
    name: String,
    value: List<String>?,
  ): String? =
    option(
      name = name,
      value = value,
      toString = {
        it.joinToString(", ", "[", "]", transform = ::escape)
      },
    )

  fun toJson(): String =
    sequenceOf(
      option("target", target),
      option("lib", lib),
      option("typesRoot", typesRoot),
      option("extensions", extensions),
      option("vitePressExtensions", vitePressExtensions),
      option("petiteVueExtensions", petiteVueExtensions),
      option("strictTemplates", strictTemplates),
      option("strictVModel", strictVModel),
      option("strictCssModules", strictCssModules),
      option("checkUnknownProps", checkUnknownProps),
      option("checkUnknownEvents", checkUnknownEvents),
      option("checkUnknownDirectives", checkUnknownDirectives),
      option("checkUnknownComponents", checkUnknownComponents),
      option("inferComponentDollarEl", inferComponentDollarEl),
      option("inferComponentDollarRefs", inferComponentDollarRefs),
      option("inferTemplateDollarAttrs", inferTemplateDollarAttrs),
      option("inferTemplateDollarEl", inferTemplateDollarEl),
      option("inferTemplateDollarRefs", inferTemplateDollarRefs),
      option("inferTemplateDollarSlots", inferTemplateDollarSlots),
      option("skipTemplateCodegen", skipTemplateCodegen),
      option("fallthroughAttributes", fallthroughAttributes),
      option("dataAttributes", dataAttributes),
      option("htmlAttributes", htmlAttributes),
      option("optionsWrapper", optionsWrapper),
      option("fallthroughComponentNames", fallthroughComponentNames),
      option("resolveStyleImports", resolveStyleImports),
      // option("resolveStyleClassNames", resolveStyleClassNames),
      option("jsxSlots", jsxSlots),
      // option("macros", macros),
      // option("composables", composables),
      option("plugins", plugins),
      // option("experimentalModelPropName", experimentalModelPropName),
    ).filterNotNull()
      .joinToString(separator = "\n", prefix = "\"vueCompilerOptions\": {\n", postfix = "\n},")
}