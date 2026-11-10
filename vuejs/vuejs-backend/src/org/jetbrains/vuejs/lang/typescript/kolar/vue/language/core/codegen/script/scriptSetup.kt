// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRAttr
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScriptSetup
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.CodeTransform
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.asType
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCodeWithTransforms
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSfcBlockSection
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateTypeAlias
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateTypedVar
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRE
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.insert
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.isTsLang
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.replace
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.DefineModel
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptSetupRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize
import kotlin.math.max

fun generateScriptSetupImports(
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
): Sequence<Code> = sequence {
  yieldAll(generateSfcBlockSection(
    scriptSetup,
    scriptSetup.content.startOffset,
    max(scriptSetupRanges.importSectionEndOffset, scriptSetupRanges.leadingCommentEndOffset),
    codeFeatures.all,
  ))
}

fun generateGeneric(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
  generic: IRAttr,
  body: Sequence<Code>,
): Sequence<Code> = sequence {
  val vueCompilerOptions = options.vueCompilerOptions
  yield("(")
  if (generic is IRAttr.WithText) {
    val boundary = yield(Boundary.start(
      scriptSetup.name,
      generic.offset,
      generic.offset + generic.text.length,
      codeFeatures.verification,
    ))
    yield("<")
    yield(DataSegment(
      text = generic.text,
      source = scriptSetup.name,
      sourceOffset = generic.offset,
      data = codeFeatures.all,
    ))
    if (!generic.text.trimEnd().endsWith(",")) {
      yield(",")
    }
    yield(">")
    yield(boundary.end())
  }
  yield(
    "(${newLine}" +
    "\t${names.props}: NonNullable<Awaited<typeof ${names.setup}>>['props'],${newLine}" +
    "\t${names.ctx}?: ${ctx.localTypes.PrettifyLocal}<Pick<NonNullable<Awaited<typeof ${names.setup}>>, 'attrs' | 'emit' | 'slots'>>,${newLine}" +
    "\t${names.exposed}?: NonNullable<Awaited<typeof ${names.setup}>>['expose'],${newLine}" +
    "\t${names.setup} = (async () => {${newLine}"
  )

  yieldAll(body)

  val propTypes = mutableListOf<String>()
  val emitTypes = mutableListOf<String>()

  if (ctx.generatedTypes.contains(names.PublicProps)) {
    propTypes.add(names.PublicProps)
  }
  scriptSetupRanges.defineProps?.arg?.let { arg ->
    yield("const ${names.propsOption} = ")
    yieldAll(generateSfcBlockSection(scriptSetup, arg.start, arg.end, codeFeatures.navigation))
    yield(endOfLine)
    propTypes.add(
      "import('${vueCompilerOptions.lib}').${
        if (vueCompilerOptions.target >= 3.3) "ExtractPublicPropTypes" else "ExtractPropTypes"
      }<typeof ${names.propsOption}>"
    )
  }
  val hasEmitProps = scriptSetupRanges.defineEmits != null || scriptSetupRanges.defineModel.isNotEmpty()
  if (hasEmitProps) {
    propTypes.add(names.EmitProps)
  }
  if (options.templateAndStyleTypes.contains(names.InheritedAttrs)) {
    propTypes.add(
      if (hasEmitProps)
        "Omit<${names.InheritedAttrs}, keyof ${names.EmitProps}>"
      else
        names.InheritedAttrs
    )
  }
  scriptSetupRanges.defineEmits?.let { defineEmits ->
    emitTypes.add("typeof ${defineEmits.name ?: names.emit}")
  }
  if (scriptSetupRanges.defineModel.isNotEmpty()) {
    emitTypes.add("typeof ${names.modelEmit}")
  }

  yield("return {} as {${newLine}")
  yield("\tprops: ")
  yield(
    when {
      vueCompilerOptions.target >= 3.4 ->
        "import('${vueCompilerOptions.lib}').PublicProps"
      vueCompilerOptions.target >= 3.0 ->
        "import('${vueCompilerOptions.lib}').VNodeProps" +
        " & import('${vueCompilerOptions.lib}').AllowedComponentProps" +
        " & import('${vueCompilerOptions.lib}').ComponentCustomProps"
      else ->
        "globalThis.JSX.IntrinsicAttributes"
    }
  )
  if (propTypes.isNotEmpty()) {
    yield(" & ${ctx.localTypes.PrettifyLocal}<${propTypes.joinToString(" & ")}>")
  }
  yield(" & (typeof globalThis extends { __VLS_PROPS_FALLBACK: infer P } ? P : {})${endOfLine}")
  yield("\texpose: (exposed: ")
  yield(
    if (scriptSetupRanges.defineExpose != null)
      "import('${vueCompilerOptions.lib}').ShallowUnwrapRef<typeof ${names.exposed}>"
    else
      "{}"
  )
  if (vueCompilerOptions.inferComponentDollarRefs && options.templateAndStyleTypes.contains(names.TemplateRefs)) {
    yield(" & { \$refs: ${names.TemplateRefs}; }")
  }
  if (vueCompilerOptions.inferComponentDollarEl && options.templateAndStyleTypes.contains(names.RootEl)) {
    yield(" & { \$el: ${names.RootEl}; }")
  }
  yield(") => void${endOfLine}")
  yield("\tattrs: any${endOfLine}")
  yield("\tslots: ${if (hasSlotsType(options)) names.Slots else "{}"}${endOfLine}")
  yield("\temit: ${if (emitTypes.isNotEmpty()) emitTypes.joinToString(" & ") else "{}"}${endOfLine}")
  yield("}${endOfLine}")
  yield("})(),${newLine}")
  yield(") => ({} as import('${vueCompilerOptions.lib}').VNode & { __ctx?: NonNullable<Awaited<typeof ${names.setup}>> }))${endOfLine}")
}

