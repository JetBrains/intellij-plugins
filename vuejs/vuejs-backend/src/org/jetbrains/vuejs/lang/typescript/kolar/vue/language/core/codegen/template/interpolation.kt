// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSInitializerOwner
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.JSTypeDeclarationOwner
import com.intellij.lang.javascript.psi.TypeScriptSatisfiesExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getBindingElements
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrayBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrowFunction
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isAsExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBinaryExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBlock
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isCallExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isConditionalExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionLike
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isNonNullExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isParenthesizedExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAccessExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isSatisfiesExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isShorthandPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isSpreadAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTaggedTemplateExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeAssertionExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeNode
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeQueryNode
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isVariableDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.CommonCodegenOptions
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.identifierRE
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingNames
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getNodeText
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getStartEnd
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.isGloballyAllowed

// https://github.com/vuejs/core/blob/fb0c3ca519f1fccf52049cd6b8db3a67a669afe9/packages/compiler-core/src/transforms/transformExpression.ts#L47
private val literalWhitelist = setOf(
  "true",
  "false",
  "null",
  "this",
)

fun generateInterpolation(
  options: CommonCodegenOptions,
  ctx: TemplateCodegenContext,
  block: IRBlock,
  data: VueCodeInformation,
  code: String,
  start: Int,
  prefix: String = "",
  suffix: String = "",
): Sequence<Code> = sequence {
  if (prefix.isNotEmpty()) {
    yield(prefix)
  }

  var prevEnd = 0
  for ((name, offset, isShorthand, isCall) in forEachIdentifiers(options, ctx, code, prefix, suffix)) {
    if (isShorthand) {
      yieldAll(generateNonIdentifierCode(
        code = code.substring(prevEnd, offset + name.length),
        source = block.name,
        offset = start + prevEnd,
        data = data,
        shouldCut = prevEnd > 0,
      ))
      yield(": ")
    }
    else if (prevEnd < offset) {
      yieldAll(generateNonIdentifierCode(
        code = code.substring(prevEnd, offset),
        source = block.name,
        offset = start + prevEnd,
        data = data,
        shouldCut = prevEnd > 0,
      ))
    }

    // Access strategy, in precedence order:
    // - destructured props / imported components → direct reference
    // - template refs → direct `.value`
    // - other bindings → `.value` + `__VLS_withDotValue` assertion
    // - otherwise → `__VLS_ctx.<name>`
    if (options.destructuredProps.contains(name) || options.importedComponents.contains(name)) {
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = if (isShorthand) data.copy(__shorthandExpression = VueCodeInformation.ShorthandExpression.js) else data,
      ))
    }
    else if (options.setupRefs.contains(name)) {
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = data,
      ))
      yield(".value")
    }
    else if (options.setupBindings.contains(name)) {
      ctx.accessVariable(block.name, name, start + offset)
      if (isCall) {
        ctx.accessVariableAsCall(name)
      }
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = if (isShorthand) data.copy(__shorthandExpression = VueCodeInformation.ShorthandExpression.js) else data,
      ))
      if (!isCall) {
        yield(".value")
      }
    }
    else {
      // #1205, #1264
      val boundary = yield(Boundary.start(
        block.name,
        start + offset,
        start + offset + name.length,
        codeFeatures.verification,
      ))
      if (ctx.dollarVars.contains(name)) {
        yield(names.dollars)
      }
      else {
        ctx.accessVariable(block.name, name, start + offset)
        yield(names.ctx)
      }
      yield(".")
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = if (isShorthand) data.copy(__shorthandExpression = VueCodeInformation.ShorthandExpression.js) else data,
      ))
      yield(boundary.end())
    }

    prevEnd = offset + name.length
  }

  if (prevEnd < code.length) {
    yieldAll(generateNonIdentifierCode(
      code = code.substring(prevEnd),
      source = block.name,
      offset = start + prevEnd,
      data = data,
      shouldCut = prevEnd > 0,
    ))
  }

  if (suffix.isNotEmpty()) {
    yield(suffix)
  }
}

/**
 * Yield a code chunk, cutting the boundary character off as verification-only.
 *
 * Adjacent mappings share the boundary offset (closed interval), so both the
 * neighbouring token's end and this chunk's start claim the same source offset.
 * Downgrading the boundary character to verification-only keeps content-sensitive
 * features (rename / navigation) from firing on the neighbouring chunk.
 */
