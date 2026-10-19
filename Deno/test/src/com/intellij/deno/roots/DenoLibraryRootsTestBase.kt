package com.intellij.deno.roots

import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoTestBase
import com.intellij.deno.DenoUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.ProjectScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.RunAll
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.rules.TempDirectory
import com.intellij.util.ThrowableRunnable

/**
 * Points the Deno cache at an empty local temp directory for each test.
 */
abstract class DenoLibraryRootsTestBase : DenoTestBase() {
  private val tempDirectory = TempDirectory()
  private lateinit var oldDenoCache: String
  protected lateinit var denoCache: VirtualFile

  protected open val useWorkspaceModelRoots: Boolean = true

  override fun setUp() {
    super.setUp()
    tempDirectory.before(name)
    Registry.get("deno.use.workspace.model.roots").setValue(useWorkspaceModelRoots, testRootDisposable)
    denoCache = createDenoCacheDir("denoCache")
    val service = DenoSettings.getService(project)
    oldDenoCache = service.getDenoCache()
    service.setDenoCache(denoCache.path)
    updateLibraries()
  }

  override fun tearDown() {
    RunAll(
      ThrowableRunnable { DenoSettings.getService(project).setDenoCache(oldDenoCache) },
      ThrowableRunnable { tempDirectory.after() },
      ThrowableRunnable { super.tearDown() },
    ).run()
  }

  protected fun createDenoCacheDir(name: String): VirtualFile = tempDirectory.newVirtualDirectory(name)

  protected fun createCacheFile(relativePath: String, cache: VirtualFile = denoCache): VirtualFile {
    return VfsTestUtil.createFile(cache, relativePath, "export const a = 1;")
  }

  protected fun hashName(char: Char): String = char.toString().repeat(DenoUtil.HASH_FILE_NAME_LENGTH)

  protected fun updateLibraries() {
    DenoSettings.getService(project).updateLibraries()
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  protected fun isInLibraries(file: VirtualFile): Boolean {
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    return ReadAction.compute<Boolean, RuntimeException> { ProjectScope.getLibrariesScope(project).contains(file) }
  }
}
