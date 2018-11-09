package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;

/**
 * @author Roman.Chernyatchik
 */
public abstract class GherkinInspection extends LocalInspectionTool {
  @Override
  @NotNull
  public String getGroupDisplayName() {
    return CucumberBundle.message("cucumber.inspection.group.name");
  }

}