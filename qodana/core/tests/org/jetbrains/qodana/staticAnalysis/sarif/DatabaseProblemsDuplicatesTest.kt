package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV1
import org.junit.Test
import java.io.StringWriter
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class DatabaseProblemsDuplicatesTest : QodanaTestCase() {
  private val commonPathsInspectionsSample = listOf(
    Path.of("Duplicates.json"),
    Path.of("MergeTags.json"),
  )
  private val gson = SarifUtil.createGson()

  @Test
  fun `duplicates eliminated tags are merged`(): Unit = runBlocking {
    QodanaToolResultDatabase.create(getTempOutputPath()).use { database ->
      commonPathsInspectionsSample.forEach { p ->
        val run = gson.fromJson(getTestDataPath().resolve(p).toFile().readText(), Run::class.java)
        run.results.forEach { r ->
          database.insert("", r.ruleId, BaselineEqualityV1.calculate(r), gson.toJson(r, Result::class.java))
        }
      }
      val testDataPath = getTestDataPath()
      val testSarifPath = testDataPath.resolve("test-sarif.json")
      val report = SarifReport().withRuns(listOf(Run().withResults(database.resultsFlowByGroup("").toList())))
      val writer = StringWriter()
      SarifUtil.writeReport(writer, report)
      assertSameLinesWithFile(testSarifPath.absolutePathString(), writer.toString())
    }
  }

  private fun getTestDataPath(): Path {
    return Path.of(PathManager.getHomePath(), "contrib", "qodana", "core", "test-data", DatabaseProblemsDuplicatesTest::class.java.simpleName)
  }

  private fun getTempOutputPath(): Path {
    return FileUtil.createTempDirectory(getTestName(false), null, true).toPath()
  }
}
