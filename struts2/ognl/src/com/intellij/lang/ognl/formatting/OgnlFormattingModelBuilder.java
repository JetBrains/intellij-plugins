/*
 * Copyright 2013 The authors
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

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.psi.OgnlTokenGroups;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.ognl.OgnlTypes.*;

/**
 * Provides basic (whitespace only) formatting for OGNL using (Java) default settings.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlFormattingModelBuilder implements FormattingModelBuilder {

  @NotNull
  @Override
  public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {

    final SpacingBuilder spacingBuilder = createSpacingBuilder(settings);

    final PsiFile containingFile = element.getContainingFile();
    final OgnlBlock ognlBlock = new OgnlBlock(element.getNode(), spacingBuilder);

    return FormattingModelProvider.createFormattingModelForPsiFile(containingFile, ognlBlock, settings);
  }

  @Override
  public TextRange getRangeAffectingIndent(final PsiFile file, final int offset, final ASTNode elementAtOffset) {
    return null;
  }

  private static SpacingBuilder createSpacingBuilder(final CodeStyleSettings settings) {
    return new SpacingBuilder(settings)
      .after(COMMA).spaceIf(settings.SPACE_AFTER_COMMA)
      .before(COMMA).spaceIf(settings.SPACE_BEFORE_COMMA)

      .after(QUESTION).spaceIf(settings.SPACE_AFTER_QUEST)
      .before(QUESTION).spaceIf(settings.SPACE_BEFORE_QUEST)

      .after(COLON).spaceIf(settings.SPACE_AFTER_COLON)
      .before(COLON).spaceIf(settings.SPACE_BEFORE_COLON)

      .withinPair(LPARENTH, RPARENTH).spaceIf(settings.SPACE_WITHIN_PARENTHESES)
      .withinPair(LBRACE, RBRACE).spaceIf(settings.SPACE_WITHIN_BRACES)
      .withinPair(LBRACKET, RBRACKET).spaceIf(settings.SPACE_WITHIN_BRACKETS)

      .aroundInside(OgnlTokenGroups.ADDITION_OPS, BINARY_EXPRESSION).spaceIf(settings.SPACE_AROUND_ADDITIVE_OPERATORS)
      .aroundInside(OgnlTokenGroups.MULTIPLICATION_OPS, BINARY_EXPRESSION).spaceIf(settings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)

      .around(OgnlTokenGroups.UNARY_OPS).spaceIf(settings.SPACE_AROUND_UNARY_OPERATOR)
      .around(OgnlTokenGroups.EQUALITY_OPS).spaceIf(settings.SPACE_AROUND_EQUALITY_OPERATORS)
      .around(OgnlTokenGroups.RELATIONAL_OPS).spaceIf(settings.SPACE_AROUND_RELATIONAL_OPERATORS)

      .around(OgnlTokenGroups.LOGICAL_OPS).spaceIf(settings.SPACE_AROUND_LOGICAL_OPERATORS)
      .around(OgnlTokenGroups.SHIFT_OPS).spaceIf(settings.SPACE_AROUND_SHIFT_OPERATORS)
      .around(OgnlTokenGroups.BITWISE_OPS).spaceIf(settings.SPACE_AROUND_BITWISE_OPERATORS);
  }
}