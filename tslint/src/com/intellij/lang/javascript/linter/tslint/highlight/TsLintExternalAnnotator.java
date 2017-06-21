package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.TsLintBundle;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintConfigFileSearcher;
import com.intellij.lang.javascript.linter.tslint.execution.TsLinterError;
import com.intellij.lang.javascript.linter.tslint.fix.TsLintErrorFixAction;
import com.intellij.lang.javascript.linter.tslint.fix.TsLintFileFixAction;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
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
import com.intellij.util.ResultWithError;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(file.getProject());
    if (configuration.isAllowJs() && JSUtils.isJavaScriptFile(file)) return true;
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
    VirtualFile config = myConfigFileSearcher.getConfig(state, psiFile.getVirtualFile());
    boolean skipProcessing = config != null && saveConfigFileAndReturnSkipProcessing(psiFile.getProject(), config);
    if (skipProcessing) {
      return null;
    }

    return new TsLinterInput(project, psiFile, fileContent, state, colorsScheme, config);
  }


  @Nullable
  @Override
  public JSLinterAnnotationResult<TsLintState> annotate(@NotNull TsLinterInput collectedInfo) {
    TsLintLanguageService service = TsLintLanguageService.getService(collectedInfo.getProject());
    VirtualFile config = collectedInfo.getConfig();

    final Future<List<TsLinterError>> future = service.highlight(collectedInfo.getVirtualFile(), config, collectedInfo.getFileContent());
    final ResultWithError<List<TsLinterError>, String> result = JSLanguageServiceUtil.awaitLanguageService(future, service);
    if (result.getError() != null) {
      return JSLinterAnnotationResult.create(collectedInfo, new JSLinterFileLevelAnnotation(result.getError()), config);
    }

    final List<TsLinterError> annotationErrors = result.getResult();
    if (annotationErrors == null || annotationErrors.isEmpty()) return null;

    final Optional<TsLinterError> globalError = annotationErrors.stream().filter(error -> error.isGlobal()).findFirst();
    if (globalError.isPresent() && !StringUtil.isEmptyOrSpaces(globalError.get().getDescription())) {
      return createGlobalErrorMessage(collectedInfo, config, globalError.get().getDescription());
    }

    final List<JSLinterError> filtered = filterResultByFile(collectedInfo, annotationErrors);
    return JSLinterAnnotationResult.createLinterResult(collectedInfo, filtered, config);
  }

  public List<JSLinterError> filterResultByFile(@NotNull TsLinterInput collectedInfo, @NotNull List<TsLinterError> annotationErrors) {
    final String filePath = collectedInfo.getVirtualFile().getPath();
    final String fileName = collectedInfo.getPsiFile().getName();

    final Set<String> filteredPaths = annotationErrors.stream().map(TsLinterError::getAbsoluteFilePath)
      .distinct()
      .filter(path -> {
        if (path == null) return true;
        if (!path.endsWith(fileName)) return false;
        return FileUtil.pathsEqual(filePath, path);
      }).collect(Collectors.toSet());

    return annotationErrors.stream().filter(el -> filteredPaths.contains(el.getAbsoluteFilePath())).collect(Collectors.toList());
  }

  @NotNull
  private static JSLinterAnnotationResult<TsLintState> createGlobalErrorMessage(@NotNull TsLinterInput collectedInfo,
                                                                                @Nullable VirtualFile config,
                                                                                @NotNull String error) {
    final ProcessOutput output = new ProcessOutput();
    output.appendStderr(error);
    final IntentionAction detailsAction = JSLinterUtil.createDetailsAction(collectedInfo.getProject(), collectedInfo.getVirtualFile(),
                                                                           null, output, null);
    final JSLinterFileLevelAnnotation annotation = new JSLinterFileLevelAnnotation(error, detailsAction);
    return JSLinterAnnotationResult.create(collectedInfo, annotation, config);
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
    TsLintConfigurable configurable = new TsLintConfigurable(file.getProject(), true);


    final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    IntentionAction fixAllFileIntention = new TsLintFileFixAction().asIntentionAction();
    JSLinterStandardFixes fixes = new JSLinterStandardFixes() {
      @Override
      public List<IntentionAction> createListForError(@Nullable VirtualFile configFile,
                                                      @NotNull UntypedJSLinterConfigurable configurable,
                                                      @NotNull JSLinterErrorBase errorBase) {
        List<IntentionAction> defaultIntentions = super.createListForError(configFile, configurable, errorBase);
        if (errorBase instanceof TsLinterError && ((TsLinterError)errorBase).hasFix()) {
          ArrayList<IntentionAction> result = ContainerUtil.newArrayList();
          if (document != null && myOnTheFly) {
            result.add(new TsLintErrorFixAction((TsLinterError)errorBase, document));
          }
          result.add(fixAllFileIntention);
          result.addAll(defaultIntentions);
          return result;
        }

        return defaultIntentions;
      }
    };


    new JSLinterAnnotationsBuilder<>(file, annotationResult, holder, TsLintInspection.getHighlightDisplayKey(),
                                     configurable, TsLintBundle.message("tslint.framework.title") + ": ",
                                     getInspectionClass(), fixes)
      .setHighlightingGranularity(HighlightingGranularity.element).apply(document);
  }
}
