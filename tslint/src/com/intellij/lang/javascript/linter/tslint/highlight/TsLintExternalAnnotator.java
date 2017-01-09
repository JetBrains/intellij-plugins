package com.intellij.lang.javascript.linter.tslint.highlight;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.integration.JSAnnotationError;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.config.TsLintBinFileVersionManager;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintExternalRunner;
import com.intellij.lang.javascript.linter.tslint.service.TsLintLanguageService;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
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
  private static final Logger LOG = Logger.getInstance(TsLintConfiguration.LOG_CATEGORY);

  private static final TsLintExternalAnnotator INSTANCE_FOR_BATCH_INSPECTION = new TsLintExternalAnnotator(false);
  private static final String TSLINT_CODE_TEMP_FILE_MAP_KEY_NAME = "TSLINT_CODE_TEMP_FILE_MAP_KEY";
  private static final String TSLINT_CONFIG_TEMP_FILE_MAP_KEY_NAME = "TSLINT_CONFIG_TEMP_FILE_MAP_KEY";

  private final FilesMirror myCodeFilesMirror;
  private final FilesMirror myConfigFilesMirror;
  private final TsLintBinFileVersionManager myBinFileVersionManager;

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
    myCodeFilesMirror = new FilesMirror(TSLINT_CODE_TEMP_FILE_MAP_KEY_NAME, "tslint");
    myConfigFilesMirror = new FilesMirror(TSLINT_CONFIG_TEMP_FILE_MAP_KEY_NAME, "tslint");
    myBinFileVersionManager = new TsLintBinFileVersionManager();
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
  public JSLinterAnnotationResult<TsLintState> doAnnotate(@Nullable JSLinterInput<TsLintState> collectedInfo) {
    if (collectedInfo == null) return null;

    if (!StringUtil.isEmpty(System.getProperty("use.tslint.new"))) {
      TsLintLanguageService service = TsLintLanguageService.getService(collectedInfo.getProject());

      Future<List<JSAnnotationError>> highlight =
        service.highlightImpl(collectedInfo.getPsiFile(), collectedInfo.getVirtualFile(), collectedInfo.getFileContent());
      List<JSAnnotationError> annotationErrors = JSLanguageServiceUtil.awaitFuture(highlight);
      if (annotationErrors == null) {
        return null;
      }

      List<JSLinterError> errors = annotationErrors
        .stream()
        .map(el -> ((JSLinterError)el)).collect(Collectors.toList());

      return JSLinterAnnotationResult.createLinterResult(collectedInfo, errors, null);
    }

    return new TsLintExternalRunner(collectedInfo,
                                    myCodeFilesMirror,
                                    myConfigFilesMirror,
                                    myBinFileVersionManager,
                                    collectedInfo.getProject()).execute();
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
