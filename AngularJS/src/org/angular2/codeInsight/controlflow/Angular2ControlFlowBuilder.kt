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
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.psi.Angular2HtmlLet
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings

/**
 * @see JSControlFlowBuilder
 */
class Angular2ControlFlowBuilder : JSControlFlowBuilder() {
  companion object {
    const val NG_TEMPLATE_CONTEXT_GUARD = "ngTemplateContextGuard"
    const val NG_TEMPLATE_GUARD_PREFIX = "ngTemplateGuard_"

    private const val STAR = "*"

    private val CUSTOM_GUARD = Key.create<JSElement>("CUSTOM_GUARD")
    private const val BINDING_GUARD = "binding" // See interface TemplateGuardMeta in Angular sources
  }

  enum class HtmlTagVisitingMode {
    Default, VisitChildren, Skip
  }

  private val visitingModeOverrides = mutableMapOf<PsiElement, HtmlTagVisitingMode>()
  private val visitedNodes = mutableSetOf<PsiElement>()

  private var PsiElement.visitingMode: HtmlTagVisitingMode
    set(value) = Unit.also { visitingModeOverrides[this] = value }
    get() = visitingModeOverrides[this] ?: HtmlTagVisitingMode.Default

  override fun doBuild(scope: JSControlFlowScope) {
    super.doBuild(scope)
    visitingModeOverrides.clear()
    visitedNodes.clear()
  }

  override fun visitElement(element: PsiElement) {
    if (visitedNodes.contains(element)) {
      // handled as part of HtmlTag
      return
    }

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
          val guard = processControlFlowSignificantAttribute(conditionAttribute!!)

          if (guard != null) {
            val templateExpression: JSExpression? = guard.templateExpression
            if (!guard.useNativeNarrowing) {
              templateExpression?.putUserData(CUSTOM_GUARD, guard.classMember)
            }

            processIfBranching(element, templateExpression)
          }
          else {
            super.visitElement(element)
          }
        }
        else {
          super.visitElement(element)
        }
      }
      else -> {
        super.visitElement(element)
      }
    }
  }

  private fun processControlFlowSignificantAttribute(initialAttribute: XmlAttribute): Angular2GuardInfo? {
    val declarationsScope = Angular2DeclarationsScope(initialAttribute)

    // todo collect all guards, then "logical and" them
    var result: Angular2GuardInfo? = null

    if (initialAttribute is Angular2HtmlTemplateBindings) { // structural directives micro-syntax
      val templateBindings = Angular2TemplateBindings.get(initialAttribute)
      val directivesProvider = Angular2ApplicableDirectivesProvider(templateBindings)

      templateBindings.bindings.asSequence().filter { !it.keyIsVar() }.forEach { binding ->
        val guardElement = findTemplateGuardClassMember(directivesProvider, declarationsScope, binding.key)
        if (result == null && guardElement != null) {
          result = Angular2GuardInfo(guardElement, binding.expression)
        }
        else {
          binding.expression?.accept(this)
        }
      }

      templateBindings.bindings.asSequence().filter { it.keyIsVar() }.forEach { binding ->
        binding.variableDefinition?.accept(this)
      }

      visitedNodes.add(initialAttribute)
    }
    else { // ng-template tag
      val templateTag = initialAttribute.parent
      val directivesProvider = Angular2ApplicableDirectivesProvider(templateTag)

      templateTag.attributes.asSequence().filterIsInstance<Angular2HtmlPropertyBinding>().forEach { attribute ->
        val guardElement = findTemplateGuardClassMember(directivesProvider, declarationsScope, attribute.propertyName)
        val templateExpression = PsiTreeUtil.findChildOfType(attribute.valueElement, JSExpression::class.java)
        if (result == null && guardElement != null) {
          result = Angular2GuardInfo(guardElement, templateExpression)
        }
        else {
          templateExpression?.accept(this)
        }

        visitedNodes.add(attribute)
      }

      templateTag.attributes.asSequence().filterIsInstance<Angular2HtmlLet>().forEach { attribute ->
        attribute.variable?.accept(this)

        visitedNodes.add(attribute)
      }
    }

    return result
  }

  /**
   * Returns a type-narrowing class member:
   * * either a user-defined type guard (function that returns type predicate `parameterName is Type`)
   * * or a 'binding' literal type
   */
  private fun findTemplateGuardClassMember(directivesProvider: Angular2ApplicableDirectivesProvider,
                                           declarationsScope: Angular2DeclarationsScope,
                                           inputName: String): JSElement? {
    val directives = directivesProvider.matched.filter { d ->
      declarationsScope.contains(d)
    }

    val templateGuardName = "$NG_TEMPLATE_GUARD_PREFIX$inputName"
    directives.firstOrNull()?.typeScriptClass?.let { cls ->
      for (member in cls.members) {
        if (member.name == templateGuardName) {
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

  private var currentTopConditionExpression: JSExpression? = null

  override fun isStatementCondition(parent: PsiElement?, currentElement: PsiElement?): Boolean {
    return currentElement == currentTopConditionExpression
  }

  private fun processIfBranching(element: HtmlTag, conditionExpression: JSExpression?) {
    element.visitingMode = HtmlTagVisitingMode.VisitChildren
    myBuilder.startNode(element)
    val elseBranch = null // no support for the else branch narrowing in Angular, see https://github.com/angular/angular/issues/21504
    currentTopConditionExpression = conditionExpression
    processBranching(element, conditionExpression, element, elseBranch, BranchOwner.IF)
    currentTopConditionExpression = null
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

  private data class Angular2GuardInfo(val classMember: JSElement, val templateExpression: JSExpression?) {
    val useNativeNarrowing: Boolean

    init {
      val jsType = if (classMember is TypeScriptField) classMember.jsType else null
      useNativeNarrowing = jsType is JSExoticStringLiteralType && jsType.asSimpleLiteralType().literal == BINDING_GUARD
    }
  }
}
