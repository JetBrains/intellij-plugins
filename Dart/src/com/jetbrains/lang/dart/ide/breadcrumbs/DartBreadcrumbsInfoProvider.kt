// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.breadcrumbs

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.siblings
import com.intellij.psi.util.skipTokens
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.intellij.util.castSafelyTo
import com.jetbrains.lang.dart.DartLanguage
import com.jetbrains.lang.dart.ide.documentation.DartDocUtil
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
      ComponentHelper,
      CallHelper,
      NewHelper,
      //NamedArgumentHelper,
      ClosureHelper,
      KeyValueHelper,
      AssignmentHelper,
      VarInitHelper
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


  /** Applies to many named elements: named compilation unit members, class members, etc. */
  private object ComponentHelper : Helper<DartComponent>(DartComponent::class.java) {
    override fun getPresentation(e: DartComponent):String = e.name ?: e.text
    override fun getVerbosePresentation(e: DartComponent):String = DartDocUtil.getSignature(e) ?: getPresentation(e)
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

  private object AssignmentHelper : Helper<DartAssignExpression>(DartAssignExpression::class.java) {
    override fun getPresentation(e: DartAssignExpression): String {
      return e.expressionList.firstOrNull()?.text ?: "assignment"
    }
  }

  private object VarInitHelper : Helper<DartVarInit>(DartVarInit::class.java) {
    override fun getPresentation(e: DartVarInit): String {
      return e.prevNonWhitespaceSibling()?.castSafelyTo<DartReferenceExpression>()?.text ?: "assignment"
    }
  }

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
      return name.looksLikeDartClassName()
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

    override fun getVerbosePresentation(e: DartNewExpression): String {
      return e.text
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

  private object NamedArgumentHelper : Helper<DartNamedArgument>(DartNamedArgument::class.java) {
    override fun getPresentation(e: DartNamedArgument): String {
      return e.parameterReferenceExpression.text
    }
  }

  private object ClosureHelper : Helper<DartFunctionExpression>(DartFunctionExpression::class.java) {
    private fun getPresentations(e: DartFunctionExpression, isVerbose: Boolean): String {
      val parent = e.parent
      if (parent is DartNamedArgument || parent is DartArgumentList) {
        val parameterDescription = when (parent) {
          is DartNamedArgument -> if (isVerbose)  "${parent.parameterReferenceExpression.text}: ..." else "${parent.parameterReferenceExpression.text}=>"
          else -> if (isVerbose) "..." else "=>"
        }

        val alreadyHasBreadcrumbForConstructor = true

        val callExpression = PsiTreeUtil.findFirstParent(e) { it is DartCallExpression || it is DartNewExpression }
        val callTargetName:String = when (callExpression) {
          is DartNewExpression ->
            (if (alreadyHasBreadcrumbForConstructor && !isVerbose) "" else callExpression.type?.referenceExpression?.text)
            ?: "<constructor>"
          is DartCallExpression -> {
            val functionExpression = callExpression.expression
            val reference = functionExpression.castSafelyTo<DartReference>()?.resolve()
            if (alreadyHasBreadcrumbForConstructor && !isVerbose && reference?.parent.castSafelyTo<DartComponent>()?.isConstructor == true) {
              ""
            }
            else {
              // Resolve this so we don't get just the method name
              // and not its target, preceding chained accesses, etc.
              reference?.castSafelyTo<DartNamedElement>()?.name
              // If that isn't available (e.g., no analysis server) fall back to the whole expression.
              ?: functionExpression.text
            }
          }
          else -> ""
        }

        val prefix = if (isVerbose) "closure for " else ""

        return if (callTargetName == "") "${prefix}$parameterDescription" else "$prefix$callTargetName($parameterDescription)"
      } else if (parent is DartVarInit) {
        val reference = parent
          .siblings(forward = false)
          .filterIsInstance<DartReference>()
          .firstOrNull()
        if (reference != null) {
          val name = reference.canonicalText
          return "$name=>"
        }
      }

      return "=>"
    }

    override fun getPresentation(e: DartFunctionExpression): String = getPresentations(e, false)
    override fun getVerbosePresentation(e: DartFunctionExpression): String = getPresentations(e, true)
  }

  private object KeyValueHelper : Helper<DartMapEntry>(DartMapEntry::class.java) {
    override fun getPresentation(e: DartMapEntry): String = e.firstChild?.text ?: "key"
  }
}

fun String.looksLikeDartClassName(): Boolean  {
  return Regex("^[_$]*[A-Z]").containsMatchIn(this)
}

fun PsiElement.prevNonWhitespaceSibling(): PsiElement? {
  return siblings(forward = false).drop(1).skipTokens(TokenSet.WHITE_SPACE).firstOrNull()
}