package com.intellij.deno.roots

import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil
import com.intellij.deno.denoCodeInsightFixture
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.ProjectScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import java.nio.file.Path

/**
 * The fixture of the Deno library roots tests. Create it with [denoLibraryRootsFixture].
 *
 * It points the Deno cache at an empty local temp directory for each test.
 */
class DenoLibraryRootsFixture internal constructor(val fixture: CodeInsightTestFixture, private val tempRoot: VirtualFile) {
  val project: Project
    get() = fixture.project

  lateinit var denoCache: VirtualFile
    internal set

  fun createDenoCacheDir(name: String): VirtualFile {
    val existing = tempRoot.findFileByRelativePath(name)
    require(existing == null) { "Already exists: ${existing!!.path}" }
    return VfsTestUtil.createDir(tempRoot, name)
  }

  fun createCacheFile(relativePath: String, cache: VirtualFile = denoCache): VirtualFile {
    return VfsTestUtil.createFile(cache, relativePath, "export const a = 1;")
  }

  fun hashName(char: Char): String = char.toString().repeat(DenoUtil.HASH_FILE_NAME_LENGTH)

  suspend fun updateLibraries() {
    withContext(Dispatchers.EDT) {
      DenoSettings.getService(project).updateLibraries()
    }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  suspend fun isInLibraries(file: VirtualFile): Boolean {
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    return readAction { ProjectScope.getLibrariesScope(project).contains(file) }
  }
}

/**
 * Creates a [DenoLibraryRootsFixture] on the module of [projectFixture].
 * [projectFixture] must come from `sharedCodeInsightProjectFixture()`.
 *
 * [useWorkspaceModelRoots] gives the value of the `deno.use.workspace.model.roots` registry key for the test.
 * The fixture reads it when the test starts, so a subclass can override the property that it reads.
 */
@TestOnly
fun denoLibraryRootsFixture(
  projectFixture: TestFixture<Project>,
  useWorkspaceModelRoots: () -> Boolean,
): TestFixture<DenoLibraryRootsFixture> = testFixture("denoLibraryRootsFixture") {
  val fixture = denoCodeInsightFixture(projectFixture).init()
  val tempPath: Path = tempPathFixture().init()
  val tempRoot = checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(tempPath)) { "No temp root: $tempPath" }
  val roots = DenoLibraryRootsFixture(fixture, tempRoot)
  Registry.get("deno.use.workspace.model.roots").setValue(useWorkspaceModelRoots(), fixture.testRootDisposable)
  roots.denoCache = roots.createDenoCacheDir("denoCache")
  val service = DenoSettings.getService(fixture.project)
  val oldDenoCache = service.getDenoCache()
  service.setDenoCache(roots.denoCache.path)
  roots.updateLibraries()
  initialized(roots) {
    service.setDenoCache(oldDenoCache)
  }
}
