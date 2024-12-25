// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinSyntaxHighlighter;
import org.jetbrains.plugins.cucumber.psi.PlainGherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl;

/**
 * @author Roman.Chernyatchik
 */
public final class GherkinLiveTemplateContextType extends TemplateContextType {
  public GherkinLiveTemplateContextType() {
    super(CucumberBundle.message("live.templates.context.cucumber.name"));
  }

  @Override
  public boolean isInContext(final @NotNull PsiFile file, final int offset) {
    return file instanceof GherkinFileImpl;
  }

  @Override
  public SyntaxHighlighter createHighlighter() {
    return new GherkinSyntaxHighlighter(new PlainGherkinKeywordProvider());
  }
}
