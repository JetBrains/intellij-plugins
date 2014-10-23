package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.*;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartSdkIsNotConfiguredInspection extends LocalInspectionTool {
  @Override
  @NotNull
  public String getGroupDisplayName() {
    return DartBundle.message("inspections.group.name");
  }

  @Override
  @Nls
  @NotNull
  public String getDisplayName() {
    return DartBundle.message("dart.sdk.is.not.configured");
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(final @NotNull PsiFile file, final @NotNull InspectionManager manager, final boolean isOnTheFly) {
    if (!isOnTheFly) return ProblemDescriptor.EMPTY_ARRAY;

    if (file.getLanguage() != DartLanguage.INSTANCE && !PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName())) {
      return ProblemDescriptor.EMPTY_ARRAY;
    }

    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module == null) return ProblemDescriptor.EMPTY_ARRAY;

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      return createProblemDescriptors(file, manager, DartBundle.message("dart.sdk.is.not.configured"),
                                      new OpenDartSettingsQuickFix(DartBundle.message("setup.dart.sdk")));
    }

    if (!DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) {
      final String message = DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
                             ? DartBundle.message("dart.support.is.not.enabled.for.module.0", module.getName())
                             : DartBundle.message("dart.support.is.not.enabled.for.project");

      return createProblemDescriptors(file, manager, message,
                                      new EnableDartSupportQuickFix(module, sdk.getGlobalLibName()),
                                      new OpenDartSettingsQuickFix(DartBundle.message("open.dart.settings")));
    }

    return ProblemDescriptor.EMPTY_ARRAY;
  }

  private ProblemDescriptor[] createProblemDescriptors(final @NotNull PsiFile file,
                                                       final @NotNull InspectionManager manager,
                                                       final @NotNull String message,
                                                       final @NotNull LocalQuickFix... quickFixes) {
    return new ProblemDescriptor[]{
      manager.createProblemDescriptor(file, message, true, quickFixes,
                                      HighlightInfo.convertSeverityToProblemHighlight(getDefaultLevel().getSeverity()))
    };
  }

  private static class OpenDartSettingsQuickFix extends IntentionAndQuickFixAction {
    private final @NotNull String myMessage;

    private OpenDartSettingsQuickFix(final @NotNull String message) {
      myMessage = message;
    }

    @Override
    @NotNull
    public String getName() {
      return myMessage;
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return getName();
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @Override
    public void applyFix(final @NotNull Project project, final @NotNull PsiFile file, final @Nullable Editor editor) {
      ShowSettingsUtilImpl.showSettingsDialog(project, DartConfigurable.DART_SETTINGS_PAGE_ID, "");
    }
  }

  private static class EnableDartSupportQuickFix extends IntentionAndQuickFixAction {
    private final @NotNull Module myModule;
    private final @NotNull String myDartSdkGlobalLibName;
    private boolean myAvailable = true;

    public EnableDartSupportQuickFix(final @NotNull Module module, final @NotNull String dartSdkGlobalLibName) {
      myModule = module;
      myDartSdkGlobalLibName = dartSdkGlobalLibName;
    }

    @Override
    @NotNull
    public String getName() {
      return DartBundle.message("enable.dart.support");
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return getName();
    }

    @Override
    public boolean isAvailable(@NotNull final Project project, @Nullable final Editor editor, final PsiFile file) {
      return myAvailable;
    }

    @Override
    public void applyFix(final @NotNull Project project, final @NotNull PsiFile file, @Nullable final Editor editor) {
      DartSdkGlobalLibUtil.configureDependencyOnGlobalLib(myModule, myDartSdkGlobalLibName);
      myAvailable = false;
    }
  }
}
