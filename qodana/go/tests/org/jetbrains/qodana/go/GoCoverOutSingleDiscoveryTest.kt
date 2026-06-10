package org.jetbrains.qodana.go

import com.goide.sdk.GoSdk
import com.goide.sdk.GoSdkService
import com.goide.sdk.GoSdkVersion
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test
import java.nio.file.Path
import javax.swing.Icon

class GoCoverOutSingleDiscoveryTest : QodanaCoverageDiscoveryTest("GoCoverageInspection") {
  // The cover profile keys files by the `go.mod` module path; the stubbed SDK resolves these GOPATH-style as
  // `srcDir/<import-path>`, so the project must live at `<root>/example.com/cover-single` with `srcDir = <root>`.
  override val projectDirName: String = "example.com/cover-single"

  override fun reportPlacements(testName: String): List<ReportLocation> = when (testName) {
    "coverageOut" -> listOf(ReportLocation("report.out", "coverage.out"))
    "coverOut" -> listOf(ReportLocation("report.out", "cover.out"))
    "coverageSubdir" -> listOf(ReportLocation("report.out", "coverage/coverage.out"))
    "dotCoverageSubdir" -> listOf(ReportLocation("report.out", ".coverage/coverage.out"))
    "buildDir" -> listOf(ReportLocation("report.out", "build/coverage.out"))
    "reportsDir" -> listOf(ReportLocation("report.out", "reports/coverage.out"))
    "resultsDir" -> listOf(ReportLocation("report.out", "test-results/coverage.out"))
    "artifactsDir" -> listOf(ReportLocation("report.out", "artifacts/coverage.out"))
    "binDir" -> listOf(ReportLocation("report.out", "bin/coverage.out"))
    "targetDir" -> listOf(ReportLocation("report.out", "target/coverage.out"))
    "cOut" -> listOf(ReportLocation("report.out", "c.out"))
    "profileTxt" -> listOf(ReportLocation("report.out", "profile.txt"))
    "coverageTxt" -> listOf(ReportLocation("report.out", "coverage.txt"))
    "covTxt" -> listOf(ReportLocation("report.out", "cov.txt"))
    "profileCov" -> listOf(ReportLocation("report.out", "profile.cov"))
    "coverageCov" -> listOf(ReportLocation("report.out", "coverage.cov"))
    else -> error("Unknown test method: $testName")
  }

  override fun setUpProject() {
    super.setUpProject()
    GoSdkService.getInstance(project).setSdk(object : GoSdk {
      override fun getIcon(): Icon? = null
      override fun getVersion(): String? = null
      override fun getHomeUrl(): String = project.basePath!!
      override fun getSrcDir(): VirtualFile? =
        VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Path.of(project.basePath!!).parent.parent)
      override fun getExecutable(): VirtualFile? = null
      override fun isValid(): Boolean = false
      override fun getMajorVersion(): GoSdkVersion = GoSdkVersion.GO_1_1
      override fun getVersionFilePath(): String? = null
      override fun getSdkRoot(): VirtualFile? = null
      override fun getRootsToAttach(): MutableCollection<VirtualFile> = mutableListOf()
      override fun toString(): String = "Test Go SDK"
    }, false)
  }

  @Test fun coverageOut() = runDiscovery()
  @Test fun coverOut() = runDiscovery()
  @Test fun coverageSubdir() = runDiscovery()
  @Test fun dotCoverageSubdir() = runDiscovery()
  @Test fun buildDir() = runDiscovery()
  @Test fun reportsDir() = runDiscovery()
  @Test fun resultsDir() = runDiscovery()
  @Test fun artifactsDir() = runDiscovery()
  @Test fun binDir() = runDiscovery()
  @Test fun targetDir() = runDiscovery()
  @Test fun cOut() = runDiscovery()
  @Test fun profileTxt() = runDiscovery()
  @Test fun coverageTxt() = runDiscovery()
  @Test fun covTxt() = runDiscovery()
  @Test fun profileCov() = runDiscovery()
  @Test fun coverageCov() = runDiscovery()
}
