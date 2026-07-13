package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class EslintServiceTestBase extends LinterHighlightingTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RegistryValue registryValue = Registry.get("eslint.service.node.path");
    registryValue.setValue(new File(getNodePackage().getSystemDependentPath()).getParent(), getTestRootDisposable());
  }

  /**
   * Runs the ESLint analysis pipeline against a service whose request never completes, so the analysis times out.
   * Deterministically exercises the timeout handling without spawning a real node service process (WEB-67172).
   * Set the desired timeout (e.g. {@code JSLanguageServiceUtil.setTimeout(1, ...)}) before calling this.
   * <p>
   * Delegates to the shared {@link EslintTestUtil#highlightWithNeverRespondingService} so the pinned-lock tiers
   * ({@link EslintPackageLockTestBase}) and these global-install tiers share one fake service.
   */
  protected @NotNull JSLinterAnnotationResult highlightWithNeverRespondingService(@NotNull PsiFile psiFile) {
    return EslintTestUtil.highlightWithNeverRespondingService(getProject(), psiFile, getNodePackage());
  }
}
