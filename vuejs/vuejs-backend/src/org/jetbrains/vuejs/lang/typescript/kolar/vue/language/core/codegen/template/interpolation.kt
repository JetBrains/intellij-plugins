// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSInitializerOwner
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.JSTypeDeclarationOwner
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getBindingElements
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrayBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrowFunction
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBlock
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionLike
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAccessExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isShorthandPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isSpreadAssignment
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
  for ((name, offset, isShorthand) in forEachIdentifiers(options, ctx, code, prefix, suffix)) {
    if (isShorthand) {
      yield(DataSegment(
        text = code.substring(prevEnd, offset + name.length),
        source = block.name,
        sourceOffset = start + prevEnd,
        data = data,
      ))
      yield(": ")
    }
    else if (prevEnd < offset) {
      yield(DataSegment(
        text = code.substring(prevEnd, offset),
        source = block.name,
        sourceOffset = start + prevEnd,
        data = data,
      ))
    }

    if (options.setupRefs.contains(name)) {
      yield(DataSegment(
        text = name,
        source = block.name,
        sourceOffset = start + offset,
        data = data,
      ))
      yield(".value")
    }
    else {
      // #1205, #1264
      val boundary = yield(Boundary.start(block.name, start + offset, codeFeatures.verification))
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
      yield(boundary.end(start + offset + name.length))
    }

    prevEnd = offset + name.length
  }

  if (prevEnd < code.length) {
    yield(DataSegment(
      text = code.substring(prevEnd),
      source = block.name,
      sourceOffset = start + prevEnd,
      data = data,
    ))
  }

  if (suffix.isNotEmpty()) {
    yield(suffix)
  }
}

private fun forEachIdentifiers(
  options: CommonCodegenOptions,
  ctx: TemplateCodegenContext,
  code: String,
  prefix: String,
  suffix: String,
): Sequence<Triple<String, Int, Boolean>> = sequence {
  if (identifierRE.matches(code) && !shouldIdentifierSkipped(ctx, code)) {
    yield(Triple(code, 0, false))
    return@sequence
  }

  val scope = ctx.scope()
  val ast = getTypeScriptAST(prefix + code + suffix, options)
  for ((id, isShorthand) in forEachDeclarations(ast, ctx, scope)) {
    val text = getNodeText(id)
    if (shouldIdentifierSkipped(ctx, text)) continue
    yield(Triple(text, getStartEnd(id).start - prefix.length, isShorthand))
  }
  scope.end()
}

private fun forEachDeclarations(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<Pair<PsiElement, Boolean>> = sequence {
  if (isIdentifier(node)) {
    yield(Pair(node, false))
  }
  else if (isShorthandPropertyAssignment(node)) {
    yield(Pair(node.nameIdentifier!!, true))
  }
  else if (isPropertyAccessExpression(node)) {
    yieldAll(forEachDeclarations(node.qualifier!!, ctx, scope))
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
          yieldAll(forEachDeclarations(computedPropName.expression!!, ctx, scope))
        }
        yieldAll(forEachDeclarations(prop.initializer!!, ctx, scope))
      }
      // fix https://github.com/vuejs/language-tools/issues/1156
      else if (isShorthandPropertyAssignment(prop)) {
        yieldAll(forEachDeclarations(prop, ctx, scope))
      }
      // fix https://github.com/vuejs/language-tools/issues/1148#issuecomment-1094378126
      else if (isSpreadAssignment(prop)) {
        // TODO: cannot report "Spread types may only be created from object types.ts(2698)"
        yieldAll(forEachDeclarations(prop.expression!!, ctx, scope))
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
      yieldAll(forEachDeclarations(child, ctx, scope))
    }
    scope.end()
  }
  else {
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarations(child, ctx, scope))
    }
  }
}

private fun forEachDeclarationsInBinding(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<Pair<PsiElement, Boolean>> = sequence {
  val type = (node as? JSTypeDeclarationOwner)?.typeElement
  if (type != null) {
    yieldAll(forEachDeclarationsInTypeNode(type))
  }
  val nameIdentifier = (node as? JSNamedElement)?.nameIdentifier
  if (nameIdentifier != null && !isIdentifier(nameIdentifier)) {
    yieldAll(forEachDeclarations(nameIdentifier, ctx, scope))
  }
  val initializer = (node as? JSInitializerOwner)?.initializer
  if (initializer != null) {
    yieldAll(forEachDeclarations(initializer, ctx, scope))
  }
}

private fun forEachDeclarationsInFunction(
  node: JSFunction,
  ctx: TemplateCodegenContext,
): Sequence<Pair<PsiElement, Boolean>> = sequence {
  val scope = ctx.scope()
  for (param in node.parameters) {
    scope.declare(collectBindingNames(param.declarationElement!!))
    yieldAll(forEachDeclarationsInBinding(param, ctx, scope))
  }
  val body = node.block
  if (body != null) {
    yieldAll(forEachDeclarations(body, ctx, scope))
  }
  scope.end()
}

private fun forEachDeclarationsInTypeNode(
  node: PsiElement,
): Sequence<Pair<PsiElement, Boolean>> = sequence {
  if (isTypeQueryNode(node)) {
    var id = node.expression!!
    while (isPropertyAccessExpression(id)) {
      id = id.qualifier ?: break
    }
    yield(Pair(id, false))
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
