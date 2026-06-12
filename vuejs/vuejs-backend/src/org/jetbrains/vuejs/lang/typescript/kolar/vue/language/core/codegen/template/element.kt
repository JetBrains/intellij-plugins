// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.typescript.kolar.KolarCodeInformation.SemanticInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.toString
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrayLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isComputedPropertyName
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isExpressionStatement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isParenthesizedExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isShorthandPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStringLiteral
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStringLiteralLike
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.AttributeNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.createVBindShorthandInlayHintInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateStringLiteralKey
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRegex
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.startBoundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getElementTagOffsets
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.hyphenateTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.normalizeAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.capitalize
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node as TsNode

fun generateComponent(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Sequence<Code> = sequence {
  var tag = node.tag
  var props: List<Node> = node.props
  val tagOffsets = getElementTagOffsets(node, options.template)
  var startTagOffset = tagOffsets[0]
  var endTagOffset: Int? = tagOffsets.getOrNull(1)
  var isExpression = false
  var isIsShorthand = false

  if (tag.contains('.')) {
    isExpression = true
  }
  else if (tag == "component") {
    for (rawProp in node.props) {
      val prop = rawProp as? DirectiveNode ?: continue
      val propArg = prop.arg as? SimpleExpressionNode ?: continue
      val propExp = prop.exp as? SimpleExpressionNode ?: continue
      if (prop.name != "bind" || propArg.loc.source != "is") continue
      isIsShorthand = propArg.loc.end.offset == propExp.loc.end.offset
      if (isIsShorthand) {
        ctx.inlayHints.add(createVBindShorthandInlayHintInfo(propExp.loc, "is"))
      }
      isExpression = true
      tag = propExp.content
      startTagOffset = propExp.loc.start.offset
      endTagOffset = null
      props = props.filter { it !== rawProp }
      break
    }
  }

  val componentVar = ctx.getInternalVariable()

  if (isExpression) {
    yield("const $componentVar = ")
    yieldAll(generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = if (isIsShorthand) codeFeatures.withoutHighlightAndCompletion else codeFeatures.all,
      code = tag,
      start = startTagOffset,
      prefix = "(",
      suffix = ")",
    ))
    if (endTagOffset != null) {
      yield(" || ")
      yieldAll(generateInterpolation(
        options = options,
        ctx = ctx,
        block = options.template,
        data = codeFeatures.withoutCompletion,
        code = tag,
        start = endTagOffset,
        prefix = "(",
        suffix = ")",
      ))
    }
    yield(endOfLine)
  }
  else {
    val originalNames = setOf(
      capitalize(camelize(tag)),
      camelize(tag),
      tag,
    )
    val matchedSetupConst = originalNames.find { it in options.setupConsts }
    if (matchedSetupConst != null) {
      // navigation & auto import support
      yield("const $componentVar = ")
      yieldAll(generateCamelized(
        matchedSetupConst[0].toString() + tag.substring(1),
        "template",
        startTagOffset,
        VueCodeInformation(
          verification = VerificationInfo.Enabled,
          semantic = SemanticInfo.WithOptions(shouldHighlight = { false }),
          navigation = codeFeatures.withoutHighlightAndCompletion.navigation,
          __importCompletion = true,
        ),
      ))
      if (endTagOffset != null) {
        yield(" || ")
        yieldAll(generateCamelized(
          matchedSetupConst[0].toString() + tag.substring(1),
          "template",
          endTagOffset,
          codeFeatures.withoutHighlightAndCompletion,
        ))
      }
      yield(endOfLine)
    }
    else {
      yield("let $componentVar!: ${names.WithComponent}<'$tag', ${names.LocalComponents}, ${names.GlobalComponents}")
      yield(if (originalNames.contains(options.componentName)) ", typeof ${names.export}" else ", void")
      for (name in originalNames) {
        yield(", '$name'")
      }
      yield(")[")
      yieldAll(generateStringLiteralKey(
        tag,
        startTagOffset,
        VueCodeInformation(
          semantic = SemanticInfo.WithOptions(shouldHighlight = { false }),
          verification = if (options.vueCompilerOptions.checkUnknownComponents) VerificationInfo.Enabled
          else VerificationInfo.WithFilter(shouldReport = { _, code -> code != "2339" && code != "2551" }),
        ),
      ))
      yield("]$endOfLine")

      if (identifierRegex.matches(camelize(tag))) {
        // navigation support
        yield("/** @ts-ignore @type {")
        for (offset in listOfNotNull(startTagOffset, endTagOffset)) {
          yield(" | typeof ${names.components}.")
          yieldAll(generateCamelized(tag, "template", offset, codeFeatures.navigation))
          if (tag[0] != tag[0].uppercaseChar()) {
            yield(" | typeof ${names.components}.")
            yieldAll(generateCamelized(capitalize(tag), "template", offset, codeFeatures.navigation))
          }
          if (tag.contains('-')) {
            yield(" | typeof ${names.components}[")
            yieldAll(generateStringLiteralKey(tag, offset, codeFeatures.navigation))
            yield("]")
          }
        }
        yield("} */$newLine")
        // auto import support
        yieldAll(generateCamelized(tag, "template", startTagOffset, codeFeatures.importCompletionOnly))
        yield(endOfLine)
      }
    }
  }

  val functionalVar = ctx.getInternalVariable()
  val vnodeVar = ctx.getInternalVariable()
  val ctxVar = ctx.getInternalVariable()
  val propsVar = ctx.getInternalVariable()

  var isCtxVarUsed = false
  var isPropsVarUsed = false
  val getCtxVar = { isCtxVarUsed = true; ctxVar }
  val getPropsVar = { isPropsVarUsed = true; propsVar }
  ctx.components.add(getCtxVar)

  val failedPropExps = mutableListOf<FailedPropExpressions>()
  val propCodes = generateElementProps(
    options = options,
    ctx = ctx,
    node = node,
    props = props,
    checkUnknownProps = options.vueCompilerOptions.checkUnknownProps,
    failedPropExps = failedPropExps,
  ).toList()
  val propsStr = toString(propCodes)

  yield("// @ts-ignore$newLine")
  yield("const $functionalVar = ${if (options.vueCompilerOptions.checkUnknownProps) names.asFunctionalComponent0 else names.asFunctionalComponent1}($componentVar, new $componentVar({$newLine")
  yield(propsStr)
  yield("}))$endOfLine")

  yield("const ")
  val token = yield(startBoundary("template", node.loc.start.offset, codeFeatures.doNotReportTs6133))
  yield(vnodeVar)
  yield(endBoundary(token, node.loc.end.offset))
  yield(" = $functionalVar")

  val generic = ctx.currentInfo.generic
  if (generic != null) {
    val content = generic.content
    val offset = generic.offset
    val genericToken = yield(startBoundary("template", offset, codeFeatures.verification))
    yield("<")
    yield(DataSegment(text = content, source = "template", sourceOffset = offset, data = codeFeatures.all))
    yield(">")
    yield(endBoundary(genericToken, offset + content.length))
  }

  val shouldInheritAttrs = hasVBindAttrs(options, ctx, node)

  yield("(")
  val token2 = yield(startBoundary(
    "template",
    startTagOffset,
    if (shouldInheritAttrs && options.vueCompilerOptions.checkRequiredFallthroughAttributes) VueCodeInformation()
    else codeFeatures.verification,
  ))
  yield("{")
  yield(DataSegment(
    text = "",
    source = "template",
    sourceOffset = node.loc.start.offset,
    data = VueCodeInformation(__propsCompletion = true),
  ))
  yield(newLine)
  yieldAll(propCodes)
  yield("}")
  yield(endBoundary(token2, startTagOffset + tag.length))
  yield(", ...${names.functionalComponentArgsRest}($functionalVar)$endOfLine")

  yieldAll(generateFailedExpressions(options, ctx, failedPropExps))
  yieldAll(generateElementEvents(
    options = options,
    ctx = ctx,
    node = node,
    componentOriginalVar = componentVar,
    getCtxVar = getCtxVar,
    getPropsVar = getPropsVar,
  ))
  yieldAll(generateElementDirectives(options, ctx, node))

  val templateRef = getTemplateRef(node)
  val isSingleRoot = node in ctx.singleRootNodes
                     && !options.vueCompilerOptions.fallthroughComponentNames.contains(hyphenateTag(tag))

  if (templateRef != null || isSingleRoot) {
    val componentInstanceVar = ctx.getInternalVariable()
    yield("var $componentInstanceVar!: Parameters<NonNullable<typeof ${getCtxVar()}['expose']>>[0]")
    yield(endOfLine)

    if (templateRef != null) {
      var typeExp = "typeof ${ctx.getHoistVariable(componentInstanceVar)} | null"
      if (ctx.inVFor) {
        typeExp = "($typeExp)[]"
      }
      ctx.addTemplateRef(templateRef.first, typeExp, templateRef.second)
    }
    if (isSingleRoot) {
      ctx.singleRootElTypes.add("NonNullable<typeof $componentInstanceVar>['\$el']")
    }
  }

  if (shouldInheritAttrs) {
    if (options.vueCompilerOptions.checkRequiredFallthroughAttributes) {
      val restsVar = ctx.getInternalVariable()
      yield("var $restsVar = ${names.omit}(${getPropsVar()}, {\n${propsStr}})$endOfLine")
      ctx.inheritedAttrVars.add(restsVar)
    }
    else {
      ctx.inheritedAttrVars.add(getPropsVar())
    }
  }

  yieldAll(generateStyleScopedClassReferences(options, node))

  val slotDir = node.props.filterIsInstance<DirectiveNode>().find { it.name == "slot" }
  if (slotDir != null || node.children.isNotEmpty()) {
    yieldAll(generateVSlot(options, ctx, node, slotDir, getCtxVar()))
  }

  if (isCtxVarUsed) {
    yield("var $ctxVar!: ${names.FunctionalComponentCtx}<typeof $componentVar, typeof $vnodeVar>$endOfLine")
  }
  if (isPropsVarUsed) {
    yield("var $propsVar!: ${names.FunctionalComponentProps}<typeof $componentVar, typeof $vnodeVar>$endOfLine")
  }
  ctx.components.removeLast()
}

