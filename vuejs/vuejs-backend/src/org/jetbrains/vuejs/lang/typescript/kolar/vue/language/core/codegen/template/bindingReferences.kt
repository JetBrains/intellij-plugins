// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.template

import com.intellij.lang.ecmascript6.psi.ES6ComputedName
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSAssignmentExpression
import com.intellij.lang.javascript.psi.JSBinaryExpression
import com.intellij.lang.javascript.psi.JSBreakStatement
import com.intellij.lang.javascript.psi.JSCatchBlock
import com.intellij.lang.javascript.psi.JSCommaExpression
import com.intellij.lang.javascript.psi.JSContinueStatement
import com.intellij.lang.javascript.psi.JSDefinitionExpression
import com.intellij.lang.javascript.psi.JSDoWhileStatement
import com.intellij.lang.javascript.psi.JSEmptyExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSForInStatement
import com.intellij.lang.javascript.psi.JSForStatement
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionExpression
import com.intellij.lang.javascript.psi.JSIfStatement
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSInitializerOwner
import com.intellij.lang.javascript.psi.JSLabeledStatement
import com.intellij.lang.javascript.psi.JSNamedElement
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSPostfixExpression
import com.intellij.lang.javascript.psi.JSPrefixExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSSpreadExpression
import com.intellij.lang.javascript.psi.JSSwitchStatement
import com.intellij.lang.javascript.psi.JSTypeArgumentsOwner
import com.intellij.lang.javascript.psi.JSTypeDeclarationOwner
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSWhileStatement
import com.intellij.lang.javascript.psi.TypeScriptSatisfiesExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnum
import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnumField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterListOwner
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.getBindingElements
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isArrayBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isAsExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isBlock
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isCallExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isConditionalExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isExpressionStatement
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isFunctionLike
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isIdentifier
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isInterfaceDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isNonNullExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectBindingPattern
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isObjectLiteralExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isParenthesizedExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAccessExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isSatisfiesExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isShorthandPropertyAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isSpreadAssignment
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTaggedTemplateExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeAliasDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeAssertionExpression
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeNode
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isTypeQueryNode
import org.jetbrains.vuejs.lang.typescript.kolar.typescript.isVariableDeclaration
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.FakeSourceFile
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.forEachNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils.statements
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.collectBindingNames
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.getNodeText
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.isGloballyAllowed

// https://github.com/vuejs/core/blob/fb0c3ca519f1fccf52049cd6b8db3a67a669afe9/packages/compiler-core/src/transforms/transformExpression.ts#L47
private val literalWhitelist = setOf(
  "true",
  "false",
  "null",
  "this",
)

class DeclarationItem(
  val id: PsiElement,
  val isShorthand: Boolean,
  val isNarrowing: Boolean,
  val skipped: Boolean,
  var inTypeQuery: Boolean,
  val isNewOperand: Boolean,
)

private fun identifierItem(
  ctx: TemplateCodegenContext,
  id: PsiElement,
  isShorthand: Boolean = false,
  isNarrowing: Boolean = false,
  inTypeQuery: Boolean = false,
  isNewOperand: Boolean = false,
): DeclarationItem =
  DeclarationItem(
    id = id,
    isShorthand = isShorthand,
    isNarrowing = isNarrowing,
    skipped = shouldIdentifierSkipped(ctx, getNodeText(id)),
    inTypeQuery = inTypeQuery,
    isNewOperand = isNewOperand,
  )

// manual: `ts.isSourceFile(node)` branch, the PSI root is not a dedicated node type
fun forEachDeclarationsInSourceFile(
  ast: FakeSourceFile,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
  inNarrowing: Boolean,
): Sequence<DeclarationItem> = sequence {
  val statements = ast.statements.toList()
  predeclareFunctionDeclarations(statements, scope)
  for (statement in statements) {
    yieldAll(forEachDeclarations(statement, ctx, scope, inNarrowing))
  }
}

