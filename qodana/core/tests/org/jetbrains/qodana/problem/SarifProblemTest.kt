package org.jetbrains.qodana.problem

import com.intellij.openapi.editor.Document
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.intellij.util.PathUtil.toSystemIndependentName
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Result.BaselineState
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.report.ValidatedSarif
import java.nio.file.Path
import kotlin.io.path.Path

private const val PROJECT_PATH = "SarifProblemTest/vcsRoot/monorepo/project"

class SarifProblemTest: QodanaPluginLightTestBase() {

  private val sarifProblemTestDir by lazy { Path(myFixture.testDataPath, "SarifProblemTest") }

  private val problems1 by lazy { getReport(sarifProblemTestDir.resolve("report1.sarif.json")) }
  private val problemsBad by lazy { getReport(sarifProblemTestDir.resolve("reportBad.sarif.json")) }
  private val problemsAbsPaths by lazy { getReport(sarifProblemTestDir.resolve("reportAbsPaths.sarif.json")) }
  private val problemsWithEndLineColumn by lazy { getReport(sarifProblemTestDir.resolve("reportWithEndLineColumn.sarif.json")) }
  private val problemsWithGraph by lazy { getReport(sarifProblemTestDir.resolve("reportWithGraph.sarif.json")) }
  private val problemsWithComplexGraph by lazy { getReport(sarifProblemTestDir.resolve("reportWithComplexGraph.sarif.json")) }
  private val problemsMultipleRuns by lazy { getReport(sarifProblemTestDir.resolve("reportMultipleRuns.sarif.json")) }

  private val problemsWithOriginalUriBaseIds by lazy { getReport(sarifProblemTestDir.resolve("reportWithOriginalUriBaseIds.sarif.json")) }
  private val problemsWithOriginalUriBaseIdsNoSlash by lazy { getReport(sarifProblemTestDir.resolve("reportWithOriginalUriBaseIdsNoSlash.sarif.json")) }

  private val problemsBadWithOriginalUriBaseIds by lazy { getReport(sarifProblemTestDir.resolve("reportBadWithOriginalUriBaseIds.sarif.json")) }

  private val problemsWithParentBaseUri by lazy { getReport(sarifProblemTestDir.resolve("reportWithParentBaseUri.sarif.json")) }

  private val problemsWithMixedPaths by lazy { getReport(sarifProblemTestDir.resolve("reportWithMixedPaths.sarif.json")) }
  
  private fun projectPath() = Path(myFixture.testDataPath, PROJECT_PATH)

  override fun setUp() {
    super.setUp()
    myFixture.copyDirectoryToProject(PROJECT_PATH, ".")
  }
  
  fun `test default sarif initialization` () {
    assertThat(problems1.size).isEqualTo(3)
    val problem = problems1[0]
    assertThat(problem.relativePathToFile).isEqualTo("Main.java")
    assertThat(problem.startLine).isEqualTo(0)
    assertThat(problem.startColumn).isEqualTo(13)
    assertThat(problem.charLength).isEqualTo(1)
    assertNull(problem.endLine)
    assertNull(problem.endColumn)
    assertThat(problem.baselineState).isEqualTo(BaselineState.NEW)
    assertThat(problem.isInBaseline).isFalse
    assertNotNull(problems1[1].snippetText)
    assertNotNull(problems1[2].snippetText)
  }

  fun `test bad sarif initialization` () {
    assertThat(problemsBad.size).isEqualTo(0)
  }

  fun `test absolute paths initialization`() {
    assertThat(problemsAbsPaths.size).isEqualTo(2)
    assertThat(problemsAbsPaths[0].relativePathToFile).isEqualTo("Main.java")
    assertThat(toSystemIndependentName(problemsAbsPaths[1].relativePathToFile)).isEqualTo("src/Logic.java")
  }

  fun `test default Main problem`() {
    assertThat(problems1.size).isEqualTo(3)

    val problem1 = problems1[0]
    assertNotNull(problem1.charLength)

    val problem2 = problems1[1]
    assertNotNull(problem2.snippetText)

    val problem3 = problems1[2]
    assertNotNull(problem2.snippetText)

    val document = getDocument("Main.java")

    assertNotNull(document)

    val textRange1 = problem1.getTextRangeInDocument(document!!)
    assertThat(textRange1?.endOffset!! - textRange1.startOffset).isEqualTo(1)
    val textRange2 = problem2.getTextRangeInDocument(document)
    assertNull(textRange2)
    val textRange3 = problem3.getTextRangeInDocument(document)
    assertThat(textRange3?.endOffset!! - textRange3.startOffset).isEqualTo(1)

  }

  fun `test End Line Columns Main problem`() {
    assertThat(problemsWithEndLineColumn.size).isEqualTo(2)

    val problem = problemsWithEndLineColumn[0]
    assertNull(problem.charLength)
    assertNotNull(problem.endLine)
    assertNotNull(problem.endColumn)

    val document = getDocument("Main.java")

    assertNotNull(document)

    val textRange = problem.getTextRangeInDocument(document!!)
    assertThat(textRange?.endOffset!! - textRange.startOffset).isEqualTo(0)
  }

  fun `test End Line Columns Logic problem`() {
    assertThat(problemsWithEndLineColumn.size).isEqualTo(2)

    val problem = problemsWithEndLineColumn[1]
    assertNull(problem.charLength)
    assertNotNull(problem.endLine)
    assertNotNull(problem.endColumn)

    val document = getDocument("src/Logic.java")

    assertNotNull(document)

    val textRange = problem.getTextRangeInDocument(document!!)
    assertThat(textRange?.endOffset!! - textRange.startOffset).isEqualTo(2)

  }

