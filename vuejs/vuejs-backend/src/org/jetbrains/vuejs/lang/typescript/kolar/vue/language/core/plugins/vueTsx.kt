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
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.BindingFlag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptSetupRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.parseScriptRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.parseScriptSetupRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.computedSet
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode.VueEmbeddedCode.Companion.SCRIPT_ID
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.capitalize

private val validLangs = setOf("js", "jsx", "ts", "tsx")

private val localDirectiveRE = Regex("^v[A-Z]")

private fun computeLang(ir: IR): String {
  var lang = ir.scriptSetup?.lang ?: ir.script?.lang
  if (ir.script != null && ir.scriptSetup != null) {
    lang = if (ir.scriptSetup.lang != "js") ir.scriptSetup.lang else ir.script.lang
  }
  if (lang != null && lang in validLangs) {
    return lang
  }
  return "ts"
}

class VueTsxPlugin(
  vueCompilerOptions: VueCompilerOptions,
) : VueLanguagePlugin(vueCompilerOptions) {

  override fun resolveEmbeddedCode(
    fileName: String,
    ir: IR,
    code: VueEmbeddedCode,
  ) {
    if (code.id == SCRIPT_ID) {
      val codegen = Codegen(vueCompilerOptions, fileName, ir)
      val generatedScript = codegen.getGeneratedScript()
      code.content = generatedScript.codes
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

  private val getIsVapor: () -> Boolean = computed {
    getResolvedOptions().vapor || ir.scriptSetup?.vapor == true || ir.template?.vapor == true
  }

  val getScriptRanges: () -> ScriptRanges? = computed {
    ir.script?.takeIf { it.lang in validLangs }
      ?.let { parseScriptRanges(it.ast, getResolvedOptions()) }
  }

  val getScriptSetupRanges: () -> ScriptSetupRanges? = computed {
    ir.scriptSetup?.takeIf { it.lang in validLangs }
      ?.let { parseScriptSetupRanges(it.ast, getResolvedOptions()) }
  }

  private val getBindingFlags: () -> Map<String, Int> = computed {
    val flags = LinkedHashMap(getScriptSetupRanges()?.bindings ?: emptyMap())
    val scriptRanges = getScriptRanges()
    if (ir.scriptSetup != null && scriptRanges != null) {
      for ((name, flag) in scriptRanges.bindings) {
        if (name !in flags) {
          flags[name] = flag
        }
      }
    }
    flags
  }

  val getImportedComponents: () -> Set<String> = computedSet {
    val names = mutableSetOf<String>()
    for ((name, flags) in getBindingFlags()) {
      if (flags and BindingFlag.Component != 0) {
        names.add(name)
      }
    }
    names
  }

  private val getSetupBindings: () -> Set<String> = computedSet { getBindingFlags().keys.toSet() }

  private val getScriptSetupBindings: () -> Set<String> = computedSet {
    getScriptSetupRanges()?.bindings?.keys?.toSet() ?: emptySet()
  }

  private val getSetupConsts: () -> Set<String> = computedSet {
    val names = mutableSetOf<String>()
    for ((name, flags) in getBindingFlags()) {
      if (flags and BindingFlag.Const != 0) {
        names.add(name)
      }
    }
    val defineProps = getScriptSetupRanges()?.defineProps
    val destructured = defineProps?.destructured
    if (destructured != null) {
      names.addAll(destructured.keys)
      defineProps.destructuredRest?.let { names.add(it) }
    }
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

  private fun generateTemplatePass(dotValueBindings: Set<String>): TemplateGenerateResult? {
    val template = ir.template ?: return null
    if (getResolvedOptions().skipTemplateCodegen) return null
    return generateTemplate(
      TemplateCodegenOptions(
        vueCompilerOptions = getResolvedOptions(),
        template = template,
        isVapor = getIsVapor(),
        scriptLang = computeLang(ir),
        componentName = getComponentName(),
        importedComponents = getImportedComponents(),
        setupRefs = getSetupRefs(),
        setupConsts = getSetupConsts(),
        setupBindings = getSetupBindings(),
        dotValueBindings = dotValueBindings,
        reassertBindings = dotValueBindings.filterTo(mutableSetOf()) {
          (getBindingFlags()[it] ?: 0) and BindingFlag.Variable != 0
        },
        hasDefineSlots = hasDefineSlots(),
        propsAssignName = getSetupPropsAssignName(),
        slotsAssignName = getSetupSlotsAssignName(),
        inheritAttrs = getInheritAttrs(),
      )
    )
  }

  private fun generateStylePass(dotValueBindings: Set<String>): StyleGenerateResult? {
    if (ir.styles.isEmpty()) return null
    return generateStyle(
      StyleCodegenOptions(
        vueCompilerOptions = getResolvedOptions(),
        styles = ir.styles,
        scriptLang = computeLang(ir),
        setupRefs = getSetupRefs(),
        setupConsts = getSetupConsts(),
        setupBindings = getSetupBindings(),
        dotValueBindings = dotValueBindings,
      )
    )
  }

  val getLocalComponents: () -> Set<String> = computedSet {
    val bindings = getSetupBindings()
    if (bindings.isEmpty()) {
      return@computedSet bindings
    }
    (ir.template?.ast?.components ?: emptyList())
      .flatMap { listOf(camelize(it), capitalize(camelize(it))) }
      .filterTo(mutableSetOf()) { it in bindings }
  }

  private val getLocalDirectives: () -> Set<String> = computedSet {
    val bindings = getSetupBindings()
    if (bindings.isEmpty()) {
      return@computedSet bindings
    }
    // `v[A-Z]` is a naming heuristic: without type analysis there is no
    // reliable signal to tell a directive from a same-named value binding.
    // This feeds the local-directive type / completion, where false positives
    // are harmless (they only surface as extra completion candidates).
    bindings.filterTo(mutableSetOf()) { localDirectiveRE.containsMatchIn(it) }
  }

  // First pass: collect bindings used in narrowing positions (output discarded);
  // the second pass (the computeds below) finalizes every access of these with `.value`.
  private val getDotValueBindings: () -> Set<String> = computedSet {
    val bindings = getSetupBindings()
    if (bindings.isEmpty()) {
      return@computedSet bindings
    }
    val names = mutableSetOf<String>()
    generateTemplatePass(emptySet())?.ctx?.dotValueAccesses?.let { names.addAll(it) }
    generateStylePass(emptySet())?.ctx?.dotValueAccesses?.let { names.addAll(it) }
    names
  }

  val getGeneratedTemplate: () -> TemplateGenerateResult? = computed { generateTemplatePass(getDotValueBindings()) }
  private val getGeneratedStyle: () -> StyleGenerateResult? = computed { generateStylePass(getDotValueBindings()) }

  // Imports and `let`/`var` read only inside template functions are re-asserted there instead.
  private val getTopLevelDotValueBindings: () -> Set<String> = computedSet {
    val topLevelAccesses = buildSet {
      getGeneratedTemplate()?.ctx?.topLevelAccesses?.let { addAll(it) }
      getGeneratedStyle()?.ctx?.topLevelAccesses?.let { addAll(it) }
    }
    getDotValueBindings().filterTo(mutableSetOf()) { name ->
      (getBindingFlags()[name] ?: 0) and BindingFlag.Variable == 0 || name in topLevelAccesses
    }
  }

  private val getReferencedBindings: () -> Set<String> = computedSet {
    val bindings = getSetupBindings()
    if (bindings.isEmpty()) {
      return@computedSet bindings
    }
    val candidates = mutableSetOf<String>()
    getGeneratedTemplate()?.ctx?.contextAccesses?.keys?.let { candidates.addAll(it) }
    getGeneratedStyle()?.ctx?.contextAccesses?.keys?.let { candidates.addAll(it) }
    candidates.filterTo(mutableSetOf()) { it in bindings }
  }

  val getUsedSetupBindings: () -> Set<String> = computedSet {
    getReferencedBindings() + getLocalComponents()
  }

  val getGeneratedScript: () -> ScriptGenerateResult = computed {
    generateScript(
      ScriptCodegenOptions(
        vueCompilerOptions = getResolvedOptions(),
        fileName = fileName,
        script = ir.script,
        scriptSetup = ir.scriptSetup,
        scriptLang = computeLang(ir),
        setupBindings = getScriptSetupBindings(),
        localComponents = getLocalComponents(),
        localDirectives = getLocalDirectives(),
        dotValueBindings = getTopLevelDotValueBindings(),
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
