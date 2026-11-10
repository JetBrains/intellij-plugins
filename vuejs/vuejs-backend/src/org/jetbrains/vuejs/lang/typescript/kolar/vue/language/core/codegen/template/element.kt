// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.typescript.kolar.KolarCodeInformation.SemanticInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.toString
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
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateCamelized
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateStringLiteralKey
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateTypedVar
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRE
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.newLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getElementTagOffsets
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.hyphenateTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.normalizeAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.camelize
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.capitalize

private val lineCommentRE = Regex("//(.*)$", RegexOption.MULTILINE)

fun generateComponent(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Sequence<Code> = sequence {
  var tag = node.tag
  var props: List<Node> = node.props
  val tagOffsets = getElementTagOffsets(node)
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
      isIsShorthand = propArg.loc.endOffset == propExp.loc.endOffset
      if (isIsShorthand) {
        ctx.inlayHints.add(createVBindShorthandInlayHintInfo(propExp.loc, "is"))
      }
      isExpression = true
      tag = propExp.content
      startTagOffset = propExp.loc.startOffset
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
    val matchedImportedComponent = originalNames.find { it in options.importedComponents }
    if (matchedImportedComponent != null) {
      // navigation & auto import support
      yield("const $componentVar = ")
      yieldAll(generateCamelized(
        matchedImportedComponent[0].toString() + tag.substring(1),
        Source("template"),
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
          matchedImportedComponent[0].toString() + tag.substring(1),
          Source("template"),
          endTagOffset,
          codeFeatures.withoutHighlightAndCompletion,
        ))
      }
      yield(endOfLine)
    }
    else {
      yieldAll(generateTypedVar("let", componentVar, options.scriptLang) {
        yield("${names.WithComponent}<'$tag', ${names.LocalComponents}, ${names.GlobalComponents}")
        yield(if (originalNames.contains(options.componentName)) ", typeof ${names.export}" else ", void")
        yield(", ${options.vueCompilerOptions.checkUnknownComponents}")
        for (name in originalNames) {
          yield(", '$name'")
        }
        yield(">[")
        yieldAll(generateStringLiteralKey(
          tag,
          startTagOffset,
          VueCodeInformation(
            semantic = SemanticInfo.WithOptions(shouldHighlight = { false }),
            verification = VerificationInfo.Enabled,
          ),
        ))
        yield("]")
      })

      if (identifierRE.matches(camelize(tag))) {
        // navigation support
        yield("/** @ts-ignore @type {")
        for (offset in listOfNotNull(startTagOffset, endTagOffset)) {
          yield(" | typeof ${names.components}.")
          yieldAll(generateCamelized(tag, Source("template"), offset, codeFeatures.navigation))
          if (tag[0] != tag[0].uppercaseChar()) {
            yield(" | typeof ${names.components}.")
            yieldAll(generateCamelized(capitalize(tag), Source("template"), offset, codeFeatures.navigation))
          }
          if (tag.contains('-')) {
            yield(" | typeof ${names.components}[")
            yieldAll(generateStringLiteralKey(tag, offset, codeFeatures.navigation))
            yield("]")
          }
        }
        yield("} */$newLine")
        // auto import support
        yieldAll(generateCamelized(tag, Source("template"), startTagOffset, codeFeatures.importCompletionOnly))
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

  // The instance type comes from a real construct call with a copy of the props: only a call resolves the
  // overloads of the component and infers its type parameters from the props, including intersections of
  // construct signatures such as `DefineComponent<...> & (new <T>(props: { items: T[] }, slots) => ...)` of
  // Vuetify's `genericComponent`. Inferring from the constructor type alone takes the last overload and loses
  // the type parameters of intersections. The two `@ts-ignore`s cover what the call reports for valid templates:
  // - components that are not constructable (functional components, plain objects, `{}`) or abstract;
  // - construct signatures of other arities (`new () => ...`, Vuetify's `(props, slots)`);
  // - excess or weakly matching props, since attributes and events are passed along with the props;
  // - diagnostics of the copy itself: its expressions have the parameter types of the constructor as contextual
  //   type (`any` for `DefineComponent`), so function expressions report implicit `any`, and typing it like
  //   `$props` instead would report every props error a second time. The copy is not mapped to the template.
  yield("// @ts-ignore$newLine")
  yield("const $functionalVar = ${if (options.vueCompilerOptions.checkUnknownProps) names.asFunctionalComponent0 else names.asFunctionalComponent1}($componentVar, new $componentVar({$newLine")
  yield("// @ts-ignore$newLine")
  yield(
    propsStr
      .replace(lineCommentRE) { "/*${it.groupValues[1].replace("*/", "*\\/")} */" }
      .replace("\n", " ")
  )
  yield("}))$endOfLine")

  yield("const ")
  val boundary = yield(Boundary.start(
    Source("template"),
    node.loc.startOffset,
    node.loc.endOffset,
    codeFeatures.doNotReportTs6133,
  ))
  yield(vnodeVar)
  yield(boundary.end())
  yield(" = $functionalVar")

  val generic = ctx.getCommentInfo().generic
  if (generic != null) {
    val content = generic.content
    val offset = generic.offset
    val genericBoundary = yield(Boundary.start(
      Source("template"),
      offset,
      offset + content.length,
      codeFeatures.verification,
    ))
    yield("<")
    yield(DataSegment(text = content, source = Source("template"), sourceOffset = offset, data = codeFeatures.all))
    yield(">")
    yield(genericBoundary.end())
  }

  val shouldInheritAttrs = hasVBindAttrs(options, ctx, node)

  yield("(")
  val boundary2 = yield(Boundary.start(
    Source("template"),
    startTagOffset,
    startTagOffset + tag.length,
    if (shouldInheritAttrs && options.vueCompilerOptions.checkRequiredFallthroughAttributes) VueCodeInformation()
    else codeFeatures.verification,
  ))
  yield("{")
  yield(DataSegment(
    text = "",
    source = Source("template"),
    sourceOffset = node.loc.startOffset,
    data = VueCodeInformation(__propsCompletion = true),
  ))
  yield(newLine)
  yieldAll(propCodes)
  yield("}")
  yield(boundary2.end())
  yield(", ...${names.functionalComponentArgsRest}($functionalVar))$endOfLine")

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
  val inferRootEl = ctx.dollarVars.contains("\$el") || options.vueCompilerOptions.inferComponentDollarEl
  val inferTemplateRefs = options.vueCompilerOptions.inferTemplateDollarRefs
                          || options.vueCompilerOptions.inferComponentDollarRefs
                          || options.setupRefs.isNotEmpty()

  if ((templateRef != null && inferTemplateRefs) || (isSingleRoot && inferRootEl)) {
    val componentInstanceVar = ctx.getInternalVariable()
    yieldAll(generateTypedVar("var", componentInstanceVar, options.scriptLang) {
      yield("Parameters<NonNullable<typeof ${getCtxVar()}['expose']>>[0]")
    })

    if (templateRef != null && inferTemplateRefs) {
      var typeExp = "typeof ${ctx.getHoistVariable(componentInstanceVar)} | null"
      if (ctx.inVFor) {
        typeExp = "($typeExp)[]"
      }
      ctx.addTemplateRef(templateRef.first, typeExp, templateRef.second)
    }
    if (isSingleRoot && inferRootEl) {
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
  val hasSlots = slotDir != null || node.children.isNotEmpty()

  // declared ahead of the slot code that reads them: JS has no `var x!: T`,
  // so a later declaration would be read before assignment (TS2454)
  if (isCtxVarUsed || hasSlots) {
    yieldAll(generateTypedVar("var", ctxVar, options.scriptLang) {
      yield("${names.ExtractComponentContext}<typeof $componentVar, typeof $vnodeVar>")
    })
  }
  if (isPropsVarUsed) {
    yieldAll(generateTypedVar("var", propsVar, options.scriptLang) {
      yield("${names.ExtractComponentProps}<typeof $componentVar, typeof $vnodeVar>")
    })
  }

  if (hasSlots) {
    yieldAll(generateVSlot(options, ctx, node, slotDir, ctxVar))
  }
  ctx.components.removeLast()
}

fun generateElement(
  options: TemplateCodegenOptions,
  ctx: TemplateCodegenContext,
  node: ElementNode,
): Sequence<Code> = sequence {
  val tagOffsets = getElementTagOffsets(node)
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
  val boundary = yield(Boundary.start(
    Source("template"),
    startTagOffset,
    startTagOffset + node.tag.length,
    codeFeatures.verification,
  ))
  yield("{$newLine")
  yieldAll(generateElementProps(
    options = options,
    ctx = ctx,
    node = node,
    props = node.props,
    checkUnknownProps = options.vueCompilerOptions.checkUnknownProps,
    failedPropExps = failedPropExps,
    attrsType = "typeof ${names.intrinsics}['${node.tag}']",
  ))
  yield("}")
  yield(boundary.end())
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
  if (
    node in ctx.singleRootNodes && (ctx.dollarVars.contains("\$el") || options.vueCompilerOptions.inferComponentDollarEl)
  ) {
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
  val startTagOffset = getElementTagOffsets(node)[0]
  val failedPropExps = mutableListOf<FailedPropExpressions>()

  // special case for <template v-for="..." :key="..." />
  if (node.props.isNotEmpty()) {
    yield("${if (options.vueCompilerOptions.checkUnknownProps) names.asFunctionalElement0 else names.asFunctionalElement1}(${names.intrinsics}.template)(")
    val boundary = yield(Boundary.start(
      Source("template"),
      startTagOffset,
      startTagOffset + node.tag.length,
      codeFeatures.verification,
    ))
    yield("{$newLine")
    yieldAll(generateElementProps(
      options = options,
      ctx = ctx,
      node = node,
      props = node.props,
      checkUnknownProps = options.vueCompilerOptions.checkUnknownProps,
      failedPropExps = failedPropExps,
      attrsType = "typeof ${names.intrinsics}.template",
    ))
    yield("}")
    yield(boundary.end())
    yield(")$endOfLine")

    yieldAll(generateFailedExpressions(options, ctx, failedPropExps))
  }

  for (child in node.children) {
    yieldAll(generateTemplateChild(options, ctx, child))
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
      start = node.loc.startOffset,
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
