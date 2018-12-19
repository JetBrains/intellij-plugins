// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.intellij.util.io.URLUtil;
import com.jetbrains.lang.dart.DartStartupActivity;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
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

  public DartServerRootsHandler(@NotNull final Project project) {
    myProject = project;
  }

  public void reset() {
    myIncludedRoots.clear();
    myExcludedRoots.clear();
  }

  void ensureProjectServed() {
    assert (myIncludedRoots.isEmpty());
    assert (myExcludedRoots.isEmpty());

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

  void updateRoots() {
    final DartSdk sdk = DartSdk.getDartSdk(myProject);
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
      DartAnalysisServerService.getInstance(myProject).stopServer();
      return;
    }

    final List<String> newIncludedRoots = new SmartList<>();
    final List<String> newExcludedRoots = new SmartList<>();

    final DartProblemsView problemsView = DartProblemsView.getInstance(myProject);
    final boolean isPackageScopedAnalysis = problemsView.getScopeAnalysisMode() == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage;

    if (isPackageScopedAnalysis) {
      final VirtualFile currentFile = problemsView.getCurrentFile();
      if (currentFile == null || ProjectFileIndex.getInstance(myProject).isInLibraryClasses(currentFile)) {
        return; // keep server roots as is until another file is open
      }

      newIncludedRoots.add(FileUtil.toSystemDependentName(getEnclosingDartPackageDirectory(currentFile)));
    }

    final String dotIdeaPath = PathUtil.getParentPath(StringUtil.notNullize(myProject.getProjectFilePath()));
    if (dotIdeaPath.endsWith("/.idea")) {
      newExcludedRoots.add(FileUtil.toSystemDependentName(dotIdeaPath));
    }

    for (Module module : DartSdkLibUtil.getModulesWithDartSdkEnabled(myProject)) {
      final Set<String> excludedPackageSymlinkUrls = getExcludedPackageSymlinkUrls(module);

      for (ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
        final String contentEntryUrl = contentEntry.getUrl();
        if (contentEntryUrl.startsWith(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) {
          if (!isPackageScopedAnalysis) {
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

    if (!myIncludedRoots.equals(newIncludedRoots) || !myExcludedRoots.equals(newExcludedRoots)) {
      myIncludedRoots.clear();
      myExcludedRoots.clear();

      if (DartAnalysisServerService.getInstance(myProject).setAnalysisRoots(newIncludedRoots, newExcludedRoots)) {
        myIncludedRoots.addAll(newIncludedRoots);
        myExcludedRoots.addAll(newExcludedRoots);
      }
    }
  }

  boolean isInIncludedRoots(@Nullable final VirtualFile vFile) {
    if (vFile == null) return false;

    final DartProblemsViewSettings.ScopedAnalysisMode scopedAnalysisMode = DartProblemsView.getInstance(myProject).getScopeAnalysisMode();
    if (scopedAnalysisMode == DartProblemsViewSettings.ScopedAnalysisMode.All) {
      final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
      if (fileIndex.isInLibraryClasses(vFile)) return true;

      final Module module = fileIndex.getModuleForFile(vFile);
      return (module != null && DartSdkLibUtil.isDartSdkEnabled(module));
    }
    else if (scopedAnalysisMode == DartProblemsViewSettings.ScopedAnalysisMode.DartPackage) {
      for (String root : myIncludedRoots) {
        if (vFile.getPath().startsWith(FileUtil.toSystemIndependentName(root) + "/")) {
          return true;
        }
      }
    }
    return false;
  }

  @NotNull
  private String getEnclosingDartPackageDirectory(@NotNull final VirtualFile vFile) {
    final VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                                ? DartBuildFileUtil.findPackageRootBuildFile(myProject, vFile)
                                : PubspecYamlUtil.findPubspecYamlFile(myProject, vFile);
    // temp var to make sure that value is initialized
    final VirtualFile packageRoot;
    if (pubspec == null) {
      packageRoot = ProjectFileIndex.getInstance(myProject).getContentRootForFile(vFile, false);
    }
    else {
      packageRoot = pubspec.getParent();
    }

    if (packageRoot != null) {
      return packageRoot.getPath();
    }

    // As a fall-back, return the enclosing directory
    return vFile.getParent().getPath();
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
