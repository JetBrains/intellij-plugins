package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.scopes.LibraryScope;
import com.intellij.openapi.module.impl.scopes.LibraryScopeBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveScopeProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;

public class DartResolveScopeProvider extends ResolveScopeProvider {

  @Nullable
  @Override
  public GlobalSearchScope getResolveScope(@NotNull final VirtualFile file, @NotNull final Project project) {
    return getDartScope(project, file, false);
  }

  /**
   * @param strict Strict scope respects <a href="https://www.dartlang.org/tools/pub/package-layout.html">Dart Package Layout Conventions</a>,
   *               not strict includes the whole Dart project and Path Packages it depends on.
   *               But both strict and not strict scope for file in 'packages' folder includes only 'packages', 'lib' and Path Packages folders.
   */
  @Nullable
  public static GlobalSearchScope getDartScope(@NotNull final Project project, @NotNull final VirtualFile file, boolean strict) {
    if (file.getFileType() != DartFileType.INSTANCE) return null;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final DartSdk sdk = DartSdk.getDartSdk(project);

    if (fileIndex.isInLibraryClasses(file) && !fileIndex.isInContent(file)) {
      if (sdk != null && file.getPath().startsWith(sdk.getHomePath() + "/")) {
        return getDartSdkResolveScope(project);
      }

      return getLibraryAndSdkScope(project, file, sdk);
    }

    final Module module = fileIndex.getModuleForFile(file);
    if (module == null) {
      return null;
    }

    VirtualFile contextSubdir = null;
    VirtualFile dir = file.getParent();

    while (dir != null && fileIndex.isInContent(dir)) {
      final VirtualFile pubspecFile = dir.findChild(PubspecYamlUtil.PUBSPEC_YAML);
      if (pubspecFile != null) {
        final boolean inPackages = contextSubdir != null && PACKAGES_FOLDER_NAME.equals(contextSubdir.getName());
        return getDartResolveScope(module, pubspecFile, strict || inPackages ? contextSubdir : null);
      }
      contextSubdir = dir;
      dir = dir.getParent();
    }

    // no pubspec.yaml => return module content scope + libs + SDK
    return module.getModuleContentWithDependenciesScope().union(module.getModuleWithLibrariesScope());
  }

  @Nullable
  private static GlobalSearchScope getDartSdkResolveScope(@NotNull final Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      final Library library = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraryByName(DartSdk.DART_SDK_LIB_NAME);
      final LibraryScope scope = library == null ? null : new LibraryScope(project, library);
      return new CachedValueProvider.Result<GlobalSearchScope>(scope, ProjectRootManager.getInstance(project));
    });
  }

  private static GlobalSearchScope getLibraryAndSdkScope(@NotNull final Project project,
                                                         @NotNull final VirtualFile file,
                                                         @Nullable final DartSdk sdk) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      final GlobalSearchScope sdkScope = sdk == null ? null : getDartSdkResolveScope(project);

      final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
      final Set<VirtualFile> roots = new THashSet<>();

      for (OrderEntry orderEntry : fileIndex.getOrderEntriesForFile(file)) {
        Collections.addAll(roots, orderEntry.getFiles(OrderRootType.CLASSES));
      }

      final LibraryScopeBase libraryScope = new DartLibraryScope(project, roots);
      final GlobalSearchScope scope = sdkScope != null ? sdkScope.union(libraryScope)
                                                       : libraryScope;

      return new CachedValueProvider.Result<>(scope, ProjectRootManager.getInstance(project));
    });
  }

  @Nullable
  private static GlobalSearchScope getDartResolveScope(@NotNull final Module module,
                                                       @NotNull final VirtualFile pubspecFile,
                                                       @Nullable final VirtualFile contextSubdir) {
    final Project project = module.getProject();
    final UserDataHolder dataHolder = contextSubdir != null ? PsiManager.getInstance(project).findDirectory(contextSubdir)
                                                            : PsiManager.getInstance(project).findFile(pubspecFile);
    if (dataHolder == null) {
      return null; // unlikely as contextSubdir and pubspecFile are checked to be in project
    }

    return CachedValuesManager.getManager(project).getCachedValue(dataHolder, () -> {
      final Collection<VirtualFile> pathPackageRoots = getPathPackageRoots(module.getProject(), pubspecFile);

      final VirtualFile dartRoot = pubspecFile.getParent();

      final GlobalSearchScope scope;

      if (contextSubdir == null || "test".equals(contextSubdir.getName())) {
        // the biggest scope
        scope = createDirectoriesScope(project, pathPackageRoots, dartRoot);
      }
      else if ("lib".equals(contextSubdir.getName()) || PACKAGES_FOLDER_NAME.equals(contextSubdir.getName())) {
        // the smallest scope
        scope = createDirectoriesScope(project, pathPackageRoots, dartRoot.findChild("lib"), dartRoot.findChild(PACKAGES_FOLDER_NAME));
      }
      else {
        scope = createDirectoriesScope(project, pathPackageRoots, contextSubdir, dartRoot.findChild("lib"),
                                       dartRoot.findChild(PACKAGES_FOLDER_NAME));
      }

      final GlobalSearchScope scopeWithLibs =
        scope.intersectWith(GlobalSearchScope.projectScope(project)).union(getModuleLibrariesClassesScope(module));
      return new CachedValueProvider.Result<>(scopeWithLibs, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  private static Collection<VirtualFile> getPathPackageRoots(@NotNull final Project project, @NotNull final VirtualFile pubspecFile) {
    final Collection<VirtualFile> result = new SmartList<>();

    PubspecYamlUtil.processInProjectPathPackagesRecursively(project, pubspecFile, (packageName, packageDir) -> result.add(packageDir));

    return result;
  }

  @NotNull
  private static GlobalSearchScope createDirectoriesScope(@NotNull final Project project,
                                                          @NotNull final Collection<VirtualFile> pathPackageRoots,
                                                          @NotNull final VirtualFile... dirs) {
    GlobalSearchScope scope = null;
    for (VirtualFile dir : dirs) {
      if (dir != null && dir.isDirectory()) {
        if (scope == null) {
          scope = GlobalSearchScopesCore.directoryScope(project, dir, true);
        }
        else {
          scope = scope.union(GlobalSearchScopesCore.directoryScope(project, dir, true));
        }
      }
    }

    assert scope != null;

    for (VirtualFile packageRoot : pathPackageRoots) {
      scope = scope.union(GlobalSearchScopesCore.directoryScope(project, packageRoot, true));
    }

    return scope;
  }

  private static GlobalSearchScope getModuleLibrariesClassesScope(@NotNull final Module module) {
    return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> {
      final Set<VirtualFile> roots = new THashSet<>();

      for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
        if (orderEntry instanceof LibraryOrderEntry) {
          ContainerUtil.addAll(roots, ((LibraryOrderEntry)orderEntry).getRootFiles(OrderRootType.CLASSES));
        }
      }

      return new CachedValueProvider.Result<GlobalSearchScope>(new DartLibraryScope(module.getProject(), roots),
                                                               ProjectRootManager.getInstance(module.getProject()));
    });
  }

  private static class DartLibraryScope extends LibraryScopeBase {
    DartLibraryScope(@NotNull final Project project, @NotNull final Set<VirtualFile> roots) {
      super(project, roots.toArray(VirtualFile.EMPTY_ARRAY), VirtualFile.EMPTY_ARRAY);
    }
  }
}
