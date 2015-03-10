package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.*;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartSdkIsOutOfDateInspection extends LocalInspectionTool {
  @Override
  @NotNull
  public String getGroupDisplayName() {
    return DartBundle.message("inspections.group.name");
  }

  @Override
  @Nls
  @NotNull
  public String getDisplayName() {
    return DartBundle.message("dart.sdk.is.out.of.date");
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

    final DartSdk sdk = DartSdk.getDartSdk(file.getProject());
    if (sdk == null) {
      return ProblemDescriptor.EMPTY_ARRAY;
    }

    final String sdkHome = sdk.getHomePath();
    if (sdkHome == null) {
      return ProblemDescriptor.EMPTY_ARRAY;
    }

    final DartSdkUtil.SdkUpdateInfo updateInfo = DartSdkUtil.checkForFreshSdk(sdkHome);
    if (updateInfo != null) {
      final String message = DartBundle.message("new.dart.sdk.available", updateInfo.getRevision(), sdk.getVersion());
      return createProblemDescriptors(file, manager, message,
                                      new OpenWebPageFix(DartBundle.message("download.dart.sdk"), updateInfo.getDownloadUrl()),
                                      new OpenDartSettingsQuickFix(DartBundle.message("open.dart.settings")));
    }


    return ProblemDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public SuppressQuickFix[] getBatchSuppressActions(@Nullable final PsiElement element) {
    return super.getBatchSuppressActions(element);
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

  private static class OpenWebPageFix extends IntentionAndQuickFixAction {
    @NotNull private final String myMessage;
    @NotNull private final String myUrl;

    private OpenWebPageFix(@NotNull final String message, @NotNull final String url) {
      myMessage = message;
      myUrl = url;
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
    public void applyFix(@NotNull final Project project, @NotNull final PsiFile file, @Nullable final Editor editor) {
      BrowserUtil.browse(myUrl);
    }
  }
}
