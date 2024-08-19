// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.lang.annotations.Language
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.codeinsight.AddClosingQuoteQuickFix
import org.intellij.terraform.hcl.codeinsight.ReplaceToDoubleQuoteQuickFix
import org.intellij.terraform.hcl.codeinsight.UnwrapHCLStringQuickFix
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLNumberLiteral
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.isTerraformCompatiblePsiFile
import java.util.regex.Pattern

class HCLLiteralValidnessInspection : LocalInspectionTool() {
  // TODO: Check HCL supported escapes
  @Language("RegExp")
  private val COMMON_REGEX = "[\\\\abfntrv]|([0-7]{3})|(X[0-9a-fA-F]{2})|(u[0-9a-fA-F]{4})|(U[0-9a-fA-F]{8})"
  private val DQS_VALID_ESCAPE = Pattern.compile("\\\\(\"|$COMMON_REGEX)")
  private val SQS_VALID_ESCAPE = Pattern.compile("\\\\(\'|$COMMON_REGEX)")

  // TODO: AFAIK that should be handled by lexer/parser
  private val VALID_NUMBER_LITERAL = Pattern.compile("-?(0x)?([0-9])\\d*(\\.\\d+)?([e][+-]?\\d+)?([kmgb][b]?)?", Pattern.CASE_INSENSITIVE)
  private val VALID_HEX_NUMBER_LITERAL = Pattern.compile("-?(0x)?([0-9a-f])+", Pattern.CASE_INSENSITIVE)

  override fun getID(): String {
    return "LiteralValidness"
  }

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return StringLiteralVisitor(holder)
  }

  inner class StringLiteralVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitStringLiteral(element: HCLStringLiteral) {
      val text = HCLPsiUtil.getElementTextWithoutHostEscaping(element)
      val length = text.length

      // Check that string literal is closed properly
      if (length <= 1 || text[0] != text[length - 1] || HCLPsiUtil.isEscapedChar(text, length - 1)) {
        holder.registerProblem(element, HCLBundle.message("hcl.literal.inspection.missing.closing.quote"), ProblemHighlightType.ERROR,
                               AddClosingQuoteQuickFix(element))
      }

      val pattern = when (element.quoteSymbol) {
        '\'' -> SQS_VALID_ESCAPE
        '\"' -> DQS_VALID_ESCAPE
        else -> {
          // Assume default string
          DQS_VALID_ESCAPE
        }
      }

      val elementOffset = element.node.startOffset
      for (fragment in element.textFragments) {
        val fragmentText = fragment.getSecond()
        if (fragmentText.length > 1 && fragmentText[0] == '\\' && !pattern.matcher(fragmentText).matches()) {
          val shifted = fragment.getFirst().shiftRight(elementOffset)
          val errText = when (fragmentText[1]) {
            in '0'..'7' -> HCLBundle.message("hcl.literal.inspection.illegal.octal.escape.sequence")
            'X' -> HCLBundle.message("hcl.literal.inspection.illegal.hex.escape.sequence")
            'u', 'U' -> HCLBundle.message("hcl.literal.inspection.illegal.unicode.escape.sequence")
            else -> HCLBundle.message("hcl.literal.inspection.illegal.escape.sequence")
          }

          holder.registerProblem(element, shifted, errText)
        }
      }
      // HCL2/TF0.12 compliance
      if (HCLPsiUtil.isPartOfPropertyKey(element) || HCLPsiUtil.isBlockNameElement(element, 0)) {
        if (element.getTerraformModule().isHCL2Supported()) {
          // see test-data/terraform/annotator/HCL2StringKeys.hcl for details of if case
          if (!HCLPsiUtil.isUnderPropertyUnderPropertyWithObjectValue(element)
              && !HCLPsiUtil.isUnderPropertyUnderPropertyWithObjectValueAndArray(element)
              && !HCLPsiUtil.isUnderPropertyInsideObjectArgument(element)
              && !HCLPsiUtil.isUnderPropertyInsideObjectConditionalExpression(element)) {
            holder.registerProblem(element, HCLBundle.message("hcl.literal.inspection.argument.names.must.not.be.quoted"),
                                   ProblemHighlightType.ERROR, UnwrapHCLStringQuickFix(element))
          }
        }
      }

      if (element.quoteSymbol == '\'') {
        holder.registerProblem(element, HCLBundle.message("hcl.literal.inspection.invalid.quotes"), ProblemHighlightType.ERROR,
                               ReplaceToDoubleQuoteQuickFix(element))
      }
    }

    override fun visitNumberLiteral(element: HCLNumberLiteral) {
      val text = HCLPsiUtil.getElementTextWithoutHostEscaping(element)
      if (!VALID_NUMBER_LITERAL.matcher(text).matches() && !VALID_HEX_NUMBER_LITERAL.matcher(text).matches()) {
        holder.registerProblem(element, HCLBundle.message("hcl.literal.inspection.illegal.number.literal"), ProblemHighlightType.ERROR)
      }
    }
  }
}