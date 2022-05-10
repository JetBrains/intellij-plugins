package org.jetbrains.plugins.cucumber;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinSyntaxHighlighter;
import org.jetbrains.plugins.cucumber.psi.PlainGherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinLiveTemplateContextType extends TemplateContextType {
  @NonNls
  private static final String CONTEXT_NAME = "CUCUMBER_FEATURE_FILE";

  public GherkinLiveTemplateContextType() {
    super(CONTEXT_NAME, CucumberBundle.message("live.templates.context.cucumber.name"));
  }

  @Override
  public boolean isInContext(@NotNull final PsiFile file, final int offset) {
    return file instanceof GherkinFileImpl;
  }

  @Override
  public SyntaxHighlighter createHighlighter() {
    return new GherkinSyntaxHighlighter(new PlainGherkinKeywordProvider());
  }
}
