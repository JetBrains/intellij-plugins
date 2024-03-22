// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.breadcrumbs

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.jetbrains.lang.dart.DartComponentType
import com.jetbrains.lang.dart.DartLanguage
import com.jetbrains.lang.dart.psi.*
import javax.swing.Icon

/**
 * Generates breadcrumbs for the selected element in a Dart file.
 */
class DartBreadcrumbsInfoProvider : BreadcrumbsProvider {
  override fun getLanguages() = arrayOf(DartLanguage.INSTANCE)

  override fun acceptElement(element: PsiElement) = getHandler(element) != null

  override fun getElementInfo(element: PsiElement): String =
    getHandler(element)!!.getElementInfo(element as DartPsiCompositeElement)

  override fun getElementIcon(element: PsiElement): Icon? =
    getHandler(element)!!.getElementIcon(element as DartPsiCompositeElement)
    ?: DartComponentType.typeOf(element)?.icon

  /**
   * Handles generating crumbs for elements it supports.
   */
  private abstract class ElementHandler<TElement : DartPsiCompositeElement>(val type: Class<TElement>) {
    /** Whether this handler can generate a crumb for the specified element. */
    open fun accepts(element: TElement): Boolean = true

    /** The icon to display for some element. */
    open fun getElementIcon(element: TElement): Icon? = null

    /** A crumb for the specified element. */
    abstract fun getElementInfo(element: TElement): String
  }

  @Suppress("UNCHECKED_CAST")
  private fun getHandler(e: PsiElement): ElementHandler<in DartPsiCompositeElement>? {
    if (e !is DartPsiCompositeElement) return null
    val handler = Handlers.handlers.firstOrNull { it.type.isInstance(e) && (it as ElementHandler<in DartPsiCompositeElement>).accepts(e) }
    return handler as ElementHandler<in DartPsiCompositeElement>?
  }

  /**
   * Element handlers to use when generating crumbs.
   */
  private object Handlers {
    val handlers: List<ElementHandler<*>> = listOf<ElementHandler<*>>(
      ConstructorHandler,
      NonLocalVariableHandler,
      NamedTypeHandler,
      UnitTestHandler,
    )
  }

  /**
   * Generates crumbs for constructor invocations.
   */
  private object ConstructorHandler : ElementHandler<DartCallExpression>(DartCallExpression::class.java) {
    override fun accepts(element: DartCallExpression): Boolean {
      if (element.expression?.text == null) {
        return false
      }

      val reference = (element.expression as? DartReference)?.resolve()
      if (DartComponentType.typeOf(reference) == DartComponentType.CONSTRUCTOR) {
        return true
      }

      return false
    }

    override fun getElementInfo(element: DartCallExpression): String = element.expression?.text!!
  }

  /**
   * Generates crumbs for non-local variables.
   */
  private object NonLocalVariableHandler : ElementHandler<DartVarDeclarationList>(DartVarDeclarationList::class.java) {
    override fun accepts(element: DartVarDeclarationList): Boolean = !isInMethodOrFunction(element)

    override fun getElementInfo(element: DartVarDeclarationList): String =
      element.varAccessDeclaration.componentName.text

    override fun getElementIcon(element: DartVarDeclarationList): Icon? = DartComponentType.GLOBAL_VARIABLE.icon
  }

  /**
   * Generates crumbs for named types like classes, enums, mixins, functions, methods, getters, and setters.
   */
  private object NamedTypeHandler : ElementHandler<DartComponent>(DartComponent::class.java) {
    override fun accepts(element: DartComponent): Boolean =
      (
        element is DartMethodDeclaration
        || element is DartGetterDeclaration
        || element is DartSetterDeclaration
        || element is DartFunctionDeclarationWithBodyOrNative
        || element is DartClassDefinition
        || element is DartEnumDefinition
        || element is DartEnumConstantDeclaration
        || element is DartMixinDeclaration
      ) && !isInMethodOrFunction(element)

    override fun getElementInfo(element: DartComponent): String {
      val name = element.name ?: element.text

      if (element is DartSetterDeclaration) return "set $name"
      if (element is DartGetterDeclaration) return "get $name"

      return name
    }
  }

  /**
   * Generates crumbs for `group('...')` and `test('...')` calls in Dart unit tests.
   */
  private object UnitTestHandler : ElementHandler<DartCallExpression>(DartCallExpression::class.java) {
    override fun accepts(element: DartCallExpression): Boolean {
      val name = expressionName(element)
      return name == "test" || name == "group"
    }

    override fun getElementInfo(element: DartCallExpression): String {
      val name = expressionName(element)
      val label = getTestLabel(element)
      return "$name('$label')"
    }

    private fun expressionName(element: DartCallExpression): String? =
      (element.expression as? DartReferenceExpression)?.text

    private fun getTestLabel(testCallExpression: DartCallExpression): String? {
      val arguments = testCallExpression.arguments
      val argumentList = arguments?.argumentList
      val argumentExpressions = argumentList?.expressionList

      // The first argument to a `group(...)` or `test(...)` call is the label
      if (argumentExpressions?.isNotEmpty() == true) {
        if (argumentExpressions[0] is DartStringLiteralExpression) {
          return StringUtil.unquoteString(argumentExpressions[0].text)
        }
      }

      return null
    }
  }
}

/**
 * Determines whether the element is nested inside a method or function body.
 */
private fun isInMethodOrFunction(element: DartPsiCompositeElement): Boolean {
  val parents = element.parents(false).iterator()
  while (parents.hasNext()) {
    if (parents.next() is DartFunctionBody) {
      return true
    }
  }
  return false
}