package org.jetbrains.qodana.cpp

import com.intellij.ide.starter.extended.allure.Subsystems
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.nio.file.Files
import java.nio.file.Path

@Suppress("Junit4RunWithInspection")
@RunWith(Parameterized::class)
@Subsystems.Qodana
class CppCoverageDiscoveryTest(private val case: Case) : QodanaCoverageDiscoveryTest("CppCoverageInspection", case) {
  override fun getProjectSourcesPath(): Path {
    val projectRoot = super.getProjectSourcesPath()
    val sourcePath = projectRoot.resolve("main.cpp").toAbsolutePath().normalize().toString().replace('\\', '/')
    for ((_, reportLocation) in case.placements) {
      val report = projectRoot.resolve(reportLocation)
      var reportText = Files.readString(report).replace(SOURCE_PATH_MARKER, sourcePath)
      if (case.name == LLVM_TYPE_AT_END_CASE) {
        reportText = moveLlvmTypeToEnd(reportText)
      }
      Files.writeString(report, reportText)
    }
    addNonCandidates(projectRoot)
    return projectRoot
  }

  private fun addNonCandidates(projectRoot: Path) {
    val llvmReport = Files.readString(getTestDataPath("report.json"))
    val gcovReport = Files.readString(getTestDataPath("report.gcov"))
    Files.createDirectories(projectRoot.resolve(".coverage"))
    Files.writeString(projectRoot.resolve(".coverage/coverage.json"), llvmReport)
    Files.createDirectories(projectRoot.resolve("target/debug/coverage"))
    Files.writeString(projectRoot.resolve("target/debug/coverage/coverage.json"), llvmReport)
    Files.writeString(projectRoot.resolve("package.json"), "{}")
    Files.writeString(projectRoot.resolve("not-coverage.gcov"), "This is not a gcov report.")
    Files.writeString(projectRoot.resolve("coverage.gcov.json.gz"), gcovReport)
  }

  @Test
  fun discoverCoverage() {
    val projectRoot = Path.of(project.basePath!!).toAbsolutePath().normalize()
    val expected = case.placements
      .mapTo(LinkedHashSet()) { projectRoot.resolve(it.projectReportLocation).normalize() }
    assertEquals(expected, CppCoverageFileProvider.getCoverageFiles(project).keys)
  }

  companion object {
    private const val SOURCE_PATH_MARKER = "__SOURCE_PATH__"
    private const val LLVM_TYPE_AT_END_CASE = "llvmTypeAtEnd"
    private const val LLVM_TYPE_PROPERTY = "  \"type\": \"llvm.coverage.json.export\",\n"

    private fun moveLlvmTypeToEnd(reportText: String): String {
      val withoutType = reportText.replace(LLVM_TYPE_PROPERTY, "")
      val closingBrace = withoutType.lastIndexOf('}')
      return buildString(withoutType.length + 5000) {
        append(withoutType, 0, closingBrace)
        append(",\n  \"padding\": \"")
        repeat(4500) { append('x') }
        append("\",\n")
        append(LLVM_TYPE_PROPERTY.removeSuffix(",\n"))
        append("\n}\n")
      }
    }

    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      Case("gcovProjectRoot", ReportLocation("report.gcov", "coverage.gcov")),
      Case("gcovCoverageDir", ReportLocation("report.gcov", "coverage/coverage.gcov")),
      Case("gcovBuildDir", ReportLocation("report.gcov", "build/coverage.gcov")),
      Case("gcovBuildTree", ReportLocation("report.gcov", "build/generated/coverage.gcov")),
      Case("gcovCmakeCoverageDir", ReportLocation("report.gcov", "cmake-build-debug/coverage/coverage.gcov")),
      Case("gcovCTestCoverageInfo", ReportLocation("report.gcov", "cmake-build-debug/Testing/CoverageInfo/coverage.gcov")),
      Case("gcovReportsDir", ReportLocation("report.gcov", "reports/gcov/report.gcov")),
      Case("llvmProjectRoot", ReportLocation("report.json", "coverage.json")),
      Case("llvmCoverageDir", ReportLocation("report.json", "coverage/coverage.json")),
      Case("llvmBuildDir", ReportLocation("report.json", "build/coverage.json")),
      Case("llvmBuildTree", ReportLocation("report.json", "build/generated/coverage.json")),
      Case("llvmCoverageReportDir", ReportLocation("report.json", "coverage-report/app.coverage.json")),
      Case("llvmSwiftCodecovDir", ReportLocation("report.json", ".build/debug/codecov/Package.json")),
      Case("llvmSwiftTripleCodecovDir", ReportLocation("report.json", ".build/arm64/debug/codecov/Package.json")),
      Case(LLVM_TYPE_AT_END_CASE, ReportLocation("report.json", "coverage-report/type-at-end.json")),
    )
  }
}
