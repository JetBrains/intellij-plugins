// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.testFramework.web.checkImports
import com.intellij.javascript.testFramework.web.moveFiles
import com.intellij.javascript.testFramework.web.renameFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile

/**
 * Moves or renames the files that `src/pages/index.vue` imports, or moves the page itself.
 * Each test then checks the module name and the resolved file of each import in the page.
 * The `relativeImport` project has relative imports.
 * The `pathAlias` project adds the `@components` path alias and imports through it.
 * Each project has its own `tsconfig.json`, so the default one of [VueTestCase] is disabled.
 * The `tsconfig.json` sets `allowJs`, as the Astro base config does, so the three framework tests check the same case.
 */
class VueFileRefactoringTest : VueTestCase("refactoring/file", testMode = VueTestMode.NO_PLUGIN) {
  fun testMoveComponentRelativeImport() =
    doMoveComponentsTest("relativeImport",
                         "../components/sub/Hello.vue -> src/components/sub/Hello.vue",
                         "../components/sub/animals.js -> src/components/sub/animals.ts")

  fun testMoveComponentPathAlias() =
    doMoveComponentsTest("pathAlias",
                         "@components/sub/Hello.vue -> src/components/sub/Hello.vue",
                         "@components/sub/animals.js -> src/components/sub/animals.ts")

  fun testMovePageRelativeImport() =
    doMovePageTest("relativeImport",
                   "../../components/Hello.vue -> src/components/Hello.vue",
                   "../../components/animals.js -> src/components/animals.ts")

  fun testMovePagePathAlias() =
    doMovePageTest("pathAlias",
                   "@components/Hello.vue -> src/components/Hello.vue",
                   "@components/animals.js -> src/components/animals.ts")

  fun testRenameComponentRelativeImport() =
    doRenameComponentsTest("relativeImport",
                           "../components/Greeting.vue -> src/components/Greeting.vue",
                           "../components/pets.js -> src/components/pets.ts")

  fun testRenameComponentPathAlias() =
    doRenameComponentsTest("pathAlias",
                           "@components/Greeting.vue -> src/components/Greeting.vue",
                           "@components/pets.js -> src/components/pets.ts")

  private fun doMoveComponentsTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/index.vue", expectedImports) {
      moveFiles("src/components/sub", "src/components/Hello.vue", "src/components/animals.ts")
    }

  private fun doMovePageTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/blog/index.vue", expectedImports) {
      moveFiles("src/pages/blog", "src/pages/index.vue")
    }

  private fun doRenameComponentsTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/index.vue", expectedImports) {
      renameFile("src/components/Hello.vue", "Greeting.vue")
      renameFile("src/components/animals.ts", "pets.ts")
    }

  private fun doFileRefactoringTest(
    project: String,
    importingFile: String,
    expectedImports: Array<out String>,
    refactoring: CodeInsightTestFixture.() -> Unit,
  ) =
    doConfiguredTest(dir = true, dirName = project, configureFileName = "src/pages/index.vue",
                     configurators = listOf(VueTsConfigFile(enabled = false))) {
      refactoring()
      checkImports(importingFile, *expectedImports)
    }
}
