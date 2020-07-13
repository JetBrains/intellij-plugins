// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.extensions.ExtensionPointUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.prettierjs.codeStyle.PrettierCodeStyleInstaller;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.EditorNotificationsImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrettierCodeStyleEditorNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel>
  implements DumbAware {

  private static final Key<EditorNotificationPanel> KEY = Key.create("prettier.codestyle.notification.panel");
  private static final String NOTIFICATION_DISMISSED_PROPERTY = "prettier.import.notification.dismissed";

  private final Project myProject;

  public PrettierCodeStyleEditorNotificationProvider(@NotNull Project project) {
    myProject = project;
    PrettierCodeStyleInstaller.EP_NAME.addChangeListener(() -> {
      EditorNotifications.getInstance(myProject).updateNotifications(this);
    }, ExtensionPointUtil.createExtensionDisposable(this, EditorNotificationsImpl.EP_PROJECT.getPoint(myProject)));
  }

  private boolean isNotificationDismissed(@NotNull VirtualFile file) {
    return PropertiesComponent.getInstance(myProject).getBoolean(NOTIFICATION_DISMISSED_PROPERTY) ||
           !PrettierUtil.isConfigFileOrPackageJson(file);
  }

  private void dismissNotification() {
    PropertiesComponent.getInstance(myProject).setValue(NOTIFICATION_DISMISSED_PROPERTY, true);
    EditorNotifications.getInstance(myProject).updateAllNotifications();
  }

  @NotNull
  @Override
  public Key<EditorNotificationPanel> getKey() {
    return KEY;
  }

  @Nullable
  @Override
  public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file,
                                                         @NotNull FileEditor fileEditor,
                                                         @NotNull Project project) {
    if (!(fileEditor instanceof TextEditor)) return null;
    if (!file.isWritable() || JSProjectUtil.isInLibrary(file, project) || JSLibraryUtil.isProbableLibraryFile(file)) {
      return null;
    }
    if (isNotificationDismissed(file)) {
      return null;
    }

    PrettierConfig config = null;
    if (PrettierUtil.isConfigFile(file)) {
      config = PrettierUtil.parseConfig(project, file);
    }
    if (PackageJsonUtil.isPackageJsonFile(file)) {
      //if package.json is currently opened, but there is a neighboring config file
      VirtualFile configVFile = PrettierUtil.findSingleConfigInDirectory(file.getParent());
      if (configVFile != null) {
        config = PrettierUtil.parseConfig(project, configVFile);
      }
      else {
        config = PrettierUtil.parseConfig(project, file);
      }
    }
    if (config == null) {
      return null;
    }
    if (config.isInstalled(project)) {
      return null;
    }
    final EditorNotificationPanel panel = new EditorNotificationPanel(EditorColors.GUTTER_BACKGROUND);
    panel.setText(PrettierBundle.message("editor.notification.title"));

    panel.createActionLabel(PrettierBundle.message("editor.notification.yes.text"), PrettierImportCodeStyleAction.ACTION_ID, false);
    panel.createActionLabel(PrettierBundle.message("editor.notification.no.text"), () -> dismissNotification(), false);

    return panel;
  }
}
