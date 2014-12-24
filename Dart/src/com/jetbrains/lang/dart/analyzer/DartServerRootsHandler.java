package com.jetbrains.lang.dart.analyzer;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class DartServerRootsHandler {
  private final Set<Project> myTrackedProjects = new THashSet<Project>();
  private final List<String> myIncludedRoots = new SmartList<String>();
  private final List<String> myExcludedRoots = new SmartList<String>();

  public DartServerRootsHandler() {
    ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerAdapter() {
      @Override
      public void projectClosed(final Project project) {
        if (myTrackedProjects.remove(project)) {
          updateRoots();
        }
      }
    });
  }

  public void reset() {
    myTrackedProjects.clear();
    myIncludedRoots.clear();
    myExcludedRoots.clear();
  }

  public void ensureProjectServed(@NotNull final Project project) {
    if (myTrackedProjects.contains(project)) return;

    myTrackedProjects.add(project);
    updateRoots();

    project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
      @Override
      public void rootsChanged(final ModuleRootEvent event) {
        updateRoots();
      }
    });
  }

  private void updateRoots() {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    final List<String> newIncludedRoots = new SmartList<String>();
    final List<String> newExcludedRoots = new SmartList<String>();

    if (sdk != null) {
      for (Project project : myTrackedProjects) {
        for (Module module : DartSdkGlobalLibUtil.getModulesWithDartSdkGlobalLibAttached(project, sdk.getGlobalLibName())) {
          for (ContentEntry contentEntry : ModuleRootManager.getInstance(module).getContentEntries()) {
            newIncludedRoots.add(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(contentEntry.getUrl())));

            for (String excludedUrl : contentEntry.getExcludeFolderUrls()) {
              newExcludedRoots.add(FileUtil.toSystemDependentName(VfsUtilCore.urlToPath(excludedUrl)));
            }
          }
        }
      }
    }

    if (!myIncludedRoots.equals(newIncludedRoots) || !myExcludedRoots.equals(newExcludedRoots)) {
      myIncludedRoots.clear();
      myExcludedRoots.clear();

      if (DartAnalysisServerService.getInstance().updateRoots(newIncludedRoots, newExcludedRoots)) {
        myIncludedRoots.addAll(newIncludedRoots);
        myExcludedRoots.addAll(newExcludedRoots);
      }
    }
  }
}
