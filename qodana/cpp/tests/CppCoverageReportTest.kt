package org.jetbrains.qodana.cpp

import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.PsiFile
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineCoverage
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.sourceRootFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.testFramework.junit5.fixture.virtualFileFixture
import com.intellij.testFramework.replaceService
import com.intellij.testFramework.utils.vfs.getPsiFile
import com.jetbrains.cidr.cpp.execution.coverage.CMakeCoverageEngine
import com.jetbrains.cidr.execution.coverage.CidrCoverageSettings
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.issueWithCoverage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import java.nio.file.Path
import kotlin.io.path.writeText

@TestApplication
@Subsystems.Qodana
class CppCoverageReportTest {
  @TestDisposable
  private lateinit var testDisposable: Disposable

  private val tempPath = tempPathFixture()
  private val project = projectFixture(tempPath)
  private val sourceRoot = project.moduleFixture("CppCoverageReportTest-Module").sourceRootFixture()
  private val cppFile by sourceRoot.virtualFileFixture("main.cpp", CPP_SOURCE)
  private val cFile by sourceRoot.virtualFileFixture("main.c", C_SOURCE)

  @BeforeEach
  fun setUpCoverageSettings() {
    ApplicationManager.getApplication().replaceService(
      CidrCoverageSettings::class.java,
      CidrCoverageSettings(),
      testDisposable,
    )
  }

  @Test
  fun testGcovCoverageForCppFileWithSeveralMethods() {
    val report = tempPath.get().resolve("coverage.gcov")
    report.writeText(gcovReport(cppFile.path))

    val coverageData = loadCoverageData(report)
    val fileData = coverageData.classes.values.singleOrNull()
    assertNotNull(fileData)
    assertEquals(LineCoverage.FULL, fileData.getLineData(3)?.status?.toByte())
    assertEquals(LineCoverage.NONE, fileData.getLineData(7)?.status?.toByte())
    assertEquals(LineCoverage.NONE, fileData.getLineData(12)?.status?.toByte())
    assertEquals(LineCoverage.NONE, fileData.getLineData(16)?.status?.toByte())

    val psiFile = runReadActionBlocking { cppFile.getPsiFile(project.get()) }
    assertTrue(CppCoverageInspection().validateFileType(psiFile))
    assertCoverageIssue(psiFile, fileData)
  }

  @Test
  fun testLlvmCoverageForCFile() {
    val report = tempPath.get().resolve("coverage.json")
    report.writeText(llvmReport(cFile.path))

    val coverageData = loadCoverageData(report)
    val fileData = coverageData.classes[cFile.path]
    assertNotNull(fileData)
    val psiFile = runReadActionBlocking { cFile.getPsiFile(project.get()) }
    assertTrue(CppCoverageInspection().validateFileType(psiFile))
    assertCoverageIssue(psiFile, fileData)
  }

  private fun loadCoverageData(report: Path): ProjectData {
    val coverageData = retrieveCppCoverageData(CMakeCoverageEngine(), listOf(report), project.get())
    assertNotNull(coverageData)
    return coverageData
  }

  private fun assertCoverageIssue(psiFile: PsiFile, fileData: ClassData) {
    assertFalse(runReadActionBlocking {
      issueWithCoverage(fileData, psiFile, psiFile.textRange, project.get(), 0, warnMissingCoverage = false)
    })
    assertTrue(runReadActionBlocking {
      issueWithCoverage(fileData, psiFile, psiFile.textRange, project.get(), 50, warnMissingCoverage = false)
    })
  }

  private companion object {
    val CPP_SOURCE = """
      class Calculator {
      public:
        int increment(int value) const {
          return value + 1;
        }

        int decrement(int value) const {
          return value - 1;
        }
      };

      int square(int value) {
        return value * value;
      }

      int main() {
        Calculator calculator;
        return calculator.increment(square(1));
      }
    """.trimIndent()

    val C_SOURCE = """
      int double_value(int value) {
        return value * 2;
      }

      int unused_value(int value) {
        return value - 1;
      }

      int main(void) {
        return double_value(21);
      }
    """.trimIndent()

    fun gcovReport(source: String) = """
      -:    0:Source:$source
      -:    1:class Calculator {
      -:    2:public:
      1:    3:  int increment(int value) const {
      1:    4:    return value + 1;
      -:    5:  }
      -:    6:
      #####:    7:  int decrement(int value) const {
      #####:    8:    return value - 1;
      -:    9:  }
      -:   10:};
      -:   11:
      #####:   12:int square(int value) {
      #####:   13:  return value * value;
      -:   14:}
      -:   15:
      #####:   16:int main() {
      #####:   17:  Calculator calculator;
      #####:   18:  return calculator.increment(square(1));
      -:   19:}
    """.trimIndent()

    fun llvmReport(source: String) = """
      {
        "type": "llvm.coverage.json.export",
        "version": "2.0.0",
        "data": [
          {
            "files": [
              {
                "filename": ${JsonPrimitive(source)},
                "segments": [
                  [1, 1, 0, true, true],
                  [11, 2, 0, false, false]
                ],
                "summary": {
                  "lines": {"count": 6, "covered": 0},
                  "branches": {"count": 0, "covered": 0, "notcovered": 0, "percent": 0.0}
                }
              }
            ]
          }
        ]
      }
    """.trimIndent()
  }
}
