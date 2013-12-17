package com.jetbrains.lang.dart;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 10/12/11
 * Time: 8:02 PM
 */
public class DartLanguage extends Language {
  public static final Language INSTANCE = new DartLanguage();

  private DartLanguage() {
    super("Dart", "application/dart");
  }

  @Nullable
  @Override
  public LanguageFileType getAssociatedFileType() {
    return DartFileType.INSTANCE;
  }
}
