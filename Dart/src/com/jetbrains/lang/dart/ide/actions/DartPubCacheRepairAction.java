package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubCacheRepairAction extends DartPubActionBase {
  @Override
  @NotNull
  protected String getTitle(@NotNull final VirtualFile pubspecYamlFile) {
    return DartBundle.message("dart.pub.cache.repair.title");
  }

  @Nullable
  protected String[] calculatePubParameters(final Project project) {
    final int choice = Messages.showOkCancelDialog(project, DartBundle.message("dart.pub.cache.repair.message"),
                                                   DartBundle.message("dart.pub.cache.repair.title"), Messages.getWarningIcon());
    return choice == Messages.OK ? new String[]{"cache", "repair"} : null;
  }
}
