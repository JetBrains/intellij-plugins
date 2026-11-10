// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrowFunction
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isElementAccessExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isExpressionStatement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAccessExpression
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.InlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.FakeSourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endsWithSemicolon
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateTypedVar
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getRefBrandArgument
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRE
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.isTsLang
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.statements
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers.getUnwrappedExpression
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.capitalize

private data class EventDefinition(
  val propPrefix: String,
  val emitPrefix: String,
  val propName: String,
  val emitName: String,
  val items: MutableList<EventItem>,
)

private data class EventItem(
  val prop: DirectiveNode,
  val source: String,
  val offset: Int?,
)

fun generateElementEvents(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
  componentOriginalVar: String,
  getCtxVar: () -> String,
  getPropsVar: () -> String,
): Sequence<Code> = sequence {
  val definitions = LinkedHashMap<String, EventDefinition>()

  for (rawProp in node.props) {
    val prop = rawProp as? DirectiveNode ?: continue
    val propArg = prop.arg as? SimpleExpressionNode
    val isOnStatic = prop.name == "on" && propArg != null && propArg.isStatic
    val isModelStatic = options.vueCompilerOptions.strictVModel &&
                        prop.name == "model" && (prop.arg == null || propArg != null && propArg.isStatic)
    if (!isOnStatic && !isModelStatic) continue

    var source = propArg?.loc?.source ?: "model-value"
    var offset: Int? = propArg?.loc?.startOffset
    var propPrefix = "on-"
    var emitPrefix = ""
    if (prop.name == "model") {
      propPrefix = "onUpdate:"
      emitPrefix = "update:"
    }
    else if (source.startsWith("vue:")) {
      source = source.substring("vue:".length)
      offset = offset!! + "vue:".length
      propPrefix = "onVnode-"
      emitPrefix = "vnode-"
    }
    val propName = camelize(propPrefix + source)
    val emitName = emitPrefix + source
    val key = (listOf(prop.name, propName) + prop.modifiers.map { it.content }).joinToString("+")

    definitions.getOrPut(key) {
      EventDefinition(propPrefix, emitPrefix, propName, emitName, mutableListOf())
    }.items.add(EventItem(prop, source, offset))
  }

  if (definitions.isEmpty()) return@sequence

  val emitsVar = ctx.getInternalVariable()
  yieldAll(generateTypedVar("let", emitsVar, options.scriptLang) {
    yield("${names.ResolveEmits}<typeof $componentOriginalVar, typeof ${getCtxVar()}.emit>")
  })

  for ((propPrefix, emitPrefix, propName, emitName, items) in definitions.values) {
    val eventVar = ctx.getInternalVariable()
    val eventType = "${
      if (options.vueCompilerOptions.checkUnknownEvents) "" else "Record<string, unknown> & "
    }Partial<${names.ResolveEvent}<typeof ${getPropsVar()}, typeof $emitsVar, '$propName', '$emitName', '${
      camelize(emitName)
    }'>>"
    if (isTsLang(options.scriptLang)) {
      yield("const $eventVar: $eventType = {$newLine")
    }
    else {
      yield("/** @type {$eventType} */${newLine}const $eventVar = {$newLine")
    }
    for ((prop, source, offset) in items) {
      val argsType = "${names.EventArgs}<$eventType, '$propName'>"
      if (prop.name == "on") {
        yield("/** @type {typeof $emitsVar.")
        yieldAll(generateEventArg(options, source, offset!!, emitPrefix.dropLast(1), codeFeatures.navigation))
        yield("} */$newLine")
        // in JS, the JSDoc above types the property, and fails to parse for a non-identifier name
        val emitArgName = getEventArgName(source, emitPrefix.dropLast(1))
        yieldAll(generateEventArg(options, source, offset, propPrefix.dropLast(1)))
        yield(": ")
        yieldAll(generateEventExpression(
          options,
          ctx,
          prop,
          if (isTsLang(options.scriptLang))
            argsType
          else if (identifierRE.matches(emitArgName))
            "${names.EventArgs}<typeof $emitsVar, '$emitArgName'>"
          else
            "any[]",
        ))
      }
      else {
        yield("'$propName': ")
        yieldAll(generateModelEventExpression(options, ctx, prop, argsType))
      }
      yield(",$newLine")
    }
    yield("}$endOfLine")
    yield("void $eventVar$endOfLine")
  }
}

fun generateEventArg(
  options: TemplateCodegenOptions,
  name: String,
  start: Int,
  directive: String = "on",
  features: VueCodeInformation? = null,
): Sequence<Code> = sequence {
  val computedFeatures = features ?: VueCodeInformation(
    semantic = codeFeatures.semanticWithoutHighlight.semantic,
    navigation = codeFeatures.navigationWithoutRename.navigation,
    verification = if (options.vueCompilerOptions.checkUnknownEvents)
      codeFeatures.verification.verification
    else
      codeFeatures.doNotReportTs2353AndTs2561.verification,
  )
  val camelizedName = if (directive.isNotEmpty()) capitalize(name) else name
  val boundary = yield(Boundary.start(Source("template"), start, start + camelizedName.length, computedFeatures))
  if (identifierRE.matches(camelize(camelizedName))) {
    yield(directive)
    yieldAll(generateCamelized(camelizedName, Source("template"), start, boundary.features))
  }
  else {
    yield("'")
    yield(directive)
    yieldAll(generateCamelized(camelizedName, Source("template"), start, boundary.features))
    yield("'")
    yield(boundary.end())
  }
}