fun generateSetupFunction(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
  body: Sequence<Code>,
  output: Sequence<Code>? = null,
): Sequence<Code> = sequence {
  val transforms = mutableListOf<CodeTransform>()

  for (defineModel in scriptSetupRanges.defineModel) {
    val defaultPropsArg = defineModel.defaultPropsArg ?: continue
    transforms.add(insert(defaultPropsArg.end) {
      sequence {
        yield(": ")
        if (scriptSetupRanges.defineProps?.typeArg != null) {
          yield(names.Props)
        }
        else {
          // FIXME: (props: typeof props) => ... would cause TS2502
          yield("typeof ")
          yield(scriptSetupRanges.defineProps?.name ?: names.props)
        }
      }
    })
  }

  scriptSetupRanges.defineProps?.let { defineProps ->
    val callExp = scriptSetupRanges.withDefaults?.callExp ?: defineProps.callExp
    transforms.addAll(
      generateDefineWithTypeTransforms(
        scriptSetup,
        defineProps.statement,
        callExp,
        defineProps.typeArg,
        defineProps.name,
        names.props,
        names.Props,
      )
    )
  }
  scriptSetupRanges.defineEmits?.let { defineEmits ->
    transforms.addAll(
      generateDefineWithTypeTransforms(
        scriptSetup,
        defineEmits.statement,
        defineEmits.callExp,
        defineEmits.typeArg,
        defineEmits.name,
        names.emit,
        names.Emit,
      )
    )
  }
  scriptSetupRanges.defineSlots?.let { defineSlots ->
    transforms.addAll(
      generateDefineWithTypeTransforms(
        scriptSetup,
        defineSlots.statement,
        defineSlots.callExp,
        defineSlots.typeArg,
        defineSlots.name,
        names.slots,
        names.Slots,
      )
    )
    if (defineSlots.name == null) {
      transforms.add(insert(defineSlots.statement.end) {
        sequence {
          yield("${newLine}void ${names.slots}${endOfLine}")
        }
      })
    }
  }
  scriptSetupRanges.defineExpose?.let { defineExpose ->
    val typeArg = defineExpose.typeArg
    val arg = defineExpose.arg
    when {
      typeArg != null -> {
        transforms.add(insert(defineExpose.callExp.start) {
          sequence {
            yield("let ${names.exposed}!: ")
            yieldAll(generateSfcBlockSection(scriptSetup, typeArg.start, typeArg.end, codeFeatures.all))
            yield(endOfLine)
          }
        })
        transforms.add(replace(typeArg.start, typeArg.end) {
          sequence {
            yield("typeof ${names.exposed}")
          }
        })
      }
      arg != null -> {
        transforms.add(insert(defineExpose.callExp.start) {
          sequence {
            yield("const ${names.exposed} = ")
            yieldAll(generateSfcBlockSection(scriptSetup, arg.start, arg.end, codeFeatures.all))
            yield(endOfLine)
          }
        })
        transforms.add(replace(arg.start, arg.end) {
          sequence {
            yield(names.exposed)
          }
        })
      }
      else -> {
        transforms.add(insert(defineExpose.callExp.start) {
          sequence {
            yield("const ${names.exposed} = {}${endOfLine}")
          }
        })
      }
    }
  }
  if (options.vueCompilerOptions.inferTemplateDollarAttrs) {
    for (useAttrs in scriptSetupRanges.useAttrs) {
      val type = "typeof ${names.dollars}.\$attrs"
      transforms.add(insert(useAttrs.callExp.start) {
        sequence { yield(if (isTsLang(options.scriptLang)) "(" else "/** @type {$type} */ (") }
      })
      transforms.add(insert(useAttrs.callExp.end) {
        sequence { yield(if (isTsLang(options.scriptLang)) " as $type)" else ")") }
      })
    }
  }
  for (useCssModule in scriptSetupRanges.useCssModule) {
    val type = if (options.templateAndStyleTypes.contains(names.StyleModules)) names.StyleModules else "{}"
    val cssArg = useCssModule.arg
    if (cssArg != null) {
      transforms.add(insert(useCssModule.callExp.start) {
        sequence {
          if (isTsLang(options.scriptLang)) {
            yield("(")
          }
          else {
            yield("/** @type {Omit<${type}, '\$style'>[")
            yieldAll(generateSfcBlockSection(scriptSetup, cssArg.start, cssArg.end, codeFeatures.withoutSemantic))
            yield("]} */ (")
          }
        }
      })
      transforms.add(insert(useCssModule.callExp.end) {
        sequence {
          if (isTsLang(options.scriptLang)) {
            yield(" as Omit<${type}, '\$style'>[")
            yieldAll(generateSfcBlockSection(scriptSetup, cssArg.start, cssArg.end, codeFeatures.withoutSemantic))
            yield("])")
          }
          else {
            yield(")")
          }
        }
      })
      transforms.add(replace(cssArg.start, cssArg.end) { sequence { yield(asType("any", options.scriptLang)) } })
    }
    else {
      transforms.add(insert(useCssModule.callExp.start) {
        sequence { yield(if (isTsLang(options.scriptLang)) "(" else "/** @type {${type}['\$style']} */ (") }
      })
      transforms.add(insert(useCssModule.callExp.end) {
        sequence {
          if (isTsLang(options.scriptLang)) {
            yield(" as ${type}[")
            val boundary = yield(Boundary.start(
              scriptSetup.name,
              useCssModule.exp.start,
              useCssModule.exp.end,
              codeFeatures.verification,
            ))
            yield("'\$style'")
            yield(boundary.end())
            yield("])")
          }
          else {
            yield(")")
          }
        }
      })
    }
  }
  if (options.vueCompilerOptions.inferTemplateDollarSlots) {
    for (useSlots in scriptSetupRanges.useSlots) {
      val type = "typeof ${names.dollars}.\$slots"
      transforms.add(insert(useSlots.callExp.start) {
        sequence { yield(if (isTsLang(options.scriptLang)) "(" else "/** @type {$type} */ (") }
      })
      transforms.add(insert(useSlots.callExp.end) {
        sequence { yield(if (isTsLang(options.scriptLang)) " as $type)" else ")") }
      })
    }
  }
  for (useTemplateRef in scriptSetupRanges.useTemplateRef) {
    val refArg = useTemplateRef.arg
    transforms.add(insert(useTemplateRef.callExp.start) {
      sequence {
        if (isTsLang(options.scriptLang)) {
          yield("(")
          return@sequence
        }
        yield("/** @type {Readonly<import('${options.vueCompilerOptions.lib}').ShallowRef<")
        if (refArg != null) {
          yield(names.TemplateRefs)
          yield("[")
          yieldAll(generateSfcBlockSection(scriptSetup, refArg.start, refArg.end, codeFeatures.withoutSemantic))
          yield("]")
        }
        else {
          yield("unknown")
        }
        yield(" | null>>} */ (")
      }
    })
    transforms.add(insert(useTemplateRef.callExp.end) {
      sequence {
        if (isTsLang(options.scriptLang)) {
          yield(" as Readonly<import('${options.vueCompilerOptions.lib}').ShallowRef<")
          if (refArg != null) {
            yield(names.TemplateRefs)
            yield("[")
            yieldAll(generateSfcBlockSection(scriptSetup, refArg.start, refArg.end, codeFeatures.withoutSemantic))
            yield("]")
          }
          else {
            yield("unknown")
          }
          yield(" | null>>)")
        }
        else {
          yield(")")
        }
      }
    })
    refArg?.let { ra ->
      transforms.add(replace(ra.start, ra.end) { sequence { yield(asType("any", options.scriptLang)) } })
    }
  }

  yieldAll(generateCodeWithTransforms(
    max(scriptSetupRanges.importSectionEndOffset, scriptSetupRanges.leadingCommentEndOffset),
    scriptSetup.content.endOffset,
    transforms,
  ) { start, end -> generateSfcBlockSection(scriptSetup, start, end, codeFeatures.all) })
  yieldAll(generateModels(options, scriptSetup, scriptSetupRanges))
  yieldAll(generatePublicProps(options, ctx, scriptSetup, scriptSetupRanges))
  yieldAll(body)

  if (output != null) {
    if (hasSlotsType(options)) {
      yield("const ${names.base} = ")
      yieldAll(generateComponent(options, ctx, scriptSetup, scriptSetupRanges))
      yield(endOfLine)
      yieldAll(output)
      yield(asType("${ctx.localTypes.WithSlots}<typeof ${names.base}, ${names.Slots}>", options.scriptLang))
      yield(endOfLine)
    }
    else {
      yieldAll(output)
      yieldAll(generateComponent(options, ctx, scriptSetup, scriptSetupRanges))
      yield(endOfLine)
    }
  }
}