fun forEachDeclarations(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
  inNarrowing: Boolean,
): Sequence<DeclarationItem> = sequence {
  if (isIdentifier(node)) {
    yield(identifierItem(ctx, node, isNarrowing = inNarrowing))
  }
  else if (isShorthandPropertyAssignment(node)) {
    yield(identifierItem(ctx, node.nameIdentifier!!, isShorthand = true, isNarrowing = inNarrowing))
  }
  else if (isPropertyAccessExpression(node)) {
    yieldAll(forEachDeclarations(node.qualifier!!, ctx, scope, true))
  }
  else if (node is JSIndexedPropertyAccessExpression) {
    node.qualifier?.let { yieldAll(forEachDeclarations(it, ctx, scope, true)) }
    node.indexExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
  }
  // `JSNewExpression` extends `JSCallExpression`, so check it first
  else if (node is JSNewExpression) {
    val expression = node.methodExpression
    if (isIdentifier(expression)) {
      yield(identifierItem(ctx, expression, isNewOperand = true))
    }
    else if (expression != null) {
      yieldAll(forEachDeclarations(expression, ctx, scope, false))
    }
    yieldAll(forEachDeclarationsInTypeArguments(node, ctx, scope))
    for (arg in node.arguments) {
      yieldAll(forEachDeclarations(arg, ctx, scope, inNarrowing))
    }
  }
  else if (isCallExpression(node)) {
    node.methodExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    yieldAll(forEachDeclarationsInTypeArguments(node, ctx, scope))
    for (arg in node.arguments) {
      yieldAll(forEachDeclarations(arg, ctx, scope, inNarrowing))
    }
  }
  else if (isTaggedTemplateExpression(node)) {
    node.tag?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    node.templateExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
  }
  else if (isParenthesizedExpression(node)) {
    node.innerExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
  }
  else if (isNonNullExpression(node)) {
    node.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
  }
  // `TypeScriptAsExpression` extends `TypeScriptCastExpression`, so check it first
  else if (isAsExpression(node) || isSatisfiesExpression(node)) {
    val (expression, type) = when (node) {
      is TypeScriptAsExpression -> node.expression to node.type
      else -> (node as TypeScriptSatisfiesExpression).expression to node.type
    }
    expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
    type?.let { yieldAll(forEachDeclarationsInTypeNode(it, ctx, scope)) }
  }
  else if (isTypeAssertionExpression(node)) {
    node.type?.let { yieldAll(forEachDeclarationsInTypeNode(it, ctx, scope)) }
    node.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
  }
  else if (node is JSBinaryExpression) {
    val operator = node.operationSign
    val isLogical = operator == JSTokenTypes.OROR
                    || operator == JSTokenTypes.ANDAND
                    || operator == JSTokenTypes.QUEST_QUEST
    val isEquality = operator == JSTokenTypes.EQEQEQ
                     || operator == JSTokenTypes.NEQEQ
                     || operator == JSTokenTypes.EQEQ
                     || operator == JSTokenTypes.NE
    val isInstanceof = operator == JSTokenTypes.INSTANCEOF_KEYWORD
    val isIn = operator == JSTokenTypes.IN_KEYWORD
    val left = node.lOperand
    val right = node.rOperand
    if (node is JSCommaExpression || operator == JSTokenTypes.COMMA) {
      left?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
      right?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
    }
    else if (isLogical) {
      yieldAll(forEachNarrowedBy(left, ctx, scope) {
        right?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
      })
    }
    else if (node is JSAssignmentExpression) {
      left?.let { yieldAll(forEachDeclarationsInAssignmentTarget(it, ctx, scope)) }
      right?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    }
    else {
      left?.let { yieldAll(forEachDeclarations(it, ctx, scope, isEquality || isInstanceof)) }
      right?.let { yieldAll(forEachDeclarations(it, ctx, scope, isEquality || isIn)) }
    }
  }
  else if (isConditionalExpression(node)) {
    yieldAll(forEachNarrowedBy(node.condition, ctx, scope) {
      node.thenBranch?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
      node.elseBranch?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    })
  }
  // `delete` and `typeof` are prefix expressions in the PSI
  else if (node is JSPrefixExpression) {
    val operator = node.operationSign
    node.expression?.let {
      yieldAll(forEachDeclarations(
        it,
        ctx,
        scope,
        operator == JSTokenTypes.EXCL
        || operator == JSTokenTypes.PLUSPLUS
        || operator == JSTokenTypes.MINUSMINUS
        || operator == JSTokenTypes.DELETE_KEYWORD
        || operator == JSTokenTypes.TYPEOF_KEYWORD,
      ))
    }
  }
  else if (node is JSPostfixExpression) {
    node.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, true)) }
  }
  else if (isVariableDeclaration(node)) {
    scope.declare(collectBindingNames(node))
    yieldAll(forEachDeclarationsInBinding(node, ctx, scope))
  }
  else if (isArrayBindingPattern(node) || isObjectBindingPattern(node)) {
    for (element in node.getBindingElements()) {
      yieldAll(forEachDeclarationsInBinding(element.source, ctx, scope))
    }
  }
  else if (isFunctionDeclaration(node)) {
    node.name?.let { scope.declare(it) }
    yieldAll(forEachDeclarationsInFunction(node, ctx))
  }
  else if (node is JSFunctionExpression) {
    yieldAll(forEachDeclarationsInFunction(node, ctx))
  }
  else if (node is JSClass && node is JSExpression) {
    val classScope = ctx.scope()
    node.name?.let { classScope.declare(it) }
    yieldAll(forEachDeclarationsInClass(node, ctx, classScope))
    classScope.end()
  }
  else if (node is JSClass) {
    node.name?.let { scope.declare(it) }
    yieldAll(forEachDeclarationsInClass(node, ctx, scope))
  }
  else if (isObjectLiteralExpression(node)) {
    for (prop in node.propertiesIncludingSpreads) {
      if (isPropertyAssignment(prop)) {
        // fix https://github.com/vuejs/language-tools/issues/1176
        val computedPropName = prop.computedPropertyName
        if (computedPropName != null) {
          computedPropName.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
        }
        yieldAll(forEachDeclarations(prop.initializer!!, ctx, scope, false))
      }
      // fix https://github.com/vuejs/language-tools/issues/1156
      else if (isShorthandPropertyAssignment(prop)) {
        yieldAll(forEachDeclarations(prop, ctx, scope, false))
      }
      // fix https://github.com/vuejs/language-tools/issues/1148#issuecomment-1094378126
      else if (isSpreadAssignment(prop)) {
        // TODO: cannot report "Spread types may only be created from object types.ts(2698)"
        yieldAll(forEachDeclarations(prop.expression!!, ctx, scope, false))
      }
      // fix https://github.com/vuejs/language-tools/issues/4604
      else if (isFunctionLike(prop) && prop.block != null) {
        (prop as? JSProperty)?.computedPropertyName?.expression?.let {
          yieldAll(forEachDeclarations(it, ctx, scope, false))
        }
        yieldAll(forEachDeclarationsInFunction(prop, ctx))
      }
    }
  }
  // fix https://github.com/vuejs/language-tools/issues/1422
  else if (isTypeNode(node)) {
    yieldAll(forEachDeclarationsInTypeNode(node, ctx, scope))
  }
  else if (isBlock(node)) {
    val blockScope = ctx.scope()
    predeclareFunctionDeclarations(node.statementListItems.toList(), blockScope)
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarations(child, ctx, blockScope, false))
    }
    blockScope.end()
  }
  else if (node is JSForInStatement) {
    val loopScope = ctx.scope()
    val declarations = node.varDeclaration?.variables
    if (declarations != null) {
      for (decl in declarations) {
        decl.initializer?.let { yieldAll(forEachDeclarations(it, ctx, loopScope, false)) }
      }
    }
    else {
      node.variableExpression?.let { yieldAll(forEachDeclarationsInAssignmentTarget(it, ctx, loopScope)) }
    }
    // The source resolves before the loop bindings are in scope (`for (const x of x)` reads the outer `x`).
    node.collectionExpression?.let { yieldAll(forEachDeclarations(it, ctx, loopScope, false)) }
    if (declarations != null) {
      for (decl in declarations) {
        loopScope.declare(collectBindingNames(decl))
      }
    }
    node.body?.let { yieldAll(forEachDeclarations(it, ctx, loopScope, false)) }
    loopScope.end()
  }
  else if (node is JSIfStatement) {
    yieldAll(forEachNarrowedBy(node.condition, ctx, scope) {
      node.thenBranch?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
      node.elseBranch?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    })
  }
  else if (node is JSWhileStatement) {
    yieldAll(forEachNarrowedBy(node.condition, ctx, scope) {
      node.body?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    })
  }
  else if (node is JSDoWhileStatement) {
    node.body?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    node.condition?.let { yieldAll(forEachDeclarations(it, ctx, scope, true)) }
  }
  else if (node is JSForStatement) {
    val loopScope = ctx.scope()
    val varDeclaration = node.varDeclaration
    if (varDeclaration != null) {
      for (decl in varDeclaration.variables) {
        loopScope.declare(collectBindingNames(decl))
        decl.initializer?.let { yieldAll(forEachDeclarations(it, ctx, loopScope, false)) }
      }
    }
    else {
      node.initialization?.let { yieldAll(forEachDeclarations(it, ctx, loopScope, false)) }
    }
    yieldAll(forEachNarrowedBy(node.condition, ctx, loopScope) {
      node.update?.let { yieldAll(forEachDeclarations(it, ctx, loopScope, false)) }
      node.body?.let { yieldAll(forEachDeclarations(it, ctx, loopScope, false)) }
    })
    loopScope.end()
  }
  else if (node is JSSwitchStatement) {
    predeclareFunctionDeclarations(node.caseClauses.flatMap { it.statementListItems.toList() }, scope)
    yieldAll(forEachNarrowedBy(node.switchExpression, ctx, scope) {
      for (clause in node.caseClauses) {
        clause.caseExpression?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
        for (statement in clause.statementListItems) {
          yieldAll(forEachDeclarations(statement, ctx, scope, false))
        }
      }
    })
  }
  else if (node is JSLabeledStatement) {
    node.statement?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
  }
  else if (node is JSBreakStatement || node is JSContinueStatement) {
    // break/continue labels are not bindings.
  }
  else if (isInterfaceDeclaration(node) || isTypeAliasDeclaration(node)) {
    yieldAll(forEachDeclarationsInTypeNode(node, ctx, scope))
  }
  else if (node is TypeScriptEnum) {
    node.name?.let { scope.declare(it) }
    for (member in node.fields) {
      (member as? TypeScriptEnumField)?.initializer?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
    }
  }
  else if (node is JSCatchBlock) {
    val catchScope = ctx.scope()
    node.parameter?.let { yieldAll(forEachDeclarations(it, ctx, catchScope, false)) }
    node.statement?.let { yieldAll(forEachDeclarations(it, ctx, catchScope, false)) }
    catchScope.end()
  }
  else if (isExpressionStatement(node)) {
    node.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, inNarrowing)) }
  }
  else {
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarations(child, ctx, scope, false))
    }
  }
}

