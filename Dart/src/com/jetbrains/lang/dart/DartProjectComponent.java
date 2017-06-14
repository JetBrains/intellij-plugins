package com.jetbrains.lang.dart;

import com.intellij.ProjectTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.ModifiableModelCommitter;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartProjectComponent extends AbstractProjectComponent {

  private SimpleModificationTracker myProjectRootsModificationTracker = new SimpleModificationTracker();

  protected DartProjectComponent(@NotNull final Project project) {
    super(project);

    VirtualFileManager.getInstance().addVirtualFileListener(new DartFileListener(project), project);

    project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      @Override
      public void rootsChanged(ModuleRootEvent event) {
        myProjectRootsModificationTracker.incModificationCount();

        if (!Registry.is("dart.projects.without.pubspec", false)) {
          DartFileListener.scheduleDartPackageRootsUpdate(myProject);
        }
      }
    });
  }

  @NotNull
  public static ModificationTracker getProjectRootsModificationTracker(@NotNull final Project project) {
    if (project.isDefault()) {
      return ModificationTracker.NEVER_CHANGED;
    }

    // standard ProjectRootManager (that is a ModificationTracker itself) doesn't work as its modificationCount is not incremented when library root is deleted
    final DartProjectComponent component = project.getComponent(DartProjectComponent.class);
    assert component != null;
    return component.myProjectRootsModificationTracker;
  }

  public void projectOpened() {
    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(() -> {
      removeGlobalDartSdkLib();

      convertOrderEntriesTargetingGlobalDartSdkLib();

      DartiumUtil.resetDartiumFlags();

      final Collection<VirtualFile> pubspecYamlFiles =
        FilenameIndex.getVirtualFilesByName(myProject, PUBSPEC_YAML, GlobalSearchScope.projectScope(myProject));

      for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
        final Module module = ModuleUtilCore.findModuleForFile(pubspecYamlFile, myProject);
        if (module != null && FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, module.getModuleContentScope())) {
          excludeBuildAndPackagesFolders(module, pubspecYamlFile);
        }
      }

      DartFileListener.scheduleDartPackageRootsUpdate(myProject);
    });
  }

  private void removeGlobalDartSdkLib() {
    for (final Library library : ApplicationLibraryTable.getApplicationTable().getLibraries()) {
      if (DartSdk.DART_SDK_LIB_NAME.equals(library.getName())) {
        final DartSdk oldGlobalSdk = DartSdk.getSdkByLibrary(library);
        if (oldGlobalSdk != null) {
          DartSdkUtil.updateKnownSdkPaths(myProject, oldGlobalSdk.getHomePath());
        }

        ApplicationManager.getApplication().runWriteAction(() -> ApplicationLibraryTable.getApplicationTable().removeLibrary(library));
        return;
      }
    }
  }

  private void convertOrderEntriesTargetingGlobalDartSdkLib() {
    final DartSdk correctSdk = DartSdk.getDartSdk(myProject);
    if (correctSdk != null) return; // already converted

    // for performance reasons avoid taking write action and modifiable models if not needed
    if (!hasIncorrectModuleDependencies()) return;

    final String sdkPath = DartSdkUtil.getFirstKnownDartSdkPath();
    if (sdkPath == null) return;

    ApplicationManager.getApplication().runWriteAction(() -> {
      DartSdkLibUtil.ensureDartSdkConfigured(myProject, sdkPath);

      final Collection<ModifiableRootModel> modelsToCommit = new SmartList<>();

      for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
        boolean hasCorrectDependency = false;
        boolean needsCorrectDependency = false;
        final List<OrderEntry> orderEntriesToRemove = new SmartList<>();

        final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();

        for (final OrderEntry orderEntry : model.getOrderEntries()) {
          if (isOldGlobalDartSdkLibEntry(orderEntry)) {
            needsCorrectDependency = true;
            orderEntriesToRemove.add(orderEntry);
          }
          else if (DartSdkLibUtil.isDartSdkOrderEntry(orderEntry)) {
            hasCorrectDependency = true;
          }
        }


        if (needsCorrectDependency && !hasCorrectDependency || !orderEntriesToRemove.isEmpty()) {
          if (needsCorrectDependency && !hasCorrectDependency) {
            model.addInvalidLibrary(DartSdk.DART_SDK_LIB_NAME, LibraryTablesRegistrar.PROJECT_LEVEL);
          }

          for (OrderEntry entry : orderEntriesToRemove) {
            model.removeOrderEntry(entry);
          }

          modelsToCommit.add(model);
        }
        else {
          model.dispose();
        }
      }

      commitModifiableModels(myProject, modelsToCommit);
    });
  }

  public static void commitModifiableModels(@NotNull final Project project, @NotNull final Collection<ModifiableRootModel> modelsToCommit) {
    if (!modelsToCommit.isEmpty()) {
      try {
        ModifiableModelCommitter.multiCommit(modelsToCommit, ModuleManager.getInstance(project).getModifiableModel());
      }
      finally {
        for (ModifiableRootModel model : modelsToCommit) {
          if (!model.isDisposed()) {
            model.dispose();
          }
        }
      }
    }
  }

  private boolean hasIncorrectModuleDependencies() {
    for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
      for (final OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
        if (isOldGlobalDartSdkLibEntry(orderEntry)) return true;
      }
    }

    return false;
  }

  private static boolean isOldGlobalDartSdkLibEntry(OrderEntry orderEntry) {
    if (orderEntry instanceof LibraryOrderEntry &&
        LibraryTablesRegistrar.APPLICATION_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
        DartSdk.DART_SDK_LIB_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName())) {
      return true;
    }
    return false;
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

    final Collection<String> oldExcludedUrls =
      ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(), new Condition<String>() {
        final String rootUrl = root.getUrl();

        public boolean value(final String url) {
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

    VfsUtilCore.visitChildrenRecursively(folder, new VirtualFileVisitor() {
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
