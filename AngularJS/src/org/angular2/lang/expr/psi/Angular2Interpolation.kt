// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import one.util.streamex.StreamEx;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Angular2Interpolation extends Angular2EmbeddedExpression {

  @Nullable
  JSExpression getExpression();

  static @NotNull Angular2Interpolation @NotNull [] get(@NotNull XmlAttribute attribute) {
    if (attribute instanceof Angular2HtmlPropertyBinding) {
      return ((Angular2HtmlPropertyBinding)attribute).getInterpolations();
    }
    if (attribute.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)) {
      return new Angular2Interpolation[0];
    }
    XmlAttributeValue value = attribute.getValueElement();
    // Pug and PHP support
    if (value != null
        && value.getTextLength() >= 2
        && Angular2LangUtil.isAngular2Context(value)) {
      PsiElement possibleHost = value.getContainingFile().findElementAt(value.getTextOffset() + 1);
      while (possibleHost != null && possibleHost != value && !(possibleHost instanceof PsiLanguageInjectionHost)) {
        possibleHost = possibleHost.getParent();
      }
      if (possibleHost != null) {
        @Nullable List<Pair<PsiElement, TextRange>> injections =
          InjectedLanguageManager.getInstance(attribute.getProject()).getInjectedPsiFiles(possibleHost);
        if (injections != null) {
          return StreamEx.of(injections)
            .map(inj -> PsiTreeUtil.findChildOfType(inj.first, Angular2Interpolation.class))
            .nonNull()
            .toArray(Angular2Interpolation[]::new);
        }
      }
    }
    return new Angular2Interpolation[0];
  }
}
