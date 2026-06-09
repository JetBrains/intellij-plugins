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