// An `import` (rather than `declare const`) keeps the macros resolvable in JS
// virtual files, where type-only declarations are syntax errors. Imports hoist,
// so this can be emitted after all user content: leading directives stay ahead
// of any statement, and the import-section/content junction stays intact.
fun generateMacros(
  options: ScriptCodegenOptions,
): Sequence<Code> = sequence {
  if (options.vueCompilerOptions.target >= 3.3) {
    val scriptSetup = options.scriptSetup
    val scriptSetupRanges = options.scriptSetupRanges
    if (scriptSetup == null || scriptSetupRanges == null) {
      return@sequence
    }

    val usedMacros = mutableListOf<Pair<String, String>>()
    val add = { canonical: String, exp: TextRange<*>? ->
      if (exp != null) {
        usedMacros.add(canonical to scriptSetup.content.substring(exp.start, exp.end))
      }
    }
    add("defineProps", scriptSetupRanges.defineProps?.exp)
    add("defineEmits", scriptSetupRanges.defineEmits?.exp)
    add("defineSlots", scriptSetupRanges.defineSlots?.exp)
    add("defineExpose", scriptSetupRanges.defineExpose?.exp)
    add("withDefaults", scriptSetupRanges.withDefaults?.exp)
    if (scriptSetupRanges.defineModel.isNotEmpty()) {
      usedMacros.add("defineModel" to "defineModel")
    }
    if (scriptSetupRanges.defineOptions != null) {
      usedMacros.add("defineOptions" to "defineOptions")
    }

    val toImport = usedMacros.filter { (_, alias) -> !options.setupBindings.contains(alias) }

    if (toImport.isNotEmpty()) {
      yield("${newLine}const { ")
      for ((canonical, alias) in toImport) {
        yield(if (alias == canonical) "$canonical, " else "$canonical: $alias, ")
      }
      yield("} = ")
      yield(asType("typeof import('${options.vueCompilerOptions.lib}')", options.scriptLang))
      yield(endOfLine)
    }
  }
}

