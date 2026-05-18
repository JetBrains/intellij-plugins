package com.intellij.lang.javascript.linter.jshint;

import com.intellij.javascript.common.icons.JavascriptCommonIcons;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.linter.JSDumbAwareLinterExternalAnnotator;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.JSLinterAnnotationsBuilder;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterEditSettingsAction;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.JSLinterStandardFixes;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JSHintExternalAnnotator extends JSDumbAwareLinterExternalAnnotator<JSHintState> {

  private static final JSHintExternalAnnotator INSTANCE_FOR_BATCH_INSPECTION = new JSHintExternalAnnotator(false);

  public static @NotNull JSHintExternalAnnotator getInstanceForBatchInspection() {
    return INSTANCE_FOR_BATCH_INSPECTION;
  }

  @SuppressWarnings("unused")
  public JSHintExternalAnnotator() {
    this(true);
  }

  public JSHintExternalAnnotator(boolean onTheFly) {
    super(onTheFly);
  }

  @Override
  protected @NotNull String getSettingsConfigurableID() {
    return JSHintConfigurable.ID;
  }

  @Override
  protected Class<? extends JSLinterConfiguration<JSHintState>> getConfigurationClass() {
    return JSHintConfiguration.class;
  }

  @Override
  protected Class<? extends JSLinterInspection> getInspectionClass() {
    return JSHintInspection.class;
  }

  @Override
  protected boolean acceptPsiFile(@NotNull PsiFile file) {
    return file instanceof JSFile && JSUtils.isJavaScriptFile(file);
  }

  @Override
  public JSLinterAnnotationResult annotate(@NotNull JSLinterInput<JSHintState> collectedInfo) {
    final Project project = collectedInfo.getProject();
    if (project.isDisposed()) {
      return null;
    }
    return new JSHintExternalRunner().execute(collectedInfo);
  }

  @Override
  public void apply(@NotNull PsiFile psiFile, JSLinterAnnotationResult annotationResult, @NotNull AnnotationHolder holder) {
    if (annotationResult == null) return;

    JSHintConfigurable configurable = new JSHintConfigurable(psiFile.getProject(), true);
    JSLinterStandardFixes fixes = new JSLinterStandardFixes()
      .setEditSettingsAction(new JSLinterEditSettingsAction(configurable, JavascriptCommonIcons.FileTypes.JsHint))
      .setShowEditSettings(false)
      .setEditConfig(false);
    new JSLinterAnnotationsBuilder(psiFile,
                                   annotationResult,
                                   holder,
                                   configurable,
                                   JSHintBundle.message("jshint.inspection.message.prefix") + " ",
                                   getInspectionClass(),
                                   fixes)
      .setTabSize(getIndent(annotationResult))
      .setDefaultFileLevelErrorIcon(JavascriptCommonIcons.FileTypes.JsHint)
      .setHighlightingGranularity(HighlightingGranularity.element).apply();
  }

  private static int getIndent(@NotNull JSLinterAnnotationResult annotationResult) {
    int indent = 4;
    if (annotationResult instanceof JSHintExternalRunner.JSHintAnnotationResult) {
      Object obj = ((JSHintExternalRunner.JSHintAnnotationResult)annotationResult).getOptionsState().getValue(JSHintOption.INDENT);
      if (obj instanceof Number) {
        indent = ((Number)obj).intValue();
      }
    }
    return Math.max(1, indent);
  }
}
