package com.intellij.tapestry.lang;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;

/**
 * @author Alexey Chmutov
 */
public final class TelLanguage extends Language implements InjectableLanguage {

  public static final TelLanguage INSTANCE = new TelLanguage();

  private TelLanguage() {
    super("TEL");
  }

  @Override
  public TelFileType getAssociatedFileType() {
    return TelFileType.INSTANCE;
  }
}
