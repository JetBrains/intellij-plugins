package com.intellij.deno

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl

abstract class DenoTestBase : BasePlatformTestCase() {

  override fun setUp() {
    super.setUp()
    val project = myFixture.project
    val service = DenoSettings.getService(project)
    val before = service.getUseDeno()
    service.setUseDenoAndReload(UseDeno.ENABLE)
    disposeOnTearDown(Disposable { service.setUseDeno(before) })
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
  }

  override fun tearDown() {
    try {
      //required to reset deno libs
      WriteAction.run<RuntimeException> {
        ProjectRootManagerEx.getInstanceEx(myFixture.project).makeRootsChange(
          EmptyRunnable.getInstance(), RootsChangeRescanningInfo.TOTAL_RESCAN)
      }
    }
    catch (e: Exception) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }
}