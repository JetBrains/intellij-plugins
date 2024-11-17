package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.EQUAL_INDICATOR
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV1
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.StringWriter
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class DatabaseProblemsFlowTest : QodanaTestCase() {
  private val commonPathsInspectionsSample = listOf(
    Path.of("JSUnresolvedVariable.json"),
    Path.of("BusyWait.json"),
  )
  private lateinit var database: QodanaToolResultDatabase
  private val gson = SarifUtil.createGson()

  @Before
  fun setUpDb() {
    database = QodanaToolResultDatabase.create(getTempOutputPath())
    commonPathsInspectionsSample.forEach { p ->
      val run = gson.fromJson(getTestDataPath().resolve(p).toFile().readText(), Run::class.java)
      run.results.forEach { r ->
        database.insert("", r.ruleId, BaselineEqualityV1.calculate(r), gson.toJson(r, Result::class.java))
      }
    }
  }

  @After
  fun closeDb() {
    database.close()
  }

  @Test
  fun `producing common results`(): Unit = runBlocking {
    val testDataPath = getTestDataPath()

    val testSarifPath = testDataPath.resolve("test-sarif.json")
    val report = SarifReport().withRuns(listOf(Run().withResults(database.resultsFlowByGroup("", QodanaMessageReporter.EMPTY).toList())))
    val writer = StringWriter()
    SarifUtil.writeReport(writer, report)

    assertSameLinesWithFile (testSarifPath.absolutePathString(), writer.toString())
  }

  @Test
  fun `iterate twice on FileProblemsIterable`(): Unit = runBlocking {
    val testDataPath = getTestDataPath()
    val databaseProblemsFlow = database.resultsFlowByGroup("", QodanaMessageReporter.EMPTY)

    val testSarifPath = testDataPath.resolve("test-sarif.json")
    val expectedResults = SarifUtil.readReport(testSarifPath)
      .runs.first().results.toMutableSet()
    val expectedResultsSecondRun = expectedResults.toMutableList()

    databaseProblemsFlow.collect { result ->
      assertTrue(couldNotFindResultMessage(result), result in expectedResults)
      expectedResults.remove(result)
    }
    assertEmpty(expectedResults)

    databaseProblemsFlow.collect { result ->
      assertTrue(couldNotFindResultMessage(result), result in expectedResultsSecondRun)
      expectedResultsSecondRun.remove(result)
    }
    assertEmpty(expectedResultsSecondRun)
  }

  @Test
  fun `iterate in parallel on FileProblemsIterable`(): Unit = runBlocking {
    val testDataPath = getTestDataPath()
    val databaseProblemsFlow = database.resultsFlowByGroup("", QodanaMessageReporter.EMPTY)

    val testSarifPath = testDataPath.resolve("test-sarif.json")
    val expectedResults = SarifUtil.readReport(testSarifPath)
      .runs.first().results.toMutableSet()
    val expectedResultsSecondRun = expectedResults.toMutableList()

    launch(Dispatchers.IO) {
      databaseProblemsFlow.collect { result ->
        assertTrue(couldNotFindResultMessage(result), result in expectedResults)
        expectedResults.remove(result)
      }
      assertEmpty(expectedResults)
    }

    launch(Dispatchers.IO) {
      databaseProblemsFlow.collect { result ->
        assertTrue(couldNotFindResultMessage(result), result in expectedResultsSecondRun)
        expectedResultsSecondRun.remove(result)
      }
      assertEmpty(expectedResultsSecondRun)
    }
  }


  private fun getTestDataPath(): Path {
    return Path.of(PathManager.getHomePath(), "contrib", "qodana", "core", "test-data", DatabaseProblemsFlowTest::class.java.simpleName)
  }

  private fun getTempOutputPath(): Path {
    return FileUtil.createTempDirectory(getTestName(false), null, true).toPath()
  }

  private fun couldNotFindResultMessage(result: Result) =
    "Couldn't find result with id ${result.ruleId}, equalIndicator: ${result.partialFingerprints.getLastValue(EQUAL_INDICATOR)}"
}