private fun generateDefineWithTypeTransforms(
  scriptSetup: IRScriptSetup,
  statement: TextRange<*>,
  callExp: TextRange<*>,
  typeArg: TextRange<*>?,
  name: String?,
  defaultName: String,
  typeName: String,
): Sequence<CodeTransform> = sequence {
  if (typeArg != null) {
    yield(insert(statement.start) {
      sequence {
        yield("type $typeName = ")
        yieldAll(generateSfcBlockSection(scriptSetup, typeArg.start, typeArg.end, codeFeatures.all))
        yield(endOfLine)
      }
    })
    yield(replace(typeArg.start, typeArg.end) {
      sequence {
        yield(typeName)
      }
    })
  }
  if (name == null) {
    if (statement.start == callExp.start && statement.end == callExp.end) {
      yield(insert(callExp.start) {
        sequence {
          yield("const $defaultName = ")
        }
      })
    }
    else if (typeArg != null) {
      yield(replace(statement.start, typeArg.start) {
        sequence {
          yield("const $defaultName = ")
          yieldAll(generateSfcBlockSection(scriptSetup, callExp.start, typeArg.start, codeFeatures.all))
        }
      })
      yield(replace(typeArg.end, callExp.end) {
        sequence {
          yieldAll(generateSfcBlockSection(scriptSetup, typeArg.end, callExp.end, codeFeatures.all))
          yield(endOfLine)
          yieldAll(generateSfcBlockSection(scriptSetup, statement.start, callExp.start, codeFeatures.all))
          yield(defaultName)
        }
      })
    }
    else {
      yield(replace(statement.start, callExp.end) {
        sequence {
          yield("const $defaultName = ")
          yieldAll(generateSfcBlockSection(scriptSetup, callExp.start, callExp.end, codeFeatures.all))
          yield(endOfLine)
          yieldAll(generateSfcBlockSection(scriptSetup, statement.start, callExp.start, codeFeatures.all))
          yield(defaultName)
        }
      })
    }
  }
  else if (!identifierRE.matches(name)) {
    yield(replace(statement.start, callExp.start) {
      sequence {
        yield("const $defaultName = ")
      }
    })
    yield(insert(statement.end) {
      sequence {
        yield(endOfLine)
        yieldAll(generateSfcBlockSection(scriptSetup, statement.start, callExp.start, codeFeatures.all))
        yield(defaultName)
      }
    })
  }
}

