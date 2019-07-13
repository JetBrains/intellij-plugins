// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.breadcrumbs

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.siblings
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.jetbrains.lang.dart.DartLanguage
import com.jetbrains.lang.dart.psi.*

// Based on PythonBreadcrumbsInfoProvider
class DartBreadcrumbsInfoProvider : BreadcrumbsProvider {
  companion object {
    private val LANGUAGES = arrayOf(DartLanguage.INSTANCE as DartLanguage)
    private val HELPERS = listOf<Helper<*>>(
      SimpleHelper(DartTryStatement::class.java, "try"),
      //ExceptHelper,
      SimpleHelper(DartFinallyPart::class.java, "finally"),
      //SimpleHelper(DartIfElement::class.java, "else"),
      //IfHelper,
      ForHelper,
      //WhileHelper,
      //WithHelper,
      ClassHelper,
      ComponentHelper(DartComponent::class.java),
      CallHelper,
      NewHelper,
      //NamedArgumentHelper,
      ClosureHelper,
      KeyValueHelper
    )
  }

  override fun getLanguages(): Array<DartLanguage> = LANGUAGES

  override fun acceptElement(e: PsiElement): Boolean = getHelper(e) != null

  override fun getParent(e: PsiElement): PsiElement? = e.parent

  override fun getElementInfo(e: PsiElement): String = getHelper(e)!!.elementInfo(e as DartPsiCompositeElement)
  override fun getElementTooltip(e: PsiElement): String = getHelper(e)!!.elementTooltip(e as DartPsiCompositeElement)

  private fun getHelper(e: PsiElement): Helper<in DartPsiCompositeElement>? {
    if (e !is DartPsiCompositeElement) return null

    @Suppress("UNCHECKED_CAST")
    val helper = HELPERS.firstOrNull { it.type.isInstance(e) } as Helper<in DartPsiCompositeElement>?
    if (helper?.accept(e) == true) {
      return helper
    }

    return null
  }

  private abstract class Helper<T : DartPsiCompositeElement>(val type: Class<T>) {
    fun elementInfo(e: T): String = truncate(getPresentation(e), 32)
    fun elementTooltip(e: T): String = truncate(getVerbosePresentation(e), 96)

    abstract fun getPresentation(e: T): String

    open fun getVerbosePresentation(e: T): String = getPresentation(e);
    open fun accept(e: T): Boolean = true

    private fun truncate(text: String, maxLength: Int) = StringUtil.shortenTextWithEllipsis(text, maxLength, 0, true)
  }

  private class SimpleHelper<T : DartPsiCompositeElement>(type: Class<T>, val representation: String) : Helper<T>(type) {
    override fun getPresentation(e: T) = representation
  }


  private class ComponentHelper<T : DartComponent>(type: Class<T>) : Helper<T>(type) {
    override fun getPresentation(e: T) = e.name ?: e.presentation?.presentableText ?: e.text ?: "wat"
  }

  private object LambdaHelper : Helper<DartFunctionExpression>(DartFunctionExpression::class.java) {
    override fun getPresentation(e: DartFunctionExpression) = "lambda ${e.formalParameterList.presentation?.presentableText}"
  }
  //
  //private object ExceptHelper : Helper<DartExceptpart>(DartExceptpart::class.java) {
  //  override fun getPresentation(e: DartExceptpart): String {
  //    val exceptClass = e.exceptClass ?: return "except"
  //    val target = e.target ?: return "except ${exceptClass.text}"
  //
  //    return "except ${exceptClass.text} as ${target.text}"
  //  }
  //}
  //
  //private object IfHelper : Helper<DartIfpart>(DartIfpart::class.java) {
  //  override fun getPresentation(e: DartIfpart): String {
  //    val prefix = if (e.isElif) "elif" else "if"
  //    val condition = e.condition ?: return prefix
  //
  //    return "$prefix ${condition.text}"
  //  }
  //}

  private object ForHelper : Helper<DartForInPart>(DartForInPart::class.java) {
    override fun getPresentation(e: DartForInPart): String {
      //val parent = e.parent
      //val prefix = if (parent is DartForElement) "async for" else "for"
      val prefix = "for";

      val target = e.varAccessDeclaration ?: return prefix
      val source = e.expression

      return "$prefix ${target.text} in ${source.text}"
    }
  }

