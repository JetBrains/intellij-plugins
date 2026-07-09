// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.typescript

import com.intellij.lang.ecmascript6.psi.ES6ComputedName
import com.intellij.lang.ecmascript6.psi.ES6ExportDeclaration
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.ES6NamedImports
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSBlockStatement
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSDestructuringArray
import com.intellij.lang.javascript.psi.JSDestructuringElement
import com.intellij.lang.javascript.psi.JSDestructuringObject
import com.intellij.lang.javascript.psi.JSEmptyStatement
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionDeclaration
import com.intellij.lang.javascript.psi.JSFunctionExpression
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSSpreadExpression
import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.TypeScriptSatisfiesExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptCallSignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptCastExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnum
import com.intellij.lang.javascript.psi.ecma6.TypeScriptImportStatement
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptNotNullExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptUnionOrIntersectionType
import com.intellij.psi.PsiElement
import kotlin.contracts.contract

fun isObjectLiteralExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSObjectLiteralExpression) }
  return node is JSObjectLiteralExpression
}

fun isStringLiteralLike(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSLiteralExpression) }
  return node is JSLiteralExpression && node.isStringLiteral
}

fun isStringLiteral(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSLiteralExpression) }
  return node is JSLiteralExpression && node.isStringLiteral
}

fun isIdentifier(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSReferenceExpression) }
  return node is JSReferenceExpression && node.qualifier == null
}

fun isBindingElement(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSDestructuringElement) }
  return node is JSDestructuringElement
}

fun isVariableDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSVariable) }
  return node is JSVariable
}

fun isFunctionLike(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSFunction) }
  return node is JSFunction
}

fun isArrowFunction(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSFunction) }
  return node is JSFunction && node.isArrowFunction
}

fun isFunctionExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSFunctionExpression) }
  return node is JSFunctionExpression
}

fun isStatement(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSStatement) }
  return node is JSStatement
}

fun isEmptyStatement(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSEmptyStatement) }
  return node is JSEmptyStatement
}

fun isExpressionStatement(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSExpressionStatement) }
  return node is JSExpressionStatement
}

fun isVariableStatement(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSVarStatement) }
  return node is JSVarStatement
}

fun isBlock(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSBlockStatement) }
  return node is JSBlockStatement
}

fun isFunctionDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSFunctionDeclaration) }
  return node is JSFunctionDeclaration
}

fun isClassDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptClass) }
  return node is TypeScriptClass
}

fun isEnumDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptEnum) }
  return node is TypeScriptEnum
}

fun isImportDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is ES6ImportDeclaration) }
  return node is ES6ImportDeclaration
}

fun isImportEqualsDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptImportStatement) }
  return node is TypeScriptImportStatement
}

fun isExportDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is ES6ExportDeclaration) }
  return node is ES6ExportDeclaration
}

fun isExportAssignment(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSExportAssignment) }
  return node is JSExportAssignment
}

fun isTypeAliasDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptTypeAlias) }
  return node is TypeScriptTypeAlias
}

fun isInterfaceDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptInterface) }
  return node is TypeScriptInterface
}

fun isAsExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptAsExpression) }
  return node is TypeScriptAsExpression
}

fun isNonNullExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptNotNullExpression) }
  return node is TypeScriptNotNullExpression
}

fun isTypeAssertionExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptCastExpression) }
  return node is TypeScriptCastExpression
}

fun isElementAccessExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSIndexedPropertyAccessExpression) }
  return node is JSIndexedPropertyAccessExpression
}

fun isSatisfiesExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptSatisfiesExpression) }
  return node is TypeScriptSatisfiesExpression
}

fun isCallExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSCallExpression) }
  return node is JSCallExpression
}

fun isParenthesizedExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSParenthesizedExpression) }
  return node is JSParenthesizedExpression
}

fun isPropertyAccessExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSReferenceExpression) }
  return node is JSReferenceExpression && node.qualifier != null
}

fun isArrayLiteralExpression(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSArrayLiteralExpression) }
  return node is JSArrayLiteralExpression
}

fun isTypeNode(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptType) }
  return node is TypeScriptType
}

fun isTypeLiteralNode(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptObjectType) }
  return node is TypeScriptObjectType
}

fun isUnionTypeNode(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptUnionOrIntersectionType) }
  return node is TypeScriptUnionOrIntersectionType && node.isUnionType
}

fun isTypeQueryNode(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptTypeofType) }
  return node is TypeScriptTypeofType
}

fun isArrayBindingPattern(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSDestructuringArray) }
  return node is JSDestructuringArray
}

fun isObjectBindingPattern(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSDestructuringObject) }
  return node is JSDestructuringObject
}

fun isComputedPropertyName(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is ES6ComputedName) }
  return node is ES6ComputedName
}

fun isPropertyAssignment(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSProperty) }
  return node is JSProperty && !node.isShorthanded
}

fun isShorthandPropertyAssignment(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSProperty) }
  return node is JSProperty && node.isShorthanded
}

fun isSpreadAssignment(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is JSSpreadExpression) }
  return node is JSSpreadExpression
}

fun isCallSignatureDeclaration(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is TypeScriptCallSignature) }
  return node is TypeScriptCallSignature
}

fun isNamedImports(node: PsiElement?): Boolean {
  contract { returns(true) implies (node is ES6NamedImports) }
  return node is ES6NamedImports
}
