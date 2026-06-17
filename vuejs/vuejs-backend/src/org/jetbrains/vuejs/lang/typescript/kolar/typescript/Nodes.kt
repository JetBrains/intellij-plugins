// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

interface Node {
  val kind: SyntaxKind
  val pos: Int
  val end: Int

  fun getStart(sourceFile: SourceFile): Int
}

interface Expression : Node

interface ObjectLiteralExpression : Expression {
  val properties: List<Node>
}

interface StringLiteralLike : Node {
  val text: String
}

interface StringLiteral : Expression, StringLiteralLike

interface Identifier : Expression {
  val text: String
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

interface ParameterDeclaration : NamedBinding

interface VariableDeclaration : NamedBinding

interface FunctionLikeDeclaration : Node {
  val body: Node?
  val parameters: List<ParameterDeclaration>
}

interface ArrowFunction : Expression, FunctionLikeDeclaration

interface FunctionExpression : Expression, FunctionLikeDeclaration

interface AccessorDeclaration : FunctionLikeDeclaration

interface MethodDeclaration : FunctionLikeDeclaration

interface Statement : Node

interface EmptyStatement : Statement

interface ExpressionStatement : Statement {
  val expression: Node
}

interface VariableDeclarationList : Node {
  val declarations: List<VariableDeclaration>
}

interface VariableStatement : Statement {
  val declarationList: VariableDeclarationList
}

interface Block : Statement

interface FunctionDeclaration : Statement {
  val name: Identifier?
}

interface ClassDeclaration : Statement {
  val name: Identifier?
}

interface EnumDeclaration : Statement {
  val name: Identifier
}

interface ImportDeclaration : Statement {
  val moduleSpecifier: Node
  val importClause: ImportClause?
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

interface ExportDeclaration : Statement

interface ExportAssignment : Statement {
  val expression: Node
}

interface TypeAliasDeclaration : Statement, HasModifiers

interface InterfaceDeclaration : Statement, HasModifiers

interface AsExpression : Expression {
  val expression: Node
}

interface HasModifiers : Node {
  val modifiers: List<Node>?
}

interface CallExpression : Expression {
  val expression: Node
  val arguments: List<Node>
  val typeArguments: List<Node>?
}

interface ParenthesizedExpression : Expression {
  val expression: Node
}

interface PropertyAccessExpression : Expression {
  val expression: Expression
}

interface ArrayLiteralExpression : Expression

interface TypeNode : Node

interface TypeLiteralNode : TypeNode {
  val members: List<Node>
}

interface UnionTypeNode : TypeNode

interface TypeQueryNode : TypeNode {
  val exprName: Node
}

interface QualifiedName : Node {
  val left: Node
}

interface BindingPattern : Node {
  val elements: List<Node>
}

interface ArrayBindingPattern : BindingPattern

interface ObjectBindingPattern : BindingPattern

interface ComputedPropertyName : Node {
  val expression: Expression
}

interface PropertyAssignment : Node {
  val name: Node
  val initializer: Expression
}

interface ShorthandPropertyAssignment : Node {
  val name: Identifier
}

interface SpreadAssignment : Node {
  val expression: Expression
}

interface CallSignatureDeclaration : Node {
  val parameters: List<ParameterDeclaration>
}

interface NamedImports : Node {
  val elements: List<ImportSpecifier>
}