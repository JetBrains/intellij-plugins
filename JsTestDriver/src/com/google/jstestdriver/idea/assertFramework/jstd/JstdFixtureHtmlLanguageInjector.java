package com.google.jstestdriver.idea.assertFramework.jstd;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class JstdFixtureHtmlLanguageInjector implements LanguageInjector {

  private static final Pattern[] PATTERNS = new Pattern[] {
    Pattern.compile("/\\*:DOC\\s*\\+=(.*)\\*/", Pattern.DOTALL),
    Pattern.compile("/\\*:DOC\\s+\\w*\\s*=(.*)\\*/", Pattern.DOTALL)
  };

  private static final Language HTML_LANGUAGE = Language.findLanguageByID("HTML");

  @Override
  public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                   @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
    if (host instanceof PsiComment) {
      PsiComment comment = (PsiComment) host;
      if (comment.getTokenType() == JSTokenTypes.C_STYLE_COMMENT) {
        String commentStr = comment.getText();
        TextRange htmlTextRange = findTextRange(commentStr);
        if (htmlTextRange != null && HTML_LANGUAGE != null) {
          injectionPlacesRegistrar.addPlace(HTML_LANGUAGE, htmlTextRange, null, null);
        }
      }
    }
  }

  @Nullable
  private static TextRange findTextRange(@NotNull String commentStr) {
    for (Pattern pattern : PATTERNS) {
      Matcher matcher = pattern.matcher(commentStr);
      if (matcher.matches()) {
        String original = matcher.group(1);
        String trimmed = original.trim();
        int offset = original.indexOf(trimmed);
        if (offset >= 0 && !trimmed.isEmpty()) {
          int startInd = matcher.start(1) + offset;
          return TextRange.create(startInd, startInd + trimmed.length());
        }
      }
    }
    return null;
  }

}
