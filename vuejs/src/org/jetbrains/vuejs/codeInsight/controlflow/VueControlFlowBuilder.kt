// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.controlflow

import com.intellij.codeInsight.controlflow.Instruction
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.psi.JSConditionalExpression
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowBuilder
import com.intellij.lang.javascript.psi.controlflow.instruction.JSBranchInstruction.BranchOwner
import com.intellij.lang.javascript.psi.types.guard.isCfgAwareInjectedFile
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlText
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent

/**
 * Only used for template block. For script scope, please see core JS/TS builders.
 *
 * @see JSControlFlowBuilder
 */
class VueControlFlowBuilder : JSControlFlowBuilder() {
  companion object {
    private const val V_IF = "v-if"
    private const val V_ELSE_IF = "v-else-if"
    private const val V_ELSE = "v-else"

    // v-for is not included because Vue template has no break/continue/return/throw etc.
    private val controlFlowRelevantDirectives = setOf(V_IF, V_ELSE_IF, V_ELSE)
  }

  enum class HtmlTagVisitingMode {
    Default, VisitChildren, Skip
  }

  private val visitingModeOverrides = mutableMapOf<PsiElement, HtmlTagVisitingMode>()

  private var PsiElement.visitingMode: HtmlTagVisitingMode
    set(value) = Unit.also { visitingModeOverrides[this] = value }
    get() = visitingModeOverrides[this] ?: HtmlTagVisitingMode.Default

  // Vue does not have strict parent relationships of scopes
  private val delayedPendingEdges: MutableList<Pair<PsiElement, Instruction>> = ArrayList()

  private fun addDelayedPendingEdge(pendingScope: PsiElement?, instruction: Instruction?) {
    if (instruction == null) return
    delayedPendingEdges.add(Pair.create(pendingScope, instruction))
  }

  private fun flushDelayedPendingEdges() {
    delayedPendingEdges.forEach { pair ->
      myBuilder.addPendingEdge(pair.first, pair.second)
    }
    delayedPendingEdges.clear()
  }

  override fun addPendingEdgeFromBranching(owner: PsiElement, instruction: Instruction?) {
    addDelayedPendingEdge(owner, instruction)
  }

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
        else if (element.getAttribute(V_IF).also { conditionAttribute = it } != null) {
          processIfBranching(element, conditionAttribute!!)
          flushDelayedPendingEdges()
        }
        else if (element.getAttribute(V_ELSE_IF).also { conditionAttribute = it } != null) {
          processIfBranching(element, conditionAttribute!!)
        }
        else if (element.getAttribute(V_ELSE).also { conditionAttribute = it } != null) {
          super.visitElement(element)
          element.visitingMode = HtmlTagVisitingMode.Skip
        }
        else {
          super.visitElement(element)
        }
      }
      is XmlAttribute -> {
        if (controlFlowRelevantDirectives.contains(element.name)) {
          // handled as part of HtmlTag
          return
        }

        super.visitElement(element)
      }
      is JSExpression -> {
        super.visitElement(element)
      }
      is VueJSEmbeddedExpressionContent -> {
        myBuilder.startNode(element) // translates to JSExpressionStatement in pure JS CFG
        super.visitElement(element)
      }
      is XmlText -> {
        val injectedFiles = InjectedLanguageManager.getInstance(element.getProject()).getInjectedPsiFiles(element)
        if (injectedFiles != null) {
          myBuilder.startNode(element) // we add XmlText mostly for more readable CFG
          for (pair in injectedFiles) {
            val injectedFile = pair.first
            // exactly like JSControlFlowBuilder.visitJSFunctionExpression
            // in other words, treat it like an IIFE w.r.t. CFG
            if (injectedFile.isCfgAwareInjectedFile()) {
              myBuilder.startNode(injectedFile)
            }
          }
        }
      }
      else -> {
        super.visitElement(element)
      }
    }
  }

  private var currentTopConditionExpression: JSExpression? = null

  override fun isStatementCondition(parent: PsiElement?, currentElement: PsiElement?): Boolean {
    return currentElement == currentTopConditionExpression
  }

  private fun processIfBranching(element: HtmlTag, conditionAttribute: XmlAttribute) {
    val conditionExpression = PsiTreeUtil.findChildOfType(conditionAttribute.valueElement, JSExpression::class.java)
    element.visitingMode = HtmlTagVisitingMode.VisitChildren
    myBuilder.startNode(element)
    currentTopConditionExpression = conditionExpression
    processBranching(element, conditionExpression, element, findElseBranch(element), BranchOwner.IF)
    currentTopConditionExpression = null
  }

  override fun visitJSConditionalExpression(node: JSConditionalExpression) {
    super.visitJSConditionalExpression(node)
    flushDelayedPendingEdges()
  }

  private fun findElseBranch(initialElement: HtmlTag): PsiElement? {
    var element = initialElement.nextSibling
    if (element is XmlText) {
      // todo check if it contains only PsiWhiteSpace & XmlComment
      element = element.nextSibling
    }
    if (element is HtmlTag) {
      element.getAttribute(V_ELSE_IF) ?: element.getAttribute(V_ELSE) ?: return null
      return element
    }

    return null
  }
}
