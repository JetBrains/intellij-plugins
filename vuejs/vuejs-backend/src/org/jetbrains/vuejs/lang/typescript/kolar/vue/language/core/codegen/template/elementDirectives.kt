// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.asType
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateStringLiteralKey
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.isBuiltInDirective

fun generateElementDirectives(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Sequence<Code> = sequence {
  for (rawProp in node.props) {
    val prop = rawProp as? DirectiveNode ?: continue
    if (prop.name == "slot" || prop.name == "on" || prop.name == "model" || prop.name == "bind") continue
    if (options.isVapor && !isBuiltInDirective(prop.name)) {
      val boundary = yield(Boundary.start(
        Source("template"),
        prop.loc.startOffset,
        prop.loc.endOffset,
        codeFeatures.verification,
      ))
      // vapor custom directives receive a value getter instead of a vdom binding object
      yieldAll(generateIdentifier(options, ctx, prop))
      yield("(${names.nonNull}(null), ")
      yieldAll(generateValue(options, ctx, prop, asBindingProperty = false))
      yieldAll(generateArg(options, ctx, prop, asBindingProperty = false))
      yieldAll(generateModifiers(options, ctx, prop, "modifiers", asBindingProperty = false))
      yield(")")
      yield(boundary.end())
      yield(endOfLine)
    }
    else {
      val hookVar = ctx.getInternalVariable()
      yield("const $hookVar = ${names.asFunctionalDirective}(")
      yieldAll(generateIdentifier(options, ctx, prop))
      yield(", ${
        asType("import('${options.vueCompilerOptions.lib}').ObjectDirective", options.scriptLang)
      })${endOfLine}")
      val boundary = yield(Boundary.start(
        Source("template"),
        prop.loc.startOffset,
        prop.loc.endOffset,
        codeFeatures.verification,
      ))
      yield("$hookVar(${names.nonNull}(null), { ...${names.directiveBindingRestFields}, ")
      yieldAll(generateArg(options, ctx, prop))
      yieldAll(generateModifiers(options, ctx, prop))
      yieldAll(generateValue(options, ctx, prop))
      // Vue invokes hooks with all four arguments regardless of the declared
      // arity; the trailing args are sized to the hook's own params, so generic
      // and overloaded hooks are checked without rebuilding their signatures.
      yield("},${newLine}...${names.directiveHookArgsRest}($hookVar))")
      yield(boundary.end())
      yield(endOfLine)
    }
  }
}

private fun generateIdentifier(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
): Sequence<Code> = sequence {
  val rawName = "v-" + prop.name
  val startOffset = prop.loc.startOffset
  val boundary = yield(Boundary.start(
    Source("template"),
    startOffset,
    startOffset + rawName.length,
    codeFeatures.verification,
  ))
  yield(names.directives)
  yield(".")
  yieldAll(generateCamelized(
    rawName,
    Source("template"),
    prop.loc.startOffset,
    VueCodeInformation(
      semantic = codeFeatures.withoutHighlightAndCompletion.semantic,
      navigation = codeFeatures.withoutHighlightAndCompletion.navigation,
      verification = if (options.vueCompilerOptions.checkUnknownDirectives && !isBuiltInDirective(prop.name))
        VerificationInfo.Enabled
      else
        null,
    ),
  ))
  if (!isBuiltInDirective(prop.name)) {
    ctx.accessVariable(Source("template"), camelize(rawName), prop.loc.startOffset)
  }
  yield(boundary.end())
}

private fun generateArg(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
  asBindingProperty: Boolean = true,
): Sequence<Code> = sequence {
  val arg = prop.arg
  if (arg is SimpleExpressionNode) {
    val startOffset = arg.loc.startOffset + arg.loc.source.indexOf(arg.content)
    if (asBindingProperty) {
      val boundary = yield(Boundary.start(
        Source("template"),
        startOffset,
        startOffset + arg.content.length,
        codeFeatures.verification,
      ))
      yield("arg")
      yield(boundary.end())
      yield(": ")
    }
    if (arg.isStatic) {
      yieldAll(generateStringLiteralKey(arg.content, startOffset, codeFeatures.all))
    }
    else {
      yieldAll(generateInterpolation(
        options = options,
        ctx = ctx,
        block = options.template,
        data = codeFeatures.all,
        code = arg.content,
        start = startOffset,
        prefix = "(",
        suffix = ")",
      ))
    }
    yield(", ")
  }
  else if (!asBindingProperty) {
    yield("undefined, ")
  }
}

fun generateModifiers(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
  propertyName: String = "modifiers",
  asBindingProperty: Boolean = true,
): Sequence<Code> = sequence {
  val modifiers = prop.modifiers
  if (modifiers.isNotEmpty()) {
    if (asBindingProperty) {
      val boundary = yield(Boundary.start(
        Source("template"),
        modifiers.first().loc.startOffset - 1,
        modifiers.last().loc.endOffset,
        codeFeatures.verification,
      ))
      yield(propertyName)
      yield(boundary.end())
      yield(": ")
    }
    yield("{ ")
    for (mod in modifiers) {
      yieldAll(generateObjectProperty(options, ctx, mod.content, mod.loc.startOffset, codeFeatures.withoutHighlight))
      yield(": true, ")
    }
    yield("}, ")
  }
  else if (!asBindingProperty) {
    yield("undefined, ")
  }
}

private fun generateValue(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
  asBindingProperty: Boolean = true,
): Sequence<Code> = sequence {
  val exp = prop.exp
  if (exp is SimpleExpressionNode) {
    val boundary = yield(Boundary.start(
      Source("template"),
      exp.loc.startOffset,
      exp.loc.endOffset,
      codeFeatures.verification,
    ))
    if (asBindingProperty) {
      yield("value")
      yield(boundary.end())
      yield(": ")
      yieldAll(generatePropExp(options, ctx, prop, exp))
    }
    else {
      yield("() => ")
      yieldAll(generatePropExp(options, ctx, prop, exp))
      yield(boundary.end())
    }
    yield(", ")
  }
  else if (!asBindingProperty) {
    yield("undefined, ")
  }
}