private fun forEachNarrowedBy(
  condition: PsiElement?,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
  body: suspend SequenceScope<DeclarationItem>.() -> Unit,
): Sequence<DeclarationItem> = sequence {
  // The condition's declarations come first (narrowing positions), then the body.
  if (condition != null) {
    yieldAll(forEachDeclarations(condition, ctx, scope, true))
  }
  body()
}

private fun predeclareFunctionDeclarations(
  statements: List<PsiElement>,
  scope: TemplateCodegenContext.Scope,
) {
  for (statement in statements) {
    if (isFunctionDeclaration(statement)) {
      statement.name?.let { scope.declare(it) }
    }
  }
}

private fun forEachDeclarationsInAssignmentTarget(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<DeclarationItem> = sequence {
  if (node is JSDefinitionExpression) {
    node.expression?.let { yieldAll(forEachDeclarationsInAssignmentTarget(it, ctx, scope)) }
  }
  else if (isIdentifier(node)) {
    yield(identifierItem(ctx, node, isNarrowing = true))
  }
  else if (node is JSObjectLiteralExpression) {
    for (prop in node.propertiesIncludingSpreads) {
      if (isPropertyAssignment(prop)) {
        prop.computedPropertyName?.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
        yieldAll(forEachDeclarationsInAssignmentTarget(prop.initializer!!, ctx, scope))
      }
      else if (isShorthandPropertyAssignment(prop)) {
        yield(identifierItem(ctx, prop.nameIdentifier!!, isShorthand = true, isNarrowing = true))
      }
      else if (prop is JSSpreadExpression) {
        prop.expression?.let { yieldAll(forEachDeclarationsInAssignmentTarget(it, ctx, scope)) }
      }
    }
  }
  else if (node is JSArrayLiteralExpression) {
    for (element in node.expressions) {
      if (element is JSEmptyExpression) {
        continue
      }
      else if (element is JSSpreadExpression) {
        element.expression?.let { yieldAll(forEachDeclarationsInAssignmentTarget(it, ctx, scope)) }
      }
      else if (element is JSBinaryExpression) {
        element.lOperand?.let { yieldAll(forEachDeclarationsInAssignmentTarget(it, ctx, scope)) }
        element.rOperand?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
      }
      else {
        yieldAll(forEachDeclarationsInAssignmentTarget(element, ctx, scope))
      }
    }
  }
  else if (isParenthesizedExpression(node)) {
    node.innerExpression?.let { yieldAll(forEachDeclarationsInAssignmentTarget(it, ctx, scope)) }
  }
  else {
    yieldAll(forEachDeclarations(node, ctx, scope, true))
  }
}

private fun forEachDeclarationsInBinding(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<DeclarationItem> = sequence {
  val computedPropertyName = PsiTreeUtil.getChildOfType(node, ES6ComputedName::class.java)
  computedPropertyName?.expression?.let { yieldAll(forEachDeclarations(it, ctx, scope, true)) }
  val nameIdentifier = (node as? JSNamedElement)?.nameIdentifier
  if (nameIdentifier != null && !isIdentifier(nameIdentifier)) {
    yieldAll(forEachDeclarations(nameIdentifier, ctx, scope, false))
  }
  val type = (node as? JSTypeDeclarationOwner)?.typeElement
  if (type != null) {
    yieldAll(forEachDeclarationsInTypeNode(type, ctx, scope))
  }
  val initializer = (node as? JSInitializerOwner)?.initializer
  if (initializer != null) {
    yieldAll(forEachDeclarations(initializer, ctx, scope, false))
  }
}

private fun forEachDeclarationsInFunction(
  node: JSFunction,
  ctx: TemplateCodegenContext,
): Sequence<DeclarationItem> = sequence {
  val scope = ctx.scope()
  if (node is JSFunctionExpression) {
    node.name?.let { scope.declare(it) }
  }
  for (param in node.parameters) {
    param.declarationElement?.let { scope.declare(collectBindingNames(it)) }
  }
  (node as? TypeScriptTypeParameterListOwner)?.typeParameterList?.let {
    yieldAll(forEachDeclarationsInTypeNode(it, ctx, scope))
  }
  for (param in node.parameters) {
    yieldAll(forEachDeclarationsInBinding(param, ctx, scope))
  }
  node.returnTypeElement?.let { yieldAll(forEachDeclarationsInTypeNode(it, ctx, scope)) }
  val body = node.block
             ?: if (node.isShorthandArrowFunction) PsiTreeUtil.getChildOfType(node, JSExpression::class.java) else null
  if (body != null) {
    yieldAll(forEachDeclarations(body, ctx, scope, false))
  }
  scope.end()
}

// manual: the PSI class model differs from the TS one, so the members are walked by their PSI kind
private fun forEachDeclarationsInClass(
  node: JSClass,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<DeclarationItem> = sequence {
  (node as? TypeScriptTypeParameterListOwner)?.typeParameterList?.let {
    yieldAll(forEachDeclarationsInTypeNode(it, ctx, scope))
  }
  node.extendsList?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
  node.implementsList?.let { yieldAll(forEachDeclarationsInTypeNode(it, ctx, scope)) }
  for (member in node.members) {
    if (member is JSFunction) {
      PsiTreeUtil.getChildOfType(member, ES6ComputedName::class.java)?.expression?.let {
        // A computed name in a class property must be an entity name (grammar rule): no `__VLS_unwrap` call allowed.
        yieldAll(forEachDeclarations(it, ctx, scope, true))
      }
      yieldAll(forEachDeclarationsInFunction(member, ctx))
    }
    else if (member is JSVarStatement) {
      for (field in member.variables) {
        PsiTreeUtil.getChildOfType(field, ES6ComputedName::class.java)?.expression?.let {
          yieldAll(forEachDeclarations(it, ctx, scope, true))
        }
        field.typeElement?.let { yieldAll(forEachDeclarationsInTypeNode(it, ctx, scope)) }
        field.initializer?.let { yieldAll(forEachDeclarations(it, ctx, scope, false)) }
      }
    }
    else if (isBlock(member)) {
      val blockScope = ctx.scope()
      predeclareFunctionDeclarations(member.statementListItems.toList(), blockScope)
      for (statement in member.statementListItems) {
        yieldAll(forEachDeclarations(statement, ctx, blockScope, false))
      }
      blockScope.end()
    }
  }
}

private fun forEachDeclarationsInTypeArguments(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<DeclarationItem> = sequence {
  if (node is JSTypeArgumentsOwner) {
    for (typeArg in node.typeArguments) {
      yieldAll(forEachDeclarationsInTypeNode(typeArg, ctx, scope))
    }
  }
}

private fun forEachDeclarationsInTypeNode(
  node: PsiElement,
  ctx: TemplateCodegenContext,
  scope: TemplateCodegenContext.Scope,
): Sequence<DeclarationItem> = sequence {
  if (isTypeQueryNode(node)) {
    var id = node.expression!!
    while (isPropertyAccessExpression(id)) {
      id = id.qualifier ?: break
    }
    yield(identifierItem(ctx, id, inTypeQuery = true))
  }
  else if (node is ES6ComputedName) {
    // TS1170: a computed name in a type literal must be an entity-name expression (no calls).
    node.expression?.let {
      for (item in forEachDeclarations(it, ctx, scope, false)) {
        item.inTypeQuery = true
        yield(item)
      }
    }
  }
  else {
    for (child in forEachNode(node)) {
      yieldAll(forEachDeclarationsInTypeNode(child, ctx, scope))
    }
  }
}

fun shouldIdentifierSkipped(
  ctx: TemplateCodegenContext,
  text: String,
): Boolean =
  ctx.scopes.any { it.contains(text) }
  // https://github.com/vuejs/core/blob/245230e135152900189f13a4281302de45fdcfaa/packages/compiler-core/src/transforms/transformExpression.ts#L342-L352
  || isGloballyAllowed(text)
  || literalWhitelist.contains(text)
  || text == "require"
  || text.startsWith("__VLS_")
