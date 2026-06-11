// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.script

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.path.browserify.dirname
import org.jetbrains.vuejs.lang.typescript.kolar.path.browserify.isAbsolute
import org.jetbrains.vuejs.lang.typescript.kolar.path.browserify.relative
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRAttr
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRScript
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.InlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateSfcBlockSection
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.startBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptExportDefault
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.ScriptRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

private val exportExpression = "{} as typeof ${names.export}"

class ScriptGenerateResult(
  val codes: List<Code>,
  val ctx: ScriptCodegenContext,
)

fun generateScript(
  options: ScriptCodegenOptions,
): ScriptGenerateResult {
  val ctx = createScriptCodegenContext(options)
  val codes = generateWorker(options, ctx).toList()
  return ScriptGenerateResult(codes = codes, ctx = ctx)
}

private fun generateWorker(
  options: ScriptCodegenOptions,
  ctx: ScriptCodegenContext,
): Sequence<Code> = sequence {
  val script = options.script
  val scriptRanges = options.scriptRanges
  val scriptSetup = options.scriptSetup
  val scriptSetupRanges = options.scriptSetupRanges
  val vueCompilerOptions = options.vueCompilerOptions
  val fileName = options.fileName

  yieldAll(generateGlobalTypesReference(vueCompilerOptions, fileName))

  // <script src="">
  val scriptSrc = script?.src
  if (scriptSrc is IRAttr.WithText) {
    var src = scriptSrc.text
    when {
      src.endsWith(".ts") && !src.endsWith(".d.ts") -> src = src.dropLast(3) + ".js"
      src.endsWith(".tsx") -> src = src.dropLast(4) + ".jsx"
    }
    val features = if (src != scriptSrc.text)
      codeFeatures.all.copy(navigation = codeFeatures.navigationWithoutRename.navigation)
    else
      codeFeatures.all
    yield("import ${names.src} from ")
    val token = yield(startBoundary(script.name, scriptSrc.offset, features))
    yield("'")
    yield(DataSegment(
      text = src.substring(0, scriptSrc.text.length),
      source = script.name,
      sourceOffset = scriptSrc.offset,
      data = VueCodeInformation(__combineToken = token),
    ))
    yield(src.substring(scriptSrc.text.length))
    yield("'")
    yield(endBoundary(token, scriptSrc.offset + scriptSrc.text.length))
    yield(endOfLine)
    yield("export default ${names.src}${endOfLine}")
    yieldAll(generateTemplate(options, ctx, names.src))
  }
  // <script> + <script setup>
  else if (script != null && scriptRanges != null && scriptSetup != null && scriptSetupRanges != null) {
    yieldAll(generateScriptSetupImports(scriptSetup, scriptSetupRanges))

    var selfType: String? = null
    val exportDefault = scriptRanges.exportDefault
    if (exportDefault != null) {
      selfType = names.self
      yieldAll(generateScriptWithExportDefault(ctx, script, scriptRanges, exportDefault, vueCompilerOptions, selfType))
    }
    else {
      yieldAll(generateSfcBlockSection(script, 0, script.content.length, codeFeatures.all))
      yield("export default ${exportExpression}${endOfLine}")
    }

    yieldAll(generateExportDeclareEqual(scriptSetup, names.export))
    if (scriptSetup.generic != null) {
      yieldAll(generateGeneric(
        options,
        ctx,
        scriptSetup,
        scriptSetupRanges,
        scriptSetup.generic,
        generateSetupFunction(options, ctx, scriptSetup, scriptSetupRanges, generateTemplate(options, ctx, selfType)),
      ))
    }
    else {
      yield("await (async () => {${newLine}")
      yieldAll(generateSetupFunction(
        options,
        ctx,
        scriptSetup,
        scriptSetupRanges,
        generateTemplate(options, ctx, selfType),
        sequence { yield("return ") },
      ))
      yield("})()${endOfLine}")
    }
  }
  // only <script setup>
  else if (scriptSetup != null && scriptSetupRanges != null) {
    yieldAll(generateScriptSetupImports(scriptSetup, scriptSetupRanges))

    if (scriptSetup.generic != null) {
      yieldAll(generateExportDeclareEqual(scriptSetup, names.export))
      yieldAll(generateGeneric(
        options,
        ctx,
        scriptSetup,
        scriptSetupRanges,
        scriptSetup.generic,
        generateSetupFunction(options, ctx, scriptSetup, scriptSetupRanges, generateTemplate(options, ctx)),
      ))
    }
    else {
      yieldAll(generateSetupFunction(
        options,
        ctx,
        scriptSetup,
        scriptSetupRanges,
        generateTemplate(options, ctx),
        generateExportDeclareEqual(scriptSetup, names.export),
      ))
    }
    yield("export default ${exportExpression}${endOfLine}")
  }
  // only <script>
  else if (script != null && scriptRanges != null) {
    val exportDefault = scriptRanges.exportDefault
    if (exportDefault != null) {
      yieldAll(generateScriptWithExportDefault(
        ctx,
        script,
        scriptRanges,
        exportDefault,
        vueCompilerOptions,
        names.export,
        generateTemplate(options, ctx, names.export),
      ))
    }
    else {
      yieldAll(generateSfcBlockSection(script, 0, script.content.length, codeFeatures.all))
      yieldAll(generateExportDeclareEqual(script, names.export))
      yield("(await import('${vueCompilerOptions.lib}')).defineComponent({})${endOfLine}")
      yieldAll(generateTemplate(options, ctx, names.export))
      yield("export default ${exportExpression}${endOfLine}")
    }
  }

  yieldAll(ctx.localTypes.generate())
}

