package org.jetbrains.plugins.cucumber.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.CodeInsightTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

/**
 * User: Andrey.Vokin
 * Date: 7/20/12
 */
public abstract class CucumberResolveTest extends CodeInsightTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void doTest(@NotNull final String folder, @NotNull final String step, @NotNull final String stepDefinitionName) throws Exception {
    CucumberStepsIndex.getInstance(myFixture.getProject()).reset();
    myFixture.copyDirectoryToProject(folder, "");

    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("test.feature"));
    final CucumberStepReference ref = (CucumberStepReference)findReferenceBySignature(step);

    final ResolveResult[] result = ref.multiResolve(true);
    boolean ok = false;
    for (ResolveResult rr : result) {
      final PsiElement resolvedElement = rr.getElement();
      if (resolvedElement != null) {
        final String resolvedStepDefName = getStepDefinitionName(resolvedElement);
        if (resolvedStepDefName != null && resolvedStepDefName.equals(stepDefinitionName)) {
          ok = true;
          break;
        }
      }
    }
    assertTrue(ok);
  }

  @Nullable
  protected abstract String getStepDefinitionName(PsiElement stepDefinition);

  protected abstract String getTestDataPath();
}
