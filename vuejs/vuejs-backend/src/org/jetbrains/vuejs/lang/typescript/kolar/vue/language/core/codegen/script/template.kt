// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.asType
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSfcBlockSection
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSpreadMerge
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateTypeAlias
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateTypedVar
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getRefBrandArgument
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

fun generateTemplate(
  options: ScriptCodegenOptions,
  selfType: String? = null,
): Sequence<Code> = sequence {
  yieldAll(generateTemplateCtx(options, selfType))
  yieldAll(generateTemplateComponents(options))
  yieldAll(generateTemplateDirectives(options))

  yield("void ${names.ctx}, ${names.components}, ${names.intrinsics}, ${names.directives}${endOfLine}")

  for (name in options.dotValueBindings) {
    yield("// @ts-ignore${newLine}")
    yield("${names.withDotValue}($name, ${getRefBrandArgument(options.vueCompilerOptions, options.scriptLang)})${endOfLine}")
  }

  if (options.templateAndStyleCodes.isNotEmpty()) {
    yieldAll(options.templateAndStyleCodes)
  }
}

private fun generateTemplateCtx(
  options: ScriptCodegenOptions,
  selfType: String?,
): Sequence<Code> = sequence {
  val vueCompilerOptions = options.vueCompilerOptions
  val scriptSetupRanges = options.scriptSetupRanges
  val scriptLang = options.scriptLang

  val exps = mutableListOf<Code>()
  val emitTypes = mutableListOf<String>()
  val propTypes = mutableListOf<String>()

  if (vueCompilerOptions.petiteVueExtensions.any { options.fileName.endsWith(it) }) {
    exps.add(StringSegment("globalThis"))
  }
  if (selfType != null) {
    exps.add(StringSegment(asType("InstanceType<${names.PickNotAny}<typeof $selfType, new () => {}>>", scriptLang)))
  }
  else {
    exps.add(StringSegment(asType("import('${vueCompilerOptions.lib}').ComponentPublicInstance", scriptLang)))
  }
  if (options.templateAndStyleTypes.contains(names.StyleModules)) {
    exps.add(StringSegment(asType(names.StyleModules, scriptLang)))
  }

  scriptSetupRanges?.defineEmits?.let { defineEmits ->
    emitTypes.add("typeof ${defineEmits.name ?: names.emit}")
  }
  if (scriptSetupRanges?.defineModel?.isNotEmpty() == true) {
    emitTypes.add("typeof ${names.modelEmit}")
  }
  if (emitTypes.isNotEmpty()) {
    yieldAll(generateTypeAlias(names.EmitProps, scriptLang) {
      yield("${names.EmitsToProps}<${names.NormalizeEmits}<${emitTypes.joinToString(" & ")}>>")
    })
    exps.add(StringSegment(asType("{ \$emit: ${emitTypes.joinToString(" & ")} }", scriptLang)))
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
    exps.add(StringSegment(asType("{ \$props: ${propTypes.joinToString(" & ")} }", scriptLang)))
    exps.add(StringSegment(asType(propTypes.joinToString(" & "), scriptLang)))
  }

  yield("const ${names.ctx} = ")
  yieldAll(generateSpreadMerge(exps))
  yield(endOfLine)
}

private fun generateTemplateComponents(
  options: ScriptCodegenOptions,
): Sequence<Code> = sequence {
  val vueCompilerOptions = options.vueCompilerOptions
  val types = mutableListOf<String>()

  if (options.localComponents.isNotEmpty()) {
    types.add(generateExposedType(vueCompilerOptions.lib, options.localComponents))
  }
  options.script?.let { script ->
    options.scriptRanges?.exportDefault?.options?.components?.also { components ->
      yield("const ${names.componentsOption} = ")
      yieldAll(generateSfcBlockSection(script, components.start, components.end, codeFeatures.navigation))
      yield(endOfLine)
      types.add("typeof ${names.componentsOption}")
    }
  }

  yieldAll(generateTypeAlias(names.LocalComponents, options.scriptLang) {
    yield(if (types.isEmpty()) "{}" else types.joinToString(" & "))
  })
  yieldAll(generateTypeAlias(names.GlobalComponents, options.scriptLang) {
    yield(
      if (vueCompilerOptions.target >= 3.5)
        "import('${vueCompilerOptions.lib}').GlobalComponents"
      else
        "import('${vueCompilerOptions.lib}').GlobalComponents & Pick<typeof import('${vueCompilerOptions.lib}'), 'Transition' | 'TransitionGroup' | 'KeepAlive' | 'Suspense' | 'Teleport'>"
    )
  })
  yieldAll(generateTypedVar("let", names.components, options.scriptLang) {
    yield("${names.LocalComponents} & ${names.GlobalComponents}")
  })
  yieldAll(generateTypedVar("let", names.intrinsics, options.scriptLang) {
    yield(
      if (vueCompilerOptions.target >= 3.3)
        "import('${vueCompilerOptions.lib}/jsx-runtime').JSX.IntrinsicElements"
      else
        "globalThis.JSX.IntrinsicElements"
    )
  })
}

private fun generateTemplateDirectives(
  options: ScriptCodegenOptions,
): Sequence<Code> = sequence {
  val vueCompilerOptions = options.vueCompilerOptions
  val types = mutableListOf<String>()

  if (options.localDirectives.isNotEmpty()) {
    types.add(generateExposedType(vueCompilerOptions.lib, options.localDirectives))
  }
  options.script?.let { script ->
    options.scriptRanges?.exportDefault?.options?.directives?.also { directives ->
      yield("const ${names.directivesOption} = ")
      yieldAll(generateSfcBlockSection(script, directives.start, directives.end, codeFeatures.navigation))
      yield(endOfLine)
      types.add("${names.ResolveDirectives}<typeof ${names.directivesOption}>")
    }
  }

  yieldAll(generateTypeAlias(names.LocalDirectives, options.scriptLang) {
    yield(if (types.isEmpty()) "{}" else types.joinToString(" & "))
  })
  yieldAll(generateTypedVar("let", names.directives, options.scriptLang) {
    yield("${names.LocalDirectives} & import('${vueCompilerOptions.lib}').GlobalDirectives")
  })
}

private fun generateExposedType(lib: String, bindings: Set<String>): String =
  "import('$lib').ShallowUnwrapRef<{\n${bindings.joinToString("\n") { "$it: typeof $it;" }}\n}>"