private fun generateScriptWithExportDefault(
  ctx: ScriptCodegenContext,
  script: IRScript,
  scriptRanges: ScriptRanges,
  exportDefault: ScriptExportDefault,
  vueCompilerOptions: VueCompilerOptions,
  varName: String,
  templateGenerator: Sequence<Code>? = null,
): Sequence<Code> = sequence {
  val componentOptions = scriptRanges.exportDefault?.options
  val expression = componentOptions?.expression ?: exportDefault.expression
  val isObjectLiteral = componentOptions?.isObjectLiteral ?: exportDefault.isObjectLiteral

  var wrapLeft: String? = null
  var wrapRight: String? = null
  if (isObjectLiteral && vueCompilerOptions.optionsWrapper.isNotEmpty()) {
    wrapLeft = vueCompilerOptions.optionsWrapper.getOrNull(0)
    wrapRight = vueCompilerOptions.optionsWrapper.getOrNull(1)
    ctx.inlayHints.add(InlayHintInfo(
      blockName = script.name,
      offset = expression.start,
      setting = "vue.inlayHints.optionsWrapper",
      label = wrapLeft ?: "[Missing optionsWrapper[0]]",
      tooltip = "This is virtual code that is automatically wrapped for type support, it does not affect your runtime behavior, you can customize it via `vueCompilerOptions.optionsWrapper` option in tsconfig / jsconfig.\n\nTo hide it, you can set `\"vue.inlayHints.optionsWrapper\": false` in IDE settings.",
    ))
    ctx.inlayHints.add(InlayHintInfo(
      blockName = script.name,
      offset = expression.end,
      setting = "vue.inlayHints.optionsWrapper",
      label = wrapRight ?: "[Missing optionsWrapper[1]]",
    ))
  }

  yieldAll(generateSfcBlockSection(script, 0, expression.start, codeFeatures.all))
  yield(exportExpression)
  yieldAll(generateSfcBlockSection(script, expression.end, exportDefault.end, codeFeatures.all))
  yield(endOfLine)
  if (templateGenerator != null) {
    yieldAll(templateGenerator)
  }
  yieldAll(generateExportDeclareEqual(script, varName))
  if (wrapLeft != null && wrapRight != null) {
    yield(wrapLeft)
    yieldAll(generateSfcBlockSection(script, expression.start, expression.end, codeFeatures.all))
    yield(wrapRight)
  }
  else {
    yieldAll(generateSfcBlockSection(script, expression.start, expression.end, codeFeatures.all))
  }
  yield(endOfLine)
  yieldAll(generateSfcBlockSection(script, exportDefault.end, script.content.length, codeFeatures.all))
}

private fun generateGlobalTypesReference(
  vueCompilerOptions: VueCompilerOptions,
  fileName: String,
): Sequence<Code> = sequence {
  val typesRoot = vueCompilerOptions.typesRoot
  val typesPath = if (isAbsolute(typesRoot)) {
    var relativePath = relative(dirname(fileName), typesRoot)
    if (relativePath != typesRoot
        && !relativePath.startsWith("./")
        && !relativePath.startsWith("../")) {
      relativePath = "./$relativePath"
    }
    relativePath
  }
  else {
    typesRoot
  }
  yield("/// <reference types=\"${typesPath}/template-helpers.d.ts\" />${newLine}")
  if (!vueCompilerOptions.checkUnknownProps) {
    yield("/// <reference types=\"${typesPath}/props-fallback.d.ts\" />${newLine}")
  }
  if (vueCompilerOptions.lib == "vue" && vueCompilerOptions.target < 3.5) {
    yield("/// <reference types=\"${typesPath}/vue-3.4-shims.d.ts\" />${newLine}")
  }
}

private fun generateExportDeclareEqual(
  block: IRBlock,
  name: String,
): Sequence<Code> = sequence {
  yield("const ")
  val token = yield(startBoundary(block.name, 0, codeFeatures.doNotReportTs6133))
  yield(name)
  yield(endBoundary(token, block.content.length))
  yield(" = ")
}