private fun generatePublicProps(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
): Sequence<Code> = sequence {
  if (scriptSetupRanges.defineProps?.typeArg != null && scriptSetupRanges.withDefaults?.arg != null) {
    yield("const ${names.defaults} = ")
    yieldAll(generateSfcBlockSection(
      scriptSetup,
      scriptSetupRanges.withDefaults.arg.start,
      scriptSetupRanges.withDefaults.arg.end,
      codeFeatures.navigation,
    ))
    yield(endOfLine)
    yield("void ${names.defaults}${endOfLine}")
  }

  val propTypes = mutableListOf<String>()
  if (options.vueCompilerOptions.jsxSlots && hasSlotsType(options)) {
    propTypes.add("${ctx.localTypes.PropsChildren}<${names.Slots}>")
  }
  if (scriptSetupRanges.defineProps?.typeArg != null) {
    propTypes.add(names.Props)
  }
  if (scriptSetupRanges.defineModel.isNotEmpty()) {
    propTypes.add(names.ModelProps)
  }
  val target = options.vueCompilerOptions.target
  val used = scriptSetup.generic != null
             || target < 3.6
             || (target >= 3.5 && scriptSetupRanges.defineProps?.arg == null)
  if (propTypes.isNotEmpty() && used) {
    yieldAll(generateTypeAlias(names.PublicProps, options.scriptLang) {
      yield(propTypes.joinToString(" & "))
    })
    ctx.generatedTypes.add(names.PublicProps)
  }
}

private fun hasSlotsType(
  options: ScriptCodegenOptions,
): Boolean =
  options.scriptSetupRanges?.defineSlots != null || options.templateAndStyleTypes.contains(names.Slots)

