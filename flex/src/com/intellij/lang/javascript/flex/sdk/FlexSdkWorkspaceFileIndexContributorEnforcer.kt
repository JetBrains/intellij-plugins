// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.sdk

import com.intellij.platform.workspace.jps.entities.SdkEntity
import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributorEnforcer

internal class FlexSdkWorkspaceFileIndexContributorEnforcer: WorkspaceFileIndexContributorEnforcer {
  override fun shouldContribute(entity: WorkspaceEntity, storage: EntityStorage): Boolean {
    return entity is SdkEntity && entity.type == FlexSdkType2.NAME
  }
}