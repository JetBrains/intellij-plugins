// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintDescriptor;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.linter.tslint.fix.TsLintErrorFixAction;
import com.intellij.lang.javascript.linter.tslint.fix.TsLintFileFixAction;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.lang.javascript.linter.tslint.service.TslintLanguageServiceManager;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.lang.javascript.validation.JSAnnotatorProblemGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class TsLintExternalAnnotator extends JSLinterWithInspectionExternalAnnotator<TsLintState, TsLinterInput> {

  private static final TsLintExternalAnnotator INSTANCE_FOR_BATCH_INSPECTION = new TsLintExternalAnnotator(false);

  public static @NotNull TsLintExternalAnnotator getInstanceForBatchInspection() {
    return INSTANCE_FOR_BATCH_INSPECTION;
  }

  @SuppressWarnings("unused")
  public TsLintExternalAnnotator() {
    this(true);
  }

  public TsLintExternalAnnotator(boolean onTheFly) {
    super(onTheFly);
  }

  @Override
  protected @NotNull String getSettingsConfigurableID() {
    return TsLintConfigurable.SETTINGS_JAVA_SCRIPT_LINTERS_TSLINT;
  }

  @Override
  protected Class<? extends JSLinterConfiguration<TsLintState>> getConfigurationClass() {
    return TsLintConfiguration.class;
  }

  @Override
  protected Class<? extends JSLinterInspection> getInspectionClass() {
    return TsLintInspection.class;
  }

  @Override
  protected boolean acceptPsiFile(@NotNull PsiFile file) {
    if (!(file instanceof JSFile)) return false;
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(file.getProject());
    if (configuration.getExtendedState().getState().isAllowJs() && JSUtils.isJavaScriptFile(file)) return true;
    final DialectOptionHolder holder = DialectDetector.dialectOfElement(file);
    return holder != null && holder.isTypeScript;
  }

  @Override
  protected @Nullable TsLinterInput createInfo(@NotNull PsiFile psiFile,
                                               TsLintState state,
                                               EditorColorsScheme colorsScheme) {
    VirtualFile config = TslintUtil.getConfig(state, psiFile.getProject(), psiFile.getVirtualFile());
    boolean skipProcessing = config != null && saveConfigFileAndReturnSkipProcessing(psiFile.getProject(), config);
    if (skipProcessing) {
      return null;
    }

    return new TsLinterInput(psiFile, state, colorsScheme, config);
  }


  @Override
  public @Nullable JSLinterAnnotationResult annotate(@NotNull TsLinterInput collectedInfo) {
    return TslintLanguageServiceManager.getInstance(collectedInfo.getProject())
      .useService(collectedInfo.getVirtualFile(), collectedInfo.getState().getNodePackageRef(),
                  service -> annotateWithService(collectedInfo, service));
  }

  private static @Nullable JSLinterAnnotationResult annotateWithService(@NotNull TsLinterInput collectedInfo, @Nullable TsLintLanguageService service) {
    VirtualFile config = collectedInfo.getConfig();
    final Project project = collectedInfo.getProject();
    final TsLintState linterState = collectedInfo.getState();
    if (service == null) {
      return null;
    }
    final JSLinterFileLevelAnnotation interpreterAndPackageError =
      JSLinterUtil.validateInterpreterAndPackage(project, NodeJsInterpreterManager.getInstance(project).getInterpreter(),
                                                 service.getNodePackage(), TsLintDescriptor.PACKAGE_NAME,
                                                 collectedInfo.getVirtualFile());
    if (interpreterAndPackageError != null) return JSLinterAnnotationResult.create(collectedInfo, interpreterAndPackageError, config);

    final CompletableFuture<List<TsLinterError>> future = service.highlight(collectedInfo.getVirtualFile(),
                                                                            config, collectedInfo.getFileContent(), linterState);
    final List<TsLinterError> result;
    try {
      result = JSLanguageServiceUtil.awaitLanguageService(future, service, collectedInfo.getVirtualFile());
    }
    catch (ExecutionException e) {
      return createGlobalErrorMessage(collectedInfo, config, e.getMessage());
    }
    if (result == null || result.isEmpty()) return null;

    final Optional<TsLinterError> globalError = result.stream().filter(error -> error.isGlobal()).findFirst();
    if (globalError.isPresent() && !StringUtil.isEmptyOrSpaces(globalError.get().getDescription())) {
      return createGlobalErrorMessage(collectedInfo, config, globalError.get().getDescription());
    }

    final List<TsLinterError> filtered = filterResultByFile(collectedInfo.getVirtualFile(), result);
    return JSLinterAnnotationResult.createLinterResult(collectedInfo, filtered, config);
  }

  private static List<TsLinterError> filterResultByFile(@NotNull VirtualFile virtualFile, @NotNull List<TsLinterError> annotationErrors) {
    final String filePath = virtualFile.getPath();
    final String fileName = virtualFile.getName();

    return ContainerUtil.filter(annotationErrors, error -> {
      String path = error.getAbsoluteFilePath();
      return path == null || (/*optimization?*/path.endsWith(fileName) && FileUtil.pathsEqual(filePath, path));
    });
  }

  private static @NotNull JSLinterAnnotationResult createGlobalErrorMessage(@NotNull TsLinterInput collectedInfo,
                                                                            @Nullable VirtualFile config,
                                                                            @NotNull @InspectionMessage String error) {
    final ProcessOutput output = new ProcessOutput();
    output.appendStderr(error);
    final IntentionAction detailsAction = JSLinterUtil.createDetailsAction(collectedInfo.getProject(), collectedInfo.getVirtualFile(),
                                                                           null, output, null);
    final JSLinterFileLevelAnnotation annotation = new JSLinterFileLevelAnnotation(error, detailsAction);
    return JSLinterAnnotationResult.create(collectedInfo, annotation, config);
  }

  @Override
  protected void cleanNotification(@NotNull TsLinterInput collectedInfo) {
    JSLinterEditorNotifications.clearNotification(collectedInfo.getProject(), getInspectionClass(), collectedInfo.getVirtualFile());
  }

  public boolean saveConfigFileAndReturnSkipProcessing(@NotNull Project project,
                                                       @NotNull VirtualFile config) {
    return ReadAction.compute(() -> {
      final FileDocumentManager manager = FileDocumentManager.getInstance();
      Document document = manager.getCachedDocument(config);
      if (document != null) {
        boolean unsaved = manager.isDocumentUnsaved(document);
        if (unsaved) {
          ApplicationManager.getApplication().invokeLater(() -> {
            Document newDocument = manager.getCachedDocument(config);
            if (newDocument != null) {
              FileDocumentManager.getInstance().saveDocument(newDocument);
            }

            DaemonCodeAnalyzer.getInstance(project).restart();
          }, project.getDisposed());
        }

        return unsaved;
      }
      return false;
    });
  }


  @Override
  public void apply(@NotNull PsiFile file,
                    @Nullable JSLinterAnnotationResult annotationResult,
                    @NotNull AnnotationHolder holder) {
    if (annotationResult == null) return;
    TsLintConfigurable configurable = new TsLintConfigurable(file.getProject(), true);
    final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    long documentModificationStamp = document != null ? document.getModificationStamp() : -1;
    IntentionAction fixAllFileIntention = new TsLintFileFixAction().asIntentionAction();

    JSLinterStandardFixes fixes = new JSLinterStandardFixes()
      .setShowEditSettings(false)
      .setEditConfig(false);

    fixes.setErrorToIntentionConverter(errorBase -> {
      if (!(errorBase instanceof TsLinterError tslintError)) {
        return ContainerUtil.emptyList();
      }
      ArrayList<IntentionAction> result = new ArrayList<>();
      if (tslintError.hasFix()) {
        if (document != null && isOnTheFly()) {
          result.add(new TsLintErrorFixAction(file, tslintError, documentModificationStamp));
        }
        result.add(fixAllFileIntention);
      }
      else if (!holder.isBatchMode()){
        ContainerUtil.addIfNotNull(result, TsLintSuppressionUtil.INSTANCE.getSuppressForLineAction(tslintError, documentModificationStamp));
      }
      return result;
    }).setProblemGroup(error -> {
      if (isOnTheFly() && error instanceof TsLinterError) {
        return new JSAnnotatorProblemGroup(TsLintSuppressionUtil.INSTANCE.getSuppressionsForError((TsLinterError)error, documentModificationStamp), null);
      }
      return null;
    });


    new JSLinterAnnotationsBuilder(file, annotationResult, holder,
                                   configurable, TsLintBundle.message("tslint.framework.title") + ": ",
                                   getInspectionClass(), fixes)
      .setHighlightingGranularity(HighlightingGranularity.element).apply();
  }
}
