// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.idea.IJIgnore
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.lang.withRenameUsages
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@IJIgnore(issue = "WEB-75792")
@TestApplication
class NuxtRenameTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/nuxt/rename")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @Test
  fun testComponentFileWithUsages() {
    doTestRenameComponent("NewName.vue", "components/AppCard.vue", true)
  }

  @Test
  fun testComponentFileWithUsagesKebabCase() {
    doTestRenameComponent("new-name.vue", "components/AppCard.vue", true)
  }

  private fun doTestRenameComponent(
    newName: String,
    fileName: String,
    renameUsages: Boolean,
  ): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    val dirName = hybrid.getTestName(true)
    myFixture.copyDirectoryToProject(dirName, "")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.NUXT_2_15_6)
    myFixture.configureFromTempProjectFile(fileName)

    withRenameUsages(renameUsages) {
      myFixture.renameElement(myFixture.file, newName)
      WriteCommandAction.runWriteCommandAction(project) { PostprocessReformattingAspect.getInstance(project).doPostponedFormatting() }
      FileDocumentManager.getInstance().saveAllDocuments()
    }

    checkResultByDir(listOf(".", ".nuxt", "components"))
  }

  private fun checkResultByDir(dirsToCheck: List<String> = listOf("."), resultsDir: String = hybrid.getTestName(true) + "_after") {
    val extensions = setOf("vue", "html", "ts", "js")
    for (dir in dirsToCheck) {
      myFixture.tempDirFixture.findOrCreateDir(dir)
        .children
        .filter { !it.isDirectory && extensions.contains(it.extension) }.forEach {
          myFixture.checkResultByFile(
            FileUtil.toSystemIndependentName(FileUtil.join(dir, it.name)),
            FileUtil.toSystemIndependentName(FileUtil.join(resultsDir, dir, it.name)),
            true
          )
        }
    }
  }
}
