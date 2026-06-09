// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

interface ObjectLiteralExpression : Expression

interface StringLiteral : Expression, StringLiteralLike

interface Identifier : Expression {
  val text: String
}

interface BindingElement : Node

interface ParameterDeclaration : Node

interface VariableDeclaration : Node

interface ArrowFunction : Expression

interface FunctionExpression : Expression

interface AccessorDeclaration : Node

interface MethodDeclaration : Node

interface Statement : Node

interface EmptyStatement : Statement

interface ExpressionStatement : Statement

interface VariableStatement : Statement

interface Block : Statement

interface FunctionDeclaration : Statement

interface ClassDeclaration : Statement

interface EnumDeclaration : Statement

interface ImportDeclaration : Statement

interface ImportEqualsDeclaration : Statement

interface ExportDeclaration : Statement

interface ExportAssignment : Statement

interface TypeAliasDeclaration : Statement

interface InterfaceDeclaration : Statement

interface CallExpression : Expression

interface ParenthesizedExpression : Expression

interface PropertyAccessExpression : Expression

interface ArrayLiteralExpression : Expression

interface TypeNode : Node

interface TypeLiteralNode : TypeNode

interface UnionTypeNode : TypeNode

interface TypeQueryNode : TypeNode

interface FunctionLikeDeclaration : Node

interface ArrayBindingPattern : Node

interface ObjectBindingPattern : Node

interface ComputedPropertyName : Node

interface PropertyAssignment : Node

interface ShorthandPropertyAssignment : Node

interface SpreadAssignment : Node

interface CallSignatureDeclaration : Node

interface NamedImports : Node