fun getEventArgName(name: String, directive: String = "on"): String {
  val capitalized = if (directive.isNotEmpty()) capitalize(name) else name
  return directive + capitalized.split('-').mapIndexed { i, part -> if (i != 0) capitalize(part) else part }.joinToString("")
}

private fun generateEventParameter(
  options: TemplateCodegenOptions,
  argsType: String,
): Sequence<Code> = sequence {
  if (isTsLang(options.scriptLang)) {
    yield("(...[\$event]: $argsType) => {$newLine")
  }
  else {
    yield("(/** @type {$argsType} */ ...[\$event]) => {$newLine")
  }
}

fun generateEventExpression(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
  argsType: String,
): Sequence<Code> = sequence {
  val exp = prop.exp
  if (exp is SimpleExpressionNode) {
    val ast = getTypeScriptAST(exp.content, options)
    val isCompound = isCompoundExpression(ast)
    val interpolation = generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = codeFeatures.all,
      code = exp.content,
      start = exp.loc.startOffset,
      prefix = if (isCompound) "" else "(",
      suffix = if (isCompound) "" else ")",
    )
    if (isCompound) {
      val scope = ctx.scope()
      scope.declare("\$event")

      val accessMark = ctx.accessLog.size
      val codes = interpolation.toList()

      yieldAll(generateEventParameter(options, argsType))
      // consume $event to avoid TS6133 when the expression doesn't use it
      yield("void \$event$endOfLine")

      yieldAll(generateReasserts(options, ctx, accessMark))
      yieldAll(ctx.generateConditionGuards())

      if (isSingleExpression(ast)) {
        yield("return (")
        yieldAll(codes)
        yield(")")
      }
      else {
        yieldAll(codes)
      }
      yield(endOfLine)
      yieldAll(scope.end())
      yield("}")
      ctx.inlayHints.add(InlayHintInfo(
        blockName = Source("template"),
        offset = exp.loc.startOffset,
        setting = "vue.inlayHints.inlineHandlerLeading",
        label = "\$event =>",
        paddingRight = true,
        tooltip = "`\$event` is a hidden parameter, you can use it in this callback.\n\nTo hide this hint, set `vue.inlayHints.inlineHandlerLeading` to `false` in IDE settings.\n\n[More info](https://github.com/vuejs/language-tools/issues/2445#issuecomment-1444771420)",
      ))
    }
    else {
      yieldAll(interpolation)
    }
  }
  else {
    yield("() => {}")
  }
}

fun generateModelEventExpression(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
  argsType: String,
): Sequence<Code> = sequence {
  val exp = prop.exp
  if (exp is SimpleExpressionNode) {
    val accessMark = ctx.accessLog.size
    val codes = generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = codeFeatures.verification,
      code = exp.content,
      start = exp.loc.startOffset,
      inNarrowing = true,
    ).toList()
    yieldAll(generateEventParameter(options, argsType))
    yieldAll(generateReasserts(options, ctx, accessMark))
    yieldAll(ctx.generateConditionGuards())
    val boundary = yield(Boundary.start(
      Source("template"),
      exp.loc.startOffset,
      exp.loc.endOffset,
      codeFeatures.verification,
    ))
    yield("(")
    yieldAll(codes)
    yield(")")
    yield(boundary.end())
    yield(" = \$event$endOfLine")
    yield("}")
  }
  else {
    yield("() => {}")
  }
}

// Assertion narrowing does not flow into nested closures for imports and
// `let`/`var` bindings (TS limitation), so re-assert those bindings at closure tops.
private fun generateReasserts(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  accessMark: Int,
): Sequence<Code> = sequence {
  val reasserts = linkedSetOf<String>()
  val collect = { name: String ->
    if (options.reassertBindings.contains(name) && ctx.scopes.none { name in it }) {
      reasserts.add(name)
    }
  }
  for (name in ctx.accessLog.subList(accessMark, ctx.accessLog.size).toList()) {
    collect(name)
  }
  for (condition in ctx.conditions) {
    for (name in condition.accesses) {
      collect(name)
    }
  }
  for (name in reasserts) {
    yield("${names.withDotValue}($name, ${getRefBrandArgument(options.vueCompilerOptions, options.scriptLang)})$endOfLine")
  }
}

fun isCompoundExpression(ast: FakeSourceFile): Boolean {
  if (ast.statements.none()) {
    return false
  }

  val statement = ast.statements.singleOrNull()
  if (statement != null && !statement.endsWithSemicolon()) {
    if (isExpressionStatement(statement)) {
      val node = getUnwrappedExpression(statement.expression!!)
      if (
        isArrowFunction(node)
        || isIdentifier(node)
        || isElementAccessExpression(node)
        || isPropertyAccessExpression(node)
      ) {
        return false
      }
    }
    else if (isFunctionDeclaration(statement)) {
      return false
    }
  }
  return true
}

private fun isSingleExpression(ast: FakeSourceFile): Boolean {
  val statement = ast.statements.singleOrNull()
  if (statement != null && !statement.endsWithSemicolon()) {
    if (isExpressionStatement(statement)) {
      return true
    }
  }
  return false
}
