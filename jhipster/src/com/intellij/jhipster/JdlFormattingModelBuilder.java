// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.formatting.FormattingModelProvider.createFormattingModelForPsiFile;
import static com.intellij.jhipster.psi.JdlTokenSets.SPACING_TAIL_ELEMENTS;
import static com.intellij.jhipster.psi.JdlTokenTypes.COMMA;
import static com.intellij.jhipster.psi.JdlTokenTypes.LBRACE;
import static com.intellij.jhipster.psi.JdlTokenTypes.LBRACKET;
import static com.intellij.jhipster.psi.JdlTokenTypes.RBRACE;
import static com.intellij.jhipster.psi.JdlTokenTypes.RBRACKET;

final class JdlFormattingModelBuilder implements FormattingModelBuilder {
  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    var settings = formattingContext.getCodeStyleSettings();
    var spacingBuilder = createSpacingBuilder(settings);
    var topBlock = new JdlBlock(
      formattingContext.getNode(),
      null,
      settings.getCustomSettings(JdlCodeStyleSettings.class),
      null,
      Indent.getSmartIndent(Indent.Type.CONTINUATION),
      null,
      spacingBuilder
    );
    return createFormattingModelForPsiFile(formattingContext.getContainingFile(), topBlock, settings);
  }

  static @NotNull SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
    var jdlSettings = settings.getCustomSettings(JdlCodeStyleSettings.class);
    var commonSettings = settings.getCommonSettings(JdlLanguage.INSTANCE);

    var spacesBeforeComma = commonSettings.SPACE_BEFORE_COMMA ? 1 : 0;
    var spacesBeforeLbrace = jdlSettings.SPACE_BEFORE_LBRACE ? 1 : 0;

    return new SpacingBuilder(settings, JdlLanguage.INSTANCE)
      .withinPair(LBRACKET, RBRACKET).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS, true)
      .withinPair(LBRACE, RBRACE).spaceIf(commonSettings.SPACE_WITHIN_BRACES, true)
      .before(COMMA).spacing(spacesBeforeComma, spacesBeforeComma, 0, false, 0)
      .after(COMMA).spaceIf(commonSettings.SPACE_AFTER_COMMA)
      .before(LBRACE).spacing(spacesBeforeLbrace, spacesBeforeLbrace, 0, false, 0)
      .after(SPACING_TAIL_ELEMENTS).spacing(1, 1, 0, false, 0);
  }
}
