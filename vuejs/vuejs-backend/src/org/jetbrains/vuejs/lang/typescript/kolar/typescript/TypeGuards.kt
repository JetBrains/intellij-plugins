// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.vuejs.lang.typescript.kolar.typescript

import kotlin.contracts.contract

fun isIdentifier(node: Node): Boolean {
  contract { returns(true) implies (node is Identifier) }
  return node is Identifier
}

fun isObjectLiteralExpression(node: Node): Boolean {
  contract { returns(true) implies (node is ObjectLiteralExpression) }
  return node is ObjectLiteralExpression
}

fun isPropertyAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is PropertyAssignment) }
  return node is PropertyAssignment
}

fun isStringLiteral(node: Node): Boolean {
  contract { returns(true) implies (node is StringLiteral) }
  return node is StringLiteral
}

fun isStringLiteralLike(node: Node): Boolean {
  contract { returns(true) implies (node is StringLiteralLike) }
  return node is StringLiteralLike
}

fun isCallExpression(node: Node): Boolean {
  contract { returns(true) implies (node is CallExpression) }
  return node is CallExpression
}

fun isParenthesizedExpression(node: Node): Boolean {
  contract { returns(true) implies (node is ParenthesizedExpression) }
  return node is ParenthesizedExpression
}

fun isExportAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is ExportAssignment) }
  return node is ExportAssignment
}

fun isImportDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ImportDeclaration) }
  return node is ImportDeclaration
}

fun isExportDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ExportDeclaration) }
  return node is ExportDeclaration
}

fun isEmptyStatement(node: Node): Boolean {
  contract { returns(true) implies (node is EmptyStatement) }
  return node is EmptyStatement
}

fun isImportEqualsDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ImportEqualsDeclaration) }
  return node is ImportEqualsDeclaration
}

fun isTypeAliasDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is TypeAliasDeclaration) }
  return node is TypeAliasDeclaration
}

fun isInterfaceDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is InterfaceDeclaration) }
  return node is InterfaceDeclaration
}

fun isVariableDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is VariableDeclaration) }
  return node is VariableDeclaration
}

fun isObjectBindingPattern(node: Node): Boolean {
  contract { returns(true) implies (node is ObjectBindingPattern) }
  return node is ObjectBindingPattern
}

fun isTypeLiteralNode(node: Node): Boolean {
  contract { returns(true) implies (node is TypeLiteralNode) }
  return node is TypeLiteralNode
}

fun isCallSignatureDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is CallSignatureDeclaration) }
  return node is CallSignatureDeclaration
}

fun isUnionTypeNode(node: Node): Boolean {
  contract { returns(true) implies (node is UnionTypeNode) }
  return node is UnionTypeNode
}

fun isStatement(node: Node): Boolean {
  contract { returns(true) implies (node is Statement) }
  return node is Statement
}

fun isFunctionLike(node: Node): Boolean {
  contract { returns(true) implies (node is FunctionLikeDeclaration) }
  return node is FunctionLikeDeclaration
}

fun isArrayBindingPattern(node: Node): Boolean {
  contract { returns(true) implies (node is ArrayBindingPattern) }
  return node is ArrayBindingPattern
}

fun isBindingElement(node: Node): Boolean {
  contract { returns(true) implies (node is BindingElement) }
  return node is BindingElement
}

fun isVariableStatement(node: Node): Boolean {
  contract { returns(true) implies (node is VariableStatement) }
  return node is VariableStatement
}

fun isFunctionDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is FunctionDeclaration) }
  return node is FunctionDeclaration
}

fun isClassDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ClassDeclaration) }
  return node is ClassDeclaration
}

fun isEnumDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is EnumDeclaration) }
  return node is EnumDeclaration
}

fun isNamedImports(node: Node): Boolean {
  contract { returns(true) implies (node is NamedImports) }
  return node is NamedImports
}

fun isExpressionStatement(node: Node): Boolean {
  contract { returns(true) implies (node is ExpressionStatement) }
  return node is ExpressionStatement
}

fun isArrowFunction(node: Node): Boolean {
  contract { returns(true) implies (node is ArrowFunction) }
  return node is ArrowFunction
}

fun isArrayLiteralExpression(node: Node): Boolean {
  contract { returns(true) implies (node is ArrayLiteralExpression) }
  return node is ArrayLiteralExpression
}

fun isComputedPropertyName(node: Node): Boolean {
  contract { returns(true) implies (node is ComputedPropertyName) }
  return node is ComputedPropertyName
}

fun isShorthandPropertyAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is ShorthandPropertyAssignment) }
  return node is ShorthandPropertyAssignment
}

fun isPropertyAccessExpression(node: Node): Boolean {
  contract { returns(true) implies (node is PropertyAccessExpression) }
  return node is PropertyAccessExpression
}

fun isFunctionExpression(node: Node): Boolean {
  contract { returns(true) implies (node is FunctionExpression) }
  return node is FunctionExpression
}

fun isSpreadAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is SpreadAssignment) }
  return node is SpreadAssignment
}

fun isTypeNode(node: Node): Boolean {
  contract { returns(true) implies (node is TypeNode) }
  return node is TypeNode
}

fun isBlock(node: Node): Boolean {
  contract { returns(true) implies (node is Block) }
  return node is Block
}

fun isTypeQueryNode(node: Node): Boolean {
  contract { returns(true) implies (node is TypeQueryNode) }
  return node is TypeQueryNode
}