private fun generateNonIdentifierCode(
  code: String,
  source: Source,
  offset: Int,
  data: VueCodeInformation,
  shouldCut: Boolean = true,
): Sequence<Code> = sequence {
  if (code.isEmpty()) {
    return@sequence
  }
  if (!shouldCut) {
    yield(DataSegment(text = code, source = source, sourceOffset = offset, data = data))
    return@sequence
  }
  yield(DataSegment(
    text = code.substring(0, 1),
    source = source,
    sourceOffset = offset,
    data = VueCodeInformation(verification = data.verification),
  ))
  if (code.length > 1) {
    yield(DataSegment(text = code.substring(1), source = source, sourceOffset = offset + 1, data = data))
  }
}

private fun forEachIdentifiers(
  options: CommonCodegenOptions,
  ctx: TemplateCodegenContext,
  code: String,
  prefix: String,
  suffix: String,
): Sequence<IdentifierInfo> = sequence {
  if (identifierRE.matches(code) && !shouldIdentifierSkipped(ctx, code)) {
    yield(IdentifierInfo(code, 0, isShorthand = false, isCall = false))
    return@sequence
  }

  val scope = ctx.scope()
  val ast = getTypeScriptAST(prefix + code + suffix, options)
  for ((id, isShorthand, isCall) in forEachDeclarations(ast, ctx, scope, false)) {
    val text = getNodeText(id)
    if (shouldIdentifierSkipped(ctx, text)) continue
    yield(IdentifierInfo(text, getStartEnd(id).start - prefix.length, isShorthand, isCall))
  }
  scope.end()
}

private data class IdentifierInfo(
  val name: String,
  val offset: Int,
  val isShorthand: Boolean,
  val isCall: Boolean,
)

private fun forEachDeclarations(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
  inCallPosition: Boolean,
): Sequence<Triple<PsiElement, Boolean, Boolean>> = sequence {
  if (isIdentifier(node)) {
    yield(Triple(node, false, inCallPosition))
  }
  else if (isShorthandPropertyAssignment(node)) {
    yield(Triple(node.nameIdentifier!!, true, inCallPosition))
  }
  else if (isPropertyAccessExpression(node)) {
    yieldAll(forEachDeclarations(node.qualifier!!, ctx, scope, false))
  }
  else if (isCallExpression(node)) {
    node.methodExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, true)) }
    for (arg in node.arguments) {
      yieldAll(forEachDeclarations(arg, ctx, scope, false))
    }
  }
  else if (isTaggedTemplateExpression(node)) {
    node.tag?.let { yieldAll(forEachDeclarations(it, ctx, scope, true)) }
    node.templateExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
  }
  else if (isParenthesizedExpression(node)) {
    node.innerExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inCallPosition)) }
  }
  else if (isNonNullExpression(node)) {
    node.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inCallPosition)) }
  }
  // `TypeScriptAsExpression` extends `TypeScriptCastExpression`, so check it first
  else if (isAsExpression(node) || isSatisfiesExpression(node)) {
    val (expression, type) = when (node) {
      is TypeScriptAsExpression -> node.expression to node.type
      else -> (node as TypeScriptSatisfiesExpression).expression to node.type
    }
    expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inCallPosition)) }
    type?.let { yieldAll(forEachDeclarationsInTypeNode(it)) }
  }
  else if (isTypeAssertionExpression(node)) {
    node.type?.let { yieldAll(forEachDeclarationsInTypeNode(it)) }
    node.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inCallPosition)) }
  }
  else if (isBinaryExpression(node)) {
    val operator = node.operationSign
    val isLogical = operator == JSTokenTypes.OROR
                    || operator == JSTokenTypes.ANDAND
                    || operator == JSTokenTypes.QUEST_QUEST
    node.lOperand?.let { yieldAll(forEachDeclarations(it, ctx, scope, isLogical && inCallPosition)) }
    node.rOperand?.let { yieldAll(forEachDeclarations(it, ctx, scope, isLogical && inCallPosition)) }
  }
  else if (isConditionalExpression(node)) {
    node.condition?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    node.thenBranch?.let { yieldAll(forEachDeclarations(it, ctx, scope, inCallPosition)) }
    node.elseBranch?.let { yieldAll(forEachDeclarations(it, ctx, scope, inCallPosition)) }
  }
  else if (isVariableDeclaration(node)) {
    scope.declare(collectBindingNames(node))
    yieldAll(forEachDeclarationsInBinding(node, ctx, scope))
  }
  else if (isArrayBindingPattern(node) || isObjectBindingPattern(node)) {
    for (element in node.getBindingElements()) {
      yieldAll(forEachDeclarationsInBinding(element.source, ctx, scope))
    }
  }
  else if (isArrowFunction(node) || isFunctionExpression(node)) {
    yieldAll(forEachDeclarationsInFunction(node, ctx))
  }
  else if (isObjectLiteralExpression(node)) {
    for (prop in node.propertiesIncludingSpreads) {
      if (isPropertyAssignment(prop)) {
        // fix https://github.com/vuejs/language-tools/issues/1176
        val computedPropName = prop.computedPropertyName
        if (computedPropName != null) {
          yieldAll(forEachDeclarations(computedPropName.expression!!, ctx, scope, false))
        }
        yieldAll(forEachDeclarations(prop.initializer!!, ctx, scope, false))
      }
      // fix https://github.com/vuejs/language-tools/issues/1156
      else if (isShorthandPropertyAssignment(prop)) {
        yieldAll(forEachDeclarations(prop, ctx, scope, false))
      }
      // fix https://github.com/vuejs/language-tools/issues/1148#issuecomment-1094378126
      else if (isSpreadAssignment(prop)) {
        // TODO: cannot report "Spread types may only be created from object types.ts(2698)"
        yieldAll(forEachDeclarations(prop.expression!!, ctx, scope, false))
      }
      // fix https://github.com/vuejs/language-tools/issues/4604
      else if (isFunctionLike(prop) && prop.block != null) {
        yieldAll(forEachDeclarationsInFunction(prop, ctx))
      }
    }
  }
  // fix https://github.com/vuejs/language-tools/issues/1422
  else if (isTypeNode(node)) {
    yieldAll(forEachDeclarationsInTypeNode(node))
  }
  else if (isBlock(node)) {
    val scope = ctx.scope()
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarations(child, ctx, scope, false))
    }
    scope.end()
  }
  else {
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarations(child, ctx, scope, false))
    }
  }
}

