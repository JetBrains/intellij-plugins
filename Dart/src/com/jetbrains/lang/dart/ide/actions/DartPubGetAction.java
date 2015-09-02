package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubGetAction extends DartPubActionBase {
  @Override
  @NotNull
  protected String getTitle(@NotNull final VirtualFile pubspecYamlFile) {
    final String projectName = PubspecYamlUtil.getDartProjectName(pubspecYamlFile);
    final String prefix = projectName == null ? "" : ("[" + projectName + "] ");
    return prefix + DartBundle.message("dart.pub.get.title");
  }

  @Nullable
  protected String[] calculatePubParameters(final Project project) {
    return new String[]{"get"};
  }
}
