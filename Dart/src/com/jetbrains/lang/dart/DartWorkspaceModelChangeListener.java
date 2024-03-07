package com.jetbrains.lang.dart;

import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.workspace.WorkspaceModelChangeListener;
import com.intellij.platform.workspace.jps.entities.ContentRootEntity;
import com.intellij.platform.workspace.jps.entities.ModuleEntity;
import com.intellij.platform.workspace.storage.EntityChange;
import com.intellij.platform.workspace.storage.VersionedStorageChange;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.toolingDaemon.DartToolingDaemonService;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import org.jetbrains.annotations.NotNull;

public class DartWorkspaceModelChangeListener implements WorkspaceModelChangeListener {

  private final @NotNull Project myProject;

  public DartWorkspaceModelChangeListener(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public void changed(@NotNull VersionedStorageChange event) {
    for (EntityChange<ContentRootEntity> change : event.getChanges(ContentRootEntity.class)) {
      ContentRootEntity oldContentRootEntity = change.getOldEntity();
      ContentRootEntity newContentRootEntity = change.getNewEntity();

      if (oldContentRootEntity != null && DartSdkLibUtil.isDartSdkEnabled(oldContentRootEntity.getModule()) ||
          newContentRootEntity != null && DartSdkLibUtil.isDartSdkEnabled(newContentRootEntity.getModule())) {
        onDartRootsChanged(myProject);
        return;
      }
    }

    for (EntityChange<ModuleEntity> change : event.getChanges(ModuleEntity.class)) {
      ModuleEntity oldModuleEntity = change.getOldEntity();
      ModuleEntity newModuleEntity = change.getNewEntity();
      boolean oldDartEnabled = oldModuleEntity != null && DartSdkLibUtil.isDartSdkEnabled(oldModuleEntity);
      boolean newDartEnabled = newModuleEntity != null && DartSdkLibUtil.isDartSdkEnabled(newModuleEntity);

      if (oldDartEnabled != newDartEnabled) {
        onDartRootsChanged(myProject);
        return;
      }
    }
  }

  private static void onDartRootsChanged(@NotNull Project project) {
    if (!project.isInitialized() || DartSdk.getDartSdk(project) == null) return;

    DartFileListener.scheduleDartPackageRootsUpdate(project);
    DartAnalysisServerService.getInstance(project).ensureAnalysisRootsUpToDate();
    DartToolingDaemonService.getInstance(project).ensureRootsUpToDate();
  }
}
