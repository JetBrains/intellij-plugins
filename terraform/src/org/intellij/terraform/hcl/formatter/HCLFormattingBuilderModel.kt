// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.intellij.terraform.hcl.HCLElementTypes.BLOCK
import org.intellij.terraform.hcl.HCLElementTypes.BLOCK_OBJECT
import org.intellij.terraform.hcl.HCLElementTypes.COMMA
import org.intellij.terraform.hcl.HCLElementTypes.EQUALS
import org.intellij.terraform.hcl.HCLElementTypes.HEREDOC_LITERAL
import org.intellij.terraform.hcl.HCLElementTypes.IDENTIFIER
import org.intellij.terraform.hcl.HCLElementTypes.L_BRACKET
import org.intellij.terraform.hcl.HCLElementTypes.L_CURLY
import org.intellij.terraform.hcl.HCLElementTypes.L_PAREN
import org.intellij.terraform.hcl.HCLElementTypes.OBJECT
import org.intellij.terraform.hcl.HCLElementTypes.OP_COLON
import org.intellij.terraform.hcl.HCLElementTypes.OP_DOT
import org.intellij.terraform.hcl.HCLElementTypes.OP_ELLIPSIS
import org.intellij.terraform.hcl.HCLElementTypes.OP_MAPPING
import org.intellij.terraform.hcl.HCLElementTypes.OP_NOT
import org.intellij.terraform.hcl.HCLElementTypes.OP_QUEST
import org.intellij.terraform.hcl.HCLElementTypes.PARAMETER_LIST
import org.intellij.terraform.hcl.HCLElementTypes.R_BRACKET
import org.intellij.terraform.hcl.HCLElementTypes.R_CURLY
import org.intellij.terraform.hcl.HCLElementTypes.R_PAREN
import org.intellij.terraform.hcl.HCLElementTypes.STRING_LITERAL
import org.intellij.terraform.hcl.HCLLanguage

open class HCLFormattingBuilderModel(val language: Language = HCLLanguage) : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val settings = formattingContext.codeStyleSettings
    val builder = createSpacingBuilder(settings)
    val block = HCLBlock(null, formattingContext.node, null, null, builder, Indent.getNoneIndent(),
                         settings.getCustomSettings(HclCodeStyleSettings::class.java))

    return FormattingModelProvider.createFormattingModelForPsiFile(formattingContext.containingFile, block, settings)
  }

  private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
    val commonSettings = settings.getCommonSettings(language)

    val spacesBeforeComma = if (commonSettings.SPACE_BEFORE_COMMA) 1 else 0
    val spacesAroundAssignment = if (commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS) 1 else 0

    return SpacingBuilder(settings, language)
      .after(HEREDOC_LITERAL).lineBreakInCode()
      .before(EQUALS).spacing(spacesAroundAssignment, spacesAroundAssignment, 0, false, 0)
      .after(EQUALS).spacing(spacesAroundAssignment, spacesAroundAssignment, 0, false, 0)
      .afterInside(IDENTIFIER, BLOCK).spaces(1)
      .afterInside(STRING_LITERAL, BLOCK).spaces(1)
      .between(L_CURLY, R_CURLY).none()
      .withinPairInside(L_CURLY, R_CURLY, OBJECT).parentDependentLFSpacing(1, 0, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_IN_CODE)
      .withinPairInside(L_CURLY, R_CURLY, BLOCK_OBJECT).parentDependentLFSpacing(1, 0, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_IN_CODE)
      .withinPair(L_BRACKET, R_BRACKET).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS, true)
      .withinPair(L_CURLY, R_CURLY).spaceIf(commonSettings.SPACE_WITHIN_BRACES, true)
      .before(COMMA).spacing(spacesBeforeComma, spacesBeforeComma, 0, false, 0)
      .after(COMMA).spaceIf(commonSettings.SPACE_AFTER_COMMA)
      .before(OP_COLON).spaceIf(commonSettings.SPACE_BEFORE_COLON)
      .after(OP_COLON).spaceIf(commonSettings.SPACE_AFTER_COLON)
      .after(BLOCK).lineBreakInCode()
      .before(PARAMETER_LIST).none()
      .before(OP_ELLIPSIS).none()
      .before(OP_DOT).none()
      .after(OP_DOT).none()
      .after(OP_NOT).none()
      .around(L_CURLY).spaces(1)
      .around(R_CURLY).spaces(1)
      .around(OP_MAPPING).spaces(1)
      .withinPair(L_PAREN, R_PAREN).none()
      .around(OP_QUEST).spaces(1)
  }

  override fun getRangeAffectingIndent(file: PsiFile?, offset: Int, elementAtOffset: ASTNode?): TextRange? {
    return null
  }
}
