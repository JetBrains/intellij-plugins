package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeStyle.AbstractConvertLineSeparatorsAction;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonCommonUtil;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterFixAction;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.LineSeparator;
import com.intellij.util.concurrency.annotations.RequiresWriteLock;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class EsLintFixAction extends JSLinterFixAction {
  public EsLintFixAction() {
    super(EslintBundle.messagePointer("settings.javascript.linters.eslint.configurable.name"),
          EslintBundle.messagePointer("eslint.action.fix.problems.description"), JavaScriptLanguageIcons.FileTypes.Eslint);
  }

  protected EsLintFixAction(@NotNull Supplier<String> message, @NotNull Supplier<String> description) {
    super(message, description, null);
  }

  @Override
  protected @NotNull JSLinterConfiguration getConfiguration(@NotNull Project project) {
    return EslintConfiguration.getInstance(project);
  }

  @Override
  protected boolean shouldSaveBeforeToolInvocation(@Nullable VirtualFile file) {
    return file != null && (EslintUtil.isFlatOrLegacyConfigFile(file) || PackageJsonCommonUtil.isPackageJsonFile(file));
  }

  @Override
  public boolean isFileAccepted(@NotNull Project project, @NotNull VirtualFile file) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    return psiFile != null && EslintUtil.isPossiblyAcceptableFileType(psiFile);
  }

  @Override
  protected boolean needRefreshFilesAfter() {
    return false;
  }

  public @Nullable String fixFile(@NotNull PsiFile psiFile) throws LinterExecutionException {
    JSLinterInput<EslintState> input = ReadAction.compute(() -> {
      EslintState state = EslintConfiguration.getInstance(psiFile.getProject()).getExtendedState().getState();
      return JSLinterInput.create(psiFile, state, null);
    });
    EslintLanguageServiceManager languageServiceManager = EslintLanguageServiceManager.getInstance(input.getProject());
    return languageServiceManager.useService(input.getVirtualFile(), input.getState().getNodePackageRef(), service -> {
      if (service == null) {
        return null;
      }
      return service.useService(() -> EsLintExternalRunner.fixFile(input, service));
    });
  }

  @Override
  protected Task createTask(@NotNull Project project,
                            @NotNull Collection<? extends VirtualFile> filesToProcess,
                            @NotNull Runnable completeCallback,
                            boolean modalProgress) {
    return new EsLintFixTask(project, filesToProcess, completeCallback).createTask(modalProgress);
  }

  @Override
  public boolean isEnabled(@NotNull Project project, VirtualFile @NotNull [] files) {
    if (!super.isEnabled(project, files) || files.length == 0) return false;
    EslintState state = EslintConfiguration.getInstance(project).getExtendedState().getState();
    return EslintLanguageServiceManager.getInstance(project).useService(files[0], state.getNodePackageRef(), service -> {
      return service != null;
    });
  }

  private class EsLintFixTask extends JSLinterReformatterTask {
    private final List<EsLintResult> myResults;

    private EsLintFixTask(@NotNull Project project,
                          @NotNull Collection<? extends VirtualFile> filesToProcess,
                          @NotNull Runnable completeCallback) {
      super(project, EslintBundle.message("settings.javascript.linters.eslint.configurable.name"), filesToProcess, completeCallback);
      myResults = new ArrayList<>(filesToProcess.size());
    }

    @Override
    protected void runLinter(@NotNull PsiFile psiFile, @NotNull Document document) {
      long docModStamp = document.getModificationStamp();
      try {
        String result = fixFile(psiFile);
        if (result != null) {
          myResults.add(new EsLintResult(psiFile, docModStamp, result));
        }
      }
      catch (LinterExecutionException e) {
        error(psiFile.getVirtualFile(), e.getAnnotation().getMessage());
      }
    }

    @Override
    protected void doOnSuccess() {
      for (EsLintResult result : myResults) {
        if (!result.myPsiFile.isValid()) {
          Logger.getInstance(EsLintFixAction.this.getClass())
            .info("Can't apply 'eslint --fix' result because the file has become invalid: " + result.myPsiFile.getName());
          continue;
        }

        Document document = PsiDocumentManager.getInstance(myProject).getDocument(result.myPsiFile);
        if (document == null || document.getModificationStamp() != result.myDocModStamp) {
          Logger.getInstance(EsLintFixAction.this.getClass())
            .info("Can't apply 'eslint --fix' result because the document was changed while running ESLint: " + result.myPsiFile.getName());
          continue;
        }

        String linterName = EslintBundle.message("settings.javascript.linters.eslint.configurable.name");
        String commandName = JavaScriptBundle.message("javascript.linter.action.fix.problems.name", linterName);
        WriteCommandAction.runWriteCommandAction(myProject, commandName, null, () -> {
          applyResultText(myProject, document, result.myResult);
        });
      }
      super.doOnSuccess();
    }
  }

  @RequiresWriteLock
  public static void applyResultText(@NotNull Project project, @NotNull Document document, @NotNull String resultText) {
    String documentContent = StringUtil.convertLineSeparators(resultText);
    document.setText(documentContent);

    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
    if (file == null) return;

    LineSeparator newLineSeparator = StringUtil.detectSeparators(resultText);
    String newLineSeparatorString = newLineSeparator != null ? newLineSeparator.getSeparatorString() : null;
    if (newLineSeparatorString != null && !StringUtil.equals(file.getDetectedLineSeparator(), newLineSeparatorString)) {
      AbstractConvertLineSeparatorsAction.changeLineSeparators(project, file, newLineSeparatorString);
    }
  }

  private static class EsLintResult {
    private final @NotNull PsiFile myPsiFile;
    private final long myDocModStamp;
    private final @NotNull String myResult;

    private EsLintResult(@NotNull PsiFile psiFile, long docModStamp, @NotNull String result) {
      myPsiFile = psiFile;
      myDocModStamp = docModStamp;
      myResult = result;
    }
  }
}
