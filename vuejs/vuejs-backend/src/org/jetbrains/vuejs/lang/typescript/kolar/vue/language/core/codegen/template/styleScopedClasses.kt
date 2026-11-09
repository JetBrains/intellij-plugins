// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.yield
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrayLiteralExpression
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
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.codeFeatures
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.common.getTypeScriptAST
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.names
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.Boundary
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.endOfLine
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.generateEscaped
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.normalizeAttributeValue
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.yield

private val classNameEscapeRE = Regex("""([\\'])""")

fun generateStyleScopedClassReferences(
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
    val startOffset = exp.loc.startOffset - 1
    val ast = getTypeScriptAST(content, options)
    val literals = mutableListOf<PsiElement>()

    fun walkObjectLiteral(objectNode: PsiElement): Sequence<Code> = sequence {
      if (!isObjectLiteralExpression(objectNode)) return@sequence
      for (property in objectNode.properties) {
        when {
          isPropertyAssignment(property) -> {
            val nameIdentifier = property.nameIdentifier
            val computedPropName = property.computedPropertyName
            when {
              isIdentifier(nameIdentifier) ->
                yieldAll(generateStyleScopedClassReference(template, property.name!!, nameIdentifier.startOffset + startOffset))
              isStringLiteral(nameIdentifier) -> literals.add(nameIdentifier)
              computedPropName != null -> {
                val expr = computedPropName.expression
                if (isStringLiteralLike(expr)) literals.add(expr)
              }
            }
          }
          isShorthandPropertyAssignment(property) -> {
            val nameIdentifier = property.nameIdentifier!!

            yieldAll(generateStyleScopedClassReference(
              template,
              property.name!!,
              nameIdentifier.startOffset + startOffset,
            ))
          }
        }
      }
    }

    fun walkArrayLiteral(arrayNode: PsiElement): Sequence<Code> = sequence {
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
      val start = literal.startOffset + 1 + startOffset
      for ((className, offset) in forEachClassName(literal.stringValue!!)) {
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

fun generateStyleScopedClassReference(
  block: IRBlock,
  className: String,
  offset: Int,
  fullStart: Int = offset,
): Sequence<Code> = sequence {
  if (className.isEmpty()) {
    yield("/** @type {${names.StyleScopedClasses}['")
    yield(DataSegment(text = "", source = block.name, sourceOffset = offset, data = codeFeatures.completion))
    yield("']} */$endOfLine")
    return@sequence
  }

  yield("/** @type {${names.StyleScopedClasses}[")
  val boundary = yield(Boundary.start(
    block.name,
    fullStart,
    offset + className.length,
    codeFeatures.navigationAndCompletion,
  ))
  yield("'")
  yieldAll(generateEscaped(
    className,
    block.name,
    offset,
    boundary.features,
    classNameEscapeRE,
  ))
  yield("'")
  yield(boundary.end())
  yield("]} */$endOfLine")
}
