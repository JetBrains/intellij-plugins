package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;

/**
 * @author Dennis.Ushakov
 */
public class CucumberAddExamplesColonFix implements LocalQuickFix {

  @NotNull
  public String getName() {
    return "Add missing ':' after examples keyword";
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }


  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiElement examples = descriptor.getPsiElement();

    final PsiFile featureFile = examples.getContainingFile();

    if (!CodeInsightUtilBase.prepareFileForWrite(featureFile)) {
      return;
    }

    final PsiElement[] elements = GherkinElementFactory.getTopLevelElements(project, ":");
    examples.getParent().addAfter(elements[0], examples);
  }
}
