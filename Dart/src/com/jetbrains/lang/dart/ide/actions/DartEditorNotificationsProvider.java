// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
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
import com.jetbrains.lang.dart.flutter.FlutterUtil;
import com.jetbrains.lang.dart.sdk.*;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class DartEditorNotificationsProvider extends EditorNotifications.Provider<EditorNotificationPanel> {
  private static final Key<EditorNotificationPanel> KEY = Key.create("DartEditorNotificationsProvider");
  private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("Dart Support");

  @Override
  @NotNull
  public Key<EditorNotificationPanel> getKey() {
    return KEY;
  }

  @Override
  @Nullable
  public EditorNotificationPanel createNotificationPanel(@NotNull final VirtualFile vFile, @NotNull final FileEditor fileEditor, @NotNull Project project) {
    if (!vFile.isInLocalFileSystem()) {
      return null;
    }

    boolean isPubspecFile = PubspecYamlUtil.isPubspecFile(vFile);

    if (isPubspecFile) {
      final Module module = ModuleUtilCore.findModuleForFile(vFile, project);
      if (module == null) return null;

      // Defer to the Flutter plugin for package management and SDK configuration if appropriate.
      if (FlutterUtil.isFlutterPluginInstalled() && FlutterUtil.isPubspecDeclaringFlutter(vFile)) return null;

      final DartSdk sdk = DartSdk.getDartSdk(project);
      if (sdk != null && DartSdkLibUtil.isDartSdkEnabled(module)) {
        return new PubActionsPanel();
      }
    }

    if (isPubspecFile || FileTypeRegistry.getInstance().isFileOfType(vFile, DartFileType.INSTANCE)) {
      final DartSdk sdk = DartSdk.getDartSdk(project);

      final PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
      if (psiFile == null) return null;

      final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
      if (module == null) return null;

      // no SDK
      if (sdk == null) {
        final String sdkPath = DartSdkUtil.getFirstKnownDartSdkPath();
        if (DartSdkUtil.isDartSdkHome(sdkPath)) {
          return createNotificationToEnableDartSupport(module);
        }

        final String message = DartBundle.message("dart.sdk.is.not.configured");
        final String downloadUrl = DartSdkUpdateChecker.SDK_STABLE_DOWNLOAD_URL;

        final EditorNotificationPanel panel = new EditorNotificationPanel().icon(DartIcons.Dart_16).text(message);
        panel.createActionLabel(DartBundle.message("download.dart.sdk"), new OpenWebPageRunnable(downloadUrl));
        panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(project));
        return panel;
      }

      // SDK not enabled for this module
      if (!DartSdkLibUtil.isDartSdkEnabled(module)) {
        return createNotificationToEnableDartSupport(module);
      }

      if (!DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
        final String message = DartBundle.message("old.dart.sdk.configured", DartAnalysisServerService.MIN_SDK_VERSION, sdk.getVersion());
        final String downloadUrl = DartSdkUpdateChecker.SDK_STABLE_DOWNLOAD_URL;

        final EditorNotificationPanel panel = new EditorNotificationPanel().icon(DartIcons.Dart_16).text(message);
        panel.createActionLabel(DartBundle.message("download.dart.sdk"), new OpenWebPageRunnable(downloadUrl));
        panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(project));
        return panel;
      }
    }

    return null;
  }

  @NotNull
  private static EditorNotificationPanel createNotificationToEnableDartSupport(@NotNull final Module module) {
    final String message = DartSdkLibUtil.isIdeWithMultipleModuleSupport()
                           ? DartBundle.message("dart.support.is.not.enabled.for.module.0", module.getName())
                           : DartBundle.message("dart.support.is.not.enabled.for.project");
    final EditorNotificationPanel panel = new EditorNotificationPanel().icon(DartIcons.Dart_16).text(message);
    panel.createActionLabel(DartBundle.message("enable.dart.support"), new EnableDartSupportForModule(module));
    panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(module.getProject()));
    return panel;
  }

  private static class PubActionsPanel extends EditorNotificationPanel {
    private PubActionsPanel() {
      super(EditorColors.GUTTER_BACKGROUND);
      createActionLabel(DartBundle.message("get.dependencies"), "Dart.pub.get");
      createActionLabel(DartBundle.message("upgrade.dependencies"), "Dart.pub.upgrade");
      createActionLabel(DartBundle.message("action.label.text.build"), "Dart.build");
      myLinksPanel.add(new JSeparator(SwingConstants.VERTICAL));
      createActionLabel(DartBundle.message("action.label.text.repair.cache"), "Dart.pub.cache.repair");
    }
  }

  private static class EnableDartSupportForModule implements Runnable {
    private final Module myModule;

    EnableDartSupportForModule(@NotNull final Module module) {
      this.myModule = module;
    }

    @Override
    public void run() {
      final Project project = myModule.getProject();

      ApplicationManager.getApplication().runWriteAction(() -> {
        if (DartSdk.getDartSdk(project) == null) {
          final String sdkPath = DartSdkUtil.getFirstKnownDartSdkPath();
          if (DartSdkUtil.isDartSdkHome(sdkPath)) {
            DartSdkLibUtil.ensureDartSdkConfigured(project, sdkPath);
          }
          else {
            return; // shouldn't happen, sdk path is already checked
          }
        }

        DartSdkLibUtil.enableDartSdk(myModule);
      });

      DartAnalysisServerService.getInstance(project).serverReadyForRequest();

      final DartSdk sdk = DartSdk.getDartSdk(project);
      if (sdk != null && DartSdkLibUtil.isDartSdkEnabled(myModule)) {
        final String title = DartSdkLibUtil.isIdeWithMultipleModuleSupport()
                             ? DartBundle.message("dart.support.enabled.for.module.0", myModule.getName())
                             : DartBundle.message("dart.support.enabled");
        final String message = DartBundle.message("dart.sdk.0.open.dart.settings", sdk.getVersion());

        final NotificationListener listener = new NotificationListener.Adapter() {
          @Override
          protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
            DartConfigurable.openDartSettings(project);
          }
        };

        NOTIFICATION_GROUP.createNotification(title, message, NotificationType.INFORMATION, listener).notify(project);
      }
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
