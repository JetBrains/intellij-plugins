// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

public final class DartMultiHostInjector implements MultiHostInjector {

  // Trailing space is intentional, it helps to avoid errors like "XML attribute Dart_string_template_placeholderhref is not allowed here"
  // in strings like "<a ${label}href=''></a>". As for the leading space, we decided not to add it as we'd more likely get
  // a string like "<${label}>...</${label}>" than "<a foo${label}>...</a>", and we'd like it to be parsed as HTML.
  public static final String STRING_TEMPLATE_PLACEHOLDER = "Dart_string_template_placeholder ";

  private static final @Nullable Language JS_REGEXP_LANG = Language.findLanguageByID("JSRegexp");

  @Override
  public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(DartStringLiteralExpression.class);
  }

  @Override
  public void getLanguagesToInject(final @NotNull MultiHostRegistrar registrar, final @NotNull PsiElement element) {
    if (element instanceof DartStringLiteralExpression) {
      if (isRegExp((DartStringLiteralExpression)element)) {
        injectRegExp(registrar, (DartStringLiteralExpression)element);
      }
      else {
        injectHtmlIfNeeded(registrar, (DartStringLiteralExpression)element);
      }
    }
  }

  private static boolean isRegExp(final @NotNull DartStringLiteralExpression element) {
    // new RegExp(r'\d+'); RegExp(r'\d+')
    final PsiElement parent1 = element.getParent();
    final PsiElement parentParent2 = parent1 instanceof DartArgumentList && parent1.getFirstChild() == element ? parent1.getParent() : null;
    final PsiElement parent3 = parentParent2 instanceof DartArguments ? parentParent2.getParent() : null;
    if (parent3 instanceof DartNewExpression) {
      final DartType type = ((DartNewExpression)parent3).getType();
      return type != null && "RegExp".equals(type.getText());
    }
    if (parent3 instanceof DartCallExpression) {
      final DartExpression expression = ((DartCallExpression)parent3).getExpression();
      return expression != null && "RegExp".equals(expression.getText());
    }
    return false;
  }

  private static void injectRegExp(final @NotNull MultiHostRegistrar registrar, final @NotNull DartStringLiteralExpression element) {
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

  private static void injectHtmlIfNeeded(final @NotNull MultiHostRegistrar registrar, final @NotNull DartStringLiteralExpression element) {
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
          suffix = STRING_TEMPLATE_PLACEHOLDER; // string template like $foo or ${foo}
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

    if (!textBuf.isEmpty() && looksLikeHtml(textBuf.toString())) {
      registrar.startInjecting(HTMLLanguage.INSTANCE);

      for (HtmlPlaceInfo info : infos) {
        registrar.addPlace(null, info.suffix, element, info.range);
      }

      registrar.doneInjecting();
    }
  }

  private static boolean looksLikeHtml(final @NotNull String text) {
    // similar to com.intellij.lang.javascript.JSInjectionController.willInjectHtml(), but strings like 'List<int>', '<foo> and <bar>' are not treated as HTML
    // also, unlike JavaScript, HTML is injected in Dart only if '<' is the first non-space symbol in string
    if (!text.trim().startsWith("<")) return false;

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
    private final @NotNull TextRange range;
    private final @Nullable String suffix;

    HtmlPlaceInfo(final @NotNull TextRange range, final @Nullable String suffix) {
      this.range = range;
      this.suffix = suffix;
    }
  }
}
