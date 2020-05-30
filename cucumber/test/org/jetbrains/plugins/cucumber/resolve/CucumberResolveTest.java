package org.jetbrains.plugins.cucumber.resolve;

import com.intellij.openapi.application.PathManager;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;

public abstract class CucumberResolveTest extends CucumberCodeInsightTestCase {
  protected void checkReference(@NotNull final String element, @Nullable final String stepDefinitionName) {
    final ResolveResult[] result = getResolveResult(element);
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

  private ResolveResult[] getResolveResult(@NotNull String step) {
    final PsiReference reference = findReferenceBySignature(step);
    if (reference instanceof PsiPolyVariantReference) {
      return ((PsiPolyVariantReference) reference).multiResolve(true);
    }
    return new ResolveResult[] {new PsiElementResolveResult(reference.resolve())};
  }

  public void doTest(@NotNull final String folder, @NotNull final String step, @Nullable final String stepDefinitionName) {
    init(folder);

    checkReference(step, stepDefinitionName);
  }

  public void doTest(@NotNull final String folder, @NotNull final String fileName, @NotNull final String step,
                     @Nullable final String stepDefinitionName) {
    init(folder, fileName);

    checkReference(step, stepDefinitionName);
  }

  protected void init(String folder) {
    init(folder, "test.feature");
  }

  protected void init(String folder, String fileName) {
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
