// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

import kotlin.contracts.contract

interface Node {
  val kind: SyntaxKind
  val pos: Int
  val end: Int

  fun getStart(sourceFile: SourceFile): Int
  
  fun getChildren(): Array<out Node>
}

interface Expression : Node

interface ObjectLiteralExpression : Expression {
  val properties: List<Node>
}

fun isObjectLiteralExpression(node: Node): Boolean {
  contract { returns(true) implies (node is ObjectLiteralExpression) }
  return node is ObjectLiteralExpression
}

interface StringLiteralLike : Node {
  val text: String
}

fun isStringLiteralLike(node: Node): Boolean {
  contract { returns(true) implies (node is StringLiteralLike) }
  return node is StringLiteralLike
}

interface StringLiteral : Expression, StringLiteralLike

fun isStringLiteral(node: Node): Boolean {
  contract { returns(true) implies (node is StringLiteral) }
  return node is StringLiteral
}

interface Identifier : Expression {
  val text: String
}

fun isIdentifier(node: Node): Boolean {
  contract { returns(true) implies (node is Identifier) }
  return node is Identifier
}

// Common interface for BindingElement | ParameterDeclaration | VariableDeclaration
interface NamedBinding : Node {
  val name: Node
  val type: TypeNode?
  val initializer: Expression?
}

interface BindingElement : NamedBinding {
  val dotDotDotToken: Any?
}

fun isBindingElement(node: Node): Boolean {
  contract { returns(true) implies (node is BindingElement) }
  return node is BindingElement
}

interface ParameterDeclaration : NamedBinding

interface VariableDeclaration : NamedBinding

fun isVariableDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is VariableDeclaration) }
  return node is VariableDeclaration
}

interface FunctionLikeDeclaration : Node {
  val body: Node?
  val parameters: List<ParameterDeclaration>
}

fun isFunctionLike(node: Node): Boolean {
  contract { returns(true) implies (node is FunctionLikeDeclaration) }
  return node is FunctionLikeDeclaration
}

interface ArrowFunction : Expression, FunctionLikeDeclaration

fun isArrowFunction(node: Node): Boolean {
  contract { returns(true) implies (node is ArrowFunction) }
  return node is ArrowFunction
}

interface FunctionExpression : Expression, FunctionLikeDeclaration

fun isFunctionExpression(node: Node): Boolean {
  contract { returns(true) implies (node is FunctionExpression) }
  return node is FunctionExpression
}

interface AccessorDeclaration : FunctionLikeDeclaration

interface MethodDeclaration : FunctionLikeDeclaration

interface Statement : Node

fun isStatement(node: Node): Boolean {
  contract { returns(true) implies (node is Statement) }
  return node is Statement
}

interface EmptyStatement : Statement

fun isEmptyStatement(node: Node): Boolean {
  contract { returns(true) implies (node is EmptyStatement) }
  return node is EmptyStatement
}

interface ExpressionStatement : Statement {
  val expression: Node
}

fun isExpressionStatement(node: Node): Boolean {
  contract { returns(true) implies (node is ExpressionStatement) }
  return node is ExpressionStatement
}

interface VariableDeclarationList : Node {
  val declarations: List<VariableDeclaration>
}

interface VariableStatement : Statement {
  val declarationList: VariableDeclarationList
}

fun isVariableStatement(node: Node): Boolean {
  contract { returns(true) implies (node is VariableStatement) }
  return node is VariableStatement
}

interface Block : Statement

fun isBlock(node: Node): Boolean {
  contract { returns(true) implies (node is Block) }
  return node is Block
}

interface FunctionDeclaration : Statement {
  val name: Identifier?
}

fun isFunctionDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is FunctionDeclaration) }
  return node is FunctionDeclaration
}

interface ClassDeclaration : Statement {
  val name: Identifier?
}

fun isClassDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ClassDeclaration) }
  return node is ClassDeclaration
}

interface EnumDeclaration : Statement {
  val name: Identifier
}

fun isEnumDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is EnumDeclaration) }
  return node is EnumDeclaration
}

interface ImportDeclaration : Statement {
  val moduleSpecifier: Node
  val importClause: ImportClause?
}

fun isImportDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ImportDeclaration) }
  return node is ImportDeclaration
}

interface ImportClause : Node {
  val isTypeOnly: Boolean
  val name: Identifier?
  val namedBindings: Node?
}

interface ImportSpecifier : Node {
  val isTypeOnly: Boolean
  val propertyName: Node?
  val name: Identifier
}

interface NamespaceImport : Node {
  val name: Identifier
}

interface ImportEqualsDeclaration : Statement

fun isImportEqualsDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ImportEqualsDeclaration) }
  return node is ImportEqualsDeclaration
}

interface ExportDeclaration : Statement

fun isExportDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is ExportDeclaration) }
  return node is ExportDeclaration
}

interface ExportAssignment : Statement {
  val expression: Node
}

fun isExportAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is ExportAssignment) }
  return node is ExportAssignment
}

interface TypeAliasDeclaration : Statement, HasModifiers

