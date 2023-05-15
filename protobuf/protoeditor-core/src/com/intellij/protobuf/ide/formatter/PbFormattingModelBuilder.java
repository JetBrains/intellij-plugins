/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.PsiBasedFormattingModel;
import com.intellij.protobuf.lang.PbLanguage;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A {@link FormattingModelBuilder} for proto files. */
public class PbFormattingModelBuilder implements FormattingModelBuilder {

  @NotNull
  @Override
  public FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    PsiFile file = formattingContext.getContainingFile();
    return new PsiBasedFormattingModel(
        file,
        new PbBlock(
            formattingContext.getNode(),
            Wrap.createWrap(WrapType.NONE, false),
            null,
            createSpaceBuilder(formattingContext.getCodeStyleSettings())),
        FormattingDocumentModelImpl.createOn(file));
  }

  private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {

    CommonCodeStyleSettings protoSettings = settings.getCommonSettings(PbLanguage.INSTANCE);

    return new SpacingBuilder(settings, PbLanguage.INSTANCE)
        .withinPair(ProtoTokenTypes.LBRACE, ProtoTokenTypes.RBRACE)
        .spaceIf(protoSettings.SPACE_WITHIN_BRACES, false)
        .withinPair(ProtoTokenTypes.LBRACK, ProtoTokenTypes.RBRACK)
        .spaceIf(protoSettings.SPACE_WITHIN_BRACKETS, false)
        .withinPair(ProtoTokenTypes.LPAREN, ProtoTokenTypes.RPAREN)
        .spaceIf(protoSettings.SPACE_WITHIN_PARENTHESES, false)
        .before(ProtoTokenTypes.COMMA)
        .spaceIf(protoSettings.SPACE_BEFORE_COMMA)
        .after(ProtoTokenTypes.COMMA)
        .spaceIf(protoSettings.SPACE_AFTER_COMMA)
        // TODO(volkman): figure out how to add these settings.
        //        .before(PbTypes.LBRACE)
        //            .spaceIf(protoSettings.SPACE_BEFORE_LEFT_BRACE)
        //        .before(PbTypes.LBRACK)
        //            .spaceIf(protoSettings.SPACE_BEFORE_LEFT_BRACKET)
        .around(ProtoTokenTypes.ASSIGN)
        .spaceIf(protoSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);
  }

  @Nullable
  @Override
  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return FormattingModelBuilder.super.getRangeAffectingIndent(file, offset, elementAtOffset);
  }
}
