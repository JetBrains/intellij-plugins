// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginAdvertiserService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

import static com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginAdvertiserServiceKt.createTryUltimateActionLabel;

final class JdlUpgradeNotification implements EditorNotificationProvider {
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

    panel.setText(JdlBundle.message("label.promo"));
    createTryUltimateActionLabel(panel, PluginAdvertiserService.Companion.getIdeaUltimate(), project, PluginId.getId(""), null);
   
    panel.createActionLabel(IdeBundle.message("plugins.advertiser.action.ignore.ultimate"), () -> {
      PropertiesComponent.getInstance().setValue(KEY, true);
      EditorNotifications.getInstance(project).updateAllNotifications();
    });

    return panel;
  }

  private static boolean isDismissed() {
    return PropertiesComponent.getInstance().getBoolean(KEY, false);
  }
}
