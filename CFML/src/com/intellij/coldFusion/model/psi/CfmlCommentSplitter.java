// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.spellchecker.inspections.BaseSplitter;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.inspections.Splitter;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public final class CfmlCommentSplitter extends BaseSplitter {

  @NotNull
  private static final Pattern CFML = Pattern.compile("</?[Cc][Ff](\\S)+");

  @NotNull
  private static final Splitter ps = PlainTextSplitter.getInstance();

  @NotNull
  public static final CfmlCommentSplitter INSTANCE = new CfmlCommentSplitter();

  private CfmlCommentSplitter() {
  }

  @Override
  public void split(@Nullable String text, @NotNull TextRange range, Consumer<TextRange> consumer) {
    if (StringUtil.isEmpty(text)) {
      return;
    }

    List<TextRange> toCheck = excludeByPattern(text, range, CFML, 1);
    for (TextRange r : toCheck) {
      ps.split(text, r, consumer);
    }
  }
}
