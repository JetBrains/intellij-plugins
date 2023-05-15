package com.jetbrains.plugins.meteor.ide.action;

import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.plugins.meteor.MeteorFacade;
import icons.MeteorIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static com.jetbrains.plugins.meteor.ide.action.MeteorPackagesUtil.PACKAGES_FOLDER;

public final class MeteorSyntheticLibraryProvider extends AdditionalLibraryRootsProvider implements JSSyntheticLibraryProvider {
  @Override
  public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
    if (!MeteorFacade.getInstance().isMeteorProject(project)) {
      return ContainerUtil.emptyList();
    }


    return getLibraries(project);
  }

  public static @NotNull SyntheticLibrary.ExcludeFileCondition createExcludeConditions() {
    return new SyntheticLibrary.ExcludeFileCondition() {
      @Override
      public boolean shouldExclude(boolean isDir,
                                   @NotNull String filename,
                                   @NotNull BooleanSupplier isRoot,
                                   @NotNull BooleanSupplier isStrictRootChild,
                                   @NotNull BooleanSupplier hasParentNotGrandparent) {
        if (isDir) {
          return MeteorPackagesUtil.EXCLUDED_FOLDERS.contains(filename) && !isRoot.getAsBoolean();
        }

        String extension = FileUtilRt.getExtension(filename);
        return !ArrayUtil.contains("." + extension, MeteorPackagesUtil.EXTENSIONS);
      }
    };
  }

  @Override
  public @NotNull Collection<VirtualFile> getRootsToWatch(@NotNull Project project) {
    return getRoots(project);
  }

  public static @NotNull Collection<VirtualFile> getRoots(@NotNull Project project) {
    if (!MeteorFacade.getInstance().isMeteorProject(project)) return ContainerUtil.emptyList();

    return getLibraries(project).stream().flatMap(el -> el.getSourceRoots().stream()).collect(Collectors.toList());
  }

  private static class MeteorSyntheticLibrary extends SyntheticLibrary implements ItemPresentation {
    private final @NlsSafe String myLibName;
    private final @NotNull Collection<VirtualFile> myRoots;

    MeteorSyntheticLibrary(@NlsSafe String libName, @NotNull Collection<VirtualFile> sourceRoots) {
      super("MeteorSyntheticLibrary::" + libName, createExcludeConditions());
      myLibName = libName;
      myRoots = sourceRoots;
    }

    @Override
    public @NotNull Collection<VirtualFile> getSourceRoots() {
      return myRoots;
    }

    @Override
    public @NotNull Set<VirtualFile> getExcludedRoots() {
      return super.getExcludedRoots();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MeteorSyntheticLibrary library = (MeteorSyntheticLibrary)o;
      return Objects.equals(myLibName, library.myLibName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(myLibName, getClass().hashCode());
    }

    @Override
    public @Nullable String getPresentableText() {
      return myLibName;
    }

    @Override
    public @Nullable String getLocationString() {
      return "meteor";
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
      return MeteorIcons.Meteor2;
    }
  }

  public static @NotNull Collection<SyntheticLibrary> getLibraries(@NotNull Project project) {
    final Collection<MeteorImportPackagesAsExternalLib.CodeType> codes = MeteorPackagesUtil.getCodes(project);
    if (codes.isEmpty()) return ContainerUtil.emptyList();

    try {
      List<MeteorPackagesUtil.PackageWrapper> wrappers = getWrappers(project);

      ArrayList<SyntheticLibrary> libraries = new ArrayList<>(wrappers.size());
      VirtualFile packagesFolder = getPackagesFolder(project);
      for (MeteorPackagesUtil.PackageWrapper wrapper : wrappers) {
        VirtualFile versionedPackage = MeteorPackagesUtil.getVersionPackage(packagesFolder, wrapper);
        List<VirtualFile> roots = new SmartList<>();
        for (MeteorImportPackagesAsExternalLib.CodeType type : codes) {
          if (versionedPackage == null) continue;

          ContainerUtil.addIfNotNull(roots, getRootForPackage(versionedPackage, type));
        }

        if (!roots.isEmpty()) {
          libraries.add(new MeteorSyntheticLibrary(wrapper.getOriginalName(), roots));
        }
      }

      return libraries;
    }
    catch (ProcessCanceledException e) {
      throw e;
    }
    catch (Exception e) {
      MeteorPackagesUtil.LOG.error(e.getMessage(), e);
    }

    return ContainerUtil.emptyList();
  }

  public static @NotNull List<MeteorPackagesUtil.PackageWrapper> getWrappers(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      final VirtualFile dotMeteorVirtualFile = MeteorPackagesUtil.getDotMeteorVirtualFile(project, null, true);
      if (dotMeteorVirtualFile == null) {
        return emptyResult();
      }
      final VirtualFile versionsFile = MeteorPackagesUtil.getVersionsFile(project, dotMeteorVirtualFile);
      if (versionsFile == null) {
        return emptyResult();
      }

      VirtualFile packagesFolder = getPackagesFolder(project);
      if (packagesFolder == null || !versionsFile.isValid()) {
        return emptyResult();
      }

      // force project because load text can lead to SOE because of the recursive project calc
      String text;
      try (AccessToken ignored = ProjectLocator.withPreferredProject(versionsFile, project)) {
        text = VfsUtilCore.loadText(versionsFile);
      }
      catch (IOException e) {
        //ignore
        return emptyResult();
      }

      String[] strings = StringUtil.splitByLines(text, true);

      return CachedValueProvider.Result.create(ContainerUtil.mapNotNull(strings, rawVersionsLine -> {
        String[] split = rawVersionsLine.split("@");
        if (split.length != 2) {
          return null;
        }
        return new MeteorPackagesUtil.PackageWrapper(split);
      }), versionsFile);
    });
  }

  private static @NotNull CachedValueProvider.Result<List<MeteorPackagesUtil.PackageWrapper>> emptyResult() {
    return CachedValueProvider.Result.create(ContainerUtil.emptyList(), PsiModificationTracker.MODIFICATION_COUNT);
  }

  public static @Nullable VirtualFile getPackagesFolder(@NotNull Project project) {
    final String pathToMeteorGlobal = MeteorPackagesUtil.getPathToGlobalMeteorRoot(project);

    String packagesPath = FileUtil.toCanonicalPath(pathToMeteorGlobal) + "/" + PACKAGES_FOLDER;

    return LocalFileFinder.findFile(packagesPath);
  }

  private static @Nullable VirtualFile getRootForPackage(@NotNull VirtualFile folder,
                                                         @NotNull MeteorImportPackagesAsExternalLib.CodeType type) {
    if (!folder.isValid()) return null;
    VirtualFile requiredFolder = folder.findChild(type.getFolder());
    if (requiredFolder == null || !requiredFolder.isValid()) return null;

    if (type == MeteorImportPackagesAsExternalLib.CodeType.NPM) {
      return requiredFolder.findChild("node_modules");
    }

    return requiredFolder;
  }
}
