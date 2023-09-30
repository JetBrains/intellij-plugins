// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

public class JdlUpgradeNotification implements EditorNotificationProvider {
  private static final String TARGET =
    "https://www.jetbrains.com/idea/download/?utm_source=product&utm_medium=link&utm_campaign=JHipsterJDL";
  private static final String KEY = "jhipster.ultimate";

  @Override
  public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent>
  collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
    var productCode = ApplicationInfo.getInstance().getBuild().getProductCode();
    if ("IU".equals(productCode)) return null;

    if (isDismissed()) return null;

    return fileEditor -> createPanel(project, fileEditor);
  }

  private static JComponent createPanel(Project project, FileEditor fileEditor) {
    var panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info);

    panel.setText("JHipster JDL plugin provides the best experience with IntelliJ IDEA Ultimate");

    panel.createActionLabel(IdeBundle.message("plugins.advertiser.action.try.ultimate", "IntelliJ IDEA Ultimate"), () ->
      BrowserUtil.browse(TARGET)
    );

    panel.createActionLabel(IdeBundle.message("plugins.advertiser.action.ignore.ultimate"), () ->
      dismiss(project)
    );

    return panel;
  }

  private static void dismiss(Project project) {
    PropertiesComponent.getInstance().setValue(KEY, true);
    EditorNotifications.getInstance(project).updateAllNotifications();
  }

  private static boolean isDismissed() {
    return PropertiesComponent.getInstance().getBoolean(KEY, false);
  }
}
