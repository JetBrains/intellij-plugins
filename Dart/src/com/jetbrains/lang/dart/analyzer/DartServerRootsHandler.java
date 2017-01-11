package com.jetbrains.lang.dart.analyzer;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.intellij.util.io.URLUtil;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartServerRootsHandler {

  private final Project myProject;

  private final List<String> myIncludedRoots = new SmartList<>();
  private final List<String> myExcludedRoots = new SmartList<>();

  public DartServerRootsHandler(Project project) {
    myProject = project;
  }

  public void reset() {
    myIncludedRoots.clear();
    myExcludedRoots.clear();
  }

  public void ensureProjectServed() {
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
      public void rootsChanged(final ModuleRootEvent event) {
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

    if (sdk != null) {
      @SuppressWarnings("ConstantConditions")
      final String dotIdeaPath = PathUtil.getParentPath(myProject.getProjectFilePath());
      if (dotIdeaPath.endsWith("/.idea")) {
        newExcludedRoots.add(FileUtil.toSystemDependentName(dotIdeaPath));
      }

      for (Module module : DartSdkGlobalLibUtil.getModulesWithDartSdkEnabled(myProject)) {
        final Set<String> excludedPackageSymlinkUrls = getExcludedPackageSymlinkUrls(module);

        for (ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
          final String contentEntryUrl = contentEntry.getUrl();
          if (contentEntryUrl.startsWith(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) {
            newIncludedRoots.add(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentEntryUrl)));

            for (String excludedUrl : contentEntry.getExcludeFolderUrls()) {
              if (excludedUrl.startsWith(contentEntryUrl) && !excludedPackageSymlinkUrls.contains(excludedUrl)) {
                newExcludedRoots.add(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(excludedUrl)));
              }
            }
          }
        }
      }
    }

    if (!myIncludedRoots.equals(newIncludedRoots) || !myExcludedRoots.equals(newExcludedRoots)) {
      myIncludedRoots.clear();
      myExcludedRoots.clear();

      if (DartAnalysisServerService.getInstance(myProject).updateRoots(newIncludedRoots, newExcludedRoots)) {
        myIncludedRoots.addAll(newIncludedRoots);
        myExcludedRoots.addAll(newExcludedRoots);
      }
    }
  }

  private static Set<String> getExcludedPackageSymlinkUrls(@NotNull final Module module) {
    final Set<String> result = new THashSet<>();

    final Collection<VirtualFile> pubspecYamlFiles =
      FilenameIndex.getVirtualFilesByName(module.getProject(), PUBSPEC_YAML, module.getModuleContentScope());

    for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
      result.addAll(DartProjectComponent.collectFolderUrlsToExclude(module, pubspecYamlFile, false));
    }

    return result;
  }
}
