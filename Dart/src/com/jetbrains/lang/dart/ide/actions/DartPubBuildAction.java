package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
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
  protected String[] calculatePubParameters(final Project project) {
    final DartPubBuildDialog dialog = new DartPubBuildDialog(project);
    dialog.show();
    if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) return null;

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk != null &&
        StringUtil.compareVersionNumbers(sdk.getVersion(), "1.12") >= 0 &&
        StringUtil.compareVersionNumbers(sdk.getVersion(), "1.13") < 0) {
      return new String[]{"build", "--mode=" + dialog.getPubBuildMode(), "--no-package-symlinks"};
    }

    return new String[]{"build", "--mode=" + dialog.getPubBuildMode()};
  }
}
