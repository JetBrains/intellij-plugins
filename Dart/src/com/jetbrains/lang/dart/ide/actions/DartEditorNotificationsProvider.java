package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
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
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class DartEditorNotificationsProvider extends EditorNotifications.Provider<EditorNotificationPanel> {
  private static final Key<EditorNotificationPanel> KEY = Key.create("DartEditorNotificationsProvider");

  @NotNull private final Project myProject;

  private static final long MILLIS_IN_ONE_DAY = TimeUnit.DAYS.toMillis(1);

  public static final String DART_LAST_SDK_CHECK_KEY = "DART_LAST_SDK_CHECK_KEY";
  public static final long DART_LAST_SDK_CHECK_DEFAULT_VALUE = 0;

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
      if (module != null && sdk != null && DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) {
        return new PubActionsPanel();
      }
    }
    else if (vFile.getFileType() == DartFileType.INSTANCE) {
      final DartSdk sdk = DartSdk.getDartSdk(myProject);

      // no SDK
      if (sdk == null) {
        return new SDKNotConfiguredPanel(DartBundle.message("dart.sdk.is.not.configured"));
      }

      final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vFile);
      if (psiFile == null) return null;

      final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
      if (module == null) return null;

      // SDK not enabled for this module
      if (!DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) {
        final String message = DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
                               ? DartBundle.message("dart.support.is.not.enabled.for.module.0", module.getName())
                               : DartBundle.message("dart.support.is.not.enabled.for.project");
        return new SDKNotConfiguredForModulePanel(message, module, sdk.getGlobalLibName());
      }

      // old SDK, pre-1.9 (no analysis server)
      if (StringUtil.compareVersionNumbers(sdk.getVersion(), DartAnalysisServerService.MIN_SDK_VERSION) < 0) {
        final String message = DartBundle.message("old.dart.sdk.configured", DartAnalysisServerService.MIN_SDK_VERSION, sdk.getVersion());
        return new OldSDKConfiguredPanel(message);
      }

      // new SDK is available for download (stable or dev)
      final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);
      boolean checkForLatestSDK = propertiesComponent
        .getBoolean(DartConfigurable.DART_DO_CHECK_FOR_LATEST_SDK_KEY, DartConfigurable.DART_DO_CHECK_FOR_LATEST_SDK_DEFAULT_VALUE);

      long currentTimeMillis = System.currentTimeMillis();
      long lastCheck = propertiesComponent.getOrInitLong(DART_LAST_SDK_CHECK_KEY, DART_LAST_SDK_CHECK_DEFAULT_VALUE);

      if (checkForLatestSDK && (currentTimeMillis - lastCheck) > MILLIS_IN_ONE_DAY) {
        propertiesComponent.setValue(DART_LAST_SDK_CHECK_KEY, Long.valueOf(currentTimeMillis).toString());

        boolean checkForStable = propertiesComponent.getBoolean(DartConfigurable.DART_CHECK_FOR_LATEST_STABLE_SDK_KEY,
                                                                DartConfigurable.DART_CHECK_FOR_LATEST_STABLE_SDK_DEFAULT_VALUE);
        if (checkForStable) {
          final DartSdkUtil.SdkUpdateInfo updateInfoStable = DartSdkUtil.checkForNewerStableSDK(sdk.getHomePath());
          if (updateInfoStable != null) {
            final String message = DartBundle.message("new.dart.sdk.available", updateInfoStable.getRevision(), sdk.getVersion());
            return new NewSDKAvailable(message, DartSdkUtil.SdkReleaseChannel.STABLE.getDownloadUrl());
          }
        }

        boolean checkForDev = propertiesComponent
          .getBoolean(DartConfigurable.DART_CHECK_FOR_LATEST_DEV_SDK_KEY, DartConfigurable.DART_CHECK_FOR_LATEST_DEV_SDK_DEFAULT_VALUE);

        if (checkForDev) {
          final DartSdkUtil.SdkUpdateInfo updateInfoDev = DartSdkUtil.checkForNewerDevSDK(sdk.getHomePath());
          if (updateInfoDev != null) {
            final String message = DartBundle.message("new.dart.sdk.available", updateInfoDev.getRevision(), sdk.getVersion());
            return new NewSDKAvailable(message, DartSdkUtil.SdkReleaseChannel.DEV.getDownloadUrl());
          }
        }
      }
    }

    return null;
  }

  private static class PubActionsPanel extends EditorNotificationPanel {
    private PubActionsPanel() {
      myLinksPanel.add(new JLabel("Pub actions:"));
      createActionLabel("Get Dependencies", "Dart.pub.get");
      createActionLabel("Upgrade Dependencies", "Dart.pub.upgrade");
      createActionLabel("Build...", "Dart.pub.build");
      myLinksPanel.add(new JLabel("        "));
      createActionLabel("Repair Cache...", "Dart.pub.cache.repair");
    }

    @Override
    public Color getBackground() {
      final Color color = EditorColorsManager.getInstance().getGlobalScheme().getColor(EditorColors.GUTTER_BACKGROUND);
      return color != null ? color : super.getBackground();
    }
  }

  private static class SDKStatusPanel extends EditorNotificationPanel {
    private SDKStatusPanel(@NotNull final String message) {
      text(message).icon(DartIcons.Dart_16);
    }

    @Override
    public Color getBackground() {
      final Color color = EditorColorsManager.getInstance().getGlobalScheme().getColor(EditorColors.GUTTER_BACKGROUND);
      return color != null ? color : super.getBackground();
    }
  }

  private static class SDKNotConfiguredPanel extends SDKStatusPanel {
    private SDKNotConfiguredPanel(@NotNull final String message) {
      super(message);
      createActionLabel(DartBundle.message("open.dart.settings"), "Dart.open.settings");
    }
  }

  private static class SDKNotConfiguredForModulePanel extends SDKStatusPanel {
    private SDKNotConfiguredForModulePanel(@NotNull final String message,
                                           @NotNull final Module module,
                                           @NotNull final String dartSdkGlobalLibName) {
      super(message);
      createActionLabel(DartBundle.message("enable.dart.support"), new EnableDartSupportForModule(module, dartSdkGlobalLibName));
      createActionLabel(DartBundle.message("open.dart.settings"), "Dart.open.settings");
    }
  }

  private static class OldSDKConfiguredPanel extends SDKStatusPanel {
    private OldSDKConfiguredPanel(@NotNull final String message) {
      super(message);
      createActionLabel(DartBundle.message("download.dart.sdk"),
                        new OpenWebPageRunnable(DartSdkUtil.SdkReleaseChannel.STABLE.getDownloadUrl()));
      createActionLabel(DartBundle.message("open.dart.settings"), "Dart.open.settings");
    }
  }

  private static class NewSDKAvailable extends SDKStatusPanel {
    private NewSDKAvailable(@NotNull final String message, @NotNull final String downloadUrl) {
      super(message);
      createActionLabel(DartBundle.message("download.dart.sdk"),
                        new OpenWebPageRunnable(downloadUrl));
      createActionLabel(DartBundle.message("open.dart.settings"), "Dart.open.settings");
    }
  }

  private static class EnableDartSupportForModule implements Runnable {
    private final Module myModule;
    private final String myDartSdkGlobalLibName;

    public EnableDartSupportForModule(@NotNull final Module module, @NotNull final String dartSdkGlobalLibName) {
      this.myModule = module;
      this.myDartSdkGlobalLibName = dartSdkGlobalLibName;
    }

    @Override
    public void run() {
      ApplicationManager.getApplication().runWriteAction(
        new Runnable() {
          public void run() {
            DartSdkGlobalLibUtil.configureDependencyOnGlobalLib(myModule, myDartSdkGlobalLibName);
          }
        }
      );
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
}
