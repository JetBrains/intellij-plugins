package org.jetbrains.plugins.cucumber.resolve;

import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberCodeInsightTestCase;

public abstract class CucumberResolveTest extends CucumberCodeInsightTestCase {
  protected void checkReference(@NotNull ResolveResult @NotNull [] result, @Nullable String stepDefinitionName) {
    boolean ok = stepDefinitionName == null;
    for (ResolveResult rr : result) {
      final PsiElement resolvedElement = rr.getElement();
      if (resolvedElement != null) {
        if (stepDefinitionName == null) {
          ok = false;
        }
        else {
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

  protected void checkReference(@NotNull String element, @Nullable String stepDefinitionName) {
    ResolveResult[] result = getResolveResult(element);
    checkReference(result, stepDefinitionName);
  }

  protected ResolveResult[] getResolveResult(@NotNull String step) {
    PsiReference reference = findReferenceBySignature(step);
    assertNotNull("reference must not be null", reference);
    if (reference instanceof PsiPolyVariantReference polyVariantReference) {
      return polyVariantReference.multiResolve(true);
    }
    return new ResolveResult[]{new PsiElementResolveResult(reference.resolve())};
  }

  protected ResolveResult[] getResolveResult(@NotNull PsiReference reference) {
    if (reference instanceof PsiPolyVariantReference polyVariantReference) {
      return polyVariantReference.multiResolve(true);
    }
    return new ResolveResult[]{new PsiElementResolveResult(reference.resolve())};
  }

  public void doTest(@NotNull String folder, @NotNull String step, @Nullable String stepDefinitionName) {
    init(folder);

    checkReference(step, stepDefinitionName);
  }

  public void doTest(@NotNull String folder, @NotNull String fileName, @NotNull String step, @Nullable String stepDefinitionName) {
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
