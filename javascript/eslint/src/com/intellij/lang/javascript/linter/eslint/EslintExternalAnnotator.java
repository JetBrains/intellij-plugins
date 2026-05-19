package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.JSDumbAwareLinterExternalAnnotator;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.JSLinterEditSettingsAction;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.JSLinterStandardFixes;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager;
import com.intellij.lang.javascript.validation.JSAnnotatorProblemGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.ArrayList;

public class EslintExternalAnnotator extends JSDumbAwareLinterExternalAnnotator<EslintState> {
  private static final EslintExternalAnnotator INSTANCE_FOR_BATCH_INSPECTION = new EslintExternalAnnotator(false);

  public static @NotNull EslintExternalAnnotator getInstanceForBatchInspection() {
    return INSTANCE_FOR_BATCH_INSPECTION;
  }

  @SuppressWarnings("unused")
  public EslintExternalAnnotator() {
    this(true);
  }

  public EslintExternalAnnotator(boolean onTheFly) {
    super(onTheFly);
  }

  @Override
  protected @NotNull String getSettingsConfigurableID() {
    return EslintConfigurable.ID;
  }

  @Override
  protected Class<EslintConfiguration> getConfigurationClass() {
    return EslintConfiguration.class;
  }

  @Override
  protected Class<EslintInspection> getInspectionClass() {
    return EslintInspection.class;
  }

  @Override
  protected boolean acceptPsiFile(@NotNull PsiFile file) {
    return EslintUtil.isPossiblyAcceptableFileType(file);
  }

  @Override
  public @Nullable JSLinterAnnotationResult annotate(final @NotNull JSLinterInput<EslintState> input) {
    EslintState state = input.getState();
    EslintLanguageServiceManager languageServiceManager = EslintLanguageServiceManager.getInstance(input.getProject());
    return languageServiceManager.useService(input.getVirtualFile(), state.getNodePackageRef(), service -> {
      if (service == null) {
        //could not resolve package for file that is not under a package.json, for example
        return JSLinterAnnotationResult.empty();
      }
      return service.useService(() -> EsLintExternalRunner.highlight(input, service, isOnTheFly()));
    });
  }

  @Override
  public void apply(@NotNull PsiFile psiFile,
                    @Nullable JSLinterAnnotationResult annotationResult,
                    @NotNull AnnotationHolder holder) {
    if (annotationResult == null) return;
    IntentionAction fixFileAction = new EsLintFixAction().asIntentionAction();
    String toolName = EslintBundle.message("settings.javascript.linters.eslint.configurable.name");
    Icon icon = JavaScriptLanguageIcons.FileTypes.Eslint;
    apply(psiFile, annotationResult, holder, fixFileAction, toolName, icon, false, null, getInspectionClass());
  }

  public static void apply(@NotNull PsiFile file,
                           @NotNull JSLinterAnnotationResult annotationResult,
                           @NotNull AnnotationHolder holder,
                           @NotNull IntentionAction fixFileAction,
                           @NotNull @Nls String toolName, @Nullable Icon icon,
                           boolean editConfig, @Nullable @Nls String editSettingCaption,
                           Class<? extends JSLinterInspection> inspectionClass) {
    final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    long documentModificationStamp = document != null ? document.getModificationStamp() : -1;
    EslintConfigurable configurable = new EslintConfigurable(file.getProject(), true);
    JSLinterEditSettingsAction editSettingsAction = new JSLinterEditSettingsAction(
      configurable, ObjectUtils.coalesce(editSettingCaption, configurable.getDisplayName()), icon);

    JSLinterStandardFixes fixes = new JSLinterStandardFixes()
      .setEditConfig(editConfig)
      .setEditSettingsAction(editSettingsAction)
      .setShowEditSettings(false)
      .setErrorToIntentionConverter(error -> {
        if (!(error instanceof EslintError eslintError)) {
          return ContainerUtil.emptyList();
        }
        ArrayList<IntentionAction> result = new ArrayList<>();
        EslintError.FixInfo fixInfo = eslintError.getFixInfo();
        if (fixInfo != null) {
          if (document != null && !holder.isBatchMode()) {
            result.add(new EslintFixSingleErrorAction(toolName, file, fixInfo, eslintError.getCode(), documentModificationStamp));
          }
          result.add(fixFileAction);
        }
        else if (!eslintError.getSuggestions().isEmpty()) {
          for (EslintError.FixInfo suggestion : eslintError.getSuggestions()) {
            result.add(new EslintFixSingleErrorAction(toolName, file, suggestion, eslintError.getCode(), documentModificationStamp));
          }
        }
        else if (!holder.isBatchMode()) {
          ContainerUtil
            .addIfNotNull(result, ESLintSuppressionUtil.INSTANCE.getSuppressForLineAction(eslintError, documentModificationStamp));
        }
        return result;
      })
      .setProblemGroup(error -> {
        if (holder.isBatchMode()) {
          return null;
        }
        if (error instanceof EslintError) {
          SuppressIntentionAction[] intentionActions = ESLintSuppressionUtil.INSTANCE
            .getSuppressionsForError((EslintError)error, documentModificationStamp);
          return new JSAnnotatorProblemGroup(intentionActions, null);
        }
        return null;
      });
    new ESLintAnnotationsBuilder(file, annotationResult, holder,
                                 configurable,
                                   toolName + ": ", inspectionClass,
                                 fixes)
      .setHighlightingGranularity(HighlightingGranularity.element)
      .setDefaultFileLevelErrorIcon(icon)
      .apply();
  }

  @Override
  protected void cleanNotification(@NotNull JSLinterInput<EslintState> collectedInfo) {
    EslintLanguageServiceManager.getInstance(collectedInfo.getProject()).applyFileLevelAnnotation(collectedInfo.getPsiFile(), null);
  }
}
