package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberElementFactory;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

/**
 * @author Roman.Chernyatchik
 * @date Sep 5, 2009
 */
public class GherkinElementFactory {
  private static final Logger LOG = Logger.getInstance(GherkinElementFactory.class.getName());

  private GherkinElementFactory() {
  }

  public static GherkinFeature createFeatureFromText(final Project project, @NotNull final String text) {
    final PsiElement[] list = getTopLevelElements(project, text);
    for (PsiElement psiElement : list) {
      if (psiElement instanceof GherkinFeature) {
        return (GherkinFeature)psiElement;
      }
    }

    LOG.error("Failed to create Feature from text:\n" + text);
    return null;
  }

  public static GherkinStepsHolder createScenarioFromText(final Project project, final String language, @NotNull final String text) {
    final GherkinKeywordProvider provider = JsonGherkinKeywordProvider.getKeywordProvider();
    final GherkinKeywordTable keywordsTable = provider.getKeywordsTable(language);
    String featureText = "# language: " + language + "\n" + keywordsTable.getFeatureSectionKeyword() + ": Dummy\n" + text;
    GherkinFeature feature = createFeatureFromText(project, featureText);
    return feature.getScenarios() [0];
  }

  public static PsiElement[] getTopLevelElements(final Project project, @NotNull final String text) {
    return CucumberElementFactory.createTempPsiFile(project, text).getChildren();
  }
}
