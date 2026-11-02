// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion

import com.intellij.coldFusion.model.psi.CfmlComponent
import com.intellij.coldFusion.model.psi.stubs.CfmlComponentIndex
import com.intellij.coldFusion.model.psi.stubs.CfmlIndex
import com.intellij.coldFusion.model.psi.stubs.CfmlInterfaceIndex
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIdList
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexEx
import com.intellij.psi.stubs.StubRecoveryExtraStep
import com.intellij.psi.stubs.StubRecoveryService
import com.intellij.psi.stubs.StubRecoveryServiceImpl
import com.intellij.psi.stubs.StubTextInconsistencyException
import com.intellij.psi.stubs.StubUpdatingIndex
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.psiFileFixture
import com.intellij.testFramework.junit5.fixture.sourceRootFixture
import com.intellij.testFramework.replaceService
import com.intellij.util.indexing.FileBasedIndex
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@TestApplication
internal class CfmlIndexRecoveryTest {
  private val project = projectFixture()
  private val sourceRoot = project.moduleFixture().sourceRootFixture()
  private val component = sourceRoot.psiFileFixture("Test.cfc", "<cfcomponent></cfcomponent>")
  private val cfmlInterface = sourceRoot.psiFileFixture("TestInterface.cfc", "<cfinterface></cfinterface>")

  @TestDisposable
  private lateinit var disposable: Disposable

  @Test
  fun `reindex a component after a wrong PSI type in the index`() = checkRecovery(false)

  @Test
  fun `reindex an interface after a wrong PSI type in the index`() = checkRecovery(true)

  private fun checkRecovery(isInterface: Boolean): Unit = timeoutRunBlocking {
    val file = if (isInterface) cfmlInterface.get() else component.get()
    val name = if (isInterface) "TestInterface" else "Test"
    val indexName = name.lowercase()
    val project = project.get()
    val virtualFile = file.virtualFile
    val key = if (isInterface) CfmlInterfaceIndex.KEY else CfmlComponentIndex.KEY
    val requests = mutableListOf<VirtualFile>()

    IndexingTestUtil.suspendUntilIndexesAreReady(project)
    val lookup = {
      val index = CfmlIndex.getInstance(project)
      val scope = GlobalSearchScope.fileScope(project, virtualFile)
      if (isInterface) index.getInterfacesByNameInScope(name, scope) else index.getComponentsByNameInScope(name, scope)
    }
    readAction {
      assertThat(lookup()).singleElement().isInstanceOf(CfmlComponent::class.java)
    }

    coroutineScope {
      val recovery = StubRecoveryServiceImpl(this)
      val startRecovery = CompletableDeferred<Unit>()
      val recorder = object : StubRecoveryService {
        override fun scheduleReindexing(project: Project?, file: VirtualFile, vararg extraSteps: StubRecoveryExtraStep) {
          requests.add(file)
          launch {
            startRecovery.await()
            recovery.scheduleReindexing(project, file, *extraSteps)
          }
        }
      }
      ApplicationManager.getApplication().replaceService(StubRecoveryService::class.java, recorder, disposable)

      readAction {
        val tree = checkNotNull(FileBasedIndex.getInstance().getSingleEntryIndexData(StubUpdatingIndex.INDEX_ID, virtualFile, project))
        val entries = checkNotNull(tree.stubIndicesValueMap[key])
        // Stub ID zero denotes the file, which is not a CFML component.
        val original = checkNotNull(entries.put(indexName, StubIdList(0)))
        val stubIndex = StubIndex.getInstance() as StubIndexEx
        stubIndex.cleanCaches()
        try {
          assertThrows<StubTextInconsistencyException> {
            assertThat(lookup()).isEmpty()
          }
          assertThat(requests).isNotEmpty().containsOnly(virtualFile)
        }
        finally {
          entries[indexName] = original
          stubIndex.cleanCaches()
        }
      }
      readAction { assertThat(lookup()).isEmpty() }
      try {
        startRecovery.complete(Unit)
        while (readAction { lookup().isEmpty() }) {
          delay(1)
        }
      }
      finally {
        coroutineContext.cancelChildren()
      }
    }

    IndexingTestUtil.suspendUntilIndexesAreReady(project)
    readAction {
      val restored = lookup()
      assertThat(restored).hasSize(1)
      assertThat(restored.single().containingFile.virtualFile).isEqualTo(virtualFile)
    }
  }
}
