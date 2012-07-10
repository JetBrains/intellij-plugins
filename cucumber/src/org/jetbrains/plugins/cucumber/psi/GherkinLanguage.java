package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.Language;

/**
 * @author yole
 */
public class GherkinLanguage extends Language {
  public static GherkinLanguage INSTANCE = new GherkinLanguage();

  protected GherkinLanguage() {
    super("Gherkin", "");
  }

  @Override
  public String getDisplayName() {
    return "Gherkin";
  }
}