fun isTypeAliasDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is TypeAliasDeclaration) }
  return node is TypeAliasDeclaration
}

interface InterfaceDeclaration : Statement, HasModifiers

fun isInterfaceDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is InterfaceDeclaration) }
  return node is InterfaceDeclaration
}

interface AsExpression : Expression {
  val expression: Node
}

fun isAsExpression(node: Node): Boolean {
  contract { returns(true) implies (node is AsExpression) }
  return node is AsExpression
}

interface NonNullExpression : Expression {
  val expression: Node
}

fun isNonNullExpression(node: Node): Boolean {
  contract { returns(true) implies (node is NonNullExpression) }
  return node is NonNullExpression
}

interface TypeAssertionExpression : Expression {
  val expression: Node
}

fun isTypeAssertionExpression(node: Node): Boolean {
  contract { returns(true) implies (node is TypeAssertionExpression) }
  return node is TypeAssertionExpression
}

interface ElementAccessExpression : Expression

fun isElementAccessExpression(node: Node): Boolean {
  contract { returns(true) implies (node is ElementAccessExpression) }
  return node is ElementAccessExpression
}

interface SatisfiesExpression : Expression {
  val expression: Node
}

fun isSatisfiesExpression(node: Node): Boolean {
  contract { returns(true) implies (node is SatisfiesExpression) }
  return node is SatisfiesExpression
}

interface HasModifiers : Node {
  val modifiers: List<Node>?
}

interface CallExpression : Expression {
  val expression: Node
  val arguments: List<Node>
  val typeArguments: List<Node>?
}

fun isCallExpression(node: Node): Boolean {
  contract { returns(true) implies (node is CallExpression) }
  return node is CallExpression
}

interface ParenthesizedExpression : Expression {
  val expression: Node
}

fun isParenthesizedExpression(node: Node): Boolean {
  contract { returns(true) implies (node is ParenthesizedExpression) }
  return node is ParenthesizedExpression
}

interface PropertyAccessExpression : Expression {
  val expression: Expression
}

fun isPropertyAccessExpression(node: Node): Boolean {
  contract { returns(true) implies (node is PropertyAccessExpression) }
  return node is PropertyAccessExpression
}

interface ArrayLiteralExpression : Expression

fun isArrayLiteralExpression(node: Node): Boolean {
  contract { returns(true) implies (node is ArrayLiteralExpression) }
  return node is ArrayLiteralExpression
}

interface TypeNode : Node

fun isTypeNode(node: Node): Boolean {
  contract { returns(true) implies (node is TypeNode) }
  return node is TypeNode
}

interface TypeLiteralNode : TypeNode {
  val members: List<Node>
}

fun isTypeLiteralNode(node: Node): Boolean {
  contract { returns(true) implies (node is TypeLiteralNode) }
  return node is TypeLiteralNode
}

interface UnionTypeNode : TypeNode

fun isUnionTypeNode(node: Node): Boolean {
  contract { returns(true) implies (node is UnionTypeNode) }
  return node is UnionTypeNode
}

interface TypeQueryNode : TypeNode {
  val exprName: Node
}

fun isTypeQueryNode(node: Node): Boolean {
  contract { returns(true) implies (node is TypeQueryNode) }
  return node is TypeQueryNode
}

interface QualifiedName : Node {
  val left: Node
}

interface BindingPattern : Node {
  val elements: List<Node>
}

interface ArrayBindingPattern : BindingPattern

fun isArrayBindingPattern(node: Node): Boolean {
  contract { returns(true) implies (node is ArrayBindingPattern) }
  return node is ArrayBindingPattern
}

interface ObjectBindingPattern : BindingPattern

fun isObjectBindingPattern(node: Node): Boolean {
  contract { returns(true) implies (node is ObjectBindingPattern) }
  return node is ObjectBindingPattern
}

interface ComputedPropertyName : Node {
  val expression: Expression
}

fun isComputedPropertyName(node: Node): Boolean {
  contract { returns(true) implies (node is ComputedPropertyName) }
  return node is ComputedPropertyName
}

interface PropertyAssignment : Node {
  val name: Node
  val initializer: Expression
}

fun isPropertyAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is PropertyAssignment) }
  return node is PropertyAssignment
}

interface ShorthandPropertyAssignment : Node {
  val name: Identifier
}

fun isShorthandPropertyAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is ShorthandPropertyAssignment) }
  return node is ShorthandPropertyAssignment
}

interface SpreadAssignment : Node {
  val expression: Expression
}

fun isSpreadAssignment(node: Node): Boolean {
  contract { returns(true) implies (node is SpreadAssignment) }
  return node is SpreadAssignment
}

interface CallSignatureDeclaration : Node {
  val parameters: List<ParameterDeclaration>
}

fun isCallSignatureDeclaration(node: Node): Boolean {
  contract { returns(true) implies (node is CallSignatureDeclaration) }
  return node is CallSignatureDeclaration
}

interface NamedImports : Node {
  val elements: List<ImportSpecifier>
}

fun isNamedImports(node: Node): Boolean {
  contract { returns(true) implies (node is NamedImports) }
  return node is NamedImports
}
