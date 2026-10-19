package com.intellij.deno.roots

import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.entities.DenoEntityBuilder
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceEntityLifecycleSupporter

/**
 * Keeps one [DenoEntity] from [createDenoEntity] while the project uses the Deno library.
 */
internal class DenoEntityLifecycleSupporter : WorkspaceEntityLifecycleSupporter<DenoEntity, DenoEntityBuilder> {
  override fun getEntityClass(): Class<DenoEntity> = DenoEntity::class.java

  override fun createSampleEntity(project: Project): DenoEntityBuilder? = createDenoEntity(project)

  override fun haveEqualData(first: DenoEntityBuilder, second: DenoEntity): Boolean = first.hasSameRoots(second)
}
