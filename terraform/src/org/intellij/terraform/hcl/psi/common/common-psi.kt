// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

/**
 * Common PSI that could be used both for HCL2 and HIL
 */
package org.intellij.terraform.hcl.psi.common

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.IElementType


interface BaseExpression : PsiElement
interface Identifier : BaseExpression, PsiNamedElement
interface LiteralExpression : BaseExpression {
  val unquotedText: String
}


interface ParenthesizedExpression<T : BaseExpression> : BaseExpression {
  val expression: T?
}

interface UnaryExpression<T : BaseExpression> : BaseExpression {
  val operationSign: IElementType
}

interface BinaryExpression<T : BaseExpression> : BaseExpression {
  val leftOperand: T
  val rightOperand: T?
  val operationSign: IElementType
}

// Ternary operator
interface ConditionalExpression<T : BaseExpression> : BaseExpression {
  val condition: T
  val then: T?
  val otherwise: T?
}

interface MethodCallExpression<T : BaseExpression> : BaseExpression {
  val callee: T
  val parameterList: ParameterList<T>

  val method: Identifier? get() = callee as? Identifier
}

interface CollectionExpression<T : BaseExpression> : BaseExpression {
  val elements: List<T>
}

interface ParameterList<T : BaseExpression> : CollectionExpression<T>

interface SelectExpression<T : BaseExpression> : BaseExpression {
  val from: T
  val field: T?
}

interface IndexSelectExpression<T : BaseExpression> : SelectExpression<T>
