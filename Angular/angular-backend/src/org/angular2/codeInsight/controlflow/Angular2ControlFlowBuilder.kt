// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.controlflow

import com.intellij.codeInsight.controlflow.Instruction
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSBinaryExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowBuilder
import com.intellij.lang.javascript.psi.controlflow.instruction.JSBranchInstruction.BranchOwner
import com.intellij.lang.javascript.psi.controlflow.instruction.JSConditionInstruction
import com.intellij.lang.javascript.psi.controlflow.instruction.JSConditionInstruction.ConditionState
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.blocks.*
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.entities.Angular2TemplateGuard
import org.angular2.entities.Angular2TemplateGuard.Kind
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.*

/**
 * @see JSControlFlowBuilder
 */
class Angular2ControlFlowBuilder : JSControlFlowBuilder() {
  companion object {
    const val NG_TEMPLATE_CONTEXT_GUARD: String = "ngTemplateContextGuard"
    const val NG_TEMPLATE_GUARD_PREFIX: String = "ngTemplateGuard_"

    private val CUSTOM_GUARD = Key.create<JSElement>("CUSTOM_GUARD")
    const val BINDING_GUARD: String = "binding" // See interface TemplateGuardMeta in Angular sources
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
        val templateGuards = directives.flatMap { it.templateGuards }.groupBy { it.inputName }

        val guards = element.attributes.flatMap {
          processAttribute(it, templateGuards)
        }
        if (guards.isNotEmpty()) {
          val guard = guards[0] // TODO support multiple guards
          val templateExpression: JSExpression? = guard.templateExpression
          if (guard.kind == Kind.Method) {
            templateExpression?.putUserData(CUSTOM_GUARD, guard.classMember)
          }
          processIfBranching(element, templateExpression)
        }
        else {
          super.visitElement(element)
        }
      }
      is XmlAttribute, is Angular2HtmlBlockParameters -> {
        if (visitedNodes.add(element))
          super.visitElement(element)
      }
      is Angular2HtmlBlock -> {
        if (visitedNodes.add(element)) {
          when (element.getName()) {
            BLOCK_IF, BLOCK_ELSE_IF -> {
              visitIfBlock(element)
            }
            BLOCK_ELSE, BLOCK_SWITCH, BLOCK_DEFAULT -> {
              myBuilder.startNode(element)
              super.visitElement(element)
            }
            BLOCK_CASE -> {
              visitSwitchCaseBlock(element)
            }
            else -> {
              super.visitElement(element)
            }
          }
        }
      }
      is Angular2HtmlBlockContents ->
        if (visitedNodes.add(element)) {
          super.visitElement(element)
        }
      else -> super.visitElement(element)
    }
  }

  override fun createConditionInstruction(conditionExpression: JSExpression,
                                          value: Boolean,
                                          state: ConditionState): JSConditionInstruction =
    conditionExpression.getUserData(CUSTOM_GUARD)
      ?.let { Angular2CustomGuardConditionInstruction(conditionExpression, it, value, state) }
    ?: super.createConditionInstruction(conditionExpression, value, state)


  override fun isStatementCondition(parent: PsiElement?, currentElement: PsiElement?): Boolean =
    currentElement == currentTopConditionExpression

  override fun addPendingEdgeFromBranching(owner: PsiElement, instruction: Instruction?) {
    if (owner.isControlFlowBlock) {
      addDelayedPendingEdge(owner, instruction)
    }
    else {
      super.addPendingEdgeFromBranching(owner, instruction)
    }
  }

  private fun visitIfBlock(element: Angular2HtmlBlock) {
    myBuilder.startNode(element)
    currentTopConditionExpression = element.parameters.getOrNull(0)?.expression
    processBranching(element, currentTopConditionExpression,
                     element.contents, element.blockSiblingsForward().firstOrNull(), BranchOwner.IF)
    currentTopConditionExpression = null
    flushDelayedPendingEdges(element)
  }

  private fun visitSwitchCaseBlock(element: Angular2HtmlBlock) {
    myBuilder.startNode(element)

    val nextCase = element.blockSiblingsForward().firstOrNull { it.getName() == BLOCK_CASE }
                   ?: element.parent.childrenOfType<Angular2HtmlBlock>().firstOrNull { it.getName() == BLOCK_DEFAULT }

    val switchExpression = element.parent?.parent?.asSafely<Angular2HtmlBlock>()?.takeIf { it.getName() == BLOCK_SWITCH }
      ?.parameters?.get(0)?.expression

    val caseExpression = element.parameters.getOrNull(0)?.expression

    currentTopConditionExpression = Angular2FakeBinaryExpression(element, switchExpression, caseExpression, JSTokenTypes.EQEQEQ)
    processBranching(element, currentTopConditionExpression,
                     element.contents, nextCase, BranchOwner.IF)
    currentTopConditionExpression = null
    flushDelayedPendingEdges(element)
  }

  private fun processAttribute(attribute: XmlAttribute, templateGuards: Map<String, List<Angular2TemplateGuard>>): List<Angular2GuardInfo> {
    val descriptor = attribute.descriptor as? Angular2AttributeDescriptor
    val info = descriptor?.info

    if (info != null && info.type == Angular2AttributeType.TEMPLATE_BINDINGS) {
      val templateBindings = Angular2TemplateBindings.get(attribute)
      val result = mutableListOf<Angular2GuardInfo>()

      templateBindings.bindings.asSequence().filter { !it.keyIsVar() }.forEach { binding ->
        val guardElements = templateGuards[binding.key]
        if (!guardElements.isNullOrEmpty()) {
          result.addAll(guardElements.map { Angular2GuardInfo(it.member, it.type, binding.expression) })
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
          return guardElements.map { Angular2GuardInfo(it.member, it.type, expression) }
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

  private var currentTopConditionExpression: JSExpression? = null

  private fun processIfBranching(element: HtmlTag, conditionExpression: JSExpression?) {
    myBuilder.startNode(element)
    val elseBranch = null // no support for the else branch narrowing in Angular, see https://github.com/angular/angular/issues/21504
    currentTopConditionExpression = conditionExpression
    processBranching(element, conditionExpression, element, elseBranch, BranchOwner.IF)
    currentTopConditionExpression = null
  }

  // Angular does not have strict parent relationships of scopes for blocks
  private val delayedPendingEdges: MutableList<Pair<PsiElement, Instruction>> = ArrayList()

  private fun addDelayedPendingEdge(pendingScope: PsiElement?, instruction: Instruction?) {
    if (instruction == null) return
    delayedPendingEdges.add(Pair.create(pendingScope, instruction))
  }

  private fun flushDelayedPendingEdges(element: Angular2HtmlBlock) {
    val scope = element.parent
    val iterator = delayedPendingEdges.asReversed().listIterator()
    while (iterator.hasNext()) {
      val edge = iterator.next()
      if (PsiTreeUtil.isAncestor(scope, edge.first, false)) {
        iterator.remove()
        myBuilder.addPendingEdge(edge.first, edge.second)
      }
    }
  }

  private val PsiElement.isControlFlowBlock
    get() = this is Angular2HtmlBlock && primaryBlockDefinition?.name.let { def -> def == BLOCK_IF || def == BLOCK_SWITCH }

  class Angular2CustomGuardConditionInstruction(element: PsiElement,
                                                val customGuard: JSElement?,
                                                value: Boolean,
                                                state: ConditionState)
    : JSConditionInstruction(element, value, state)

  private data class Angular2GuardInfo(val classMember: JSElement,
                                       val kind: Kind,
                                       val templateExpression: JSExpression?)

  private class Angular2FakeBinaryExpression(private val parent: PsiElement?,
                                             private val lOperand: JSExpression?,
                                             private val rOperand: JSExpression?,
                                             private val operationSign: IElementType) : FakePsiElement(), JSBinaryExpression {
    override fun getOperationSign(): IElementType = operationSign
    override fun getLOperand(): JSExpression? = lOperand
    override fun getROperand(): JSExpression? = rOperand
    override fun getParent(): PsiElement? = parent

    override fun replace(other: JSExpression): JSExpression {
      throw UnsupportedOperationException()
    }

    override fun getOperationNode(): ASTNode? {
      throw UnsupportedOperationException()
    }
  }
}
