package com.intellij.prettierjs;

import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrettierNotificationUtil {
  private PrettierNotificationUtil() {
  }

  public static void reportCodeStyleSettingsImported(@NotNull Project project,
                                                     @NotNull VirtualFile file,
                                                     @Nullable Runnable resetAction) {
    JSLinterUtil.reportCodeStyleSettingsImported(project, PrettierBundle.message("import.notification"), file, resetAction);
  }
}
