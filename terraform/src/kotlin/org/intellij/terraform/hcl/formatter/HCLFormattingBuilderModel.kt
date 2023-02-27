/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hcl.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.intellij.terraform.hcl.HCLElementTypes.*
import org.intellij.terraform.hcl.HCLLanguage


open class HCLFormattingBuilderModel(val language: Language = HCLLanguage) : FormattingModelBuilder {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val settings = formattingContext.codeStyleSettings
    val builder = createSpacingBuilder(settings)
    val block = HCLBlock(null, formattingContext.node, null, null, builder, Indent.getNoneIndent(),
                         settings.getCustomSettings(HCLCodeStyleSettings::class.java))
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
  }

  override fun getRangeAffectingIndent(file: PsiFile?, offset: Int, elementAtOffset: ASTNode?): TextRange? {
    return null
  }
}
