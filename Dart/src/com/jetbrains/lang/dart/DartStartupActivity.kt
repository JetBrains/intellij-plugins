// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.projectWizard.DartModuleBuilder;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

/**
 * {@link DartStartupActivity} configures "Dart Packages" library (based on Dart-specific pubspec.yaml and .packages files) on project open.
 * Afterwards the "Dart Packages" library is kept up-to-dated thanks to {@link DartFileListener} and {@link DartWorkspaceModelChangeListener}.
 *
 * @see DartFileListener
 * @see DartWorkspaceModelChangeListener
 */
public final class DartStartupActivity implements StartupActivity, DumbAware {
  @Override
  public void runActivity(@NotNull Project project) {
    ReadAction.nonBlocking(() -> {
        Collection<VirtualFile> pubspecYamlFiles =
          ReadAction.compute(() -> FilenameIndex.getVirtualFilesByName(PUBSPEC_YAML, GlobalSearchScope.projectScope(project)));

        if (pubspecYamlFiles.isEmpty()) {
          return Collections.<Pair<Module, VirtualFile>>emptyList();
        }

        Collection<Pair<Module, VirtualFile>> modulesAndPubspecs = new ArrayList<>(pubspecYamlFiles.size());

        for (VirtualFile pubspecYamlFile : pubspecYamlFiles) {
          Module module = ModuleUtilCore.findModuleForFile(pubspecYamlFile, project);
          if (module != null) {
            modulesAndPubspecs.add(Pair.create(module, pubspecYamlFile));
          }
        }

        return modulesAndPubspecs;
      })
      .expireWith(DartAnalysisServerService.getInstance(project))
      .finishOnUiThread(ModalityState.nonModal(), modulesAndPubspecs -> {
        if (modulesAndPubspecs.isEmpty()) return;

        for (Pair<Module, VirtualFile> moduleAndPubspec : modulesAndPubspecs) {
          excludeBuildAndToolCacheFolders(moduleAndPubspec.first, moduleAndPubspec.second);
        }

        DartFileListener.scheduleDartPackageRootsUpdate(project);
      })
      .submit(AppExecutorUtil.getAppExecutorService());

    startAnalysisServerIfNeeded(project);
  }

  public static void excludeBuildAndToolCacheFolders(final @NotNull Module module, final @NotNull VirtualFile pubspecYamlFile) {
    final VirtualFile root = pubspecYamlFile.getParent();
    final VirtualFile contentRoot =
      root == null ? null : ProjectRootManager.getInstance(module.getProject()).getFileIndex().getContentRootForFile(root);
    if (contentRoot == null) return;

    final Collection<String> oldExcludedUrls =
      ContainerUtil.filter(ModuleRootManager.getInstance(module).getExcludeRootUrls(), new Condition<>() {
        final String rootUrl = root.getUrl();

        @Override
        public boolean value(final String url) {
          if (url.equals(rootUrl + "/.dart_tool")) return true;
          if (url.equals(rootUrl + "/.pub")) return true;
          if (url.equals(rootUrl + "/build")) return true;

          return false;
        }
      });

    final Set<String> newExcludedUrls = collectFolderUrlsToExclude(pubspecYamlFile);

    if (oldExcludedUrls.size() != newExcludedUrls.size() || !newExcludedUrls.containsAll(oldExcludedUrls)) {
      ModuleRootModificationUtil.updateExcludedFolders(module, contentRoot, oldExcludedUrls, newExcludedUrls);
    }
  }

  private static Set<String> collectFolderUrlsToExclude(@NotNull final VirtualFile pubspecYamlFile) {
    final Set<String> newExcludedPackagesUrls = new HashSet<>();
    final VirtualFile root = pubspecYamlFile.getParent();

    newExcludedPackagesUrls.add(root.getUrl() + "/.dart_tool");
    newExcludedPackagesUrls.add(root.getUrl() + "/.pub");
    newExcludedPackagesUrls.add(root.getUrl() + "/build");

    return newExcludedPackagesUrls;
  }

  private static void startAnalysisServerIfNeeded(@NotNull Project project) {
    if (DartModuleBuilder.isPubGetScheduledForNewlyCreatedProject(project)) {
      // We want to start Analysis Server after initial 'pub get' is finished, this will be done in DartPubActionBase
      return;
    }

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) return;

    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      if (DartSdkLibUtil.isDartSdkEnabled(module)) {
        ReadAction.run(() -> DartAnalysisServerService.getInstance(project).serverReadyForRequest());
        break;
      }
    }
  }
}
