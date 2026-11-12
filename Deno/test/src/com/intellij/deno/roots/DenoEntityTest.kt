package com.intellij.deno.roots

import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.service.DenoTypings
import com.intellij.deno.setUseDenoLibrary
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNode
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class DenoEntityTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val roots by denoLibraryRootsFixture(projectFixture) { true }

  @Test
  fun testEntityRoots() {
    val entity = denoEntities().single()
    val typings = DenoTypings.getInstance(roots.project).getDenoTypingsVirtualFile()!!

    assertEquals(VfsUtilCore.pathToUrl("${roots.denoCache.path}/deps"), entity.depsFile?.url)
    assertEquals(VfsUtilCore.pathToUrl("${roots.denoCache.path}/npm"), entity.npmFolder?.url)
    assertEquals(typings.url, entity.denoTypes?.url)
  }

  @Test
  fun testDisableDenoRemovesEntity(): Unit = timeoutRunBlocking {
    setUseDenoLibrary(roots.project, value = false, updateLib = false)
    withContext(Dispatchers.EDT) {
      DenoSettings.getService(roots.project).setUseDenoAndReload(UseDeno.DISABLE)
    }

    assertThat(denoEntities()).isEmpty()
  }

  @Test
  fun testDepsCreatedAfterEntity(): Unit = timeoutRunBlocking {
    val hashFile = roots.createCacheFile("deps/https/deno.land/${roots.hashName('d')}")

    assertTrue(roots.isInLibraries(hashFile))
  }

  /**
   * The cache path from the settings field can end with a slash. The stored URL must still be the URL of the folder, or a folder that
   * `deno cache` creates later stays outside the index.
   */
  @Test
  fun testCachePathWithTrailingSlash(): Unit = timeoutRunBlocking {
    DenoSettings.getService(roots.project).setDenoCache(roots.denoCache.path + "/")
    withContext(Dispatchers.EDT) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }

    val entity = denoEntities().single()
    assertEquals(VfsUtilCore.pathToUrl("${roots.denoCache.path}/deps"), entity.depsFile?.url)
    assertEquals(VfsUtilCore.pathToUrl("${roots.denoCache.path}/npm"), entity.npmFolder?.url)
    assertTrue(roots.isInLibraries(roots.createCacheFile("deps/https/deno.land/${roots.hashName('g')}")))
  }

  @Test
  fun testCachePathChange(): Unit = timeoutRunBlocking {
    val oldHashFile = roots.createCacheFile("deps/https/deno.land/${roots.hashName('e')}")
    val newCache = roots.createDenoCacheDir("newDenoCache")
    val newHashFile = roots.createCacheFile("deps/https/deno.land/${roots.hashName('e')}", newCache)
    // The setter alone updates the libraries, whoever calls it.
    DenoSettings.getService(roots.project).setDenoCache(newCache.path)
    withContext(Dispatchers.EDT) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }

    assertEquals(VfsUtilCore.pathToUrl("${newCache.path}/deps"), denoEntities().single().depsFile?.url)
    assertFalse(roots.isInLibraries(oldHashFile))
    assertTrue(roots.isInLibraries(newHashFile))
  }

  @Test
  fun testProjectViewNode(): Unit = timeoutRunBlocking {
    val depsDir = roots.createCacheFile("deps/https/deno.land/${roots.hashName('f')}").parent.parent.parent
    roots.updateLibraries()

    val node = DenoExternalLibrariesWorkspaceModelNodesProvider().createNode(denoEntities().single(), roots.project, null)
    assertInstanceOf(ExternalLibrariesWorkspaceModelNode::class.java, node)
    readAction { node!!.update() }
    assertEquals(DenoBundle.message("deno.library.name"), node!!.presentation.presentableText)
    assertNotNull(node.presentation.getIcon(false))
    assertThat((node as ExternalLibrariesWorkspaceModelNode).roots).contains(depsDir)
  }

  @Test
  fun testSupporterMatchesEntity() {
    assertTrue(createDenoEntity(roots.project)!!.hasSameRoots(denoEntities().single()))
  }

  private fun denoEntities(): List<DenoEntity> {
    return WorkspaceModel.getInstance(roots.project).currentSnapshot.entities(DenoEntity::class.java).toList()
  }
}
