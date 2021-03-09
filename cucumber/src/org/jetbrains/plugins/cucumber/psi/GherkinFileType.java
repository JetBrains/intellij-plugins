package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.CucumberIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;

import javax.swing.*;

/**
 * @author yole
 */
public class GherkinFileType extends LanguageFileType {
  public static final GherkinFileType INSTANCE = new GherkinFileType();

  private GherkinFileType() {
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
    return CucumberBundle.message("filetype.cucumber.scenario.description");
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
    return "feature";
  }

  @Override
  public Icon getIcon() {
    return CucumberIcons.Cucumber;
  }
}
