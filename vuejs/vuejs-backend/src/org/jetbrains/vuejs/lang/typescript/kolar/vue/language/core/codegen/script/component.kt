// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptSetup
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateIntersectMerge
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSfcBlockSection
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSpreadMerge
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptSetupRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateComponent(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
): Sequence<Code> = sequence {
  yield("(await import('${options.vueCompilerOptions.lib}')).defineComponent({${newLine}")

  if (scriptSetupRanges.defineExpose != null) {
    yield("setup: () => ${names.exposed},${newLine}")
  }

  val emitOptionCodes = generateEmitsOption(options, scriptSetupRanges).toList()
  yieldAll(emitOptionCodes)
  yieldAll(generatePropsOption(options, ctx, scriptSetup, scriptSetupRanges, emitOptionCodes.isNotEmpty()))

  val vueCompilerOptions = options.vueCompilerOptions
  if (vueCompilerOptions.target >= 3.5 && vueCompilerOptions.inferComponentDollarRefs
      && options.templateAndStyleTypes.contains(names.TemplateRefs)) {
    yield("__typeRefs: {} as ${names.TemplateRefs},${newLine}")
  }
  if (vueCompilerOptions.target >= 3.5 && vueCompilerOptions.inferComponentDollarEl
      && options.templateAndStyleTypes.contains(names.RootEl)) {
    yield("__typeEl: {} as ${names.RootEl},${newLine}")
  }
  yield("})")
}

private fun generateEmitsOption(
  options: ScriptCodegenOptions,
  scriptSetupRanges: ScriptSetupRanges,
): Sequence<Code> = sequence {
  val typeCodes = if (options.vueCompilerOptions.target >= 3.5
      && scriptSetupRanges.defineEmits?.hasUnionTypeArg != true) {
    generateTypeEmitsOption(scriptSetupRanges)
  } else emptyList()

  val runtimeCodes = if (typeCodes.isEmpty()) {
    generateRuntimeEmitsOption(scriptSetupRanges)
  } else emptyList()

  if (typeCodes.isNotEmpty()) {
    yield("__typeEmits: {} as ")
    yieldAll(generateIntersectMerge(*typeCodes.map { StringSegment(it) }.toTypedArray()))
    yield(",${newLine}")
  }
  else if (runtimeCodes.isNotEmpty()) {
    yield("emits: ")
    yieldAll(generateSpreadMerge(*runtimeCodes.map { StringSegment(it) }.toTypedArray()))
    yield(",${newLine}")
  }
}

private fun generateTypeEmitsOption(
  scriptSetupRanges: ScriptSetupRanges,
): List<String> = buildList {
  if (scriptSetupRanges.defineModel.isNotEmpty()) add(names.ModelEmit)
  if (scriptSetupRanges.defineEmits?.typeArg != null) add(names.Emit)
}

private fun generateRuntimeEmitsOption(
  scriptSetupRanges: ScriptSetupRanges,
): List<String> = buildList {
  if (scriptSetupRanges.defineModel.isNotEmpty()) {
    add("{} as ${names.NormalizeEmits}<typeof ${names.modelEmit}>")
  }
  scriptSetupRanges.defineEmits?.let { defineEmits ->
    add("{} as ${names.NormalizeEmits}<typeof ${defineEmits.name ?: names.emit}>")
  }
}

private fun generatePropsOption(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
  hasEmitsOption: Boolean,
): Sequence<Code> = sequence {
  val typeCodes = if (options.vueCompilerOptions.target >= 3.5
      && scriptSetupRanges.defineProps?.arg == null) {
    generateTypePropsOption(options, ctx, hasEmitsOption).toList()
  } else emptyList()

  val runtimeCodes = if (scriptSetupRanges.withDefaults != null || typeCodes.isEmpty()) {
    generateRuntimePropsOption(options, ctx, scriptSetup, scriptSetupRanges, hasEmitsOption).toList()
  } else emptyList()

  if (typeCodes.isNotEmpty()) {
    if (options.vueCompilerOptions.target >= 3.6 && scriptSetupRanges.withDefaults?.arg != null) {
      yield("__defaults: ${names.defaults},${newLine}")
    }
    yield("__typeProps: ")
    yieldAll(generateSpreadMerge(*typeCodes.toTypedArray()))
    yield(",${newLine}")
  }
  if (runtimeCodes.isNotEmpty()) {
    yield("props: ")
    yieldAll(generateSpreadMerge(*runtimeCodes.toTypedArray()))
    yield(",${newLine}")
  }
}

private fun generateTypePropsOption(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  hasEmitsOption: Boolean,
): Sequence<Code> = sequence {
  if (options.templateAndStyleTypes.contains(names.InheritedAttrs)) {
    val attrsType = if (hasEmitsOption)
      "Omit<${names.InheritedAttrs}, keyof ${names.EmitProps}>"
    else
      names.InheritedAttrs
    yield("{} as $attrsType")
  }
  if (ctx.generatedTypes.contains(names.PublicProps)) {
    yield("{} as ${names.PublicProps}")
  }
}

private fun generateRuntimePropsOption(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
  hasEmitsOption: Boolean,
): Sequence<Code> = sequence {
  if (options.templateAndStyleTypes.contains(names.InheritedAttrs)) {
    val attrsType = if (hasEmitsOption)
      "Omit<${names.InheritedAttrs}, keyof ${names.EmitProps}>"
    else
      names.InheritedAttrs
    val propsType = "${ctx.localTypes.TypePropsToOption}<${names.PickNotAny}" +
      "<${ctx.localTypes.OmitIndexSignature}<$attrsType>, {}>>"
    yield("{} as $propsType")
  }
  if (ctx.generatedTypes.contains(names.PublicProps) && options.vueCompilerOptions.target < 3.6) {
    var propsType = "${ctx.localTypes.TypePropsToOption}<${names.PublicProps}>"
    if (scriptSetupRanges.withDefaults?.arg != null) {
      propsType = "${ctx.localTypes.WithDefaults}<$propsType, typeof ${names.defaults}>"
    }
    yield("{} as $propsType")
  }
  scriptSetupRanges.defineProps?.arg?.let { arg ->
    yieldAll(generateSfcBlockSection(scriptSetup, arg.start, arg.end, codeFeatures.navigation))
  }
}
