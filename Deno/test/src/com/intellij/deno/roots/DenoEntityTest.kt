package com.intellij.deno.roots

import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.service.DenoTypings
import com.intellij.deno.setUseDenoLibrary
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNode
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.testFramework.PlatformTestUtil

class DenoEntityTest : DenoLibraryRootsTestBase() {

  fun testEntityRoots() {
    val entity = denoEntities().single()
    val typings = DenoTypings.getInstance(project).getDenoTypingsVirtualFile()!!

    assertEquals(VfsUtilCore.pathToUrl("${denoCache.path}/deps"), entity.depsFile?.url)
    assertEquals(VfsUtilCore.pathToUrl("${denoCache.path}/npm"), entity.npmFolder?.url)
    assertEquals(typings.url, entity.denoTypes?.url)
  }

  fun testDisableDenoRemovesEntity() {
    setUseDenoLibrary(project, value = false, updateLib = false)
    DenoSettings.getService(project).setUseDenoAndReload(UseDeno.DISABLE)

    assertEmpty(denoEntities())
  }

  fun testDepsCreatedAfterEntity() {
    val hashFile = createCacheFile("deps/https/deno.land/${hashName('d')}")

    assertTrue(isInLibraries(hashFile))
  }

  /**
   * The cache path from the settings field can end with a slash. The stored URL must still be the URL of the folder, or a folder that
   * `deno cache` creates later stays outside the index.
   */
  fun testCachePathWithTrailingSlash() {
    DenoSettings.getService(project).setDenoCache(denoCache.path + "/")
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

    val entity = denoEntities().single()
    assertEquals(VfsUtilCore.pathToUrl("${denoCache.path}/deps"), entity.depsFile?.url)
    assertEquals(VfsUtilCore.pathToUrl("${denoCache.path}/npm"), entity.npmFolder?.url)
    assertTrue(isInLibraries(createCacheFile("deps/https/deno.land/${hashName('g')}")))
  }

  fun testCachePathChange() {
    val oldHashFile = createCacheFile("deps/https/deno.land/${hashName('e')}")
    val newCache = createDenoCacheDir("newDenoCache")
    val newHashFile = createCacheFile("deps/https/deno.land/${hashName('e')}", newCache)
    // The setter alone updates the libraries, whoever calls it.
    DenoSettings.getService(project).setDenoCache(newCache.path)
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

    assertEquals(VfsUtilCore.pathToUrl("${newCache.path}/deps"), denoEntities().single().depsFile?.url)
    assertFalse(isInLibraries(oldHashFile))
    assertTrue(isInLibraries(newHashFile))
  }

  fun testProjectViewNode() {
    val depsDir = createCacheFile("deps/https/deno.land/${hashName('f')}").parent.parent.parent
    updateLibraries()

    val node = DenoExternalLibrariesWorkspaceModelNodesProvider().createNode(denoEntities().single(), project, null)
    assertInstanceOf(node, ExternalLibrariesWorkspaceModelNode::class.java)
    node!!.update()
    assertEquals(DenoBundle.message("deno.library.name"), node.presentation.presentableText)
    assertNotNull(node.presentation.getIcon(false))
    assertContainsElements((node as ExternalLibrariesWorkspaceModelNode).roots, depsDir)
  }

  fun testSupporterMatchesEntity() {
    assertTrue(createDenoEntity(project)!!.hasSameRoots(denoEntities().single()))
  }

  private fun denoEntities(): List<DenoEntity> {
    return WorkspaceModel.getInstance(project).currentSnapshot.entities(DenoEntity::class.java).toList()
  }
}