  fun `test problem with graph from taint analysis`() {
    assertThat(problemsWithGraph.size).isEqualTo(3)

    val problem1 = problemsWithGraph[0]
    assertThat(problem1.inspectionId).isEqualTo("PhpVulnerablePathsInspection")
    assertThat(problem1.startLine).isEqualTo(7)
    assertThat(problem1.startColumn).isEqualTo(11)
    assertThat(problem1.charLength).isEqualTo(1)

    val problem2 = problemsWithGraph[1]
    assertThat(problem2.inspectionId).isEqualTo("BadNamingCustomInspection")
    assertThat(problem2.startLine).isEqualTo(7)
    assertThat(problem2.startColumn).isEqualTo(4)
    assertThat(problem2.charLength).isEqualTo(18)

    val problem3 = problemsWithGraph[2]
    assertThat(problem3.inspectionId).isEqualTo("PhpVulnerablePathsInspection")

    val document = getDocument("Main.java")

    assertNotNull(document)

    val textRange = problem3.getTextRangeInDocument(document!!)
    assertNotNull(textRange)
    assertThat(document.getText(textRange!!)).isEqualTo("args[1]")
  }

  fun `test problem with complex graph from taint analysis`() {
    assertThat(problemsWithComplexGraph.size).isEqualTo(2)

    val document1 = getDocument("Main.java")

    assertNotNull(document1)

    val problem1 = problemsWithComplexGraph[0]

    val textRange1 = problem1.getTextRangeInDocument(document1!!)
    assertNotNull(textRange1)
    assertThat(document1.getText(textRange1!!)).isEqualTo("args[1]")

    val problem2 = problemsWithComplexGraph[1]

    val document2 = getDocument("src/Logic.java")
    assertNotNull(document2)


    val textRange2 = problem2.getTextRangeInDocument(document2!!)
    assertNotNull(textRange2)
    assertThat(document2.getText(textRange2!!)).isEqualTo("args[2]")
  }

  fun `test multiple runs`() {
    assertThat(problemsMultipleRuns.size).isEqualTo(4)
    val problem = problemsMultipleRuns[3]
    assertThat(problem.relativePathToFile).isEqualTo("Main.java")
    assertThat(problem.startLine).isEqualTo(1)
    assertThat(problem.startColumn).isEqualTo(13)
    assertThat(problem.charLength).isEqualTo(2)
    assertNull(problem.endLine)
    assertNull(problem.endColumn)
    assertThat(problem.baselineState).isEqualTo(BaselineState.NEW)
    assertThat(problem.isInBaseline).isFalse
  }

  fun `test originUriIds resolved correctly`() {
    assertThat(problemsWithOriginalUriBaseIds.size).isEqualTo(2)

    val problem1 = problemsWithOriginalUriBaseIds[0]
    assertThat(problem1.relativePathToFile).isEqualTo("src/Logic.java")

    val problem2 = problemsWithOriginalUriBaseIds[1]
    assertThat(problem2.relativePathToFile).isEqualTo("Main.java")
  }

  fun `test originUriIds resolved correctly if directory URI does not end with slash`() {
    assertThat(problemsWithOriginalUriBaseIdsNoSlash.size).isEqualTo(2)

    val problem1 = problemsWithOriginalUriBaseIdsNoSlash[0]
    assertThat(problem1.relativePathToFile).isEqualTo("src/Logic.java")

    val problem2 = problemsWithOriginalUriBaseIdsNoSlash[1]
    assertThat(problem2.relativePathToFile).isEqualTo("Main.java")
  }

  fun `test originUriIds cyclic resolution`() {
    assertThat(problemsBadWithOriginalUriBaseIds.size).isEqualTo(2)

    val problem1 = problemsBadWithOriginalUriBaseIds[0]
    assertThat(problem1.relativePathToFile).isEqualTo("Logic.java")

    val problem2 = problemsBadWithOriginalUriBaseIds[1]
    assertThat(problem2.relativePathToFile).isEqualTo("Main.java")
  }

  fun `test originUriIds resolved correctly if opened project not equal repository root`() {
    assertThat(problemsWithParentBaseUri.size).isEqualTo(2)

    val problem1 = problemsWithParentBaseUri[0]
    assertThat(problem1.relativePathToFile).isEqualTo("src/Logic.java")

    val problem2 = problemsWithParentBaseUri[1]
    assertThat(problem2.relativePathToFile).isEqualTo("Main.java")
  }

  fun `test absolute paths and relative paths in one resolved correctly`() {
    assertThat(problemsWithMixedPaths.size).isEqualTo(4)

    val problem1 = problemsWithMixedPaths[0]
    assertThat(problem1.relativePathToFile).isEqualTo("src/Logic.java")

    val problem2 = problemsWithMixedPaths[1]
    assertThat(problem2.relativePathToFile).isEqualTo("src/Logic.java")

    val problem3 = problemsWithMixedPaths[2]
    assertThat(problem3.relativePathToFile).isEqualTo("Main.java")

    val problem4 = problemsWithMixedPaths[3]
    assertThat(problem4.relativePathToFile).isEqualTo("Main.java")
  }

  private fun getDocument(documentName: String): Document? {
    val mainFile = projectPath().resolve(documentName).refreshAndFindVirtualFile() ?: return null

    val documentFile = myFixture.psiManager.findFile(mainFile) ?: return null

    return myFixture.getDocument(documentFile)
  }

  private fun getReport(path: Path): List<SarifProblem> {
    val sarif = SarifUtil.readReport(path)

    return SarifProblem.fromReport(project, ValidatedSarif(sarif))
  }
}