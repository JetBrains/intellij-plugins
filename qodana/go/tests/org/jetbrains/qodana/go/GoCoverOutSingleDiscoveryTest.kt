package org.jetbrains.qodana.go

import com.goide.sdk.GoSdk
import com.goide.sdk.GoSdkService
import com.goide.sdk.GoSdkVersion
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageDiscoveryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.nio.file.Path
import javax.swing.Icon

@RunWith(Parameterized::class)
class GoCoverOutSingleDiscoveryTest(case: Case) : QodanaCoverageDiscoveryTest("GoCoverageInspection", case) {
  // The cover profile keys files by the `go.mod` module path; the stubbed SDK resolves these GOPATH-style as
  // `srcDir/<import-path>`, so the project must live at `<root>/example.com/cover-single` with `srcDir = <root>`.
  override val projectDirName: String = "example.com/cover-single"

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

  @Test fun discoverCoverage() = runDiscovery()

  companion object {
    @Parameterized.Parameters(name = "{0}")
    @JvmStatic
    fun data(): Collection<Case> = listOf(
      // All filenames at root
      Case("coverageOut", ReportLocation("report.out", "coverage.out")),
      Case("coverOut", ReportLocation("report.out", "cover.out")),
      Case("cOut", ReportLocation("report.out", "c.out")),
      Case("profileTxt", ReportLocation("report.out", "profile.txt")),
      Case("coverageTxt", ReportLocation("report.out", "coverage.txt")),
      Case("covTxt", ReportLocation("report.out", "cov.txt")),
      Case("profileCov", ReportLocation("report.out", "profile.cov")),
      Case("coverageCov", ReportLocation("report.out", "coverage.cov")),
      // All directories with coverage.out
      Case("coverageSubdir", ReportLocation("report.out", "coverage/coverage.out")),
      Case("dotCoverageSubdir", ReportLocation("report.out", ".coverage/coverage.out")),
      Case("buildDir", ReportLocation("report.out", "build/coverage.out")),
      Case("reportsDir", ReportLocation("report.out", "reports/coverage.out")),
      Case("resultsDir", ReportLocation("report.out", "test-results/coverage.out")),
      Case("artifactsDir", ReportLocation("report.out", "artifacts/coverage.out")),
      Case("binDir", ReportLocation("report.out", "bin/coverage.out")),
      Case("targetDir", ReportLocation("report.out", "target/coverage.out")),
      // Cross-directory filename combinations
      Case("coverOutInCoverage", ReportLocation("report.out", "coverage/cover.out")),
      Case("cOutInBuild", ReportLocation("report.out", "build/c.out")),
      Case("profileTxtInReports", ReportLocation("report.out", "reports/profile.txt")),
      Case("coverageTxtInTestResults", ReportLocation("report.out", "test-results/coverage.txt")),
      Case("covTxtInArtifacts", ReportLocation("report.out", "artifacts/cov.txt")),
      Case("profileCovInBin", ReportLocation("report.out", "bin/profile.cov")),
      Case("coverageCovInTarget", ReportLocation("report.out", "target/coverage.cov")),
      Case("coverOutInDotCoverage", ReportLocation("report.out", ".coverage/cover.out"))
    )
  }
}
