package com.intellij.deno.roots

import com.intellij.deno.DenoBundle
import com.intellij.deno.entities.DenoEntity
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.WatchedRootsProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.util.indexing.roots.IndexableEntityProvider
import com.intellij.util.indexing.roots.IndexableFilesIterationMethods
import com.intellij.util.indexing.roots.IndexableFilesIterator
import com.intellij.util.indexing.roots.builders.IndexableIteratorBuilderHandler
import com.intellij.util.indexing.roots.kind.IndexableSetOrigin
import com.intellij.workspaceModel.ide.impl.virtualFile
import com.intellij.workspaceModel.storage.EntityStorage
import com.intellij.workspaceModel.storage.bridgeEntities.ModuleEntity

class DenoIndexableEntityProvider : IndexableEntityProvider.Existing<DenoEntity> {
  override fun getEntityClass(): Class<DenoEntity> = DenoEntity::class.java

  override fun getIteratorBuildersForExistingModule(entity: ModuleEntity,
                                                    entityStorage: EntityStorage,
                                                    project: Project): Collection<IndexableEntityProvider.IndexableIteratorBuilder> {
    return emptyList()
  }

  override fun getReplacedEntityIteratorBuilders(oldEntity: DenoEntity,
                                                 newEntity: DenoEntity): Collection<IndexableEntityProvider.IndexableIteratorBuilder> {
    if (!useWorkspaceModel()) return emptyList()
    return listOf(Dependency(newEntity.denoTypes?.virtualFile, newEntity.depsFile?.virtualFile))
  }

  override fun getAddedEntityIteratorBuilders(entity: DenoEntity,
                                              project: Project): Collection<IndexableEntityProvider.IndexableIteratorBuilder> {
    if (!useWorkspaceModel()) return emptyList()
    return listOf(Dependency(entity.denoTypes?.virtualFile, entity.depsFile?.virtualFile))
  }

  data class Dependency(val rootOne: VirtualFile?,
                        val rootTwo: VirtualFile?) : IndexableEntityProvider.IndexableIteratorBuilder
}

class DenoIndexableIteratorBuilderHandler : IndexableIteratorBuilderHandler {
  override fun accepts(builder: IndexableEntityProvider.IndexableIteratorBuilder): Boolean {
    return builder is DenoIndexableEntityProvider.Dependency
  }

  override fun instantiate(builders: MutableCollection<IndexableEntityProvider.IndexableIteratorBuilder>,
                           project: Project,
                           entityStorage: EntityStorage): List<IndexableFilesIterator> {
    if (!useWorkspaceModel()) return emptyList()
    return builders.filterIsInstance<DenoIndexableEntityProvider.Dependency>()
      .map { DenoIndexableFilesIterator(listOfNotNull(it.rootOne, it.rootTwo)) }
  }
}

class DenoIndexableFilesIterator(private val rootsToIndex: Collection<VirtualFile>) : IndexableFilesIterator {
  override fun getDebugName(): String = "Deno"

  override fun getIndexingProgressText(): String {
    return DenoBundle.message("deno.library.indexing")
  }

  override fun getRootsScanningProgressText(): String {
    return DenoBundle.message("deno.library.scanning")
  }

  override fun getOrigin(): IndexableSetOrigin {
    return Origin()
  }

  override fun iterateFiles(project: Project, fileIterator: ContentIterator, fileFilter: VirtualFileFilter): Boolean {
    return IndexableFilesIterationMethods.iterateRoots(project, rootsToIndex, fileIterator, fileFilter)
  }

  override fun getRootUrls(project: Project): Set<String> {
    if (!useWorkspaceModel()) return emptySet()
    return rootsToIndex.map { it.url }.toSet()
  }

  class Origin : IndexableSetOrigin
}

class DenoWatchedRootsProvider : WatchedRootsProvider {
  override fun getRootsToWatch(project: Project): Set<String> {
    if (!useWorkspaceModel()) return emptySet()
    return getRoots(project).toList().filterNotNull().map { it.path }.toSet()
  }
}