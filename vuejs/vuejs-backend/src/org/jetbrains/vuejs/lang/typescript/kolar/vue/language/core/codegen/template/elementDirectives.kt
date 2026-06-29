// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateStringLiteralKey
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
    val boundary = yield(Boundary.start("template", prop.loc.start.offset, codeFeatures.verification))
    yield("${names.asFunctionalDirective}(")
    yieldAll(generateIdentifier(options, ctx, prop))
    yield(", {} as import('${options.vueCompilerOptions.lib}').ObjectDirective)(null!, { ...${names.directiveBindingRestFields}, ")
    yieldAll(generateArg(options, ctx, prop))
    yieldAll(generateModifiers(options, ctx, prop))
    yieldAll(generateValue(options, ctx, prop))
    yield(" }, null!, null!)")
    yield(boundary.end(prop.loc.end.offset))
    yield(endOfLine)
  }
}

private fun generateIdentifier(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
): Sequence<Code> = sequence {
  val rawName = "v-" + prop.name
  val startOffset = prop.loc.start.offset
  val boundary = yield(Boundary.start("template", startOffset, codeFeatures.verification))
  yield(names.directives)
  yield(".")
  yieldAll(generateCamelized(
    rawName,
    "template",
    prop.loc.start.offset,
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
    ctx.accessVariable("template", camelize(rawName), prop.loc.start.offset)
  }
  yield(boundary.end(startOffset + rawName.length))
}

private fun generateArg(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
): Sequence<Code> = sequence {
  val arg = prop.arg as? SimpleExpressionNode ?: return@sequence
  val startOffset = arg.loc.start.offset + arg.loc.source.indexOf(arg.content)
  val boundary = yield(Boundary.start("template", startOffset, codeFeatures.verification))
  yield("arg")
  yield(boundary.end(startOffset + arg.content.length))
  yield(": ")
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

fun generateModifiers(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
  propertyName: String = "modifiers",
): Sequence<Code> = sequence {
  val modifiers = prop.modifiers
  if (modifiers.isEmpty()) return@sequence
  val startOffset = modifiers.first().loc.start.offset - 1
  val endOffset = modifiers.last().loc.end.offset
  val boundary = yield(Boundary.start("template", startOffset, codeFeatures.verification))
  yield(propertyName)
  yield(boundary.end(endOffset))
  yield(": { ")
  for (mod in modifiers) {
    yieldAll(generateObjectProperty(options, ctx, mod.content, mod.loc.start.offset, codeFeatures.withoutHighlight))
    yield(": true, ")
  }
  yield("}, ")
}

private fun generateValue(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
): Sequence<Code> = sequence {
  val exp = prop.exp as? SimpleExpressionNode ?: return@sequence
  val boundary = yield(Boundary.start("template", exp.loc.start.offset, codeFeatures.verification))
  yield("value")
  yield(boundary.end(exp.loc.end.offset))
  yield(": ")
  yieldAll(generatePropExp(options, ctx, prop, exp))
}
