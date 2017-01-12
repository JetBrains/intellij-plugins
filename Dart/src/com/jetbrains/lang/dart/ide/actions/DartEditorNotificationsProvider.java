package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUpdateChecker;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartEditorNotificationsProvider extends EditorNotifications.Provider<EditorNotificationPanel> {
  private static final Key<EditorNotificationPanel> KEY = Key.create("DartEditorNotificationsProvider");

  @NotNull private final Project myProject;

  public DartEditorNotificationsProvider(@NotNull final Project project) {
    myProject = project;
  }

  @Override
  @NotNull
  public Key<EditorNotificationPanel> getKey() {
    return KEY;
  }

  @Override
  @Nullable
  public EditorNotificationPanel createNotificationPanel(@NotNull final VirtualFile vFile, @NotNull final FileEditor fileEditor) {
    if (!vFile.isInLocalFileSystem()) {
      return null;
    }

    if (PubspecYamlUtil.PUBSPEC_YAML.equalsIgnoreCase(vFile.getName())) {
      final DartSdk sdk = DartSdk.getDartSdk(myProject);
      final Module module = ModuleUtilCore.findModuleForFile(vFile, myProject);
      if (module != null && sdk != null && DartSdkLibUtil.isDartSdkEnabled(module)) {
        return new PubActionsPanel();
      }
    }

    if (PubspecYamlUtil.PUBSPEC_YAML.equalsIgnoreCase(vFile.getName()) || vFile.getFileType() == DartFileType.INSTANCE) {
      final DartSdk sdk = DartSdk.getDartSdk(myProject);

      // no SDK
      if (sdk == null) {
        final String message = DartBundle.message("dart.sdk.is.not.configured");
        final String downloadUrl = DartSdkUpdateChecker.SDK_STABLE_DOWNLOAD_URL;

        final EditorNotificationPanel panel = new EditorNotificationPanel().icon(DartIcons.Dart_16).text(message);
        panel.createActionLabel(DartBundle.message("download.dart.sdk"), new OpenWebPageRunnable(downloadUrl));
        panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(myProject));
        return panel;
      }

      final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vFile);
      if (psiFile == null) return null;

      final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
      if (module == null) return null;

      // SDK not enabled for this module
      if (!DartSdkLibUtil.isDartSdkEnabled(module)) {
        final String message = DartSdkLibUtil.isIdeWithMultipleModuleSupport()
                               ? DartBundle.message("dart.support.is.not.enabled.for.module.0", module.getName())
                               : DartBundle.message("dart.support.is.not.enabled.for.project");
        final EditorNotificationPanel panel = new EditorNotificationPanel().icon(DartIcons.Dart_16).text(message);
        panel.createActionLabel(DartBundle.message("enable.dart.support"), new EnableDartSupportForModule(module));
        panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(myProject));
        return panel;
      }

      if (!DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
        final String message = DartBundle.message("old.dart.sdk.configured", DartAnalysisServerService.MIN_SDK_VERSION, sdk.getVersion());
        final String downloadUrl = DartSdkUpdateChecker.SDK_STABLE_DOWNLOAD_URL;

        final EditorNotificationPanel panel = new EditorNotificationPanel().icon(DartIcons.Dart_16).text(message);
        panel.createActionLabel(DartBundle.message("download.dart.sdk"), new OpenWebPageRunnable(downloadUrl));
        panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(myProject));
        return panel;
      }
    }

    return null;
  }

  private static class PubActionsPanel extends EditorNotificationPanel {
    private PubActionsPanel() {
      super(EditorColors.GUTTER_BACKGROUND);
      myLinksPanel.add(new JLabel("Pub actions:"));
      createActionLabel(DartBundle.message("get.dependencies"), "Dart.pub.get");
      createActionLabel(DartBundle.message("upgrade.dependencies"), "Dart.pub.upgrade");
      createActionLabel("Build...", "Dart.pub.build");
      myLinksPanel.add(new JSeparator(SwingConstants.VERTICAL));
      createActionLabel("Repair cache...", "Dart.pub.cache.repair");
    }
  }

  private static class EnableDartSupportForModule implements Runnable {
    private final Module myModule;

    public EnableDartSupportForModule(@NotNull final Module module) {
      this.myModule = module;
    }

    @Override
    public void run() {
      ApplicationManager.getApplication().runWriteAction(() -> DartSdkLibUtil.enableDartSdk(myModule));
    }
  }

  private static class OpenWebPageRunnable implements Runnable {
    @NotNull private final String myUrl;

    private OpenWebPageRunnable(@NotNull final String url) {
      myUrl = url;
    }

    @Override
    public void run() {
      BrowserUtil.browse(myUrl);
    }
  }

  private static class OpenDartSettingsRunnable implements Runnable {
    @NotNull private final Project myProject;

    private OpenDartSettingsRunnable(@NotNull final Project project) {
      myProject = project;
    }

    @Override
    public void run() {
      DartConfigurable.openDartSettings(myProject);
    }
  }
}
