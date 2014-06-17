package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.inspections.suppress.GherkinSuppressionUtil;

/**
 * @author Roman.Chernyatchik
 */
public abstract class GherkinInspection extends LocalInspectionTool {
  @NotNull
  public String getGroupDisplayName() {
    return CucumberBundle.message("cucumber.inspection.group.name");
  }

}