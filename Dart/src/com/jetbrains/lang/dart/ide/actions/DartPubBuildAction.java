package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class DartPubBuildAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getTitle() {
    return DartBundle.message("dart.pub.build.title");
  }

  @Nullable
  protected String[] calculatePubParameters(final Project project) {
    final DartPubBuildDialog dialog = new DartPubBuildDialog(project);
    dialog.show();
    return dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE
           ? new String[]{"build", "--mode=" + dialog.getPubBuildMode()}
           : null;
  }

  @Override
  protected String getSuccessMessage() {
    return DartBundle.message("dart.pub.build.success");
  }
}
