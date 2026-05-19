package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class StandardJSLanguageServiceManager extends MultiRootJSLinterLanguageServiceManager<StandardJSService> {
  public StandardJSLanguageServiceManager(@NotNull Project project) {
    super(project, StandardJSUtil.PACKAGE_NAME);
  }

  public static StandardJSLanguageServiceManager getInstance(Project project) {
    return project.getService(StandardJSLanguageServiceManager.class);
  }

  @Override
  protected @NotNull StandardJSService createServiceInstance(@NotNull NodePackage resolvedPackage, @NotNull VirtualFile workingDirectory) {
    return new StandardJSService(myProject, resolvedPackage, workingDirectory);
  }
}
