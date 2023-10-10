// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.controlflow

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
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlLet
import org.angular2.lang.html.psi.PropertyBindingType

/**
 * @see JSControlFlowBuilder
 */
class Angular2ControlFlowBuilder : JSControlFlowBuilder() {
  companion object {
    const val NG_TEMPLATE_CONTEXT_GUARD = "ngTemplateContextGuard"
    const val NG_TEMPLATE_GUARD_PREFIX = "ngTemplateGuard_"

    private val CUSTOM_GUARD = Key.create<JSElement>("CUSTOM_GUARD")
    private const val BINDING_GUARD = "binding" // See interface TemplateGuardMeta in Angular sources
  }

  private val visitedNodes = mutableSetOf<PsiElement>()

  override fun visitElement(element: PsiElement) {
    when (element) {
      is HtmlTag -> {
        if (!visitedNodes.add(element)) {
          // reentry in processIfBranching
          super.visitElement(element)
          return
        }
        val directives = element.attributes.flatMap { (it.descriptor as? Angular2AttributeDescriptor)?.sourceDirectives ?: emptyList() }
        val templateGuards = directives.flatMap { it.templateGuards }.groupBy { it.name!!.removePrefix(NG_TEMPLATE_GUARD_PREFIX) }

        val guards = element.attributes.flatMap {
          processAttribute(it, templateGuards)
        }
        if (guards.isNotEmpty()) {
          val guard = guards[0] // TODO support multiple guards
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
      is XmlAttribute -> {
        if (visitedNodes.add(element)) super.visitElement(element)
      }
      else -> super.visitElement(element)
    }
  }

  private fun processAttribute(attribute: XmlAttribute, templateGuards: Map<String, List<JSElement>>): List<Angular2GuardInfo> {
    val descriptor = attribute.descriptor as? Angular2AttributeDescriptor
    val info = descriptor?.info

    if (info != null && info.type == Angular2AttributeType.TEMPLATE_BINDINGS) {
      val templateBindings = Angular2TemplateBindings.get(attribute)
      val result = mutableListOf<Angular2GuardInfo>()

      templateBindings.bindings.asSequence().filter { !it.keyIsVar() }.forEach { binding ->
        val guardElements = templateGuards[binding.key]
        if (!guardElements.isNullOrEmpty()) {
          result.addAll(guardElements.map { Angular2GuardInfo(it, binding.expression) })
        }
        else {
          binding.expression?.accept(this)
        }
      }

      templateBindings.bindings.asSequence().filter { it.keyIsVar() }.forEach { binding ->
        binding.variableDefinition?.accept(this)
      }
      visitedNodes.add(attribute)
      return result
    }
    else if (info != null && isTemplateTag(attribute.parent)) {
      visitedNodes.add(attribute)
      if (info is Angular2AttributeNameParser.PropertyBindingInfo
          && info.bindingType == PropertyBindingType.PROPERTY) {
        val guardElements = templateGuards[info.name]
        val expression = Angular2Binding.get(attribute)?.expression
        if (expression != null && !guardElements.isNullOrEmpty()) {
          visitedNodes.add(attribute)
          return guardElements.map { Angular2GuardInfo(it, expression) }
        }
      }
      if (attribute is Angular2HtmlLet) {
        attribute.variable?.accept(this)
      }
      else {
        attribute.acceptChildren(this)
      }
    }
    return emptyList()
  }

  private val Angular2Directive.templateGuards: List<JSElement>
    get() = typeScriptClass?.members?.filter { it.name?.startsWith(NG_TEMPLATE_GUARD_PREFIX) == true }
            ?: emptyList()

  private var currentTopConditionExpression: JSExpression? = null

  override fun isStatementCondition(parent: PsiElement?, currentElement: PsiElement?): Boolean {
    return currentElement == currentTopConditionExpression
  }

  private fun processIfBranching(element: HtmlTag, conditionExpression: JSExpression?) {
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
      get() = classMember.asSafely<TypeScriptField>()
        ?.jsType?.asSafely<JSExoticStringLiteralType>()
        ?.asSimpleLiteralType()?.literal == BINDING_GUARD
  }
}
