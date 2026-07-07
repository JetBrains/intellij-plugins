// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

import kotlin.contracts.contract

fun isStringLiteralLike(node: Node): Boolean {
  contract { returns(true) implies (node is StringLiteralLike) }
  return node is StringLiteralLike
}

fun isIdentifier(node: Node): Boolean {
  contract { returns(true) implies (node is Identifier) }
  return node is Identifier
}

fun isBindingElement(node: Node): Boolean {
  contract { returns(true) implies (node is BindingElement) }
  return node is BindingElement
}

fun isArrowFunction(node: Node): Boolean {
  contract { returns(true) implies (node is ArrowFunction) }
  return node is ArrowFunction
}

fun isFunctionDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is FunctionDeclaration) }
  return node is FunctionDeclaration
}

fun isUnionTypeNode(node: Node): Boolean {
  contract { returns(true) implies (node is UnionTypeNode) }
  return node is UnionTypeNode
}

fun isArrayBindingPattern(node: Node): Boolean {
  contract { returns(true) implies (node is ArrayBindingPattern) }
  return node is ArrayBindingPattern
}

fun isObjectBindingPattern(node: Node): Boolean {
  contract { returns(true) implies (node is ObjectBindingPattern) }
  return node is ObjectBindingPattern
}

fun isPropertyAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is PropertyAssignment) }
  return node is PropertyAssignment
}

fun isShorthandPropertyAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is ShorthandPropertyAssignment) }
  return node is ShorthandPropertyAssignment
}

fun isSpreadAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is SpreadAssignment) }
  return node is SpreadAssignment
}
