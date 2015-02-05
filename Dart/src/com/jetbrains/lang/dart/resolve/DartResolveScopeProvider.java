package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.scopes.LibraryScope;
import com.intellij.openapi.module.impl.scopes.LibraryScopeBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ResolveScopeProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.PairConsumer;
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
    if (file.getFileType() != DartFileType.INSTANCE) return null;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final DartSdk sdk = DartSdk.getDartSdk(project);

    if (fileIndex.isInLibraryClasses(file)) {
      if (sdk != null && file.getPath().startsWith(sdk.getHomePath() + "/")) {
        return getDartSdkResolveScope(project, sdk);
      }

      return getLibraryAndSdkScope(project, file, sdk);
    }

    final Module module = fileIndex.getModuleForFile(file);
    if (module == null) {
      return null;
    }

    VirtualFile subdir = null;
    VirtualFile dir = file.getParent();

    while (dir != null && fileIndex.isInContent(dir)) {
      final VirtualFile pubspecFile = dir.findChild(PubspecYamlUtil.PUBSPEC_YAML);
      if (pubspecFile != null) {
        return getDartResolveScope(module, pubspecFile, subdir);
      }
      subdir = dir;
      dir = dir.getParent();
    }

    // no pubspec.yaml => return module content scope + libs + SDK
    return module.getModuleContentWithDependenciesScope().union(module.getModuleWithLibrariesScope());
  }

  @Nullable
  private static GlobalSearchScope getDartSdkResolveScope(@NotNull final Project project, @NotNull final DartSdk sdk) {
    return CachedValuesManager.getManager(project).getCachedValue(project, new CachedValueProvider<GlobalSearchScope>() {
      @Override
      public Result<GlobalSearchScope> compute() {
        final Library library = ApplicationLibraryTable.getApplicationTable().getLibraryByName(sdk.getGlobalLibName());
        final LibraryScope scope = library == null ? null : new LibraryScope(project, library);
        return new Result<GlobalSearchScope>(scope, ProjectRootManager.getInstance(project));
      }
    });
  }

  private static GlobalSearchScope getLibraryAndSdkScope(@NotNull final Project project,
                                                         @NotNull final VirtualFile file,
                                                         @Nullable final DartSdk sdk) {
    return CachedValuesManager.getManager(project).getCachedValue(project, new CachedValueProvider<GlobalSearchScope>() {
      @Override
      public Result<GlobalSearchScope> compute() {
        final GlobalSearchScope sdkScope = sdk == null ? null : getDartSdkResolveScope(project, sdk);

        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        final Set<VirtualFile> roots = new THashSet<VirtualFile>();

        for (OrderEntry orderEntry : fileIndex.getOrderEntriesForFile(file)) {
          Collections.addAll(roots, orderEntry.getFiles(OrderRootType.CLASSES));
        }

        final LibraryScopeBase libraryScope = new DartLibraryScope(project, roots);
        final GlobalSearchScope scope = sdkScope != null ? sdkScope.union(libraryScope)
                                                         : libraryScope;

        return new Result<GlobalSearchScope>(scope, ProjectRootManager.getInstance(project));
      }
    });
  }

  private static GlobalSearchScope getDartResolveScope(@NotNull final Module module,
                                                       @NotNull final VirtualFile pubspecFile,
                                                       @Nullable final VirtualFile subdir) {
    final Project project = module.getProject();
    final UserDataHolder dataHolder = subdir != null ? subdir : pubspecFile;

    return CachedValuesManager.getManager(project).getCachedValue(dataHolder, new CachedValueProvider<GlobalSearchScope>() {
      @Override
      public Result<GlobalSearchScope> compute() {
        final Collection<VirtualFile> pathPackageRoots = getPathPackageRoots(pubspecFile);

        final VirtualFile dartRoot = pubspecFile.getParent();

        final GlobalSearchScope scope;

        if (subdir == null || "test".equals(subdir.getName())) {
          // the biggest scope
          scope = createDirectoriesScope(project, pathPackageRoots, dartRoot);
        }
        else if ("lib".equals(subdir.getName()) || PACKAGES_FOLDER_NAME.equals(subdir.getName())) {
          // the smallest scope
          scope = createDirectoriesScope(project, pathPackageRoots, dartRoot.findChild("lib"), dartRoot.findChild(PACKAGES_FOLDER_NAME));
        }
        else {
          // Scope restricted by the project root subfolder. Isn't it too strict to resolve only within subdir? Can there be references, say, from benchmark to tool?
          scope =
            createDirectoriesScope(project, pathPackageRoots, subdir, dartRoot.findChild("lib"), dartRoot.findChild(PACKAGES_FOLDER_NAME));
        }

        final GlobalSearchScope scopeWithLibs =
          scope.intersectWith(module.getModuleContentScope()).union(getModuleLibrariesClassesScope(module));
        return new Result<GlobalSearchScope>(scopeWithLibs, PsiModificationTracker.MODIFICATION_COUNT);
      }
    });
  }

  private static Collection<VirtualFile> getPathPackageRoots(final VirtualFile pubspecFile) {
    final Collection<VirtualFile> result = new SmartList<VirtualFile>();

    PubspecYamlUtil.processPathPackages(pubspecFile, new PairConsumer<String, VirtualFile>() {
      @Override
      public void consume(final String packageName, final VirtualFile packageDir) {
        result.add(packageDir);
      }
    });

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
    return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, new CachedValueProvider<GlobalSearchScope>() {
      @Override
      public Result<GlobalSearchScope> compute() {
        final Set<VirtualFile> roots = new THashSet<VirtualFile>();

        for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
          if (orderEntry instanceof LibraryOrderEntry) {
            ContainerUtil.addAll(roots, ((LibraryOrderEntry)orderEntry).getRootFiles(OrderRootType.CLASSES));
          }
        }

        return new Result<GlobalSearchScope>(new DartLibraryScope(module.getProject(), roots),
                                             ProjectRootManager.getInstance(module.getProject()));
      }
    });
  }

  private static class DartLibraryScope extends LibraryScopeBase {
    public DartLibraryScope(@NotNull final Project project, @NotNull final Set<VirtualFile> roots) {
      super(project, roots.toArray(new VirtualFile[roots.size()]), VirtualFile.EMPTY_ARRAY);
    }
  }
}
