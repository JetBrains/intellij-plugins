package org.jetbrains.qodana.go

import com.goide.execution.testing.coverage.GoCoverageEngine
import com.goide.sdk.GoSdk
import com.goide.sdk.GoSdkService
import com.goide.sdk.GoSdkVersion
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageRunner
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.rt.coverage.util.ProjectDataLoader
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageInspectionTest
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud
import org.junit.Test
import java.nio.file.Path
import javax.swing.Icon


class GoCoverageInspectionTest: QodanaCoverageInspectionTest("GoCoverageInspection") {
  private val dummyProvider: CoverageFileProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }

  override fun setUpProject() {
    super.setUpProject()
    GoSdkService.getInstance(project).setSdk(object : GoSdk {
      override fun getIcon(): Icon? = null

      override fun getVersion(): String?  = null

      override fun getHomeUrl(): String = project.basePath!!

      override fun getSrcDir(): VirtualFile? = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(Path.of(project.basePath!!).parent)

      override fun getExecutable(): VirtualFile? = null

      override fun isValid(): Boolean = false

      override fun getMajorVersion(): GoSdkVersion = GoSdkVersion.GO_1_1

      override fun getVersionFilePath(): String? = null

      override fun getSdkRoot(): VirtualFile? = null

      override fun getRootsToAttach(): MutableCollection<VirtualFile> = mutableListOf()

    }, false)
  }

  @Test
  fun go() {
    runUnderCover()
    assertSarifResults()

    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(GoCoverageEngine::class.java)
    val path = qodanaConfig.coverage.coveragePath.resolve("GoCoverageEngine")
    val coverageRunner = CoverageRunner.getInstanceById("GoCoverage")!!
    val data = ProjectDataLoader.load(path.toFile())
    val projectDir = qodanaConfig.projectPath
    // assert that prefix was removed before storing the data
    assertFalse(data.classes.any { Path.of(it.key).startsWith(projectDir) })
    val suite = engine.createCoverageSuite("test", project, coverageRunner, dummyProvider, -1)!!
    suite.setCoverageData(data)
    val bundle = remapCoverageFromCloud(CoverageSuitesBundle(suite))
    val coverageData = bundle?.coverageData
    TestCase.assertNotNull(coverageData)
    UsefulTestCase.assertSize(2, coverageData!!.classes.entries)
    // assert that prefix was restored and bundle was correctly built
    assertTrue(coverageData.classes.all { Path.of(it.key).startsWith(projectDir) })
  }

  @Test
  fun coverageInfoWithProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun coverageInfoWithoutProblemReport() {
    runUnderCover()
    assertSarifResults()
  }

  @Test
  fun warnMissingCoverage() {
    runUnderCover("inspection-profile.xml")
    assertSarifResults()
  }
}