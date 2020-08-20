// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubGetAction extends DartPubActionBase {
  @Override
  protected @NotNull @NlsContexts.DialogTitle String getTitle(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final String projectName = PubspecYamlUtil.getDartProjectName(pubspecYamlFile);
    final String prefix = projectName == null ? "" : ("[" + projectName + "] ");
    return prefix + DartBundle.message("dart.pub.get.title");
  }

  @Override
  protected String @Nullable [] calculatePubParameters(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    return new String[]{"get"};
  }
}