private fun generateModels(
  options: ScriptCodegenOptions,
  scriptSetup: IRScriptSetup,
  scriptSetupRanges: ScriptSetupRanges,
): Sequence<Code> = sequence {
  if (scriptSetupRanges.defineModel.isEmpty()) return@sequence

  val defaultCodes = mutableListOf<String>()
  val propCodes = mutableListOf<Sequence<Code>>()
  val emitCodes = mutableListOf<Sequence<Code>>()

  for (defineModel in scriptSetupRanges.defineModel) {
    val propName = if (defineModel.name != null) {
      val nameText = scriptSetup.content.substring(defineModel.name.start, defineModel.name.end)
      camelize(nameText.substring(1, nameText.length - 1))
    }
    else "modelValue"

    val modelType = when {
      defineModel.type != null ->
        scriptSetup.content.substring(defineModel.type.start, defineModel.type.end)
      defineModel.runtimeType != null && defineModel.localName != null ->
        "typeof ${scriptSetup.content.substring(defineModel.localName.start, defineModel.localName.end)}['value']"
      defineModel.defaultValue != null && propName.isNotEmpty() ->
        "typeof ${names.defaultModels}['${propName}']"
      else -> "any"
    }

    if (defineModel.defaultValue != null) {
      defaultCodes.add("'${propName}': ${
        scriptSetup.content.substring(
          defineModel.defaultValue.start,
          defineModel.defaultValue.end,
        )
      },${newLine}")
    }

    propCodes.add(generateModelProp(options, scriptSetup, defineModel, propName, modelType))
    emitCodes.add(generateModelEmit(defineModel, propName, modelType))
  }

  if (defaultCodes.isNotEmpty()) {
    yield("const ${names.defaultModels} = {${newLine}")
    for (code in defaultCodes) {
      yield(code)
    }
    yield("}${endOfLine}")
    yield("void ${names.defaultModels}${endOfLine}")
  }

  yieldAll(generateTypeAlias(names.ModelProps, options.scriptLang) {
    yield("{${newLine}")
    for (codes in propCodes) {
      yieldAll(codes)
    }
    yield("}")
  })

  yieldAll(generateTypeAlias(names.ModelEmit, options.scriptLang) {
    yield("{${newLine}")
    for (codes in emitCodes) {
      yieldAll(codes)
    }
    yield("}")
  })

  // avoid `defineModel<...>()` to prevent JS AST issues
  yieldAll(generateTypedVar("let", names.modelEmit, options.scriptLang) {
    yield("${names.ShortEmits}<${names.ModelEmit}>")
  })
}

private fun generateModelProp(
  options: ScriptCodegenOptions,
  scriptSetup: IRScriptSetup,
  defineModel: DefineModel,
  propName: String,
  modelType: String,
): Sequence<Code> = sequence {
  // In JS the alias body lives inside a JSDoc comment; user comments could
  // contain `*/` and terminate it early, so they are dropped.
  if (defineModel.comments != null && isTsLang(options.scriptLang)) {
    yield(scriptSetup.content.substring(defineModel.comments.start, defineModel.comments.end))
    yield(newLine)
  }
  if (defineModel.name != null) {
    yieldAll(generateCamelized(
      scriptSetup.content.substring(defineModel.name.start, defineModel.name.end),
      scriptSetup.name,
      defineModel.name.start,
      codeFeatures.navigation,
    ))
  }
  else {
    yield(propName)
  }
  yield(if (defineModel.required == true) ": " else "?: ")
  yield(modelType)
  yield(endOfLine)
  if (defineModel.modifierType != null) {
    val modifierName = "${if (propName == "modelValue") "model" else propName}Modifiers"
    val modifierType = scriptSetup.content.substring(defineModel.modifierType.start, defineModel.modifierType.end)
    yield("'${modifierName}'?: Partial<Record<${modifierType}, true>>${endOfLine}")
  }
}

private fun generateModelEmit(
  defineModel: DefineModel,
  propName: String,
  modelType: String,
): Sequence<Code> = sequence {
  yield("'update:${propName}': [value: ")
  yield(modelType)
  if (defineModel.required != true && defineModel.defaultValue == null) {
    yield(" | undefined")
  }
  yield("]${endOfLine}")
}
