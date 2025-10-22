package com.intellij.deno.entities

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

internal object DenoEntitySource : EntitySource

interface DenoEntity : WorkspaceEntity {
  val depsFile: VirtualFileUrl?
  val denoTypes: VirtualFileUrl?
}
