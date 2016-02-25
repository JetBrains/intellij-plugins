package org.angularjs.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import org.angularjs.lang.parser.AngularJSParser;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSLanguage extends JSLanguageDialect {
  public static final AngularJSLanguage INSTANCE = new AngularJSLanguage();

  protected AngularJSLanguage() {
    super("AngularJS", DialectOptionHolder.OTHER);
  }

  @Override
  public String getFileExtension() {
    return "js";
  }

  @Override
  public JavaScriptParser<?, ?, ?, ?> createParser(@NotNull PsiBuilder builder) {
    return new AngularJSParser(builder);
  }
}
