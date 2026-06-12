// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script

import org.jetbrains.vuejs.lang.typescript.kolar.js.symbol.Symbol
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSfcBlockSection
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSpreadMerge
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateTemplate(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  selfType: String? = null,
): Sequence<Code> = sequence {
  yieldAll(generateSetupExposed(options, ctx))
  yieldAll(generateTemplateCtx(options, ctx, selfType))
  yieldAll(generateTemplateComponents(options, ctx))
  yieldAll(generateTemplateDirectives(options, ctx))
  if (options.templateAndStyleCodes.isNotEmpty()) {
    yieldAll(options.templateAndStyleCodes)
  }
}

private fun generateSetupExposed(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
): Sequence<Code> = sequence {
  val exposed = options.exposed
  if (exposed.isEmpty()) return@sequence

  ctx.generatedTypes.add(names.SetupExposed)

  yield("type ${names.SetupExposed} = import('${options.vueCompilerOptions.lib}').ShallowUnwrapRef<{${newLine}")
  for (bindingName in exposed) {
    val token = Symbol(bindingName.length.toString())
    yield(DataSegment(text = "", source = null, sourceOffset = 0, data = VueCodeInformation(__linkedToken = token)))
    yield("$bindingName: typeof ")
    yield(DataSegment(text = "", source = null, sourceOffset = 0, data = VueCodeInformation(__linkedToken = token)))
    yield(bindingName)
    yield(endOfLine)
  }
  yield("}>${endOfLine}")
}

private fun generateTemplateCtx(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  selfType: String?,
): Sequence<Code> = sequence {
  val vueCompilerOptions = options.vueCompilerOptions
  val scriptSetupRanges = options.scriptSetupRanges

  val exps = mutableListOf<Code>()
  val emitTypes = mutableListOf<String>()
  val propTypes = mutableListOf<String>()

  if (vueCompilerOptions.petiteVueExtensions.any { options.fileName.endsWith(it) }) {
    exps.add(StringSegment("globalThis"))
  }
  if (selfType != null) {
    exps.add(StringSegment("{} as InstanceType<${names.PickNotAny}<typeof $selfType, new () => {}>>"))
  }
  else {
    exps.add(StringSegment("{} as import('${vueCompilerOptions.lib}').ComponentPublicInstance"))
  }
  if (options.templateAndStyleTypes.contains(names.StyleModules)) {
    exps.add(StringSegment("{} as ${names.StyleModules}"))
  }

  scriptSetupRanges?.defineEmits?.let { defineEmits ->
    emitTypes.add("typeof ${defineEmits.name ?: names.emit}")
  }
  if (scriptSetupRanges?.defineModel?.isNotEmpty() == true) {
    emitTypes.add("typeof ${names.modelEmit}")
  }
  if (emitTypes.isNotEmpty()) {
    yield("type ${names.EmitProps} = ${names.EmitsToProps}<${names.NormalizeEmits}<${emitTypes.joinToString(" & ")}>>${endOfLine}")
    exps.add(StringSegment("{} as { \$emit: ${emitTypes.joinToString(" & ")} }"))
  }

  scriptSetupRanges?.defineProps?.let { defineProps ->
    propTypes.add("typeof ${defineProps.name ?: names.props}")
  }
  if (scriptSetupRanges?.defineModel?.isNotEmpty() == true) {
    propTypes.add(names.ModelProps)
  }
  if (emitTypes.isNotEmpty()) {
    propTypes.add(names.EmitProps)
  }
  if (propTypes.isNotEmpty()) {
    exps.add(StringSegment("{} as { \$props: ${propTypes.joinToString(" & ")} }"))
    exps.add(StringSegment("{} as ${propTypes.joinToString(" & ")}"))
  }

  if (ctx.generatedTypes.contains(names.SetupExposed)) {
    exps.add(StringSegment("{} as ${names.SetupExposed}"))
  }

  yield("const ${names.ctx} = ")
  yieldAll(generateSpreadMerge(exps))
  yield(endOfLine)
}

private fun generateTemplateComponents(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
): Sequence<Code> = sequence {
  val vueCompilerOptions = options.vueCompilerOptions
  val types = mutableListOf<String>()

  if (ctx.generatedTypes.contains(names.SetupExposed)) {
    types.add(names.SetupExposed)
  }
  options.script?.let { script ->
    options.scriptRanges?.exportDefault?.options?.components?.also { components ->
      yield("const ${names.componentsOption} = ")
      yieldAll(generateSfcBlockSection(script, components.start, components.end, codeFeatures.navigation))
      yield(endOfLine)
      types.add("typeof ${names.componentsOption}")
    }
  }

  yield("type ${names.LocalComponents} = ${if (types.isEmpty()) "{}" else types.joinToString(" & ")}${endOfLine}")
  yield("type ${names.GlobalComponents} = ${
    if (vueCompilerOptions.target >= 3.5)
      "import('${vueCompilerOptions.lib}').GlobalComponents"
    else
      "import('${vueCompilerOptions.lib}').GlobalComponents & Pick<typeof import('${vueCompilerOptions.lib}'), 'Transition' | 'TransitionGroup' | 'KeepAlive' | 'Suspense' | 'Teleport'>"
  }${endOfLine}")
  yield("let ${names.components}!: ${names.LocalComponents} & ${names.GlobalComponents}${endOfLine}")
  yield("let ${names.intrinsics}!: ${
    if (vueCompilerOptions.target >= 3.3)
      "import('${vueCompilerOptions.lib}/jsx-runtime').JSX.IntrinsicElements"
    else
      "globalThis.JSX.IntrinsicElements"
  }${endOfLine}")
}

private fun generateTemplateDirectives(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
): Sequence<Code> = sequence {
  val vueCompilerOptions = options.vueCompilerOptions
  val types = mutableListOf<String>()

  if (ctx.generatedTypes.contains(names.SetupExposed)) {
    types.add(names.SetupExposed)
  }
  options.script?.let { script ->
    options.scriptRanges?.exportDefault?.options?.directives?.also { directives ->
      yield("const ${names.directivesOption} = ")
      yieldAll(generateSfcBlockSection(script, directives.start, directives.end, codeFeatures.navigation))
      yield(endOfLine)
      types.add("${names.ResolveDirectives}<typeof ${names.directivesOption}>")
    }
  }

  yield("type ${names.LocalDirectives} = ${if (types.isEmpty()) "{}" else types.joinToString(" & ")}${endOfLine}")
  yield("let ${names.directives}!: ${names.LocalDirectives} & import('${vueCompilerOptions.lib}').GlobalDirectives${endOfLine}")
}
