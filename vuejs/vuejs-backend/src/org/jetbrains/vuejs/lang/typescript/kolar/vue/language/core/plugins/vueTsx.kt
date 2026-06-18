// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.plugins

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.alien.signals.computed
import org.jetbrains.vuejs.lang.typescript.kolar.path.browserify.basename
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IR
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueLanguagePlugin
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script.ScriptCodegenOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script.ScriptGenerateResult
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script.generateScript
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style.StyleCodegenOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style.StyleGenerateResult
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.style.generateStyle
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateCodegenOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.TemplateGenerateResult
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template.generateTemplate
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptSetupRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.parseScriptRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.parseScriptSetupRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.computedSet
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.capitalize

private val validLangs = setOf("js", "jsx", "ts", "tsx")

class VueTsxPlugin(
  vueCompilerOptions: VueCompilerOptions,
) : VueLanguagePlugin(vueCompilerOptions) {

  override fun resolveEmbeddedCode(
    fileName: String,
    ir: IR,
    embeddedFile: VueEmbeddedCode,
  ) {
    if (embeddedFile.id == "script") {
      val codegen = Codegen(vueCompilerOptions, fileName, ir)
      val generatedScript = codegen.getGeneratedScript()
      embeddedFile.content = generatedScript.codes
    }
  }
}

