// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT_TYPE
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifier
import com.intellij.lang.javascript.JSStringUtil.unquoteStringLiteralValue
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSVarStatement
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
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingNames
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getNodeText

object BindingFlag {
  const val None: Int = 0

  // Bindings re-asserted per closure by the template codegen (imports, `let`/`var`).
  const val Variable: Int = 1
  const val Const: Int = 1 shl 1
  const val Component: Int = 1 shl 2
}

fun parseBindings(
  ast: JSEmbeddedContent,
  componentExtensions: List<String>,
): Map<String, Int> {
  val bindings = linkedMapOf<String, Int>()

  for (node in forEachNode(ast)) {
    if (isVariableStatement(node)) {
      val flags = if (node.varKeyword == JSVarStatement.VarKeyword.CONST)
        BindingFlag.None
      else
        BindingFlag.Variable
      for (decl in node.variables) {
        for (name in collectBindingNames(decl.nameIdentifier!!)) {
          bindings[name] = flags
        }
      }
    }
    else if (isFunctionDeclaration(node)) {
      node.nameIdentifier?.let { bindings[getNodeText(it)] = BindingFlag.Const }
    }
    else if (isClassDeclaration(node)) {
      node.nameIdentifier?.let { bindings[getNodeText(it)] = BindingFlag.Const }
    }
    else if (isEnumDeclaration(node)) {
      node.nameIdentifier?.let { bindings[getNodeText(it)] = BindingFlag.Const }
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
          if (componentExtensions.any { moduleName.endsWith(it) }) {
            bindings[getNodeText(defaultImportName)] = BindingFlag.Const or BindingFlag.Component
          }
          else {
            bindings[getNodeText(defaultImportName)] = BindingFlag.Variable
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
                && componentExtensions.any { moduleName.endsWith(it) }
            ) {
              bindings[getNodeText(nameIdentifier)] = BindingFlag.Const or BindingFlag.Component
            }
            else {
              bindings[getNodeText(nameIdentifier)] = BindingFlag.Variable
            }
          }
        }

        // namespace
        val namespaceImportName = node.importedBindings
          .firstOrNull { it.isNamespaceImport() }
          ?.nameIdentifier

        if (namespaceImportName != null) {
          bindings[getNodeText(namespaceImportName)] = BindingFlag.Variable
        }
      }
    }
  }

  return bindings
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
