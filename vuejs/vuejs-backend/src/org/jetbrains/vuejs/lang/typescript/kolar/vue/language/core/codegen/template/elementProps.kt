// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.picomatch.isMatch
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.AttributeNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ConstantTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.TextNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.createVBindShorthandInlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateUnicode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRegex
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.startBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.hyphenateAttr
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.hyphenateTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.normalizeAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize

fun generateElementProps(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
  props: List<Node>,
  checkUnknownProps: Boolean,
  failedPropExps: MutableList<FailedPropExpressions>? = null,
): Sequence<Code> = sequence {
  val isComponent = node.tagType == ElementTypes.COMPONENT

  // First pass: v-on
  for (rawProp in props) {
    val prop = rawProp as? DirectiveNode ?: continue
    if (prop.name != "on") continue
    val propArg = prop.arg as? SimpleExpressionNode
    if (propArg != null && !propArg.loc.source.startsWith("[") && !propArg.loc.source.endsWith("]")) {
      if (!isComponent) {
        yield("...{ ")
        yieldAll(generateEventArg(options, propArg.loc.source, propArg.loc.start.offset))
        yield(": ")
        yieldAll(generateEventExpression(options, ctx, prop))
        yield("},")
      }
      else {
        yield("...{ '${camelize("on-" + propArg.loc.source)}': {} as any },")
      }
      yield(newLine)
    }
    else if (
      propArg != null && prop.exp is SimpleExpressionNode
      && propArg.loc.source.startsWith("[") && propArg.loc.source.endsWith("]")
    ) {
      failedPropExps?.add(FailedPropExpressions(propArg, "(", ")"))
      failedPropExps?.add(FailedPropExpressions(prop.exp as SimpleExpressionNode, "() => {", "}"))
    }
    else if (prop.arg == null && prop.exp is SimpleExpressionNode) {
      failedPropExps?.add(FailedPropExpressions(prop.exp as SimpleExpressionNode, "(", ")"))
    }
  }

  // Second pass: v-bind, v-model, attributes
  for (rawProp in props) {
    val directiveProp = rawProp as? DirectiveNode
    val attrProp = rawProp as? AttributeNode
    when {
      directiveProp != null
      && (directiveProp.name == "bind" && directiveProp.arg is SimpleExpressionNode || directiveProp.name == "model")
      && (directiveProp.exp == null || directiveProp.exp is SimpleExpressionNode) -> {
        var propName: String?
        val simpleArg = directiveProp.arg as? SimpleExpressionNode
        if (simpleArg != null) {
          propName = if (simpleArg.constType == ConstantTypes.CAN_STRINGIFY) simpleArg.content else simpleArg.loc.source
        }
        else {
          propName = getModelPropName(node, options.vueCompilerOptions)
        }
        if (propName == null || options.vueCompilerOptions.dataAttributes.any { isMatch(propName, it) }) {
          val simpleExp = directiveProp.exp as? SimpleExpressionNode
          if (simpleExp != null && simpleExp.constType != ConstantTypes.CAN_STRINGIFY) {
            failedPropExps?.add(FailedPropExpressions(simpleExp, "(", ")"))
          }
          continue
        }
        if (directiveProp.name == "bind" && directiveProp.modifiers.any { it.content == "prop" || it.content == "attr" }) {
          propName = propName.substring(1)
        }
        val shouldSpread = propName == "style" || propName == "class"
        val shouldCamelize = getShouldCamelize(options, node, directiveProp, propName)
        val features = getPropsCodeFeatures(checkUnknownProps)
        if (shouldSpread) yield("...{ ")
        val token = yield(startBoundary("template", directiveProp.loc.start.offset, codeFeatures.verification))
        if (directiveProp.arg != null) {
          yieldAll(generateObjectProperty(options, ctx, propName, directiveProp.arg!!.loc.start.offset, features, shouldCamelize))
        }
        else {
          val token2 = yield(startBoundary("template", directiveProp.loc.start.offset, codeFeatures.withoutHighlightAndCompletion))
          yield(propName)
          yield(endBoundary(token2, directiveProp.loc.start.offset + "v-model".length))
        }
        yield(": ")
        val argLoc = directiveProp.arg?.loc ?: directiveProp.loc
        val token3 = yield(startBoundary("template", argLoc.start.offset, codeFeatures.verification))
        yieldAll(generatePropExp(options, ctx, directiveProp, directiveProp.exp as? SimpleExpressionNode))
        yield(endBoundary(token3, argLoc.end.offset))
        yield(endBoundary(token, directiveProp.loc.end.offset))
        if (shouldSpread) yield(" }")
        yield(",$newLine")
        if (isComponent && directiveProp.name == "model" && directiveProp.modifiers.isNotEmpty()) {
          val propertyName = if (directiveProp.arg is SimpleExpressionNode) {
            val arg = directiveProp.arg as SimpleExpressionNode
            if (!arg.isStatic) "[${names.tryAsConstant}(`\${${arg.content}}Modifiers`)]"
            else camelize(propName) + "Modifiers"
          }
          else "modelModifiers"
          yieldAll(generateModifiers(options, ctx, directiveProp, propertyName))
          yield(newLine)
        }
      }
      attrProp != null -> {
        if (options.vueCompilerOptions.dataAttributes.any { isMatch(attrProp.name, it) }) continue
        val shouldSpread = attrProp.name == "style" || attrProp.name == "class"
        val shouldCamelize = getShouldCamelize(options, node, attrProp, attrProp.name)
        val features = getPropsCodeFeatures(checkUnknownProps)
        if (shouldSpread) yield("...{ ")
        val token = yield(startBoundary("template", attrProp.loc.start.offset, codeFeatures.verification))
        val prefix = options.template.content[attrProp.loc.start.offset]
        if (prefix == '.' || prefix == '#') {
          for (char in attrProp.name) {
            yield(DataSegment(text = char.toString(), source = "template", sourceOffset = attrProp.loc.start.offset, data = features))
          }
        }
        else {
          yieldAll(generateObjectProperty(options, ctx, attrProp.name, attrProp.loc.start.offset, features, shouldCamelize))
        }
        yield(": ")
        when {
          attrProp.name == "style" -> yield("{}")
          attrProp.value != null -> yieldAll(generateAttrValue(attrProp.value!!, codeFeatures.withoutNavigation))
          else -> yield("true")
        }
        yield(endBoundary(token, attrProp.loc.end.offset))
        if (shouldSpread) yield(" }")
        yield(",$newLine")
      }
      directiveProp != null && directiveProp.name == "bind" && directiveProp.arg == null && directiveProp.exp is SimpleExpressionNode -> {
        val simpleExp = directiveProp.exp as SimpleExpressionNode
        if (simpleExp.loc.source == "\$attrs") {
          failedPropExps?.add(FailedPropExpressions(simpleExp, "(", ")"))
        }
        else {
          val token = yield(startBoundary("template", simpleExp.loc.start.offset, codeFeatures.verification))
          yield("...")
          yieldAll(generatePropExp(options, ctx, directiveProp, simpleExp))
          yield(endBoundary(token, simpleExp.loc.end.offset))
          yield(",$newLine")
        }
      }
    }
  }
}

