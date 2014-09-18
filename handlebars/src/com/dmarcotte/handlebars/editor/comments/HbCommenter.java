package com.dmarcotte.handlebars.editor.comments;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.lang.Commenter;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageCommenters;
import org.jetbrains.annotations.Nullable;

public class HbCommenter implements Commenter {
  private static final Commenter ourHandlebarsCommenter = new HandlebarsCommenter();

  @Nullable
  @Override
  public String getLineCommentPrefix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getLineCommentPrefix() : null;
  }

  @Nullable
  @Override
  public String getBlockCommentPrefix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getBlockCommentPrefix() : null;
  }

  @Nullable
  @Override
  public String getBlockCommentSuffix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getBlockCommentSuffix() : null;
  }

  @Nullable
  @Override
  public String getCommentedBlockCommentPrefix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getCommentedBlockCommentPrefix() : null;
  }

  @Nullable
  @Override
  public String getCommentedBlockCommentSuffix() {
    Commenter commenter = getCommenter();
    return commenter != null ? commenter.getCommentedBlockCommentSuffix() : null;
  }

  private static Commenter getCommenter() {
    Language commenterLanguage = HbConfig.getCommenterLanguage();
    if (commenterLanguage == null) {
      commenterLanguage = HbLanguage.getDefaultTemplateLang().getLanguage();
    }
    else if (commenterLanguage.isKindOf(HbLanguage.INSTANCE)) {
      return ourHandlebarsCommenter;
    }

    return LanguageCommenters.INSTANCE.forLanguage(commenterLanguage);
  }
}
