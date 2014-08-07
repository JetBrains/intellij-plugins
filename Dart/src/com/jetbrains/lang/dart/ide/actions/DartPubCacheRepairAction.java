package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class DartPubCacheRepairAction extends DartPubActionBase {
  @Nls
  protected String getTitle() {
    return DartBundle.message("dart.pub.cache.repair.title");
  }

  @Nullable
  protected String[] calculatePubParameters(final Project project) {
    final int choice =
      Messages.showOkCancelDialog(project, DartBundle.message("dart.pub.cache.repair.message"), getTitle(), Messages.getWarningIcon());
    return choice == Messages.OK ? new String[]{"cache", "repair"} : null;
  }

  protected String getSuccessMessage() {
    return DartBundle.message("dart.pub.cache.repair.success");
  }
}
