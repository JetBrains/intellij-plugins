package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.eslint.EsLintExternalRunner;
import com.intellij.lang.javascript.linter.eslint.EsLintFixAction;
import com.intellij.lang.javascript.linter.eslint.EslintState;
import com.intellij.lang.javascript.linter.eslint.LinterExecutionException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StandardJSFixAction extends EsLintFixAction {

  public StandardJSFixAction() {
    super(EslintBundle.messagePointer("standardjs.name"),
          EslintBundle.messagePointer("standardjs.action.fix.problems.description"));
  }

  @Override
  protected @NotNull StandardJSConfiguration getConfiguration(@NotNull Project project) {
    return StandardJSConfiguration.getInstance(project);
  }

  @Override
  public @Nullable String fixFile(@NotNull PsiFile psiFile) throws LinterExecutionException {
    Project project = psiFile.getProject();
    StandardJSState state = getConfiguration(project).getExtendedState().getState();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    EslintState eslintState = StandardJSExternalAnnotator.createESLintInput(state, virtualFile, project);

    JSLinterInput<EslintState> eslintInput = JSLinterInput.create(psiFile, eslintState, null);
    StandardJSLanguageServiceManager instance = StandardJSLanguageServiceManager.getInstance(project);
    return instance.useService(virtualFile, eslintState.getNodePackageRef(), service -> {
      return service == null ? null : EsLintExternalRunner.fixFile(eslintInput, service);
    });
  }
}
