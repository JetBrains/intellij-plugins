package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author yole
 */
public class GherkinFileType extends LanguageFileType {
  public static final GherkinFileType INSTANCE = new GherkinFileType();

  protected GherkinFileType() {
    super(GherkinLanguage.INSTANCE);
  }

  @Override
  @NotNull
  public String getName() {
    return "Cucumber";
  }

  @Override
  @NotNull
  public String getDescription() {
    return "Cucumber scenario";
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
    return "feature";
  }

  @Override
  public Icon getIcon() {
    return icons.CucumberIcons.Cucumber;
  }
}
