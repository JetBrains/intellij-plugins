// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;

public final class JadeAttributeValueImpl extends XmlAttributeValueImpl {
  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(final @NotNull String text) {
    JadeAttributeValueManipulator.handleContentChangeSynthetic(this, new TextRange(0, getTextLength()), text);
    return this;
  }

  @Override
  public @NotNull LiteralTextEscaper<XmlAttributeValueImpl> createLiteralTextEscaper() {
    return new JadeAttributeValueLiteralEscaper(this);
  }


  @Override
  public @NotNull String getValue() {
    if (isSyntheticValue()) {
      return getText().substring(1);
    }

    final String value = super.getValue();
    return StringUtil.stripQuotesAroundValue(value);
  }

  public boolean isSyntheticValue() {
    final PsiElement firstChild = getFirstChild();
    return firstChild instanceof JadeTagIdImpl || firstChild instanceof JadeClassImpl;
  }

  public boolean isSyntheticClass() {
    final PsiElement firstChild = getFirstChild();
    return firstChild instanceof JadeClassImpl;
  }

  @Override
  public @NotNull Language getLanguage() {
    return JadeLanguage.INSTANCE;
  }

  @Override
  public boolean skipValidation() {
    return true;
  }
}
