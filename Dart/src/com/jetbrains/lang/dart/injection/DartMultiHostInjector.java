package com.jetbrains.lang.dart.injection;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DartMultiHostInjector implements MultiHostInjector {

  @Nullable private static final Language JS_REGEXP_LANG = Language.findLanguageByID("JSRegexp");

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(DartStringLiteralExpression.class);
  }

  @Override
  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement element) {
    if (element instanceof DartStringLiteralExpression) {
      if (isRegExp((DartStringLiteralExpression)element)) {
        injectRegExp(registrar, (DartStringLiteralExpression)element);
      }
      else {
        injectHtmlIfNeeded(registrar, (DartStringLiteralExpression)element);
      }
    }
  }

  private static boolean isRegExp(@NotNull final DartStringLiteralExpression element) {
    // new RegExp(r'\d+')
    final PsiElement parent1 = element.getParent();
    final PsiElement parentParent2 = parent1 instanceof DartArgumentList && parent1.getFirstChild() == element ? parent1.getParent() : null;
    final PsiElement parent3 = parentParent2 instanceof DartArguments ? parentParent2.getParent() : null;
    if (parent3 instanceof DartNewExpression) {
      final DartType type = ((DartNewExpression)parent3).getType();
      return type != null && "RegExp".equals(type.getText());
    }
    return false;
  }

  private static void injectRegExp(@NotNull final MultiHostRegistrar registrar, @NotNull final DartStringLiteralExpression element) {
    if (JS_REGEXP_LANG == null) return; // JavaScript plugin not available

    final PsiElement child = element.getFirstChild();
    final IElementType elementType = child.getNode().getElementType();
    if (elementType != DartTokenTypes.RAW_SINGLE_QUOTED_STRING || child.getNextSibling() != null) {
      return; // inject in raw single line strings only
    }

    final Pair<String, TextRange> textAndRange = DartPsiImplUtil.getUnquotedDartStringAndItsRange(child.getText());
    if (textAndRange.first.isEmpty()) {
      return;
    }

    registrar.startInjecting(JS_REGEXP_LANG);
    registrar.addPlace(null, null, element, textAndRange.second);
    registrar.doneInjecting();
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
    // similar to com.intellij.lang.javascript.JSInjectionController.willInjectHtml(), but strings like 'List<int>', '<foo> and <bar>' are not treated as HTML
    final int tagStart = text.indexOf('<');
    final int length = text.length();
    final boolean hasTag = tagStart >= 0
                           &&
                           (tagStart < length - 1 && (Character.isLetter(text.charAt(tagStart + 1)))
                            // <tag>
                            ||
                            (tagStart < length - 2 && text.charAt(tagStart + 1) == '/' && Character.isLetter(text.charAt(tagStart + 2)))
                            // </tag>
                            ||
                            (tagStart < length - 3 && text.charAt(tagStart + 1) == '!' &&
                             text.charAt(tagStart + 2) == '-') && text.charAt(tagStart + 3) == '-' // <!-- comment
                           )
                           &&
                           text.indexOf('>', tagStart) > 0;

    if (hasTag) {
      // now filter out cases like '<foo> and <bar>' or 'Map<int, int>'
      if (Character.isLetter(text.charAt(tagStart + 1)) && !text.contains("/>") && !text.contains("</")) {
        return false;
      }
    }

    return hasTag;
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
