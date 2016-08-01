package com.jetbrains.lang.dart.analyzer;

import com.intellij.ProjectTopics;
import com.intellij.codeInspection.SmartHashMap;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.PathUtil;
import com.intellij.util.SmartList;
import com.intellij.util.io.URLUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartServerRootsHandler {
  private final Set<Project> myTrackedProjects = new THashSet<Project>();
  private final List<String> myIncludedRoots = new SmartList<String>();
  private final List<String> myExcludedRoots = new SmartList<String>();
  private final Map<String, String> myPackageRoots = new THashMap<String, String>();

  public DartServerRootsHandler() {
    // ProjectManagerListener.projectClosed() is not called in unittest mode, that's why ProjectLifecycleListener is used - it is called always
    final MessageBusConnection busConnection = ApplicationManager.getApplication().getMessageBus().connect();
    busConnection.subscribe(ProjectLifecycleListener.TOPIC, new ProjectLifecycleListener() {
      @Override
      public void afterProjectClosed(@NotNull final Project project) {
        if (myTrackedProjects.remove(project)) {
          if (myTrackedProjects.isEmpty()) {
            DartAnalysisServerService.getInstance().removeDocumentListener();
          }
          updateRoots();
          DartAnalysisServerService.getInstance().updateVisibleFiles();
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

    if (myTrackedProjects.isEmpty()) {
      DartAnalysisServerService.getInstance().addDocumentListener();
    }

    ProgressManager.getInstance().executeNonCancelableSection(() -> {
      myTrackedProjects.add(project);
      updateRoots();
      DartAnalysisServerService.getInstance().updateVisibleFiles();
    });

    project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
      @Override
      public void rootsChanged(final ModuleRootEvent event) {
        updateRoots();
      }
    });
  }

  public Set<Project> getTrackedProjects() {
    return myTrackedProjects;
  }

  private void updateRoots() {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null || !DartAnalysisServerService.isDartSdkVersionSufficient(sdk)) {
      DartAnalysisServerService.getInstance().stopServer();
    }

    final List<String> newIncludedRoots = new SmartList<String>();
    final List<String> newExcludedRoots = new SmartList<String>();
    final Map<String, String> newPackageRoots = new SmartHashMap<String, String>();

    if (sdk != null) {
      for (Project project : myTrackedProjects) {
        @SuppressWarnings("ConstantConditions")
        final String dotIdeaPath = PathUtil.getParentPath(project.getProjectFilePath());
        if (dotIdeaPath.endsWith("/.idea")) {
          newExcludedRoots.add(FileUtil.toSystemDependentName(dotIdeaPath));
        }

        for (Module module : DartSdkGlobalLibUtil.getModulesWithDartSdkEnabled(project)) {
          newPackageRoots.putAll(DartConfigurable.getContentRootPathToCustomPackageRootMap(module));

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
    }

    if (!myIncludedRoots.equals(newIncludedRoots) || !myExcludedRoots.equals(newExcludedRoots) || !myPackageRoots.equals(newPackageRoots)) {
      myIncludedRoots.clear();
      myExcludedRoots.clear();
      myPackageRoots.clear();

      if (DartAnalysisServerService.getInstance().updateRoots(newIncludedRoots, newExcludedRoots, newPackageRoots)) {
        myIncludedRoots.addAll(newIncludedRoots);
        myExcludedRoots.addAll(newExcludedRoots);
        myPackageRoots.putAll(newPackageRoots);
      }
    }
  }

  private static Set<String> getExcludedPackageSymlinkUrls(@NotNull final Module module) {
    final Set<String> result = new THashSet<String>();

    final Collection<VirtualFile> pubspecYamlFiles =
      FilenameIndex.getVirtualFilesByName(module.getProject(), PUBSPEC_YAML, module.getModuleContentScope());

    final DartSdk sdk = DartSdk.getDartSdk(module.getProject());
    boolean withRootPackagesFolder = sdk != null && StringUtil.compareVersionNumbers(sdk.getVersion(), "1.12") >= 0;
    for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
      result.addAll(DartProjectComponent.collectFolderUrlsToExclude(module, pubspecYamlFile, false, withRootPackagesFolder));
    }

    return result;
  }
}
