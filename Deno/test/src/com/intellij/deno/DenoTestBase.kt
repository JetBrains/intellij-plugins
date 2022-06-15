package com.intellij.deno

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.testFramework.fixtures.BasePlatformTestCase

open class DenoTestBase : BasePlatformTestCase() {
  private var before = false

  override fun setUp() {
    super.setUp()
    val project = myFixture.project
    val service = DenoSettings.getService(project)
    before = service.isUseDeno()
    service.setUseDenoAndReload(true)
  }

  override fun tearDown() {
    try {
      DenoSettings.getService(project).setUseDeno(before)

      //required to reset deno libs
      WriteAction.run<RuntimeException> {
        ProjectRootManagerEx.getInstanceEx(myFixture.project).makeRootsChange(
          EmptyRunnable.getInstance(), false, true)
      }
    }
    catch (e: Exception) {
      addSuppressedException(e)
    } finally {
      super.tearDown()
    }
  }
}