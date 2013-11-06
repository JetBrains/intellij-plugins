package com.jetbrains.lang.dart;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class DartProjectComponent extends AbstractProjectComponent {

  protected DartProjectComponent(final Project project) {
    super(project);
  }

  public void projectOpened() {
    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        final Collection<VirtualFile> pubspecYamlFiles =
          FilenameIndex.getVirtualFilesByName(myProject, "pubspec.yaml", GlobalSearchScope.projectScope(myProject));

        for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
          final Module module = ModuleUtilCore.findModuleForFile(pubspecYamlFile, myProject);
          if (module != null) {
            excludePackagesFolders(module, pubspecYamlFile);
          }
        }
      }
    });
  }

  public static void excludePackagesFolders(final Module module, final VirtualFile pubspecYamlFile) {
    final VirtualFile root = pubspecYamlFile.getParent();

    root.refresh(false, true);

    // http://pub.dartlang.org/doc/glossary.html#entrypoint-directory
    // Entrypoint directory: A directory inside your package that is allowed to contain Dart entrypoints.
    // Pub will ensure all of these directories get a “packages” directory, which is needed for “package:” imports to work.
    // Pub has a whitelist of these directories: benchmark, bin, example, test, tool, and web.
    // Any subdirectories of those (except bin) may also contain entrypoints.
    //
    // the same can be seen in the pub tool source code: [repo root]/sdk/lib/_internal/pub/lib/src/entrypoint.dart

    final Collection<VirtualFile> foldersToExclude = new ArrayList<VirtualFile>();
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(module.getProject()).getFileIndex();

    final VirtualFile packagesFolder = VfsUtilCore.findRelativeFile("bin/packages", root);
    if (packagesFolder != null && packagesFolder.isDirectory()) {
      if (fileIndex.isInContent(packagesFolder)) {
        foldersToExclude.add(packagesFolder);
      }
    }

    appendPackagesFolders(foldersToExclude, root.findChild("benchmark"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("example"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("test"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("tool"), fileIndex);
    appendPackagesFolders(foldersToExclude, root.findChild("web"), fileIndex);

    if (!foldersToExclude.isEmpty()) {
      excludeFoldersInWriteAction(module, foldersToExclude);
    }
  }

  private static void appendPackagesFolders(final Collection<VirtualFile> foldersToExclude,
                                            final @Nullable VirtualFile folder,
                                            final ProjectFileIndex fileIndex) {
    if (folder == null) return;

    VfsUtilCore.visitChildrenRecursively(folder, new VirtualFileVisitor() {
      @NotNull
      public Result visitFileEx(@NotNull final VirtualFile file) {
        if (file.isDirectory() && "packages".equals(file.getName())) {
          if (fileIndex.isInContent(file)) {
            foldersToExclude.add(file);
          }
          return SKIP_CHILDREN;
        }
        else {
          return CONTINUE;
        }
      }
    });
  }

  private static void excludeFoldersInWriteAction(final Module module, final Collection<VirtualFile> foldersToExclude) {
    final VirtualFile firstItem = ContainerUtil.getFirstItem(foldersToExclude);
    if (firstItem == null) return;

    final VirtualFile contentRoot = ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(firstItem);
    if (contentRoot == null) return;

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
        try {
          for (final ContentEntry contentEntry : modifiableModel.getContentEntries()) {
            if (contentEntry.getFile() == contentRoot) {
              for (VirtualFile packagesFolder : foldersToExclude) {
                contentEntry.addExcludeFolder(packagesFolder);
              }
              break;
            }
          }
          modifiableModel.commit();
        }
        catch (Exception e) {
          modifiableModel.dispose();
        }
      }
    });
  }
}
