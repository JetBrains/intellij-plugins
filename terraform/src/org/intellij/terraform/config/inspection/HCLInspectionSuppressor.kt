// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.SuppressionUtil
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.formatter.HclCodeStyleSettings
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import java.util.regex.Pattern

internal class HCLInspectionSuppressor : InspectionSuppressor {
  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean = ReadAction.compute<Boolean, RuntimeException> {
    if (getSuppressedInBlock(element, toolId) != null) {
      return@compute true
    }
    if (getSuppressedProperty(element, toolId) != null) {
      return@compute true
    }
    false
  }

  private fun getSuppressedInBlock(element: PsiElement, toolId: String): PsiElement? {
    return SuppressionUtil.getStatementToolSuppressedIn(element, toolId, HCLBlock::class.java, getInLineCommentPattern(element))
  }

  private fun getSuppressedProperty(element: PsiElement, toolId: String): PsiElement? {
    return SuppressionUtil.getStatementToolSuppressedIn(element, toolId, HCLProperty::class.java, getInLineCommentPattern(element))
  }

  private fun getInLineCommentPattern(element: PsiElement): Pattern {
    val settings = CodeStyle.getCustomSettings(element.containingFile, HclCodeStyleSettings::class.java)
    val commenterChar = settings.LINE_COMMENTER_CHARACTER.prefix
    return Pattern.compile(commenterChar + SuppressionUtil.COMMON_SUPPRESS_REGEXP + ".*")
  }

  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
    if (element == null) return SuppressQuickFix.EMPTY_ARRAY

    return arrayOf(BlockSuppressQuickFix(toolId), PropertySuppressQuickFix(toolId))
  }
}

private class BlockSuppressQuickFix(toolId: String) : AbstractBatchSuppressByNoInspectionCommentFix(toolId, false) {
  override fun getContainer(context: PsiElement?): HCLBlock? = context?.parentOfType<HCLBlock>()

  override fun getText(): @IntentionName String = HCLBundle.message("suppress.inspection.block.action.name")
}

private class PropertySuppressQuickFix(toolId: String) : AbstractBatchSuppressByNoInspectionCommentFix(toolId, false) {
  override fun getContainer(context: PsiElement?): HCLProperty? = context?.parentOfType<HCLProperty>()

  override fun getText(): @IntentionName String = HCLBundle.message("suppress.inspection.property.action.name")
}