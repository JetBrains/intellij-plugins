// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubCacheRepairAction extends DartPubActionBase {
  @Override
  protected @NotNull @NlsContexts.DialogTitle String getTitle(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    return DartBundle.message("dart.pub.cache.repair.title");
  }

  @Override
  protected String @Nullable [] calculatePubParameters(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final int choice = Messages.showOkCancelDialog(project, DartBundle.message("dart.pub.cache.repair.message"),
                                                   DartBundle.message("dart.pub.cache.repair.title"), Messages.getWarningIcon());
    return choice == Messages.OK ? new String[]{"cache", "repair"} : null;
  }
}
