// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.intellij.util.io.URLUtil;
import com.jetbrains.lang.dart.DartStartupActivity;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsViewSettings;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartServerRootsHandler {

  @NotNull private final Project myProject;

  private final List<String> myIncludedRoots = new SmartList<>();
  private final List<String> myExcludedRoots = new SmartList<>();

  private DartProblemsViewSettings.ScopedAnalysisMode myScopedAnalysisMode = DartProblemsViewSettings.ScopedAnalysisMode.All;

  @Nullable private VirtualFile myCurrentFile;

  public DartServerRootsHandler(@NotNull final Project project) {
    myProject = project;
  }

  public void setScopedAnalysisModeAndRoots(@NotNull final DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode,
                                            @Nullable final VirtualFile currentFile) {
    myScopedAnalysisMode = scopedAnalysisMode;
    myCurrentFile = currentFile;
    updateRoots();
  }

  public void reset() {
    myIncludedRoots.clear();
    myExcludedRoots.clear();
  }

  public void ensureProjectServed(@NotNull final DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode,
                                  @Nullable final VirtualFile currentFile) {
    assert (myIncludedRoots.isEmpty());
    assert (myExcludedRoots.isEmpty());

    myScopedAnalysisMode = scopedAnalysisMode;
    myCurrentFile = currentFile;

    ProgressManager.getInstance().executeNonCancelableSection(() -> {
      updateRoots();

      final DartAnalysisServerService das = DartAnalysisServerService.getInstance(myProject);
      das.updateCurrentFile();
      das.updateVisibleFiles();
    });

    myProject.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      @Override
      public void rootsChanged(@NotNull final ModuleRootEvent event) {
        updateRoots();
      }
    });
  }

  private void updateRoots() {
    final DartSdk sdk = DartSdk.getDartSdk(myProject);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
      DartAnalysisServerService.getInstance(myProject).stopServer();
    }

    final List<String> newIncludedRoots = new SmartList<>();
    final List<String> newExcludedRoots = new SmartList<>();
    final boolean isPackageScopedAnalysis = myScopedAnalysisMode == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage;

    if (sdk != null) {
      if (isPackageScopedAnalysis) {
        // Set the included root to be the parent of the enclosing dart package
        // This means either:
        // - the parent directory of the enclosing lib/ or bin/ directory
        // - or, as a fall back, the parent directory of the current file
        if (myCurrentFile != null) {
          String directoryStr = getEnclosingDartPackageDirectory(myCurrentFile);
          if (directoryStr != null) {
            newIncludedRoots.add(FileUtil.toSystemDependentName(directoryStr));
          }
          else {
            return;
          }
        }
      }

      @SuppressWarnings("ConstantConditions") final String dotIdeaPath = PathUtil.getParentPath(myProject.getProjectFilePath());
      if (dotIdeaPath.endsWith("/.idea")) {
        newExcludedRoots.add(FileUtil.toSystemDependentName(dotIdeaPath));
      }

      for (Module module : DartSdkLibUtil.getModulesWithDartSdkEnabled(myProject)) {
        final Set<String> excludedPackageSymlinkUrls = getExcludedPackageSymlinkUrls(module);

        for (ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
          final String contentEntryUrl = contentEntry.getUrl();
          if (contentEntryUrl.startsWith(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) {
            if (!isPackageScopedAnalysis) {
              // All/ default Scoped Analysis settings
              newIncludedRoots.add(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentEntryUrl)));
            }
            for (String excludedUrl : contentEntry.getExcludeFolderUrls()) {
              if (excludedUrl.startsWith(contentEntryUrl) && !excludedPackageSymlinkUrls.contains(excludedUrl)) {
                newExcludedRoots.add(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(excludedUrl)));
              }
            }
          }
        }
      }
    }

    // Only call DASS.updateRoots(..,..) if there has been some change; update the local cache.
    if (!myIncludedRoots.equals(newIncludedRoots) || !myExcludedRoots.equals(newExcludedRoots)) {
      myIncludedRoots.clear();
      myExcludedRoots.clear();

      if (DartAnalysisServerService.getInstance(myProject).updateRoots(newIncludedRoots, newExcludedRoots)) {
        myIncludedRoots.addAll(newIncludedRoots);
        myExcludedRoots.addAll(newExcludedRoots);
      }
    }
  }

  boolean isInIncludedRoots(@Nullable final VirtualFile vFile) {
    if (vFile == null) return false;

    final String currentIncludedRoot = myIncludedRoots.size() > 0 ? myIncludedRoots.get(0) : "";

    if (currentIncludedRoot == null || currentIncludedRoot.isEmpty()) return false;

    if (myScopedAnalysisMode == DartProblemsViewSettings.ScopedAnalysisMode.All) {
      final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
      if (fileIndex.isInLibraryClasses(vFile)) return true;

      final Module module = fileIndex.getModuleForFile(vFile);
      return (module != null && DartSdkLibUtil.isDartSdkEnabled(module));
    }
    else if (myScopedAnalysisMode == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage) {
      String directory = getEnclosingDartPackageDirectory(vFile);
      if (directory != null) {
        return FileUtil.toSystemDependentName(directory).equals(currentIncludedRoot);
      }
    }
    return false;
  }

  @NotNull
  public static String getEnclosingDirectory(@NotNull final VirtualFile vFile) {
    return vFile.getParent().getPath();
  }

  /**
   * This method is only called when scoped analysis is true.
   * <p>
   * Null is returned if the passed {@link VirtualFile} returns true from {@link ProjectFileIndex#isInLibraryClasses(VirtualFile)}.
   */
  @Nullable
  public String getEnclosingDartPackageDirectory(@NotNull final VirtualFile vFile) {

    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    if (projectFileIndex.isInLibraryClasses(vFile)) {
      return null;
    }

    // TODO(jwren) follow up to extract method with duplicate code (from DartProblemsPresentationHelper)
    final VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                                ? DartBuildFileUtil.findPackageRootBuildFile(myProject, vFile, true)
                                : PubspecYamlUtil.findPubspecYamlFile(myProject, vFile);

    // temp var to make sure that value is initialized
    final VirtualFile packageRoot;

    if (pubspec == null) {
      packageRoot = projectFileIndex.getContentRootForFile(vFile, false);
    }
    else {
      packageRoot = pubspec.getParent();
    }

    if (packageRoot != null) {
      return packageRoot.getPath();
    }

    // As a fall-back, return the enclosing directory
    return getEnclosingDirectory(vFile);
  }

  private static Set<String> getExcludedPackageSymlinkUrls(@NotNull final Module module) {
    final Set<String> result = new THashSet<>();

    final Collection<VirtualFile> pubspecYamlFiles =
      FilenameIndex.getVirtualFilesByName(module.getProject(), PUBSPEC_YAML, module.getModuleContentScope());

    for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
      result.addAll(DartStartupActivity.getExcludedPackageSymlinkUrls(module.getProject(), pubspecYamlFile));
    }

    return result;
  }
}
