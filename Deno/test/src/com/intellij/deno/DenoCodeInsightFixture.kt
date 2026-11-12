package com.intellij.deno

import com.intellij.deno.roots.updateDenoEntity
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightFixture
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly

/**
 * Creates a [CodeInsightTestFixture] on the module of [projectFixture] with Deno turned on.
 * [projectFixture] must come from `sharedCodeInsightProjectFixture()`.
 *
 * After the test, the fixture makes a roots change to reset the Deno libraries, and restores the Deno mode of the project.
 */
@TestOnly
fun denoCodeInsightFixture(projectFixture: TestFixture<Project>): TestFixture<CodeInsightTestFixture> =
  testFixture("denoCodeInsightFixture") {
    val fixture = sharedCodeInsightFixture(projectFixture).init()
    val project = fixture.project
    val service = DenoSettings.getService(project)
    val before = service.getUseDeno()
    withContext(Dispatchers.EDT) {
      service.setUseDenoAndReload(UseDeno.ENABLE)
    }
    (fixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    initialized(fixture) {
      //required to reset deno libs
      edtWriteAction {
        ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), RootsChangeRescanningInfo.TOTAL_RESCAN)
      }
      service.setUseDeno(before)
      withContext(Dispatchers.EDT) {
        updateDenoEntity(project)
      }
    }
  }