private class Codegen(
  private val vueCompilerOptions: VueCompilerOptions,
  private val fileName: String,
  private val ir: IR,
) {
  // Placeholder: getResolvedOptions returns vueCompilerOptions directly.
  // parseVueCompilerOptions / CompilerOptionsResolver are not yet implemented in Kotlin.
  private val getResolvedOptions: () -> VueCompilerOptions = { vueCompilerOptions }

  val getScriptRanges: () -> ScriptRanges? = computed {
    ir.script?.takeIf { it.lang in validLangs }
      ?.let { parseScriptRanges(it.ast, getResolvedOptions()) }
  }

  val getScriptSetupRanges: () -> ScriptSetupRanges? = computed {
    ir.scriptSetup?.takeIf { it.lang in validLangs }
      ?.let { parseScriptSetupRanges(it.ast, getResolvedOptions()) }
  }

  val getImportedComponents: () -> Set<String> = computedSet {
    val names = mutableSetOf<String>()
    val scriptSetupRanges = getScriptSetupRanges()
    if (ir.scriptSetup != null && scriptSetupRanges != null) {
      for (range in scriptSetupRanges.components) {
        names.add(ir.scriptSetup.content.substring(range.start, range.end))
      }
      val scriptRange = getScriptRanges()
      if (ir.script != null && scriptRange != null) {
        for (range in scriptRange.components) {
          names.add(ir.script.content.substring(range.start, range.end))
        }
      }
    }
    names
  }

  private val getSetupConsts: () -> Set<String> = computedSet {
    val scriptSetupRanges = getScriptSetupRanges()
    val names = mutableSetOf<String>()
    scriptSetupRanges?.defineProps?.destructured?.keys?.let { names.addAll(it) }
    names.addAll(getImportedComponents())
    val rest = scriptSetupRanges?.defineProps?.destructuredRest
    if (rest != null) names.add(rest)
    names
  }

  private val getSetupRefs: () -> Set<String> = computedSet {
    getScriptSetupRanges()?.useTemplateRef
      ?.mapNotNull { it.name }
      ?.toSet()
    ?: emptySet()
  }

  private val hasDefineSlots: () -> Boolean = computed {
    getScriptSetupRanges()?.defineSlots != null
  }

  private val getSetupPropsAssignName: () -> String? = computed {
    getScriptSetupRanges()?.defineProps?.name
  }

  private val getSetupSlotsAssignName: () -> String? = computed {
    getScriptSetupRanges()?.defineSlots?.name
  }

  private val getInheritAttrs: () -> Boolean = computed {
    val value = getScriptSetupRanges()?.defineOptions?.inheritAttrs
                ?: getScriptRanges()?.exportDefault?.options?.inheritAttrs
    value != "false"
  }

  private val getComponentName: () -> String = computed {
    val name: String
    val componentOptions = getScriptRanges()?.exportDefault?.options
    name = if (ir.script != null && componentOptions?.name != null) {
      val nameRange = componentOptions.name
      ir.script.content.substring(nameRange.start + 1, nameRange.end - 1)
    }
    else {
      val defineOptions = getScriptSetupRanges()?.defineOptions
      if (ir.scriptSetup != null && defineOptions?.name != null) {
        defineOptions.name
      }
      else {
        val baseName = basename(fileName)
        baseName.substring(0, baseName.lastIndexOf('.'))
      }
    }
    capitalize(camelize(name))
  }

  val getGeneratedTemplate: () -> TemplateGenerateResult? = computed {
    val template = ir.template ?: return@computed null
    if (getResolvedOptions().skipTemplateCodegen) return@computed null
    generateTemplate(
      TemplateCodegenOptions(
        vueCompilerOptions = getResolvedOptions(),
        template = template,
        componentName = getComponentName(),
        setupConsts = getSetupConsts(),
        setupRefs = getSetupRefs(),
        hasDefineSlots = hasDefineSlots(),
        propsAssignName = getSetupPropsAssignName(),
        slotsAssignName = getSetupSlotsAssignName(),
        inheritAttrs = getInheritAttrs(),
      )
    )
  }

  private val getGeneratedStyle: () -> StyleGenerateResult? = computed {
    if (ir.styles.isEmpty()) return@computed null
    generateStyle(
      StyleCodegenOptions(
        vueCompilerOptions = getResolvedOptions(),
        styles = ir.styles,
        setupConsts = getSetupConsts(),
        setupRefs = getSetupRefs(),
      )
    )
  }

  val getSetupExposed: () -> Set<String> = computedSet {
    val allVars = mutableSetOf<String>()
    val scriptSetupRanges = getScriptSetupRanges()
    if (ir.scriptSetup == null || scriptSetupRanges == null) {
      return@computedSet allVars
    }
    for (range in scriptSetupRanges.bindings) {
      allVars.add(ir.scriptSetup.content.substring(range.start, range.end))
    }
    val scriptRanges = getScriptRanges()
    if (ir.script != null && scriptRanges != null) {
      for (range in scriptRanges.bindings) {
        allVars.add(ir.script.content.substring(range.start, range.end))
      }
    }
    if (allVars.isEmpty()) {
      return@computedSet allVars
    }
    val candidates = mutableSetOf<String>()
    getGeneratedTemplate()?.ctx?.componentAccessMap?.keys?.let { candidates.addAll(it) }
    getGeneratedStyle()?.ctx?.componentAccessMap?.keys?.let { candidates.addAll(it) }
    ir.template?.ast?.components?.forEach { name ->
      candidates.add(camelize(name))
      candidates.add(capitalize(camelize(name)))
    }
    candidates.filterTo(mutableSetOf()) { it in allVars }
  }

  val getGeneratedScript: () -> ScriptGenerateResult = computed {
    generateScript(
      ScriptCodegenOptions(
        vueCompilerOptions = getResolvedOptions(),
        fileName = fileName,
        script = ir.script,
        scriptSetup = ir.scriptSetup,
        exposed = getSetupExposed(),
        scriptRanges = getScriptRanges(),
        scriptSetupRanges = getScriptSetupRanges(),
        templateAndStyleTypes = buildSet {
          getGeneratedTemplate()?.ctx?.generatedTypes?.let { addAll(it) }
          getGeneratedStyle()?.ctx?.generatedTypes?.let { addAll(it) }
        },
        templateAndStyleCodes = buildList {
          getGeneratedStyle()?.codes?.let { addAll(it) }
          getGeneratedTemplate()?.codes?.let { addAll(it) }
        },
      )
    )
  }
}
