// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;


import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

public final class RippleInfo {

  private static volatile String ripplePath = null;

  @Nullable
  public static String getRipplePath() {
    String path = ripplePath;
    if (path != null) {
      return ripplePath;
    }
    String name = "ripple";
    File file = PathEnvironmentVariableUtil.findInPath(SystemInfo.isWindows ? name + ".cmd" : name);

    if (file != null) {
      path = file.getAbsolutePath();
      ripplePath = path;
    }
    return path;
  }

  public static String getIndexPath(Project project, final String platform) {
    Collection<VirtualFile> name = FilenameIndex.getVirtualFilesByName(project, "index.html", GlobalSearchScope.projectScope(project));

    VirtualFile item = ContainerUtil.find(name, file -> file.getPath().contains("platforms") && file.getPath().contains(platform));
    return item != null ? item.getParent().getPath() : null;
  }
}
