// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil.SUPPRESS_IN_LINE_COMMENT_PATTERN
import com.intellij.codeInspection.SuppressionUtil.isInspectionToolIdMentioned
import com.intellij.codeInspection.SuppressionUtilCore
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.expr.psi.Angular2PipeArgumentsList
import org.jetbrains.annotations.NonNls

object Angular2InspectionSuppressor : InspectionSuppressor {

  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
    return isSuppressedInStatement(element, stripToolIdPrefix(toolId))
  }

  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
    return arrayOf(Angular2SuppressByCommentFix(stripToolIdPrefix(toolId)))
  }

  private class Angular2SuppressByCommentFix(key: String)
    : AbstractBatchSuppressByNoInspectionCommentFix(key, false) {

    val suppressText: String
      get() = SuppressionUtilCore.SUPPRESS_INSPECTIONS_TAG_NAME + " " + myID

    @Throws(IncorrectOperationException::class)
    override fun createSuppression(project: Project, element: PsiElement, container: PsiElement) {
      val parserFacade = PsiParserFacade.getInstance(project)
      val comment = parserFacade.createLineOrBlockCommentFromText(Angular2Language.INSTANCE, suppressText)
      container.parent.addAfter(comment, container)
    }

    override fun getContainer(context: PsiElement?): PsiElement? {
      return PsiTreeUtil.getParentOfType(context, Angular2EmbeddedExpression::class.java)
    }

    override fun getText(): String {
      return Angular2Bundle.message("angular.suppress.for-expression")
    }

    override fun getCommentsFor(container: PsiElement): List<PsiElement>? {
      val next = PsiTreeUtil.skipWhitespacesForward(container) ?: return null
      return listOf(next)
    }
  }

  @NonNls
  private val PREFIXES_TO_STRIP = arrayOf("TypeScript", "JS", "Angular")

  private fun getStatementToolSuppressedIn(place: PsiElement,
                                           toolId: String): PsiElement? {
    val statement = PsiTreeUtil.getParentOfType(place, Angular2EmbeddedExpression::class.java)
    if (statement != null) {
      var candidate = PsiTreeUtil.skipWhitespacesForward(statement)
      //workaround for empty argument list
      if (candidate !is PsiComment && statement.lastChild is Angular2PipeArgumentsList) {
        candidate = PsiTreeUtil.skipWhitespacesBackward(statement.lastChild)
      }
      if (candidate is PsiComment) {
        val text = candidate.text
        val matcher = SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(text)
        if (matcher.matches() && isInspectionToolIdMentioned(matcher.group(1), toolId)) {
          return candidate
        }
      }
    }
    return null
  }

  private fun isSuppressedInStatement(place: PsiElement,
                                      toolId: String): Boolean {
    return ReadAction.compute<PsiElement, RuntimeException> { getStatementToolSuppressedIn(place, toolId) } != null
  }

  private fun stripToolIdPrefix(toolId: String): String {
    for (prefix in PREFIXES_TO_STRIP) {
      if (toolId.startsWith(prefix)) {
        return toolId.substring(prefix.length)
      }
    }
    return toolId
  }
}
