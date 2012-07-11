package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl;

import java.util.*;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinKeywordTable {
  private Map<IElementType, Collection<String>> myType2KeywordsTable = new HashMap<IElementType, Collection<String>>();

  public GherkinKeywordTable() {
    for (IElementType type : GherkinTokenTypes.KEYWORDS.getTypes()) {
      myType2KeywordsTable.put(type, new ArrayList<String>());
    }
  }

  public void put(IElementType type, String keyword) {
    if (GherkinTokenTypes.KEYWORDS.contains(type)) {
      Collection<String> keywords = getKeywords(type);
      if (keywords == null) {
        keywords = new ArrayList<String>(1);
        myType2KeywordsTable.put(type, keywords);
      }
      keywords.add(keyword);
    }
  }

  public Collection<String> getStepKeywords() {
    final Collection<String> keywords = getKeywords(GherkinTokenTypes.STEP_KEYWORD);
    assert keywords != null;
    return keywords;
  }

  public Collection<String> getScenarioLikeKeywords() {
    final Set<String> keywords = new HashSet<String>();

    final Collection<String> scenarios = getKeywords(GherkinTokenTypes.SCENARIO_KEYWORD);
    assert scenarios != null;
    keywords.addAll(scenarios);

    final Collection<String> scenarioOutline = getKeywords(GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD);
    assert scenarioOutline != null;
    keywords.addAll(scenarioOutline);

    return keywords;
  }

  public String getScenarioOutlineKeyword() {
    return getScenarioOutlineKeywords().iterator().next();
  }

  public Collection<String> getScenarioOutlineKeywords() {

    final Collection<String> scenarioOutline = getKeywords(GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD);
    assert scenarioOutline != null;

    return scenarioOutline;
  }

  public Collection<String> getBackgroundKeywords() {
    final Collection<String> bg = getKeywords(GherkinTokenTypes.BACKGROUND_KEYWORD);
    assert bg != null;

    return bg;
  }

  public String getExampleSectionKeyword() {
    return getExampleSectionKeywords().iterator().next();
  }

  public Collection<String> getExampleSectionKeywords() {
    final Collection<String> keywords = getKeywords(GherkinTokenTypes.EXAMPLES_KEYWORD);
    assert keywords != null;
    return keywords;
  }

  public String getFeatureSectionKeyword() {
    return getFeaturesSectionKeywords().iterator().next();
  }

  public Collection<String> getFeaturesSectionKeywords() {
    final Collection<String> keywords = getKeywords(GherkinTokenTypes.FEATURE_KEYWORD);
    assert keywords != null;
    return keywords;
  }

  @NotNull
  public static GherkinKeywordTable getKeywordsTable(PsiFile originalFile, Project project) {
    final GherkinKeywordProvider provider = CucumberLanguageService.getInstance(project).getKeywordProvider();

    // find language comment
    final String language = getFeatureLanguage(originalFile);
    return provider.getKeywordsTable(language);
  }

  @NotNull
  public static String getFeatureLanguage(PsiFile originalFile) {
    return  originalFile instanceof GherkinFile
            ? ((GherkinFile)originalFile).getLocaleLanguage()
            : GherkinFileImpl.getDefaultLocale();
  }

  public Collection<IElementType> getTypes() {
    return myType2KeywordsTable.keySet();
  }

  @Nullable
  public Collection<String> getKeywords(final IElementType type) {
    return myType2KeywordsTable.get(type);
  }
}