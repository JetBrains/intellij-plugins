package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.ide.actions.DartStyleAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartfmtUtil {
  public static void runDartfmt(@NotNull final Project project, @NotNull final List<VirtualFile> dartFiles) {
    DartStyleAction.runDartFmt(project, dartFiles, false);
  }
}
