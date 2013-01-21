package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairProcessor;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.module.DartModuleType;

public class DartBreakpointAware implements PairProcessor<VirtualFile, Project> {
  @Override
  public boolean process(VirtualFile file, Project project) {
    if (file.getFileType() == DartFileType.INSTANCE) {
      Module module = ModuleUtilCore.findModuleForFile(file, project);
      if (module != null) {
        // dart file not in dart module.
        return ModuleType.get(module) != DartModuleType.getInstance();
      }
    }
    return false;
  }
}
