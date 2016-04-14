package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubBuildAction extends DartPubActionBase {
  @Override
  @NotNull
  protected String getTitle(@NotNull final VirtualFile pubspecYamlFile) {
    final String projectName = PubspecYamlUtil.getDartProjectName(pubspecYamlFile);
    final String prefix = projectName == null ? "" : ("[" + projectName + "] ");
    return prefix + DartBundle.message("dart.pub.build.title");
  }

  @Nullable
  protected String[] calculatePubParameters(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final DartPubBuildDialog dialog = new DartPubBuildDialog(project, pubspecYamlFile.getParent());
    dialog.show();
    if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
      return null;
    }
    return new String[]{"build", "--mode=" + dialog.getPubBuildMode(), "--output=" + dialog.getOutputFolder()};
  }
}
