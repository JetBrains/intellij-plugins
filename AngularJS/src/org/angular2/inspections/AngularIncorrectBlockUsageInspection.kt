// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.childrenOfType
import com.intellij.util.containers.MultiMap
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_EXPRESSION_PREFIX
import org.angular2.codeInsight.Angular2HighlightingUtils.htmlName
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.blocks.Angular2HtmlBlockSymbol
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2BlockParameter
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
        val primaryBlockDefinition = block.primaryBlockDefinition
        validateBlocksStructure(block, definition, primaryBlockDefinition)
        validateBlockParameters(block, definition, primaryBlockDefinition)
      }

      private fun validateBlocksStructure(block: Angular2HtmlBlock,
                                          definition: Angular2HtmlBlockSymbol,
                                          primaryBlockDefinition: Angular2HtmlBlockSymbol?) {
        val primaryBlock = block.primaryBlock
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
                                     "angular.inspection.incorrect-block-usage.message.too-many-blocks",
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
      }

      private fun validateBlockParameters(block: Angular2HtmlBlock,
                                          definition: Angular2HtmlBlockSymbol,
                                          primaryBlockDefinition: Angular2HtmlBlockSymbol?) {
        val expectedParams = definition.parameters
        val actualParams = block.parameters

        if (expectedParams.isEmpty() && actualParams.isNotEmpty()) {
          holder.registerProblem(block.node.findChildByType(Angular2HtmlElementTypes.BLOCK_PARAMETERS)!!.psi,
                                 Angular2Bundle.htmlMessage(
                                   "angular.inspection.incorrect-block-usage.message.no-params-allowed",
                                   block.htmlName,
                                   primaryBlockDefinition.htmlName(block)))
        }
        else if (expectedParams.isNotEmpty()) {
          // Check required params
          for (requiredParam in expectedParams.filter { it.required == true }) {
            if (requiredParam.isPrimaryExpression) {
              if (actualParams.isEmpty()) {
                holder.registerProblem(block.nameElement,
                                       Angular2Bundle.htmlMessage(
                                         "angular.inspection.incorrect-block-usage.message.missing-primary-expression",
                                         block.htmlName))
              }
            }
            else if (actualParams.none { it.getName() == requiredParam.name }) {
              holder.registerProblem(block.nameElement,
                                     Angular2Bundle.htmlMessage(
                                       "angular.inspection.incorrect-block-usage.message.missing-expression",
                                       block.htmlName, requiredParam.htmlName(block)))
            }
          }

          // Check if parameters are allowed
          val namedParameters = expectedParams.mapNotNull { if (!it.isPrimaryExpression) it.name to it else null }.toMap()
          val countByPrefix = MultiMap<String, Angular2BlockParameter>()
          if (actualParams.size > 1) {
            for (parameter in actualParams.subList(1, actualParams.size)) {
              val name = parameter.getName() ?: continue
              val parameterDefinition = namedParameters[name]
              if (parameterDefinition == null) {
                holder.registerProblem(parameter.nameElement!!,
                                       Angular2Bundle.htmlMessage(
                                         "angular.inspection.incorrect-block-usage.message.unrecognized-parameter",
                                         block.htmlName, name.withColor(NG_EXPRESSION_PREFIX, block)))
              }
              else if (parameterDefinition.maxCount != null) {
                countByPrefix.putValue(name, parameter)
              }
            }
          }
          for ((name, params) in countByPrefix.entrySet()) {
            val maxCount = namedParameters[name]?.maxCount ?: continue
            if (params.size > maxCount) {
              params.forEach {
                holder.registerProblem(it.nameElement!!,
                                       Angular2Bundle.icuHtmlMessage(
                                         "angular.inspection.incorrect-block-usage.message.too-many-parameters",
                                         "block" to primaryBlockDefinition.htmlName(block),
                                         "max_count" to maxCount,
                                         "param" to name.withColor(NG_EXPRESSION_PREFIX, block),
                                       ))
              }
            }
          }
        }
      }
    }
}