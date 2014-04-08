package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;

public abstract class DartUrlResolver {

  /**
   * Returned instance becomes obsolete if/when pubspec.yaml file is added or deleted or if module-specific custom package roots are changed,
   * so do not keep returned instance too long.
   *
   * @param project
   * @param contextFile may be pubspec.yaml file, its parent folder or any file/folder within this parent folder; in case of import statements resolve this must be an analyzed file
   * @return
   */
  public static DartUrlResolver getInstance(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
    return new DartUrlResolverImpl(project, contextFile);
  }

  /**
   * Process 'Path Packages' (https://www.dartlang.org/tools/pub/dependencies.html#path-packages) and this package itself (symlink to local 'lib' folder)
   */
  public abstract void processLivePackages(final PairConsumer<String, VirtualFile> packageNameAndDirConsumer);
}
