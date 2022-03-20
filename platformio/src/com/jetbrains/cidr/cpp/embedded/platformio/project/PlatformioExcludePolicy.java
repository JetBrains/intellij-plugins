package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType.FILE_NAME;

public class PlatformioExcludePolicy implements DirectoryIndexExcludePolicy {
  private final Project myProject;

  public PlatformioExcludePolicy(final @NotNull Project project) {
    myProject = project;
  }

  @Override
  public @NotNull String[] getExcludeUrlsForProject() {
    return Stream.of(ProjectRootManager.getInstance(myProject).getContentRoots())
        .filter(root -> root.findChild(FILE_NAME) != null)
        .map(VirtualFile::getUrl)
        .map(s -> s + "/.pio/build")
        .toArray(String[]::new);
  }
}
