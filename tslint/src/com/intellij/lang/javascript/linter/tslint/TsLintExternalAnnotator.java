package com.intellij.lang.javascript.linter.tslint;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.linter.*;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.ui.TsLintConfigurable;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
