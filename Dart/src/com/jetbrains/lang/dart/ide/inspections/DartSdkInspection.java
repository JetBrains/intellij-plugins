package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartSdkInspection extends LocalInspectionTool {
  @NotNull
  public String getGroupDisplayName() {
    return DartBundle.message("inspections.group.name");
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return DartBundle.message("inspections.dart.sdk");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public String getShortName() {
    return "AbsentDartSdk";
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (!DartSettings.shouldTakeWebSettings(module)) return ProblemDescriptor.EMPTY_ARRAY;

    final DartSettings dartSettings = DartSettings.getSettingsForModule(module);
    if (dartSettings == null || StringUtil.isEmpty(dartSettings.getSdkPath())) {
      return getDescriptors(file, manager, isOnTheFly, DartBundle.message("inspections.dart.sdk.message"),
                            DartBundle.message("dart.setup.sdk"));
    } else if (!DartSettingsUtil.isDartSDKConfigured(file.getProject())) {
      return getDescriptors(file, manager, isOnTheFly, DartBundle.message("inspections.dart.sdk.disabled.message"),
                            DartBundle.message("dart.enable.sdk"));
    }
    return ProblemDescriptor.EMPTY_ARRAY;
  }

  private ProblemDescriptor[] getDescriptors(PsiFile file, InspectionManager manager, boolean isOnTheFly, String message, String fixMessage) {
    return new ProblemDescriptor[]{
      manager.createProblemDescriptor(
        file,
        message,
        new LocalQuickFix[]{new MyQuickFix(fixMessage)},
        HighlightInfo.convertSeverityToProblemHighlight(getDefaultLevel().getSeverity()),
        isOnTheFly,
        false
      )
    };
  }

  private static class MyQuickFix extends IntentionAndQuickFixAction {

    private final String myMessage;

    private MyQuickFix(String message) {
      myMessage = message;
    }

    @NotNull
    @Override
    public String getName() {
      return myMessage;
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return DartBundle.message("inspections.group.name");
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @Override
    public void applyFix(Project project, PsiFile file, @Nullable Editor editor) {
      ShowSettingsUtil.getInstance().showSettingsDialog(file.getProject(), DartBundle.message("dart.title"));
    }
  }
}
