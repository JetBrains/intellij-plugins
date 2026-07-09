// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT_TYPE
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifier
import com.intellij.lang.javascript.JSStringUtil.unquoteStringLiteralValue
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isAsExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isClassDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isEnumDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isImportDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isNonNullExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isParenthesizedExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isSatisfiesExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStatement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeAssertionExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isVariableStatement
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getStartEnd

fun parseBindingRanges(
  ast: JSEmbeddedContent,
  extensions: List<String>,
): Pair<List<TextRange<*>>, List<TextRange<*>>> {
  val bindings = mutableListOf<TextRange<*>>()
  val components = mutableListOf<TextRange<*>>()

  for (node in forEachNode(ast)) {
    if (isVariableStatement(node)) {
      for (decl in node.variables) {
        bindings.addAll(collectBindingRanges(decl.nameIdentifier!!))
      }
    }
    else if (isFunctionDeclaration(node)) {
      node.nameIdentifier?.let { bindings.add(getStartEnd(it)) }
    }
    else if (isClassDeclaration(node)) {
      node.nameIdentifier?.let { bindings.add(getStartEnd(it)) }
    }
    else if (isEnumDeclaration(node)) {
      node.nameIdentifier?.let { bindings.add(getStartEnd(it)) }
    }

    if (isImportDeclaration(node)) {
      val moduleName = node.fromClause?.referenceText
                         ?.let { unquoteStringLiteralValue(it) }
                       ?: continue

      if (node.importExportPrefixKind != IMPORT_TYPE) {
        // default
        val defaultImportName = node.importedBindings
          .firstOrNull { !it.isNamespaceImport() }
          ?.nameIdentifier

        if (defaultImportName != null) {
          if (extensions.any { moduleName.endsWith(it) }) {
            components.add(getStartEnd(defaultImportName))
          }
          else {
            bindings.add(getStartEnd(defaultImportName))
          }
        }

        // specifiers
        val namedImports = node.namedImports
        if (namedImports != null) {
          for (specifier in namedImports.specifiers) {
            if (specifier.specifierKind == ES6ImportExportSpecifier.ImportExportSpecifierKind.IMPORT_TYPE)
              continue

            val nameIdentifier = specifier.alias?.nameIdentifier ?: specifier
            if (specifier.isDefault
                && extensions.any { moduleName.endsWith(it) }
            ) {
              components.add(getStartEnd(nameIdentifier))
            }
            else {
              bindings.add(getStartEnd(nameIdentifier))
            }
          }
        }

        // namespace
        val namespaceImportName = node.importedBindings
          .firstOrNull { it.isNamespaceImport() }
          ?.nameIdentifier

        if (namespaceImportName != null) {
          bindings.add(getStartEnd(namespaceImportName))
        }
      }
    }
  }

  return Pair(bindings, components)
}

fun getClosestMultiLineCommentRange(
  node: PsiElement,
  parents: List<PsiElement>,
): TextRange<*>? {
  var currentNode = node
  for (i in parents.indices.reversed()) {
    if (isStatement(currentNode)) break
    currentNode = parents[i]
  }
  val comment = getCommentsBefore(currentNode)
    .lastOrNull { it.tokenType == JSTokenTypes.C_STYLE_COMMENT }

  if (comment != null) {
    return TextRange(
      node = currentNode,
      start = comment.startOffset,
      end = comment.endOffset,
    )
  }
  return null
}

fun getUnwrappedExpression(node: PsiElement): PsiElement {
  var current = node
  while (true) {
    current = when {
      isParenthesizedExpression(current) -> current.innerExpression ?: break
      isTypeAssertionExpression(current) -> current.expression ?: break
      isAsExpression(current) -> current.expression ?: break
      isNonNullExpression(current) -> current.expression ?: break
      isSatisfiesExpression(current) -> current.expression ?: break
      else -> break
    }
  }
  return current
}
