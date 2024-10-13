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
import kotlin.io.path.pathString

class SarifProblemTest: QodanaPluginLightTestBase() {

  private val sarifProblemTestDir by lazy { Path(myFixture.testDataPath, "SarifProblemTest") }

  private val problems1 by lazy { getReport(sarifProblemTestDir.resolve("report1.sarif.json")) }
  private val problemsBad by lazy { getReport(sarifProblemTestDir.resolve("reportBad.sarif.json")) }
  private val problemsAbsPaths by lazy { getReport(sarifProblemTestDir.resolve("reportAbsPaths.sarif.json")) }
  private val problemsWithEndLineColumn by lazy { getReport(sarifProblemTestDir.resolve("reportWithEndLineColumn.sarif.json")) }
  private val problemsWithGraph by lazy { getReport(sarifProblemTestDir.resolve("reportWithGraph.sarif.json")) }
  private val problemsWithComplexGraph by lazy { getReport(sarifProblemTestDir.resolve("reportWithComplexGraph.sarif.json")) }
  private val problemsMultipleRuns by lazy { getReport(sarifProblemTestDir.resolve("reportMultipleRuns.sarif.json")) }


  private fun projectPath() = Path(myFixture.testDataPath, "SarifProblemTest/project")

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

  private fun getDocument(documentName: String): Document? {
    val mainFile = projectPath().resolve(documentName).refreshAndFindVirtualFile() ?: return null

    val documentFile = myFixture.psiManager.findFile(mainFile) ?: return null

    return myFixture.getDocument(documentFile)
  }

  private fun getReport(path: Path): List<SarifProblem> {
    val sarif = SarifUtil.readReport(path)

    return SarifProblem.fromReport(project, ValidatedSarif(sarif), projectPath().pathString)
  }
}