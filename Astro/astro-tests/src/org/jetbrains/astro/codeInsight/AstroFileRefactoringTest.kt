package org.jetbrains.astro.codeInsight

import com.intellij.javascript.testFramework.web.checkImports
import com.intellij.javascript.testFramework.web.moveFiles
import com.intellij.javascript.testFramework.web.renameFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule

/**
 * Moves or renames the files that `src/pages/index.astro` imports, or moves the page itself.
 * Each test then checks the module name and the resolved file of each import in the page.
 * The `relativeImport` project has relative imports and the tsconfig from WEB-79637.
 * The `pathAlias` project adds the `@components` path alias and imports through it.
 */
class AstroFileRefactoringTest : AstroCodeInsightTestCase("codeInsight/refactoring/file") {
  fun testMoveComponentRelativeImport() =
    doMoveComponentsTest("relativeImport",
                         "../components/sub/Hello.astro -> src/components/sub/Hello.astro",
                         "../components/sub/animals.js -> src/components/sub/animals.ts")

  fun testMoveComponentPathAlias() =
    doMoveComponentsTest("pathAlias",
                         "@components/sub/Hello.astro -> src/components/sub/Hello.astro",
                         "@components/sub/animals.js -> src/components/sub/animals.ts")

  fun testMovePageRelativeImport() =
    doMovePageTest("relativeImport",
                   "../../components/Hello.astro -> src/components/Hello.astro",
                   "../../components/animals.js -> src/components/animals.ts")

  fun testMovePagePathAlias() =
    doMovePageTest("pathAlias",
                   "@components/Hello.astro -> src/components/Hello.astro",
                   "@components/animals.js -> src/components/animals.ts")

  fun testRenameComponentRelativeImport() =
    doRenameComponentsTest("relativeImport",
                           "../components/Greeting.astro -> src/components/Greeting.astro",
                           "../components/pets.js -> src/components/pets.ts")

  fun testRenameComponentPathAlias() =
    doRenameComponentsTest("pathAlias",
                           "@components/Greeting.astro -> src/components/Greeting.astro",
                           "@components/pets.js -> src/components/pets.ts")

  private fun doMoveComponentsTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/index.astro", expectedImports) {
      moveFiles("src/components/sub", "src/components/Hello.astro", "src/components/animals.ts")
    }

  private fun doMovePageTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/blog/index.astro", expectedImports) {
      moveFiles("src/pages/blog", "src/pages/index.astro")
    }

  private fun doRenameComponentsTest(project: String, vararg expectedImports: String) =
    doFileRefactoringTest(project, "src/pages/index.astro", expectedImports) {
      renameFile("src/components/Hello.astro", "Greeting.astro")
      renameFile("src/components/animals.ts", "pets.ts")
    }

  private fun doFileRefactoringTest(
    project: String,
    importingFile: String,
    expectedImports: Array<out String>,
    refactoring: CodeInsightTestFixture.() -> Unit,
  ) =
    doConfiguredTest(AstroTestModule.ASTRO_5_14_4, dir = true, dirName = project, configureFileName = "src/pages/index.astro") {
      refactoring()
      checkImports(importingFile, *expectedImports)
    }
}
