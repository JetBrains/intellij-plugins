// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.entities

import com.intellij.platform.workspace.jps.entities.SdkId
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.WorkspaceEntity

internal object FlexBuildConfigurationSdksEntitySource : EntitySource

/**
 * The SDKs of all Flex build configurations in the project. The module SDK is the SDK of the active build configuration only, so this
 * entity is the referrer of the other SDKs, and the file index registers their roots.
 */
interface FlexBuildConfigurationSdksEntity : WorkspaceEntity {
  val sdks: List<SdkId>
}
