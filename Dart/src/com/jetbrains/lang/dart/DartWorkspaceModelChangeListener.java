package com.jetbrains.lang.dart;

import com.intellij.openapi.project.Project;
import com.intellij.workspaceModel.ide.WorkspaceModelChangeListener;
import com.intellij.workspaceModel.storage.EntityChange;
import com.intellij.workspaceModel.storage.VersionedStorageChange;
import com.intellij.workspaceModel.storage.bridgeEntities.api.ContentRootEntity;
import com.intellij.workspaceModel.storage.bridgeEntities.api.ModuleEntity;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
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
    if (DartSdk.getDartSdk(project) == null) return;

    DartFileListener.scheduleDartPackageRootsUpdate(project);
    DartAnalysisServerService.getInstance(project).ensureAnalysisRootsUpToDate();
  }
}
