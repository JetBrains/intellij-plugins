package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartSdkIsNotConfiguredInspection extends LocalInspectionTool {
  @NotNull
  public String getGroupDisplayName() {
    return DartBundle.message("inspections.group.name");
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return DartBundle.message("dart.sdk.is.not.configured");
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(final @NotNull PsiFile file, final @NotNull InspectionManager manager, final boolean isOnTheFly) {
    if (!isOnTheFly) return ProblemDescriptor.EMPTY_ARRAY;

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

    @NotNull
    public String getName() {
      return myMessage;
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public boolean startInWriteAction() {
      return false;
    }

    public void applyFix(final @NotNull Project project, final @NotNull PsiFile file, final @Nullable Editor editor) {
      ShowSettingsUtil.getInstance().showSettingsDialog(project, DartConfigurable.DART_SETTINGS_PAGE_NAME);
    }
  }

  private static class EnableDartSupportQuickFix extends IntentionAndQuickFixAction {
    private final @NotNull Module myModule;
    private final @NotNull String myDartSdkGlobalLibName;

    public EnableDartSupportQuickFix(final @NotNull Module module, final @NotNull String dartSdkGlobalLibName) {
      myModule = module;
      myDartSdkGlobalLibName = dartSdkGlobalLibName;
    }

    @NotNull
    public String getName() {
      return DartBundle.message("enable.dart.support");
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(final @NotNull Project project, final @NotNull PsiFile file, @Nullable final Editor editor) {
      DartSdkGlobalLibUtil.configureDependencyOnGlobalLib(myModule, myDartSdkGlobalLibName);
    }
  }
}
