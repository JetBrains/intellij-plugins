package com.intellij.deno.roots

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.service.DenoTypings
import com.intellij.deno.setUseDenoLibrary
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
open class DenoLibraryRootsTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  protected open val useWorkspaceModelRoots: Boolean = true

  private val roots by denoLibraryRootsFixture(projectFixture) { useWorkspaceModelRoots }

  @Test
  fun testDepsFiles(): Unit = timeoutRunBlocking {
    val hashFile = roots.createCacheFile("deps/https/deno.land/${roots.hashName('a')}")
    val metadataFile = roots.createCacheFile("deps/https/deno.land/${roots.hashName('a')}.metadata.json")
    val definitionFile = roots.createCacheFile("deps/https/deno.land/mod.d.ts")
    val jsFile = roots.createCacheFile("deps/https/deno.land/mod.js")
    roots.updateLibraries()

    assertTrue(roots.isInLibraries(hashFile))
    assertTrue(roots.isInLibraries(metadataFile))
    assertTrue(roots.isInLibraries(definitionFile))
    assertFalse(roots.isInLibraries(jsFile))
  }

  @Test
  fun testNpmFiles(): Unit = timeoutRunBlocking {
    val definitionFile = roots.createCacheFile("npm/registry.npmjs.org/chalk/5.3.0/index.d.ts")
    roots.updateLibraries()

    assertTrue(roots.isInLibraries(definitionFile))
  }

  @Test
  fun testTypings(): Unit = timeoutRunBlocking {
    val typings = DenoTypings.getInstance(roots.project).getDenoTypingsVirtualFile()!!

    assertTrue(roots.isInLibraries(typings))
  }

  @Test
  fun testResolveScope(): Unit = timeoutRunBlocking {
    val hashFile = roots.createCacheFile("deps/https/deno.land/${roots.hashName('b')}")
    roots.updateLibraries()
    val typings = DenoTypings.getInstance(roots.project).getDenoTypingsVirtualFile()!!
    roots.fixture.configureByText("main.ts", "")

    val scope = readAction { ResolveScopeManager.getElementResolveScope(roots.fixture.file) }
    assertTrue(scope.contains(hashFile))
    assertTrue(scope.contains(typings))
  }

  @Test
  fun testDisableDeno(): Unit = timeoutRunBlocking {
    val hashFile = roots.createCacheFile("deps/https/deno.land/${roots.hashName('c')}")
    roots.updateLibraries()
    assertTrue(roots.isInLibraries(hashFile))

    setUseDenoLibrary(roots.project, value = false, updateLib = false)
    withContext(Dispatchers.EDT) {
      DenoSettings.getService(roots.project).setUseDenoAndReload(UseDeno.DISABLE)
    }

    assertFalse(roots.isInLibraries(hashFile))
  }
}
