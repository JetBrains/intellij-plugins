// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
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
import java.util.function.Function;

public final class DartEditorNotificationsProvider implements EditorNotificationProvider {
  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project,
                                                                                                                 @NotNull VirtualFile file) {
    if (!file.isInLocalFileSystem()) {
      return null;
    }

    boolean isPubspecFile = PubspecYamlUtil.isPubspecFile(file);

    if (isPubspecFile) {
      final Module module = ModuleUtilCore.findModuleForFile(file, project);
      if (module == null) return null;

      // Defer to the Flutter plugin for package management and SDK configuration if appropriate.
      if (FlutterUtil.isFlutterPluginInstalled() && FlutterUtil.isPubspecDeclaringFlutter(file)) return null;

      final DartSdk sdk = DartSdk.getDartSdk(project);
      if (sdk != null && DartSdkLibUtil.isDartSdkEnabled(module)) {
        return fileEditor -> new PubActionsPanel(fileEditor, sdk);
      }
    }

    if (isPubspecFile || FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE)) {
      final DartSdk sdk = DartSdk.getDartSdk(project);

      final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (psiFile == null) return null;

      final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
      if (module == null) return null;

      // no SDK
      if (sdk == null) {
        final String sdkPath = DartSdkUtil.getFirstKnownDartSdkPath();
        if (DartSdkUtil.isDartSdkHome(sdkPath)) {
          return fileEditor -> createNotificationToEnableDartSupport(fileEditor, module);
        }
        else {
          return fileEditor -> createNoDartSdkPanel(fileEditor, project, DartBundle.message("dart.sdk.is.not.configured"));
        }
      }

      // SDK not enabled for this module
      if (!DartSdkLibUtil.isDartSdkEnabled(module)) {
        return fileEditor -> createNotificationToEnableDartSupport(fileEditor, module);
      }

      if (!DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
        String message = DartBundle.message("old.dart.sdk.configured", DartAnalysisServerService.MIN_SDK_VERSION, sdk.getVersion());
        return fileEditor -> createNoDartSdkPanel(fileEditor, project, message);
      }
    }

    return null;
  }

  public @NotNull EditorNotificationPanel createNoDartSdkPanel(@NotNull FileEditor fileEditor,
                                                               @NotNull Project project,
                                                               @NlsContexts.Label String message) {
    final String downloadUrl = DartSdkUpdateChecker.SDK_STABLE_DOWNLOAD_URL;

    final EditorNotificationPanel panel =
      new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning).icon(DartIcons.Dart_16).text(message);
    panel.createActionLabel(DartBundle.message("download.dart.sdk"), new OpenWebPageRunnable(downloadUrl));
    panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(project));
    return panel;
  }

  private static @NotNull EditorNotificationPanel createNotificationToEnableDartSupport(@NotNull FileEditor fileEditor,
                                                                                        @NotNull Module module) {
    final String message = DartSdkLibUtil.isIdeWithMultipleModuleSupport()
                           ? DartBundle.message("dart.support.is.not.enabled.for.module.0", module.getName())
                           : DartBundle.message("dart.support.is.not.enabled.for.project");
    final EditorNotificationPanel panel =
      new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning).icon(DartIcons.Dart_16).text(message);
    panel.createActionLabel(DartBundle.message("enable.dart.support"), new EnableDartSupportForModule(module));
    panel.createActionLabel(DartBundle.message("open.dart.settings"), new OpenDartSettingsRunnable(module.getProject()));
    return panel;
  }

  private static final class PubActionsPanel extends EditorNotificationPanel {
    private PubActionsPanel(@NotNull FileEditor fileEditor, @NotNull DartSdk sdk) {
      super(fileEditor, null, EditorColors.GUTTER_BACKGROUND, Status.Info);
      createActionLabel(DartBundle.message("pub.get"), "Dart.pub.get");
      createActionLabel(DartBundle.message("pub.upgrade"), "Dart.pub.upgrade");

      if (StringUtil.compareVersionNumbers(sdk.getVersion(), DartPubOutdatedAction.MIN_SDK_VERSION) >= 0) {
        createActionLabel(DartBundle.message("pub.outdated"), "Dart.pub.outdated");
      }

      myLinksPanel.add(new JSeparator(SwingConstants.VERTICAL));
      createActionLabel(DartBundle.message("webdev.build"), "Dart.build");
    }
  }

  private static class EnableDartSupportForModule implements Runnable {
    private final Module myModule;

    EnableDartSupportForModule(@NotNull Module module) {
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

        NotificationGroupManager.getInstance()
          .getNotificationGroup("Dart Support")
          .createNotification(title, message, NotificationType.INFORMATION)
          .setListener(listener)
          .notify(project);
      }
    }
  }

  private static final class OpenWebPageRunnable implements Runnable {
    private final @NotNull String myUrl;

    private OpenWebPageRunnable(@NotNull String url) {
      myUrl = url;
    }

    @Override
    public void run() {
      BrowserUtil.browse(myUrl);
    }
  }

  private static final class OpenDartSettingsRunnable implements Runnable {
    private final @NotNull Project myProject;

    private OpenDartSettingsRunnable(@NotNull Project project) {
      myProject = project;
    }

    @Override
    public void run() {
      DartConfigurable.openDartSettings(myProject);
    }
  }
}
