// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.platform.workspace.storage.*
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceList
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

internal interface NuxtFolderEntity : WorkspaceEntity {

  val nuxtFolderUrl: VirtualFileUrl
  val libraryFileUrls: List<VirtualFileUrl>

  object MyEntitySource : EntitySource
}
