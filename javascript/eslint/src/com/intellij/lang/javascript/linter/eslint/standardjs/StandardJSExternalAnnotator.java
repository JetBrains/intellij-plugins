package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSDumbAwareLinterExternalAnnotator;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation;
import com.intellij.lang.javascript.linter.JSLinterInput;
import com.intellij.lang.javascript.linter.JSLinterInspection;
import com.intellij.lang.javascript.linter.eslint.EsLintExternalRunner;
import com.intellij.lang.javascript.linter.eslint.EslintConfigurable;
import com.intellij.lang.javascript.linter.eslint.EslintExternalAnnotator;
import com.intellij.lang.javascript.linter.eslint.EslintState;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StandardJSExternalAnnotator extends JSDumbAwareLinterExternalAnnotator<StandardJSState> {

  @SuppressWarnings("unused")
  public StandardJSExternalAnnotator() {
    this(true);
  }

  public StandardJSExternalAnnotator(boolean onTheFly) {
    super(onTheFly);
  }

  @Override
  protected @NotNull String getSettingsConfigurableID() {
    return EslintConfigurable.ID;
  }

  @Override
  protected Class<? extends JSLinterConfiguration<StandardJSState>> getConfigurationClass() {
    return StandardJSConfiguration.class;
  }

  @Override
  protected Class<? extends JSLinterInspection> getInspectionClass() {
    return StandardJSInspection.class;
  }

  @Override
  protected boolean acceptPsiFile(@NotNull PsiFile file) {
    return EslintUtil.isPossiblyAcceptableFileType(file);
  }


  @Override
  public @Nullable JSLinterAnnotationResult annotate(@NotNull JSLinterInput<StandardJSState> input) {
    StandardJSState initialState = input.getState();
    NodePackage standardPackage = initialState.getNodePackage();
    if (standardPackage.isEmptyPath()) {
      return null;
    }
    SemVer semVer = standardPackage.getVersion(input.getProject());
    if (semVer != null && !semVer.isGreaterOrEqualThan(StandardJSUtil.MIN_VERSION)) {
      return JSLinterAnnotationResult.create(input, new JSLinterFileLevelAnnotation(
        EslintBundle.message("standardjs.error.unsupported.package", StandardJSUtil.MIN_VERSION.getRawVersion())), null);
    }
    JSLinterInput<EslintState> eslintInput = ReadAction.compute(() -> {
      EslintState eslintState = createESLintInput(input.getVirtualFile(), input.getProject(), standardPackage);
      return JSLinterInput.create(input.getPsiFile(), eslintState, input.getColorsScheme());
    });
    return StandardJSLanguageServiceManager.getInstance(input.getProject())
      .useService(input.getVirtualFile(), eslintInput.getState().getNodePackageRef(), service -> {
        if (service == null) {
          return JSLinterAnnotationResult.empty();
        }
        return EsLintExternalRunner.highlight(eslintInput, service, isOnTheFly());
      });
  }

  @Override
  public void apply(@NotNull PsiFile psiFile,
                    @Nullable JSLinterAnnotationResult annotationResult,
                    @NotNull AnnotationHolder holder) {

    if (annotationResult == null) return;
    String caption = EslintBundle.message("standardjs.name");
    IntentionAction fixFileAction = new StandardJSFixAction().asIntentionAction();

    String editSettingsAction = EslintBundle.message("standardjs.edit.settings.caption");
    EslintExternalAnnotator.apply(psiFile, annotationResult, holder, fixFileAction,
                                  caption, null, false, editSettingsAction, getInspectionClass());
  }

  public static @NotNull EslintState createESLintInput(@NotNull StandardJSState standardJSState,
                                                       @NotNull VirtualFile fileToLint,
                                                       @NotNull Project project) {
    return createESLintInput(fileToLint, project, standardJSState.getNodePackage());
  }

  private static @NotNull EslintState createESLintInput(@NotNull VirtualFile fileToLint,
                                                        @NotNull Project project,
                                                        @NotNull NodePackage standardPackage) {
    VirtualFile packageJson = PackageJsonUtil.findUpPackageJson(fileToLint);
    StandardJSUtil.ConfigData packageJsonData = StandardJSUtil.getPackageJsonConfigData(project, packageJson);
    return new EslintState.Builder()
      .setEslintPackage(NodePackageRef.create(standardPackage))
      .setExtraOptions(getIgnoreOptions(packageJsonData))
      .build();
  }

  private static @NotNull String getIgnoreOptions(@Nullable StandardJSUtil.ConfigData configData) {
    List<String> commandLine = new SmartList<>("--no-eslintrc");
    if (configData != null) {
      addListArgument(commandLine, "--ignore-pattern", configData.ignored);
    }
    return StringUtil.join(commandLine, " ");
  }

  @SuppressWarnings("SameParameterValue")
  private static void addListArgument(@NotNull List<? super String> commandLine, @NotNull String name, @NotNull List<String> values) {
    if (!values.isEmpty()) {
      commandLine.add(name);
      commandLine.add(StringUtil.join(values, ","));
    }
  }
}
