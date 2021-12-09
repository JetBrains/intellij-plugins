/*
 * Copyright 2014 The authors
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

package com.intellij.lang.ognl.formatting;

import com.intellij.formatting.*;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.psi.OgnlTokenGroups;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.ognl.OgnlTypes.*;

/**
 * Provides basic (whitespace only) formatting for OGNL using (Java) default settings.
 */
public class OgnlFormattingModelBuilder implements FormattingModelBuilder {

  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
    final SpacingBuilder spacingBuilder = createSpacingBuilder(settings);

    final PsiFile containingFile = formattingContext.getContainingFile();
    final OgnlBlock ognlBlock = new OgnlBlock(formattingContext.getNode(), spacingBuilder);

    return FormattingModelProvider.createFormattingModelForPsiFile(containingFile, ognlBlock, settings);
  }

  private static SpacingBuilder createSpacingBuilder(final CodeStyleSettings settings) {
    CommonCodeStyleSettings javaSettings = settings.getCommonSettings(JavaLanguage.INSTANCE);
    return new SpacingBuilder(settings, OgnlLanguage.INSTANCE)
      .after(COMMA).spaceIf(javaSettings.SPACE_AFTER_COMMA)
      .before(COMMA).spaceIf(javaSettings.SPACE_BEFORE_COMMA)

      .after(QUESTION).spaceIf(javaSettings.SPACE_AFTER_QUEST)
      .before(QUESTION).spaceIf(javaSettings.SPACE_BEFORE_QUEST)

      .after(COLON).spaceIf(javaSettings.SPACE_AFTER_COLON)
      .before(COLON).spaceIf(javaSettings.SPACE_BEFORE_COLON)

      .withinPair(LPARENTH, RPARENTH).spaceIf(javaSettings.SPACE_WITHIN_PARENTHESES)
      .withinPair(LBRACE, RBRACE).spaceIf(javaSettings.SPACE_WITHIN_BRACES)
      .withinPair(LBRACKET, RBRACKET).spaceIf(javaSettings.SPACE_WITHIN_BRACKETS)

      .aroundInside(OgnlTokenGroups.ADDITION_OPS, BINARY_EXPRESSION).spaceIf(javaSettings.SPACE_AROUND_ADDITIVE_OPERATORS)
      .aroundInside(OgnlTokenGroups.MULTIPLICATION_OPS, BINARY_EXPRESSION).spaceIf(javaSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)

      .around(OgnlTokenGroups.UNARY_OPS).spaceIf(javaSettings.SPACE_AROUND_UNARY_OPERATOR)
      .around(OgnlTokenGroups.EQUALITY_OPS).spaceIf(javaSettings.SPACE_AROUND_EQUALITY_OPERATORS)
      .around(OgnlTokenGroups.RELATIONAL_OPS).spaceIf(javaSettings.SPACE_AROUND_RELATIONAL_OPERATORS)

      .around(OgnlTokenGroups.LOGICAL_OPS).spaceIf(javaSettings.SPACE_AROUND_LOGICAL_OPERATORS)
      .around(OgnlTokenGroups.SHIFT_OPS).spaceIf(javaSettings.SPACE_AROUND_SHIFT_OPERATORS)
      .around(OgnlTokenGroups.BITWISE_OPS).spaceIf(javaSettings.SPACE_AROUND_BITWISE_OPERATORS);
  }
}