package org.jetbrains.plugins.cucumber.resolve;

import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

public abstract class CucumberResolveTest extends CucumberCodeInsightTestCase {
  protected void checkReference(@NotNull final String step, @Nullable final String stepDefinitionName) {
    final CucumberStepReference ref = (CucumberStepReference)findReferenceBySignature(step);
    assert ref != null;

    final ResolveResult[] result = ref.multiResolve(true);
    boolean ok = stepDefinitionName == null;
    for (ResolveResult rr : result) {
      final PsiElement resolvedElement = rr.getElement();
      if (resolvedElement != null) {
        if (stepDefinitionName == null) {
          ok = false;
        } else {
          final String resolvedStepDefName = getStepDefinitionName(resolvedElement);
          if (resolvedStepDefName != null && resolvedStepDefName.equals(stepDefinitionName)) {
            ok = true;
            break;
          }
        }
      }
    }
    assertTrue(ok);
  }

  public void doTest(@NotNull final String folder, @NotNull final String step, @Nullable final String stepDefinitionName) throws Exception {
    init(folder);

    checkReference(step, stepDefinitionName);
  }

  public void doTest(@NotNull final String folder, @NotNull final String fileName, @NotNull final String step,
                     @Nullable final String stepDefinitionName) throws Exception {
    init(folder, fileName);

    checkReference(step, stepDefinitionName);
  }

  protected void init(String folder) {
    init(folder, "test.feature");
  }

  protected void init(String folder, String fileName) {
    CucumberStepsIndex.getInstance(myFixture.getProject()).reset();
    myFixture.copyDirectoryToProject(folder, "");
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir(fileName));
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + getRelatedTestDataPath();
  }

  @Nullable
  protected abstract String getStepDefinitionName(@NotNull PsiElement stepDefinition);

  protected abstract String getRelatedTestDataPath();
}
