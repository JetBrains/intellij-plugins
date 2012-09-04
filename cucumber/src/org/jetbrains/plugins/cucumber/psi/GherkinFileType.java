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

  @NotNull
  public String getName() {
    return "Cucumber";
  }

  @NotNull
  public String getDescription() {
    return "Cucumber scenario files";
  }

  @NotNull
  public String getDefaultExtension() {
    return "feature";
  }

  public Icon getIcon() {
    return icons.CucumberIcons.Cucumber;
  }
}
