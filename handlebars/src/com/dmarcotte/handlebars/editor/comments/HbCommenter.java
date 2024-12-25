// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.editor.comments;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageCommenters;
import org.jetbrains.annotations.Nullable;

public final class HbCommenter implements Commenter {
  private static final Commenter ourHandlebarsCommenter = new HandlebarsCommenter();

  @Override
  public @Nullable String getLineCommentPrefix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getLineCommentPrefix() : null;
  }

  @Override
  public @Nullable String getBlockCommentPrefix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getBlockCommentPrefix() : null;
  }

  @Override
  public @Nullable String getBlockCommentSuffix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getBlockCommentSuffix() : null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentPrefix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getCommentedBlockCommentPrefix() : null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentSuffix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getCommentedBlockCommentSuffix() : null;
  }

  private static Commenter getCommenter() {
    Language commenterLanguage = HbConfig.getCommenterLanguage();
    if (commenterLanguage.isKindOf(HbLanguage.INSTANCE)) {
      return ourHandlebarsCommenter;
    }

    return LanguageCommenters.INSTANCE.forLanguage(commenterLanguage);
  }
}
