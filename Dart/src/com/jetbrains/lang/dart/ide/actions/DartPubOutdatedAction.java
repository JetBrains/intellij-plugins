// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubOutdatedAction extends DartPubActionBase {
  public static final String MIN_SDK_VERSION = "2.8";

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);

    final Project project = e.getProject();
    DartSdk sdk = project != null ? DartSdk.getDartSdk(project) : null;
    if (sdk == null || StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_SDK_VERSION) < 0) {
      e.getPresentation().setEnabledAndVisible(false);
    }
  }

  @Override
  @NotNull
  protected String getTitle(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final String projectName = PubspecYamlUtil.getDartProjectName(pubspecYamlFile);
    final String prefix = projectName == null ? "" : ("[" + projectName + "] ");
    return prefix + DartBundle.message("dart.pub.outdated.title");
  }

  @Override
  protected String @Nullable [] calculatePubParameters(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    return new String[]{"outdated"};
  }
}
