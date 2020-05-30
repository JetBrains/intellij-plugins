// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

/**
 * {@link DartStartupActivity} configures "Dart Packages" library (based on Dart-specific pubspec.yaml and .packages files) on project open.
 * Afterwards the "Dart Packages" library is kept up-to-dated thanks to {@link DartFileListener} and {@link DartModuleRootListener}.
 *
 * @see DartFileListener
 * @see DartModuleRootListener
 */
public class DartStartupActivity implements StartupActivity {
  @Override
  public void runActivity(@NotNull Project project) {
    final Collection<VirtualFile> pubspecYamlFiles =
      FilenameIndex.getVirtualFilesByName(project, PUBSPEC_YAML, GlobalSearchScope.projectScope(project));

    for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
      final Module module = ModuleUtilCore.findModuleForFile(pubspecYamlFile, project);
      if (module != null && FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, module.getModuleContentScope())) {
        excludeBuildAndPackagesFolders(module, pubspecYamlFile);
      }
    }

    if (!pubspecYamlFiles.isEmpty()) {
      DartFileListener.scheduleDartPackageRootsUpdate(project);
    }
  }

  public static void excludeBuildAndPackagesFolders(final @NotNull Module module, final @NotNull VirtualFile pubspecYamlFile) {
    final VirtualFile root = pubspecYamlFile.getParent();
    final VirtualFile contentRoot =
      root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
    if (contentRoot == null) return;

    // http://pub.dartlang.org/doc/glossary.html#entrypoint-directory
    // Entrypoint directory: A directory inside your package that is allowed to contain Dart entrypoints.
    // Pub will ensure all of these directories get a "packages" directory, which is needed for "package:" imports to work.
    // Pub has a whitelist of these directories: benchmark, bin, example, test, tool, and web.
    // Any subdirectories of those (except bin) may also contain entrypoints.
    //
    // the same can be seen in the pub tool source code: [repo root]/sdk/lib/_internal/pub/lib/src/entrypoint.dart

    final DartSdk sdk = DartSdk.getDartSdk(module.getProject());

    final Collection<String> oldExcludedUrls =
      ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(), new Condition<String>() {
        final String rootUrl = root.getUrl();

        @Override
        public boolean value(final String url) {
          if (url.equals(rootUrl + "/.dart_tool") && sdk != null && StringUtil.compareVersionNumbers(sdk.getVersion(), "2.0") >= 0) {
            return true;
          }

          if (url.equals(rootUrl + "/.pub")) return true;
          if (url.equals(rootUrl + "/build")) return true;
          if (url.equals(rootUrl + "/packages")) return true;

          // excluded subfolder of the root 'packages' folder (older versions of the Dart plugin)
          if (url.startsWith(root + "/packages/")) return true;

          if (url.endsWith("/packages") && (url.startsWith(rootUrl + "/bin/") ||
                                            url.startsWith(rootUrl + "/benchmark/") ||
                                            url.startsWith(rootUrl + "/example/") ||
                                            url.startsWith(rootUrl + "/test/") ||
                                            url.startsWith(rootUrl + "/tool/") |
                                            url.startsWith(rootUrl + "/web/"))) {
            return true;
          }

          return false;
        }
      });

    final Set<String> newExcludedUrls = collectFolderUrlsToExclude(module, pubspecYamlFile);

    if (oldExcludedUrls.size() != newExcludedUrls.size() || !newExcludedUrls.containsAll(oldExcludedUrls)) {
      ModuleRootModificationUtil.updateExcludedFolders(module, contentRoot, oldExcludedUrls, newExcludedUrls);
    }
  }

  private static Set<String> collectFolderUrlsToExclude(@NotNull final Module module,
                                                        @NotNull final VirtualFile pubspecYamlFile) {
    final THashSet<String> newExcludedPackagesUrls = new THashSet<>();
    final VirtualFile root = pubspecYamlFile.getParent();

    final DartSdk sdk = DartSdk.getDartSdk(module.getProject());
    if (sdk != null && StringUtil.compareVersionNumbers(sdk.getVersion(), "2.0") >= 0) {
      newExcludedPackagesUrls.add(root.getUrl() + "/.dart_tool");
    }

    newExcludedPackagesUrls.add(root.getUrl() + "/.pub");
    newExcludedPackagesUrls.add(root.getUrl() + "/build");

    newExcludedPackagesUrls.addAll(getExcludedPackageSymlinkUrls(module.getProject(), root));

    return newExcludedPackagesUrls;
  }

  public static THashSet<String> getExcludedPackageSymlinkUrls(@NotNull final Project project, @NotNull final VirtualFile dartProjectRoot) {
    final THashSet<String> result = new THashSet<>();
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();

    // java.io.File is used intentionally, VFS may not yet know these files at this point
    if (new File(dartProjectRoot.getPath() + "/packages").isDirectory()) {
      result.add(dartProjectRoot.getUrl() + "/packages");
    }

    final VirtualFile binFolder = dartProjectRoot.findChild("bin");
    if (binFolder != null && binFolder.isDirectory() && fileIndex.isInContent(binFolder)) {
      if (new File(binFolder.getPath() + "/packages").isDirectory()) {
        result.add(binFolder.getUrl() + "/packages");
      }
    }

    appendPackagesFolders(result, dartProjectRoot.findChild("benchmark"), fileIndex);
    appendPackagesFolders(result, dartProjectRoot.findChild("example"), fileIndex);
    appendPackagesFolders(result, dartProjectRoot.findChild("test"), fileIndex);
    appendPackagesFolders(result, dartProjectRoot.findChild("tool"), fileIndex);
    appendPackagesFolders(result, dartProjectRoot.findChild("web"), fileIndex);
    return result;
  }

  private static void appendPackagesFolders(final @NotNull Collection<String> excludedPackagesUrls,
                                            final @Nullable VirtualFile folder,
                                            final @NotNull ProjectFileIndex fileIndex) {
    if (folder == null) return;

    VfsUtilCore.visitChildrenRecursively(folder, new VirtualFileVisitor<Void>() {
      @Override
      @NotNull
      public Result visitFileEx(@NotNull final VirtualFile file) {
        if (!fileIndex.isInContent(file)) {
          return SKIP_CHILDREN;
        }

        if (file.isDirectory()) {
          if ("packages".equals(file.getName())) {
            return SKIP_CHILDREN;
          }
          else {
            // java.io.File is used intentionally, VFS may not yet know these files at this point
            if (new File(file.getPath() + "/packages").isDirectory()) {
              excludedPackagesUrls.add(file.getUrl() + "/packages");
            }
          }
        }

        return CONTINUE;
      }
    });
  }
}
