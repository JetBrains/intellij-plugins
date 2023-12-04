// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.childrenOfType
import org.angular2.codeInsight.Angular2HighlightingUtils.htmlName
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor

class AngularIncorrectBlockUsageInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : Angular2HtmlElementVisitor() {

      override fun visitBlock(block: Angular2HtmlBlock) {
        val definition = block.definition
        if (definition == null) {
          holder.registerProblem(block.nameElement,
                                 Angular2Bundle.htmlMessage("angular.inspection.incorrect-block-usage.message.undefined",
                                                            block.htmlName))
          return
        }
        val primaryBlock = block.primaryBlock
        val primaryBlockDefinition = block.primaryBlockDefinition
        if (definition.isPrimary) {
          val parentBlock = block.parent?.parent as? Angular2HtmlBlock
          if (parentBlock != null && parentBlock.definition?.hasNestedSecondaryBlocks == true)
            holder.registerProblem(block.nameElement,
                                   Angular2Bundle.htmlMessage(
                                     "angular.inspection.incorrect-block-usage.message.cannot-be-nested",
                                     block.htmlName,
                                     parentBlock.htmlName))
        }
        else if (primaryBlock == null) {
          holder.registerProblem(block.nameElement,
                                 Angular2Bundle.htmlMessage(
                                   if (primaryBlockDefinition?.hasNestedSecondaryBlocks == true)
                                     "angular.inspection.incorrect-block-usage.message.missing-primary-block.parent"
                                   else
                                     "angular.inspection.incorrect-block-usage.message.missing-primary-block.sibling",
                                   block.htmlName,
                                   primaryBlockDefinition.htmlName(block)))
        }
        val maxCount = definition.maxCount
        val name = block.getName()
        if (maxCount != null && primaryBlock != null && primaryBlockDefinition != null) {
          val actualCount = if (primaryBlockDefinition.hasNestedSecondaryBlocks)
            primaryBlock.contents?.childrenOfType<Angular2HtmlBlock>()?.count { it.getName() == name } ?: 0
          else
            primaryBlock.blockSiblingsForward().count { it.getName() == name }
          if (actualCount > maxCount) {
            holder.registerProblem(block.nameElement,
                                   Angular2Bundle.icuHtmlMessage(
                                     "angular.inspection.incorrect-block-usage.message.too-many",
                                     "primary_block" to primaryBlockDefinition.htmlName(block),
                                     "max_count" to maxCount,
                                     "block" to block.htmlName,
                                   ))
          }
        }
        if (definition.last && block.blockSiblingsForward().filter { it.getName() != name }.any()) {
          holder.registerProblem(block.nameElement,
                                 Angular2Bundle.htmlMessage(
                                   "angular.inspection.incorrect-block-usage.message.not-last",
                                   block.htmlName,
                                   primaryBlockDefinition.htmlName(block)))
        }
        if (definition.parameters.isEmpty() && block.parameters.isNotEmpty()) {
          holder.registerProblem(block.node.findChildByType(Angular2HtmlElementTypes.BLOCK_PARAMETERS)!!.psi,
                                 Angular2Bundle.htmlMessage(
                                   "angular.inspection.incorrect-block-usage.message.no-params-allowed",
                                   block.htmlName,
                                   primaryBlockDefinition.htmlName(block)))
        }
      }
    }
}