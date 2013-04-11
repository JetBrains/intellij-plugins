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
    return getCommenter().getLineCommentPrefix();
  }

  @Nullable
  @Override
  public String getBlockCommentPrefix() {
    return getCommenter().getBlockCommentPrefix();
  }

  @Nullable
  @Override
  public String getBlockCommentSuffix() {
    return getCommenter().getBlockCommentSuffix();
  }

  @Nullable
  @Override
  public String getCommentedBlockCommentPrefix() {
    return getCommenter().getCommentedBlockCommentPrefix();
  }

  @Nullable
  @Override
  public String getCommentedBlockCommentSuffix() {
    return getCommenter().getCommentedBlockCommentSuffix();
  }

  private Commenter getCommenter() {
    Language commenterLanguage = HbConfig.getCommenterLanguage();
    if (commenterLanguage == null) {
      commenterLanguage = HbLanguage.getDefaultTemplateLang().getLanguage();
    }
    else if (commenterLanguage.getID().equals(HbLanguage.INSTANCE.getID())) {
      return ourHandlebarsCommenter;
    }

    return LanguageCommenters.INSTANCE.forLanguage(commenterLanguage);
  }
}
