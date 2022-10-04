// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.controlflow

import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowBuilder
import com.intellij.lang.javascript.psi.controlflow.instruction.JSBranchInstruction.BranchOwner
import com.intellij.lang.javascript.psi.controlflow.instruction.JSConditionInstruction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.types.JSExoticStringLiteralType
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings

/**
 * @see JSControlFlowBuilder
 */
class Angular2ControlFlowBuilder : JSControlFlowBuilder() {
  companion object {
    const val CUSTOM_GUARD_PREFIX = "ngTemplateGuard_"
    private const val STAR = "*"

    private val CUSTOM_GUARD = Key.create<JSElement>("CUSTOM_GUARD")
    private const val BINDING_GUARD = "binding" // See interface TemplateGuardMeta in Angular sources
  }

  enum class HtmlTagVisitingMode {
    Default, VisitChildren, Skip
  }

  private val visitingModeOverrides = mutableMapOf<PsiElement, HtmlTagVisitingMode>()

  private var PsiElement.visitingMode: HtmlTagVisitingMode
    set(value) = Unit.also { visitingModeOverrides[this] = value }
    get() = visitingModeOverrides[this] ?: HtmlTagVisitingMode.Default

  override fun doBuild(scope: JSControlFlowScope) {
    super.doBuild(scope)
    visitingModeOverrides.clear()
  }

  override fun visitElement(element: PsiElement) {
    when (element) {
      is HtmlTag -> {
        var conditionAttribute: XmlAttribute?
        if (element.visitingMode == HtmlTagVisitingMode.VisitChildren) {
          super.visitElement(element)
          element.visitingMode = HtmlTagVisitingMode.Skip
        }
        else if (element.visitingMode == HtmlTagVisitingMode.Skip) {
          // the whole subtree was already processed
        }
        else if (findControlFlowSignificantAttribute(element).also { conditionAttribute = it } != null) {

          val guard = when (val attribute = conditionAttribute) {
            is Angular2HtmlTemplateBindings -> { // structural directives micro-syntax
              val directivesProvider = Angular2ApplicableDirectivesProvider(Angular2TemplateBindings.get(attribute))
              val declarationsScope = Angular2DeclarationsScope(attribute)
              val relevantName = attribute.templateName
              findCustomGuard(directivesProvider, declarationsScope, relevantName)
            }
            is Angular2HtmlPropertyBinding -> { // ng-template tag
              val directivesProvider = Angular2ApplicableDirectivesProvider(attribute.parent)
              val declarationsScope = Angular2DeclarationsScope(attribute)
              val relevantName = attribute.propertyName
              findCustomGuard(directivesProvider, declarationsScope, relevantName)
            }
            else -> {
              null
            }
          }

          if (guard != null) {
            val jsType = if (guard is TypeScriptField) guard.jsType else null

            val useNativeNarrowing = jsType is JSExoticStringLiteralType && jsType.asSimpleLiteralType().literal == BINDING_GUARD
            processIfBranching(element, conditionAttribute!!, guard.takeIf { !useNativeNarrowing })
          }
          else {
            super.visitElement(element)
          }
        }
        else {
          super.visitElement(element)
        }
      }
      is XmlAttribute -> {
        if (isControlFlowSignificantAttribute(element)) {
          // handled as part of HtmlTag
          return
        }

        super.visitElement(element)
      }
      else -> {
        super.visitElement(element)
      }
    }
  }

  private fun findCustomGuard(directivesProvider: Angular2ApplicableDirectivesProvider,
                              declarationsScope: Angular2DeclarationsScope,
                              relevantName: String): JSElement? {
    val directives = directivesProvider.matched.filter { d ->
      declarationsScope.contains(d)
    }

    val guardName = "$CUSTOM_GUARD_PREFIX$relevantName"
    directives.firstOrNull()?.typeScriptClass?.let { cls ->
      for (member in cls.members) {
        if (member.name == guardName) {
          return member
        }
      }
    }

    return null
  }

  private fun findControlFlowSignificantAttribute(tag: HtmlTag): XmlAttribute? {
    val templateMode = isTemplateTag(tag)

    for (attribute in tag.attributes) {
      if ((templateMode && attribute is Angular2HtmlPropertyBinding) || attribute.name.startsWith(STAR)) {
        return attribute
      }
    }

    return null
  }

  private fun isControlFlowSignificantAttribute(attribute: XmlAttribute): Boolean {
    val templateMode = isTemplateTag(attribute.parent)
    return (templateMode && attribute is Angular2HtmlPropertyBinding) || attribute.name.startsWith(STAR)
  }

  private fun processIfBranching(element: HtmlTag, conditionAttribute: XmlAttribute, guard: JSElement?) {
    val conditionExpression = PsiTreeUtil.findChildOfType(conditionAttribute.valueElement, JSExpression::class.java)

    conditionExpression?.putUserData(CUSTOM_GUARD, guard)

    element.visitingMode = HtmlTagVisitingMode.VisitChildren
    myBuilder.startNode(element)
    val elseBranch = null // no support for the else branch narrowing in Angular, see https://github.com/angular/angular/issues/21504
    processBranching(element, conditionExpression, element, elseBranch, BranchOwner.IF)
  }

  override fun createConditionInstruction(conditionExpression: JSExpression,
                                          value: Boolean,
                                          state: JSConditionInstruction.ConditionState): JSConditionInstruction {
    return Angular2ConditionInstruction(conditionExpression, conditionExpression.getUserData(CUSTOM_GUARD), value, state)
  }

  class Angular2ConditionInstruction(element: PsiElement,
                                     val customGuard: JSElement?,
                                     value: Boolean,
                                     state: ConditionState)
    : JSConditionInstruction(element, value, state)
}