private fun forEachDeclarationsInBinding(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<Triple<PsiElement, Boolean, Boolean>> = sequence {
  val type = (node as? JSTypeDeclarationOwner)?.typeElement
  if (type != null) {
    yieldAll(forEachDeclarationsInTypeNode(type))
  }
  val nameIdentifier = (node as? JSNamedElement)?.nameIdentifier
  if (nameIdentifier != null && !isIdentifier(nameIdentifier)) {
    yieldAll(forEachDeclarations(nameIdentifier, ctx, scope, false))
  }
  val initializer = (node as? JSInitializerOwner)?.initializer
  if (initializer != null) {
    yieldAll(forEachDeclarations(initializer, ctx, scope, false))
  }
}

private fun forEachDeclarationsInFunction(
  node: JSFunction,
  ctx: TemplateCodegenContext,
): Sequence<Triple<PsiElement, Boolean, Boolean>> = sequence {
  val scope = ctx.scope()
  for (param in node.parameters) {
    scope.declare(collectBindingNames(param.declarationElement!!))
    yieldAll(forEachDeclarationsInBinding(param, ctx, scope))
  }
  val body = node.block
  if (body != null) {
    yieldAll(forEachDeclarations(body, ctx, scope, false))
  }
  scope.end()
}

private fun forEachDeclarationsInTypeNode(
  node: PsiElement,
): Sequence<Triple<PsiElement, Boolean, Boolean>> = sequence {
  if (isTypeQueryNode(node)) {
    var id = node.expression!!
    while (isPropertyAccessExpression(id)) {
      id = id.qualifier ?: break
    }
    yield(Triple(id, false, false))
  }
  else {
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarationsInTypeNode(child))
    }
  }
}

fun shouldIdentifierSkipped(
  ctx: TemplateCodegenContext,
  text: String,
): Boolean =
  ctx.scopes.any { it.contains(text) }
  || isGloballyAllowed(text)
  || literalWhitelist.contains(text)
  || text == "require"
  || text.startsWith("__VLS_")