  /// Handles calls as well as constructors without new/const (which look like function calls to the parser)
  private object CallHelper : Helper<DartCallExpression>(DartCallExpression::class.java) {
    private fun getName(e: DartCallExpression): String {
      val function = e.expression
      var text :String? = null

      // Special case for over_react: invocation of parenthesized builder
      if (function is DartParenthesizedExpression) {
        val unwrapped = function.expression
        if (unwrapped is DartValueExpression && unwrapped.expressionList.lastOrNull() is DartCascadeReferenceExpression) {
          text = unwrapped.expressionList.first().text
        } else {
          text = unwrapped?.text
        }
      }

      return text ?: function.text
    }

    override fun accept(e: DartCallExpression): Boolean {
      val name = getName(e)
      // todo handle "_"/"$" prefixes
      return name.isNotEmpty() && name[0].isUpperCase()
    }

    override fun getPresentation(e: DartCallExpression): String {
      return getName(e)
    }

    override fun getVerbosePresentation(e: DartCallExpression): String {
      return e.expression.text
    }
  }

  private object NewHelper : Helper<DartNewExpression>(DartNewExpression::class.java) {
    override fun getPresentation(e: DartNewExpression): String {
      // This should never be null, but just in case
      return e.type?.text ?: "constructor"
    }
  }

  //
  //private object WhileHelper : Helper<DartWhilepart>(DartWhilepart::class.java) {
  //  override fun getPresentation(e: DartWhilepart): String {
  //    val condition = e.condition ?: return "while"
  //
  //    return "while ${condition.text}"
  //  }
  //}
  //
  //private object WithHelper : Helper<PyWithStatement>(PyWithStatement::class.java) {
  //  override fun getPresentation(e: PyWithStatement): String {
  //    val getItemPresentation = fun(item: PyWithItem): String? {
  //      val expression = item.expression ?: return null
  //      val target = item.target ?: return expression.text
  //      return "${expression.text} as ${target.text}"
  //    }
  //
  //    val prefix = if (e.isAsync) "async with " else "with "
  //
  //    return e.withItems
  //      .orEmpty()
  //      .asSequence()
  //      .map(getItemPresentation)
  //      .filterNotNull()
  //      .joinToString(prefix = prefix)
  //  }
  //}

  private object ClassHelper : Helper<DartClass>(DartClass::class.java) {
    override fun getPresentation(e: DartClass) = e.name ?: "class"
  }

  private object NamedArgumentHelper : Helper<DartNamedArgument>(DartNamedArgument::class.java) {
    override fun getPresentation(e: DartNamedArgument): String {
      return e.parameterReferenceExpression.text
    }
  }

  private object ClosureHelper : Helper<DartFunctionExpression>(DartFunctionExpression::class.java) {
    override fun getPresentation(e: DartFunctionExpression): String {
      val parent = e.parent
      if (parent is DartNamedArgument || parent is DartArgumentList) {

        var callTargetName: String? = null
        val call = PsiTreeUtil.findFirstParent(e) { element -> element is DartCallExpression || element is DartNewExpression }
        if (call is DartCallExpression) {
          val callTargetExpression = call.expression
          if (callTargetExpression is DartReference) {
            val callTarget = callTargetExpression.resolve()
            if (callTarget is DartNamedElement) {
              callTargetName = callTarget.name
            }
          }
        }

        var parameterDescription = ""
        if (parent is DartNamedArgument) {
          parameterDescription = "${parent.parameterReferenceExpression.text}: "
        } else if (parent is DartArgumentList) {
          val index = parent.expressionList.indexOf(e);
          if (index > 0) {
            parameterDescription = ",".repeat(index) + " ";
          }
        }

        if (callTargetName != null) {
          //if (!HELPERS.contains(NamedArgumentHelper) && parameterName != null) {
            return "$callTargetName($parameterDescription<closure>)"
          //}
          //else {
          //  "$callTargetName(<closure>)"
          //}
        }
      } else if (parent is DartVarInit) {
        val reference = parent
          .siblings(forward = false)
          .filterIsInstance<DartReference>()
          .firstOrNull()
        if (reference != null) {
          val name = reference.canonicalText
          return "$name = <closure>"
        }
      }


      return "<closure>"
    }
  }

  private object KeyValueHelper : Helper<DartMapEntry>(DartMapEntry::class.java) {
    override fun getPresentation(e: DartMapEntry): String = e.firstChild?.text ?: "key"
  }
}

