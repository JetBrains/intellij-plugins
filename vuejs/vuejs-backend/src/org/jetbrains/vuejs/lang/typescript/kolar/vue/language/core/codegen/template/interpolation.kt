// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.FunctionLikeDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Identifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.NamedBinding
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.QualifiedName
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrowFunction
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBindingElement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBlock
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isComputedPropertyName
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionLike
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
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
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.getTypeScriptAST
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
  options: HasSetupRefs,
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
  for ((name, offset, isShorthand) in forEachIdentifiers(ctx, block, code, prefix, suffix)) {
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
  ctx: TemplateCodegenContext,
  block: IRBlock,
  code: String,
  prefix: String,
  suffix: String,
): Sequence<Triple<String, Int, Boolean>> = sequence {
  if (identifierRE.matches(code) && !shouldIdentifierSkipped(ctx, code)) {
    yield(Triple(code, 0, false))
    return@sequence
  }

  val scope = ctx.scope()
  val ast = getTypeScriptAST(block, prefix + code + suffix)
  for ((id, isShorthand) in forEachDeclarations(ast, ast, ctx, scope)) {
    val text = getNodeText(id, ast)
    if (shouldIdentifierSkipped(ctx, text)) continue
    yield(Triple(text, getStartEnd(id).start - prefix.length, isShorthand))
  }
  scope.end()
}

private fun forEachDeclarations(
  node: Node,
  ast: SourceFile,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<Pair<Identifier, Boolean>> = sequence {
  if (isIdentifier(node)) {
    yield(Pair(node, false))
  }
  else if (isShorthandPropertyAssignment(node)) {
    yield(Pair(node.name, true))
  }
  else if (isPropertyAccessExpression(node)) {
    yieldAll(forEachDeclarations(node.expression, ast, ctx, scope))
  }
  else if (isVariableDeclaration(node)) {
    scope.declare(collectBindingNames(node.name, ast))
    yieldAll(forEachDeclarationsInBinding(node, ast, ctx, scope))
  }
  else if (node is org.jetbrains.vuejs.lang.typescript.kolar.typescript.BindingPattern) {
    for (element in node.elements) {
      if (isBindingElement(element)) {
        yieldAll(forEachDeclarationsInBinding(element, ast, ctx, scope))
      }
    }
  }
  else if (isArrowFunction(node) || isFunctionExpression(node)) {
    yieldAll(forEachDeclarationsInFunction(node, ast, ctx))
  }
  else if (isObjectLiteralExpression(node)) {
    for (prop in node.properties) {
      if (isPropertyAssignment(prop)) {
        // fix https://github.com/vuejs/language-tools/issues/1176
        val propName = prop.name
        if (isComputedPropertyName(propName)) {
          yieldAll(forEachDeclarations(propName.expression, ast, ctx, scope))
        }
        yieldAll(forEachDeclarations(prop.initializer, ast, ctx, scope))
      }
      // fix https://github.com/vuejs/language-tools/issues/1156
      else if (isShorthandPropertyAssignment(prop)) {
        yieldAll(forEachDeclarations(prop, ast, ctx, scope))
      }
      // fix https://github.com/vuejs/language-tools/issues/1148#issuecomment-1094378126
      else if (isSpreadAssignment(prop)) {
        // TODO: cannot report "Spread types may only be created from object types.ts(2698)"
        yieldAll(forEachDeclarations(prop.expression, ast, ctx, scope))
      }
      // fix https://github.com/vuejs/language-tools/issues/4604
      else if (isFunctionLike(prop) && prop.body != null) {
        yieldAll(forEachDeclarationsInFunction(prop, ast, ctx))
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
      yieldAll(forEachDeclarations(child, ast, ctx, scope))
    }
    scope.end()
  }
  else {
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarations(child, ast, ctx, scope))
    }
  }
}

private fun forEachDeclarationsInBinding(
  node: NamedBinding,
  ast: SourceFile,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<Pair<Identifier, Boolean>> = sequence {
  val type = node.type
  if (type != null) {
    yieldAll(forEachDeclarationsInTypeNode(type))
  }
  if (!isIdentifier(node.name)) {
    yieldAll(forEachDeclarations(node.name, ast, ctx, scope))
  }
  val initializer = node.initializer
  if (initializer != null) {
    yieldAll(forEachDeclarations(initializer, ast, ctx, scope))
  }
}

private fun forEachDeclarationsInFunction(
  node: FunctionLikeDeclaration,
  ast: SourceFile,
  ctx: TemplateCodegenContext,
): Sequence<Pair<Identifier, Boolean>> = sequence {
  val scope = ctx.scope()
  for (param in node.parameters) {
    scope.declare(collectBindingNames(param.name, ast))
    yieldAll(forEachDeclarationsInBinding(param, ast, ctx, scope))
  }
  val body = node.body
  if (body != null) {
    yieldAll(forEachDeclarations(body, ast, ctx, scope))
  }
  scope.end()
}

private fun forEachDeclarationsInTypeNode(
  node: Node,
): Sequence<Pair<Identifier, Boolean>> = sequence {
  if (isTypeQueryNode(node)) {
    var id: Node = node.exprName
    while (!isIdentifier(id)) {
      id = (id as QualifiedName).left
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
