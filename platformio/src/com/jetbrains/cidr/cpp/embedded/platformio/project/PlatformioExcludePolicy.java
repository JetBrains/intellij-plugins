package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class PlatformioExcludePolicy implements DirectoryIndexExcludePolicy {

  private final Project myProject;

  public PlatformioExcludePolicy(Project project) {
    myProject = project;
  }

  @Override
  public String @NotNull [] getExcludeUrlsForProject() {
    return
      Stream.of(ProjectRootManager.getInstance(myProject).getContentRoots())
        .filter(root -> root.findChild(PlatformioFileType.FILE_NAME) != null)
        .map(VirtualFile::getUrl)
        .map(s -> s + "/.pio/build")
        .toArray(String[]::new);
  }
}
