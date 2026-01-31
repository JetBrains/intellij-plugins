// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.jhipster.model.JdlOptionModel;
import com.intellij.jhipster.psi.JdlEntityId;
import com.intellij.jhipster.psi.JdlEnumId;
import com.intellij.jhipster.psi.JdlEnumKey;
import com.intellij.jhipster.psi.JdlFieldName;
import com.intellij.jhipster.psi.JdlId;
import com.intellij.jhipster.psi.JdlOptionName;
import com.intellij.jhipster.psi.JdlStringLiteral;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

final class JdlSpellcheckingStrategy extends SpellcheckingStrategy implements DumbAware {
  private final Tokenizer<JdlStringLiteral> ourStringLiteralTokenizer = new Tokenizer<>() {
    @Override
    public void tokenize(@NotNull JdlStringLiteral element, @NotNull TokenConsumer consumer) {
      final PlainTextSplitter textSplitter = PlainTextSplitter.getInstance();
      if (element.textContains('\\')) {
        var fragments = element.getTextFragments();
        for (Pair<TextRange, String> fragment : fragments) {
          TextRange fragmentRange = fragment.getFirst();
          String escaped = fragment.getSecond();
          // Fragment without escaping, also not a broken escape sequence or a Unicode code point
          if (escaped.length() == fragmentRange.getLength() && !escaped.startsWith("\\")) {
            consumer.consumeToken(element, escaped, false, fragmentRange.getStartOffset(), TextRange.allOf(escaped), textSplitter);
          }
        }
      }
      else {
        consumer.consumeToken(element, textSplitter);
      }
    }
  };

  private final Tokenizer<PsiElement> idLiteralTokenizer = new Tokenizer<>() {
    @Override
    public void tokenize(@NotNull PsiElement element, @NotNull TokenConsumer consumer) {
      PlainTextSplitter textSplitter = PlainTextSplitter.getInstance();
      consumer.consumeToken(element, textSplitter);
    }
  };

  @Override
  public @NotNull Tokenizer<?> getTokenizer(PsiElement element) {
    if (element instanceof JdlStringLiteral) {
      return ourStringLiteralTokenizer;
    }

    if (element instanceof JdlOptionName) {
      String key = element.getText();
      if (JdlOptionModel.INSTANCE.getApplicationConfigOptions().get(key) != null
          || JdlOptionModel.INSTANCE.getDeploymentOptions().get(key) != null) {
        return EMPTY_TOKENIZER;
      }
    }

    if (element instanceof JdlId
        || element instanceof JdlEntityId
        || element instanceof JdlEnumId
        || element instanceof JdlEnumKey
        || element instanceof JdlFieldName) {
      return idLiteralTokenizer;
    }

    return super.getTokenizer(element);
  }
}