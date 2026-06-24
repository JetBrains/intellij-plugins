package com.intellij.lang.javascript.linter.eslint;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceClient;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class EslintServiceTestBase extends LinterHighlightingTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RegistryValue registryValue = Registry.get("eslint.service.node.path");
    registryValue.setValue(new File(getNodePackage().getSystemDependentPath()).getParent(), getTestRootDisposable());
  }

  /**
   * Runs the ESLint analysis pipeline against a service whose request never completes, so the analysis times out.
   * <p>
   * This deterministically exercises the timeout handling (await timeout -> file-level timeout annotation / FUS event)
   * without starting a real node service process. The previous timeout tests started a real service that kept booting
   * in the background after the analysis had already given up, and was then disposed mid-startup at tear-down, leaking
   * its startup wait / process reader thread (WEB-67172). Faking the service removes that nondeterministic process
   * entirely while still going through the real {@link EsLintExternalRunner} and annotation-building code.
   * <p>
   * Set the desired timeout (e.g. {@code JSLanguageServiceUtil.setTimeout(1, ...)}) before calling this.
   */
  protected @NotNull JSLinterAnnotationResult highlightWithNeverRespondingService(@NotNull PsiFile psiFile) {
    EslintLanguageServiceClient service = createNeverRespondingService(psiFile.getVirtualFile().getParent());
    EslintState state = EslintConfiguration.getInstance(getProject()).getExtendedState().getState();
    JSLinterInput<EslintState> input = JSLinterInput.create(psiFile, state, null);
    return EsLintExternalRunner.highlight(input, service, true);
  }

  private @NotNull EslintLanguageServiceClient createNeverRespondingService(@NotNull VirtualFile workingDirectory) {
    NodePackage nodePackage = getNodePackage();
    return new EslintLanguageServiceClient() {
      @Override
      public @NotNull NodePackage getNodePackage() {
        return nodePackage;
      }

      @Override
      public @NotNull VirtualFile getWorkingDirectory() {
        return workingDirectory;
      }

      @Override
      public CompletableFuture<Response<List<EslintError>>> highlight(@NotNull EslintRequestData requestData, String extraOptions) {
        return new CompletableFuture<>(); // never completes -> the analysis times out
      }

      @Override
      public CompletableFuture<Response<String>> fixFile(@NotNull EslintRequestData requestData, String extraOptions) {
        return new CompletableFuture<>();
      }

      @Override
      public boolean isServiceCreated() {
        return true;
      }

      @Override
      public @Nullable String getServiceCreationError() {
        return null;
      }
    };
  }
}
