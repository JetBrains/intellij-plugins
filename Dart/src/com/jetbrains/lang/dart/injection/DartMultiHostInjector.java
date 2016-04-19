package com.jetbrains.lang.dart.injection;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DartMultiHostInjector implements MultiHostInjector {
  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(DartStringLiteralExpression.class);
  }

  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement element) {
    if (element instanceof DartStringLiteralExpression) {
      injectHtmlIfNeeded(registrar, (DartStringLiteralExpression)element);
    }
  }

  private static void injectHtmlIfNeeded(@NotNull final MultiHostRegistrar registrar, @NotNull final DartStringLiteralExpression element) {
    final List<HtmlPlaceInfo> infos = new SmartList<>();
    final StringBuilder textBuf = new StringBuilder();
    PsiElement child = element.getFirstChild();

    while (child != null) {
      final IElementType type = child.getNode().getElementType();

      if (type == DartTokenTypes.REGULAR_STRING_PART) {
        textBuf.append(child.getText());

        String suffix = null;
        final PsiElement nextSibling = child.getNextSibling();
        if (nextSibling != null && nextSibling.getNode().getElementType() != DartTokenTypes.CLOSING_QUOTE) {
          suffix = "placeholder"; // string template like $foo or ${foo}
          textBuf.append(suffix);
        }

        infos.add(new HtmlPlaceInfo(TextRange.from(child.getStartOffsetInParent(), child.getTextLength()), suffix));
      }
      else if (type == DartTokenTypes.RAW_SINGLE_QUOTED_STRING || type == DartTokenTypes.RAW_TRIPLE_QUOTED_STRING) {
        final Pair<String, TextRange> stringAndRange = DartPsiImplUtil.getUnquotedDartStringAndItsRange(child.getText());
        final String string = stringAndRange.first;
        final TextRange stringRange = stringAndRange.second;
        infos.add(new HtmlPlaceInfo(stringRange.shiftRight(child.getStartOffsetInParent()), null));
        textBuf.append(string);
      }

      child = child.getNextSibling();
    }

    if (textBuf.length() > 0 && looksLikeHtml(textBuf.toString())) {
      registrar.startInjecting(HTMLLanguage.INSTANCE);

      for (HtmlPlaceInfo info : infos) {
        registrar.addPlace(null, info.suffix, element, info.range);
      }

      registrar.doneInjecting();
    }
  }

  private static boolean looksLikeHtml(@NotNull final String text) {
    // similar to com.intellij.lang.javascript.JSInjectionController.willInjectHtml()
    final int tagStart = text.indexOf('<');
    final int length = text.length();
    return tagStart >= 0
           &&
           (tagStart < length - 1 && (Character.isLetter(text.charAt(tagStart + 1))) // <tag>
            ||
            (tagStart < length - 2 && text.charAt(tagStart + 1) == '/' && Character.isLetter(text.charAt(tagStart + 2))) // </tag>
            ||
            (tagStart < length - 3 && text.charAt(tagStart + 1) == '!' &&
             text.charAt(tagStart + 2) == '-') && text.charAt(tagStart + 3) == '-' // <!-- comment
           )
           &&
           text.indexOf('>', tagStart) > 0;
  }

  private static class HtmlPlaceInfo {
    @NotNull private final TextRange range;
    @Nullable private final String suffix;

    public HtmlPlaceInfo(@NotNull final TextRange range, @Nullable final String suffix) {
      this.range = range;
      this.suffix = suffix;
    }
  }
}
