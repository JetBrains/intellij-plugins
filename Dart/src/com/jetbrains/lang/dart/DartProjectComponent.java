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
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
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
import com.jetbrains.lang.dart.sdk.DartSdkLibraryPresentationProvider;
import com.jetbrains.lang.dart.util.DartUrlResolver;
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

    project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
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
      ensureCorrectDartSdkLibName();
      updateDependenciesOnDartSdkLib();

      DartiumUtil.removeUnsupportedAsyncFlag();

      final Collection<VirtualFile> pubspecYamlFiles =
        FilenameIndex.getVirtualFilesByName(myProject, PUBSPEC_YAML, GlobalSearchScope.projectScope(myProject));

      for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
        final Module module = ModuleUtilCore.findModuleForFile(pubspecYamlFile, myProject);
        if (module != null && FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, module.getModuleContentScope())) {
          excludeBuildAndPackagesFolders(module, pubspecYamlFile);
        }
      }
    });
  }

  private static void ensureCorrectDartSdkLibName() {
    // Make sure "Dart SDK" global lib has correct roots (if possible); delete global libs like "Dart SDK (2)".

    Library correctlyNamedSdkLib = null;
    boolean mainDartSdkLibIsCorrect = false;
    Library incorrectSdkLibWithCorrectRoots = null;
    final List<Library> libsToDelete = new SmartList<>();

    for (final Library library : ApplicationLibraryTable.getApplicationTable().getLibraries()) {
      final String libraryName = library.getName();
      if (libraryName != null && isDartSdkLibName(libraryName)) {
        if (libraryName.equals(DartSdk.DART_SDK_GLOBAL_LIB_NAME)) {
          correctlyNamedSdkLib = library;
        }
        else {
          libsToDelete.add(library);
        }

        if (mainDartSdkLibIsCorrect) {
          continue;
        }

        final VirtualFile[] roots = library.getFiles(OrderRootType.CLASSES);
        if (roots.length == 1 && DartSdkLibraryPresentationProvider.isDartSdkLibRoot(roots[0])) {
          if (libraryName.equals(DartSdk.DART_SDK_GLOBAL_LIB_NAME)) {
            mainDartSdkLibIsCorrect = true;
          }
          else {
            incorrectSdkLibWithCorrectRoots = library;
          }
        }
      }
    }

    if (!mainDartSdkLibIsCorrect && incorrectSdkLibWithCorrectRoots != null || !libsToDelete.isEmpty()) {
      final Library finalCorrectlyNamedSdkLib = correctlyNamedSdkLib;
      final boolean finalCorrectSdkLibExists = mainDartSdkLibIsCorrect;
      final Library finalIncorrectSdkLibWithCorrectRoots = incorrectSdkLibWithCorrectRoots;

      ApplicationManager.getApplication().runWriteAction(() -> {
        if (!finalCorrectSdkLibExists && finalIncorrectSdkLibWithCorrectRoots != null) {
          if (finalCorrectlyNamedSdkLib != null) {
            ApplicationLibraryTable.getApplicationTable().removeLibrary(finalCorrectlyNamedSdkLib);
          }

          libsToDelete.remove(finalIncorrectSdkLibWithCorrectRoots);
          final Library.ModifiableModel libModel = finalIncorrectSdkLibWithCorrectRoots.getModifiableModel();
          libModel.setName(DartSdk.DART_SDK_GLOBAL_LIB_NAME);
          libModel.commit();
        }

        if (!libsToDelete.isEmpty()) {
          final LibraryTable.ModifiableModel model = ApplicationLibraryTable.getApplicationTable().getModifiableModel();
          for (Library library : libsToDelete) {
            model.removeLibrary(library);
          }
          model.commit();
        }
      });
    }
  }

  private static boolean isDartSdkLibName(@NotNull final String libraryName) {
    return libraryName.equals(DartSdk.DART_SDK_GLOBAL_LIB_NAME) ||
           (libraryName.startsWith(DartSdk.DART_SDK_GLOBAL_LIB_NAME + " (") &&
            libraryName.length() == (DartSdk.DART_SDK_GLOBAL_LIB_NAME + " (2)").length() &&
            libraryName.endsWith(")"));
  }

  private void updateDependenciesOnDartSdkLib() {
    // for performance reasons avoid taking write action and modifiable models if not needed
    if (!haveIncorrectModuleDependencies()) return;

    ApplicationManager.getApplication().runWriteAction(() -> {
      final Collection<ModifiableRootModel> modelsToCommit = new SmartList<>();

      for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
        boolean hasCorrectDependency = false;
        boolean needsCorrectDependency = false;
        final List<OrderEntry> orderEntriesToRemove = new SmartList<>();

        final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();

        for (final OrderEntry orderEntry : model.getOrderEntries()) {
          if (orderEntry instanceof LibraryOrderEntry &&
              LibraryTablesRegistrar.APPLICATION_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel())) {
            final String libraryName = ((LibraryOrderEntry)orderEntry).getLibraryName();
            if (libraryName == null) continue;

            if (libraryName.equals(DartSdk.DART_SDK_GLOBAL_LIB_NAME)) {
              hasCorrectDependency = true;
            }
            else if (isDartSdkLibName(libraryName)) {
              needsCorrectDependency = true;
              orderEntriesToRemove.add(orderEntry);
            }
          }
        }

        if (needsCorrectDependency && !hasCorrectDependency || !orderEntriesToRemove.isEmpty()) {
          if (needsCorrectDependency && !hasCorrectDependency) {
            model.addInvalidLibrary(DartSdk.DART_SDK_GLOBAL_LIB_NAME, LibraryTablesRegistrar.APPLICATION_LEVEL);
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

  private boolean haveIncorrectModuleDependencies() {
    for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
      for (final OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
        if (orderEntry instanceof LibraryOrderEntry &&
            LibraryTablesRegistrar.APPLICATION_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel())) {
          final String libraryName = ((LibraryOrderEntry)orderEntry).getLibraryName();
          if (libraryName != null && !libraryName.equals(DartSdk.DART_SDK_GLOBAL_LIB_NAME) && isDartSdkLibName(libraryName)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static void excludeBuildAndPackagesFolders(final @NotNull Module module, final @NotNull VirtualFile pubspecYamlFile) {
    final DartSdk sdk = DartSdk.getDartSdk(module.getProject());
    final boolean excludeRootPackagesFolder = sdk != null && StringUtil.compareVersionNumbers(sdk.getVersion(), "1.12") >= 0;

    final VirtualFile root = pubspecYamlFile.getParent();
    final VirtualFile contentRoot =
      root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
    if (contentRoot == null) return;

    // http://pub.dartlang.org/doc/glossary.html#entrypoint-directory
    // Entrypoint directory: A directory inside your package that is allowed to contain Dart entrypoints.
    // Pub will ensure all of these directories get a “packages” directory, which is needed for “package:” imports to work.
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

          // root packages folder is excluded in case of SDK 1.12 (but do not check excludeRootPackagesFolder here as SDK might be changed right now!)
          if (url.equals(rootUrl + "/packages")) return true;

          // excluded subfolder of 'packages' folder
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

    final Set<String> newExcludedUrls = collectFolderUrlsToExclude(module, pubspecYamlFile, true, excludeRootPackagesFolder);

    if (oldExcludedUrls.size() != newExcludedUrls.size() || !newExcludedUrls.containsAll(oldExcludedUrls)) {
      ModuleRootModificationUtil.updateExcludedFolders(module, contentRoot, oldExcludedUrls, newExcludedUrls);
    }
  }

  public static Set<String> collectFolderUrlsToExclude(@NotNull final Module module,
                                                       @NotNull final VirtualFile pubspecYamlFile,
                                                       final boolean withDotPubAndBuild,
                                                       final boolean withRootPackagesFolder) {
    final THashSet<String> newExcludedPackagesUrls = new THashSet<>();
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(module.getProject()).getFileIndex();
    final VirtualFile root = pubspecYamlFile.getParent();

    if (withDotPubAndBuild) {
      // java.io.File is used here because exclusion is done before FS refresh (in order not to trigger indexing of files that are going to be excluded)
      final File pubFolder = new File(root.getPath() + "/.pub");
      if (pubFolder.isDirectory() || ApplicationManager.getApplication().isUnitTestMode() && root.findChild(".pub") != null) {
        newExcludedPackagesUrls.add(root.getUrl() + "/.pub");
      }

      final File buildFolder = new File(root.getPath() + "/build");
      if (buildFolder.isDirectory() || ApplicationManager.getApplication().isUnitTestMode() && root.findChild("build") != null) {
        newExcludedPackagesUrls.add(root.getUrl() + "/build");
      }
    }

    if (withRootPackagesFolder) {
      newExcludedPackagesUrls.add(root.getUrl() + "/packages");
    }
    else {
      // Folders like packages/PathPackage and packages/ThisProject (where ThisProject is the name specified in pubspec.yaml) are symlinks to local 'lib' folders. Exclude it in order not to have duplicates. Resolve goes to local 'lib' folder.
      // Empty nodes like 'ThisProject (ThisProject/lib)' are added to Project Structure by DartTreeStructureProvider
      final DartUrlResolver resolver = DartUrlResolver.getInstance(module.getProject(), pubspecYamlFile);
      resolver.processLivePackages((packageName, packageDir) -> newExcludedPackagesUrls.add(root.getUrl() + "/packages/" + packageName));
    }

    final VirtualFile binFolder = root.findChild("bin");
    if (binFolder != null && binFolder.isDirectory() && fileIndex.isInContent(binFolder)) {
      newExcludedPackagesUrls.add(binFolder.getUrl() + "/packages");
    }

    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("benchmark"), fileIndex, withRootPackagesFolder);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("example"), fileIndex, withRootPackagesFolder);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("test"), fileIndex, withRootPackagesFolder);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("tool"), fileIndex, withRootPackagesFolder);
    appendPackagesFolders(newExcludedPackagesUrls, root.findChild("web"), fileIndex, withRootPackagesFolder);

    return newExcludedPackagesUrls;
  }

  private static void appendPackagesFolders(final @NotNull Collection<String> excludedPackagesUrls,
                                            final @Nullable VirtualFile folder,
                                            final @NotNull ProjectFileIndex fileIndex,
                                            final boolean withRootPackagesFolder) {
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
          // do not exclude 'packages' folder near another pubspec.yaml file
          else if (withRootPackagesFolder || file.findChild(PUBSPEC_YAML) == null) {
            excludedPackagesUrls.add(file.getUrl() + "/packages");
          }
        }

        return CONTINUE;
      }
    });
  }
}
