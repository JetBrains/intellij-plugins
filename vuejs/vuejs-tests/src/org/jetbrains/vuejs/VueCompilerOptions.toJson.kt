// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import org.jetbrains.vuejs.config.VueCompilerOptions

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

fun VueCompilerOptions.toJson(): String =
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
