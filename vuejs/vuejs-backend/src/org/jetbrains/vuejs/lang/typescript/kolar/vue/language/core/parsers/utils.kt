// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.parsers

import org.jetbrains.vuejs.lang.typescript.kolar.typescript.NamespaceImport
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.Node
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.SyntaxKind
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.forEachChild
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getLeadingCommentRanges
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isClassDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isEnumDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isImportDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isNamedImports
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isStatement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isVariableStatement
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.TextRange
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingRanges
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getNodeText
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getStartEnd

fun parseBindingRanges(
  ast: SourceFile,
  extensions: List<String>,
): Pair<List<TextRange<*>>, List<TextRange<*>>> {
  val bindings = mutableListOf<TextRange<*>>()
  val components = mutableListOf<TextRange<*>>()

  forEachChild(ast) { node ->
    if (isVariableStatement(node)) {
      for (decl in node.declarationList.declarations) {
        bindings.addAll(collectBindingRanges(decl.name, ast))
      }
    }
    else if (isFunctionDeclaration(node)) {
      node.name?.let { bindings.add(getStartEnd(it, ast)) }
    }
    else if (isClassDeclaration(node)) {
      node.name?.let { bindings.add(getStartEnd(it, ast)) }
    }
    else if (isEnumDeclaration(node)) {
      bindings.add(getStartEnd(node.name, ast))
    }

    if (isImportDeclaration(node)) {
      val moduleName = getNodeText(node.moduleSpecifier, ast).let { it.substring(1, it.length - 1) }
      val importClause = node.importClause
      if (importClause != null && !importClause.isTypeOnly) {
        importClause.name?.let { name ->
          if (extensions.any { moduleName.endsWith(it) }) components.add(getStartEnd(name, ast))
          else bindings.add(getStartEnd(name, ast))
        }
        val namedBindings = importClause.namedBindings
        if (namedBindings != null) {
          if (isNamedImports(namedBindings)) {
            for (element in namedBindings.elements) {
              if (element.isTypeOnly) continue
              val propName = element.propertyName
              if (propName != null && getNodeText(propName, ast) == "default"
                  && extensions.any { moduleName.endsWith(it) }) {
                components.add(getStartEnd(element.name, ast))
              }
              else {
                bindings.add(getStartEnd(element.name, ast))
              }
            }
          }
          else if (namedBindings is NamespaceImport) {
            bindings.add(getStartEnd(namedBindings.name, ast))
          }
        }
      }
    }
    null
  }

  return Pair(bindings, components)
}

fun getClosestMultiLineCommentRange(
  node: Node,
  parents: List<Node>,
  ast: SourceFile,
): TextRange<*>? {
  var currentNode = node
  for (i in parents.indices.reversed()) {
    if (isStatement(currentNode)) break
    currentNode = parents[i]
  }
  val comment = getLeadingCommentRanges(ast.text, currentNode.pos)
    ?.reversed()
    ?.find { it.kind == SyntaxKind.MultiLineCommentTrivia }
  if (comment != null) {
    return TextRange(node = currentNode, start = comment.pos, end = comment.end)
  }
  return null
}
