package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintConfigFileSearcher;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Irina.Chernushina on 6/3/2015.
 */
public final class TsLintExternalAnnotator extends JSLinterWithInspectionExternalAnnotator<TsLintState, TsLinterInput> {

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
  protected TsLinterInput createInfo(Project project,
                                     @NotNull PsiFile psiFile,
                                     TsLintState state,
                                     Document document,
                                     String fileContent,
                                     EditorColorsScheme colorsScheme) {
    VirtualFile config = myConfigFileSearcher.getConfig(state, psiFile);
    boolean skipProcessing = config != null && saveConfigFileAndReturnSkipProcessing(psiFile.getProject(), config);
    if (skipProcessing) {
      return null;
    }

    return new TsLinterInput(project, psiFile, document, fileContent, state, colorsScheme, config);
  }


  @Nullable
  @Override
  public JSLinterAnnotationResult<TsLintState> annotate(@NotNull TsLinterInput collectedInfo) {
    TsLintLanguageService service = TsLintLanguageService.getService(collectedInfo.getProject());
    VirtualFile config = collectedInfo.getConfig();

    Future<List<TsLinterError>> highlight =
      service.highlight(collectedInfo.getVirtualFile(), config, collectedInfo.getFileContent());
    List<TsLinterError> annotationErrors = JSLanguageServiceUtil.awaitFuture(highlight);
    if (annotationErrors == null) {
      return null;
    }

    return JSLinterAnnotationResult.createLinterResult(collectedInfo, ContainerUtil.newArrayList(annotationErrors), config);
  }

  protected void cleanNotification(@NotNull TsLinterInput collectedInfo) {
    JSLinterEditorNotificationPanel.clearNotification(collectedInfo.getProject(), getInspectionClass(), collectedInfo.getVirtualFile());
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
                    @Nullable JSLinterAnnotationResult<TsLintState> annotationResult,
                    @NotNull AnnotationHolder holder) {
    if (annotationResult == null) return;
    new JSLinterAnnotationsBuilder<>(file, annotationResult, holder, TsLintInspection.getHighlightDisplayKey(),
                                     new TsLintConfigurable(file.getProject(), true), "TSLint: ",
                                     getInspectionClass(), JSLinterStandardFixes.DEFAULT)
      .setHighlightingGranularity(HighlightingGranularity.element).apply();
  }
}