fun generateElement(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Sequence<Code> = sequence {
  val tagOffsets = getElementTagOffsets(node, options.template)
  val startTagOffset = tagOffsets[0]
  val endTagOffset: Int? = tagOffsets.getOrNull(1)
  val failedPropExps = mutableListOf<FailedPropExpressions>()

  yield("${if (options.vueCompilerOptions.checkUnknownProps) names.asFunctionalElement0 else names.asFunctionalElement1}(${names.intrinsics}")
  yieldAll(generatePropertyAccess(
    options = options,
    ctx = ctx,
    code = node.tag,
    offset = startTagOffset,
    features = codeFeatures.withoutHighlightAndCompletion,
  ))
  if (endTagOffset != null) {
    yield(", ")
    yield(names.intrinsics)
    yieldAll(generatePropertyAccess(
      options = options,
      ctx = ctx,
      code = node.tag,
      offset = endTagOffset,
      features = codeFeatures.withoutHighlightAndCompletion,
    ))
  }
  yield(")(")
  val token = yield(startBoundary("template", startTagOffset, codeFeatures.verification))
  yield("{$newLine")
  yieldAll(generateElementProps(
    options = options,
    ctx = ctx,
    node = node,
    props = node.props,
    checkUnknownProps = options.vueCompilerOptions.checkUnknownProps,
    failedPropExps = failedPropExps,
  ))
  yield("}")
  yield(endBoundary(token, startTagOffset + node.tag.length))
  yield(")$endOfLine")

  yieldAll(generateFailedExpressions(options, ctx, failedPropExps))
  yieldAll(generateElementDirectives(options, ctx, node))

  val templateRef = getTemplateRef(node)
  if (templateRef != null) {
    var typeExp = "${names.Elements}['${node.tag}']"
    if (ctx.inVFor) {
      typeExp += "[]"
    }
    ctx.addTemplateRef(templateRef.first, typeExp, templateRef.second)
  }
  if (node in ctx.singleRootNodes) {
    ctx.singleRootElTypes.add("${names.Elements}['${node.tag}']")
  }

  if (hasVBindAttrs(options, ctx, node)) {
    ctx.inheritedAttrVars.add("${names.intrinsics}.${node.tag}")
  }

  yieldAll(generateStyleScopedClassReferences(options, node))

  for (child in node.children) {
    yieldAll(generateTemplateChild(options, ctx, child))
  }
}

fun generateFragment(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Sequence<Code> = sequence {
  val startTagOffset = getElementTagOffsets(node, options.template)[0]

  // special case for <template v-for="..." :key="..." />
  if (node.props.isNotEmpty()) {
    yield("__VLS_asFunctionalElement(${names.intrinsics}.template)(")
    val token = yield(startBoundary("template", startTagOffset, codeFeatures.verification))
    yield("{$newLine")
    yieldAll(generateElementProps(
      options = options,
      ctx = ctx,
      node = node,
      props = node.props,
      checkUnknownProps = options.vueCompilerOptions.checkUnknownProps,
    ))
    yield("}")
    yield(endBoundary(token, startTagOffset + node.tag.length))
    yield(")$endOfLine")
  }

  for (child in node.children) {
    yieldAll(generateTemplateChild(options, ctx, child))
  }
}

private fun generateStyleScopedClassReferences(
  options: TemplateCodegenOptions,
  node: ElementNode,
): Sequence<Code> = sequence {
  val template = options.template
  for (rawProp in node.props) {
    val attrProp = rawProp as? AttributeNode
    if (attrProp != null && attrProp.name == "class" && attrProp.value != null) {
      val (text, start) = normalizeAttributeValue(attrProp.value!!)
      for ((className, offset) in forEachClassName(text)) {
        yieldAll(generateStyleScopedClassReference(template, className, start + offset))
      }
      continue
    }
    val dirProp = rawProp as? DirectiveNode ?: continue
    val arg = dirProp.arg as? SimpleExpressionNode ?: continue
    val exp = dirProp.exp as? SimpleExpressionNode ?: continue
    if (arg.content != "class") continue

    val content = "(" + exp.content + ")"
    val startOffset = exp.loc.start.offset - 1
    val ast = getTypeScriptAST(template, content)
    val literals = mutableListOf<TsNode>()

    fun walkObjectLiteral(objectNode: TsNode): Sequence<Code> = sequence {
      if (!isObjectLiteralExpression(objectNode)) return@sequence
      for (property in objectNode.properties) {
        when {
          isPropertyAssignment(property) -> {
            val name = property.name
            when {
              isIdentifier(name) ->
                yieldAll(generateStyleScopedClassReference(template, name.text, name.end - name.text.length + startOffset))
              isStringLiteral(name) -> literals.add(name)
              isComputedPropertyName(name) -> {
                val expr = name.expression
                if (isStringLiteralLike(expr)) literals.add(expr)
              }
            }
          }
          isShorthandPropertyAssignment(property) ->
            yieldAll(generateStyleScopedClassReference(
              template,
              property.name.text,
              property.name.end - property.name.text.length + startOffset,
            ))
        }
      }
    }

    fun walkArrayLiteral(arrayNode: TsNode): Sequence<Code> = sequence {
      for (element in forEachNode(arrayNode)) {
        when {
          isStringLiteralLike(element) -> literals.add(element)
          isObjectLiteralExpression(element) -> yieldAll(walkObjectLiteral(element))
        }
      }
    }

    for (astNode in forEachNode(ast)) {
      if (!isExpressionStatement(astNode)) continue
      val exprNode = astNode.expression
      if (!isParenthesizedExpression(exprNode)) continue
      val innerExpr = forEachNode(exprNode).firstOrNull() ?: continue
      when {
        isStringLiteralLike(innerExpr) -> literals.add(innerExpr)
        isArrayLiteralExpression(innerExpr) -> yieldAll(walkArrayLiteral(innerExpr))
        isObjectLiteralExpression(innerExpr) -> yieldAll(walkObjectLiteral(innerExpr))
      }
    }

    for (literal in literals) {
      if (!isStringLiteralLike(literal)) continue
      val start = literal.end - literal.text.length - 1 + startOffset
      for ((className, offset) in forEachClassName(literal.text)) {
        yieldAll(generateStyleScopedClassReference(template, className, start + offset))
      }
    }
  }
}

private fun forEachClassName(content: String): Sequence<Pair<String, Int>> = sequence {
  var offset = 0
  for (className in content.split(' ')) {
    yield(Pair(className, offset))
    offset += className.length + 1
  }
}

private fun generateFailedExpressions(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  failedPropExps: List<FailedPropExpressions>,
): Sequence<Code> = sequence {
  for ((node, prefix, suffix) in failedPropExps) {
    yieldAll(generateInterpolation(
      options = options,
      ctx = ctx,
      block = options.template,
      data = codeFeatures.all,
      code = node.loc.source,
      start = node.loc.start.offset,
      prefix = prefix,
      suffix = suffix,
    ))
    yield(endOfLine)
  }
}

private fun getTemplateRef(node: ElementNode): Pair<String, Int>? {
  for (rawProp in node.props) {
    val prop = rawProp as? AttributeNode ?: continue
    if (prop.name == "ref" && prop.value != null) {
      return normalizeAttributeValue(prop.value!!)
    }
  }
  return null
}

private fun hasVBindAttrs(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Boolean =
  options.vueCompilerOptions.fallthroughAttributes && (
    (options.inheritAttrs && node in ctx.singleRootNodes)
    || node.props.any { prop ->
      prop is DirectiveNode
      && prop.name == "bind"
      && (prop.exp as? SimpleExpressionNode)?.loc?.source == "\$attrs"
    }
                                                      )
