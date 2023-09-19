// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

public final class PrettierCodeStyleEditorNotificationProvider implements EditorNotificationProvider, DumbAware {
  private static final String NOTIFICATION_DISMISSED_PROPERTY = "prettier.import.notification.dismissed";

  private static boolean isNotificationDismissed(@NotNull Project project) {
    return PropertiesComponent.getInstance(project).getBoolean(NOTIFICATION_DISMISSED_PROPERTY);
  }

  private void dismissNotification(@NotNull Project project) {
    PropertiesComponent.getInstance(project).setValue(NOTIFICATION_DISMISSED_PROPERTY, true);
    EditorNotifications.getInstance(project).updateNotifications(this);
  }

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project,
                                                                                                                 @NotNull VirtualFile file) {
    if (JSProjectUtil.isInLibrary(file, project)) return null;

    if (!file.isWritable() || JSLibraryUtil.isProbableLibraryFile(file)) return null;
    if (isNotificationDismissed(project)) return null;
    if (!PrettierUtil.isConfigFileOrPackageJson(file)) return null;

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
    if (config == null) return null;
    if (config.isInstalled(project)) return null;

    return fileEditor -> {
      final EditorNotificationPanel panel =
        new EditorNotificationPanel(EditorColors.GUTTER_BACKGROUND, EditorNotificationPanel.Status.Info);
      panel.setText(PrettierBundle.message("editor.notification.title"));

      panel.createActionLabel(PrettierBundle.message("editor.notification.yes.text"), PrettierImportCodeStyleAction.ACTION_ID, false);
      panel.createActionLabel(PrettierBundle.message("editor.notification.no.text"), () -> dismissNotification(project), false);

      return panel;
    };
  }
}
