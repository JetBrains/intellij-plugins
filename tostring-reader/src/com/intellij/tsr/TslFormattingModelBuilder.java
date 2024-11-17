// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.tsr;

import com.intellij.formatting.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.formatting.FormattingModelProvider.createFormattingModelForPsiFile;
import static com.intellij.tsr.psi.TslTokenTypes.*;

final class TslFormattingModelBuilder implements FormattingModelBuilder {
  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    var settings = formattingContext.getCodeStyleSettings();
    var spacingBuilder = createSpacingBuilder(settings);
    var topBlock = new TslBlock(
        formattingContext.getNode(),
        null,
        null,
        Indent.getSmartIndent(Indent.Type.CONTINUATION),
        null,
        spacingBuilder
    );
    return createFormattingModelForPsiFile(formattingContext.getContainingFile(), topBlock, settings);
  }

  static @NotNull SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
    var commonSettings = settings.getCommonSettings(TslLanguage.INSTANCE);
    var spacesBeforeComma = commonSettings.SPACE_BEFORE_COMMA ? 1 : 0;

    return new SpacingBuilder(settings, TslLanguage.INSTANCE)
        .withinPair(LBRACKET, RBRACKET).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS, true)
        .withinPair(LBRACE, RBRACE).spaceIf(commonSettings.SPACE_WITHIN_BRACES, true)
        .before(COMMA).spacing(spacesBeforeComma, spacesBeforeComma, 0, false, 0)
        .after(COMMA).spaceIf(commonSettings.SPACE_AFTER_COMMA)
        .around(ASSIGN).spaces(1);
  }
}
