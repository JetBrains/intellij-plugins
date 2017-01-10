package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.integration.JSAnnotationError;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintConfigFileSearcher;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public class TsLintExternalAnnotator extends JSLinterExternalAnnotator<TsLintState> {

  private static final TsLintExternalAnnotator INSTANCE_FOR_BATCH_INSPECTION = new TsLintExternalAnnotator(false);

  @NotNull
  private final TsLintConfigFileSearcher myConfigFileSearcher;

  @NotNull
  public static TsLintExternalAnnotator getInstanceForBatchInspection() {
    return INSTANCE_FOR_BATCH_INSPECTION;
  }

  @SuppressWarnings("unused")
  public TsLintExternalAnnotator() {
    this(true);
  }

  public TsLintExternalAnnotator(boolean onTheFly) {
    super(onTheFly);
    myConfigFileSearcher = new TsLintConfigFileSearcher();
  }

  @NotNull
  @Override
  protected JSLinterConfigurable<TsLintState> createSettingsConfigurable(@NotNull Project project) {
    return new TsLintConfigurable(project, true);
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
    final DialectOptionHolder holder = DialectDetector.dialectOfFile(file);
    return holder != null && holder.isTypeScript;
  }

  @Nullable
  @Override
  protected JSLinterInput<TsLintState> collectInformation(@NotNull PsiFile psiFile, @Nullable Editor editor) {
    JSLinterInput<TsLintState> result = super.collectInformation(psiFile, editor);
    if (result == null) {
      return null;
    }
    VirtualFile config = myConfigFileSearcher.getConfig(result.getState(), psiFile);
    boolean skipProcessing = config != null && saveConfigFileAndReturnSkipProcessing(psiFile.getProject(), psiFile, config);
    if (skipProcessing) {
      return null;
    }

    return result;
  }

  @Nullable
  @Override
  public JSLinterAnnotationResult<TsLintState> annotate(@NotNull JSLinterInput<TsLintState> collectedInfo) {
    TsLintLanguageService service = TsLintLanguageService.getService(collectedInfo.getProject());
    PsiFile file = collectedInfo.getPsiFile();
    VirtualFile config = myConfigFileSearcher.getConfig(collectedInfo.getState(), file);

    Future<List<JSAnnotationError>> highlight =
      service.highlightImpl(collectedInfo.getVirtualFile(), config, collectedInfo.getFileContent());
    List<JSAnnotationError> annotationErrors = JSLanguageServiceUtil.awaitFuture(highlight);
    if (annotationErrors == null) {
      return null;
    }

    List<JSLinterError> errors = annotationErrors
      .stream()
      .map(el -> ((JSLinterError)el)).collect(Collectors.toList());

    return JSLinterAnnotationResult.createLinterResult(collectedInfo, errors, config);
  }

  public boolean saveConfigFileAndReturnSkipProcessing(@NotNull Project project,
                                                       @NotNull PsiFile file,
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
                    @Nullable JSLinterAnnotationResult<TsLintState> annotationResult,
                    @NotNull AnnotationHolder holder) {
    if (annotationResult == null) return;
    new JSLinterAnnotationsBuilder<>(file, annotationResult, holder, TsLintInspection.getHighlightDisplayKey(),
                                     new TsLintConfigurable(file.getProject(), true), "TSLint: ",
                                     getInspectionClass(), JSLinterStandardFixes.DEFAULT)
      .setHighlightingGranularity(HighlightingGranularity.element).apply();
  }
}