fun generatePropExp(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  prop: DirectiveNode,
  exp: SimpleExpressionNode?,
): Sequence<Code> = sequence {
  if (exp == null) {
    yield("{}")
  }
  else if (prop.arg?.loc?.start?.offset != prop.exp?.loc?.start?.offset) {
    yieldAll(generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = codeFeatures.all,
      code = exp.loc.source,
      start = exp.loc.start.offset,
      prefix = "(",
      suffix = ")",
    ))
  }
  else {
    val propVariableName = camelize(exp.loc.source)
    if (identifierRegex.matches(propVariableName)) {
      val codes = generateCamelized(
        exp.loc.source,
        "template",
        exp.loc.start.offset,
        VueCodeInformation(
          semantic = codeFeatures.withoutHighlightAndCompletion.semantic,
          navigation = codeFeatures.withoutHighlightAndCompletion.navigation,
          verification = codeFeatures.withoutHighlightAndCompletion.verification,
          __shorthandExpression = VueCodeInformation.ShorthandExpression.html,
        ),
      ).toList()
      when {
        shouldIdentifierSkipped(ctx, propVariableName) -> yieldAll(codes)
        options.setupRefs.contains(propVariableName) -> {
          yieldAll(codes)
          yield(".value")
        }
        else -> {
          ctx.accessVariable("template", propVariableName, exp.loc.start.offset)
          yield(names.ctx)
          yield(".")
          yieldAll(codes)
        }
      }
      ctx.inlayHints.add(createVBindShorthandInlayHintInfo(prop.loc, propVariableName))
    }
  }
}

private fun generateAttrValue(
  node: TextNode,
  features: VueCodeInformation,
): Sequence<Code> = sequence {
  val quote = if (node.loc.source.startsWith("'")) "'" else "\""
  val (content, offset) = normalizeAttributeValue(node)
  yield(quote)
  yieldAll(generateUnicode(content, offset, features))
  yield(quote)
}

private fun getShouldCamelize(
  options: TemplateCodegenOptions,
  node: ElementNode,
  prop: Node,
  propName: String,
): Boolean {
  val isComponentOrSlot = node.tagType == ElementTypes.COMPONENT || node.tagType == ElementTypes.SLOT
  val propArgOk = prop !is DirectiveNode || (prop.arg as? SimpleExpressionNode)?.isStatic == true
  return isComponentOrSlot && propArgOk
         && hyphenateAttr(propName) == propName
         && (node.tagType == ElementTypes.SLOT || !options.vueCompilerOptions.htmlAttributes.any { isMatch(propName, it) })
}

private fun getPropsCodeFeatures(
  checkUnknownProps: Boolean,
): VueCodeInformation =
  VueCodeInformation(
    verification = if (checkUnknownProps) codeFeatures.verification.verification else codeFeatures.doNotReportTs2353AndTs2561.verification,
    semantic = codeFeatures.withoutHighlightAndCompletion.semantic,
    navigation = codeFeatures.withoutHighlightAndCompletion.navigation,
    __propsCompletion = true,
  )

private fun getModelPropName(node: ElementNode, vueCompilerOptions: VueCompilerOptions): String? {
  val modelPropNames = vueCompilerOptions.experimentalModelPropName ?: return "modelValue"
  for ((modelName, tags) in modelPropNames) {
    for (tag in tags.keys) {
      if (node.tag == tag || node.tag == hyphenateTag(tag)) {
        return modelName.ifEmpty { null }
      }
    }
  }
  return "modelValue"
}
