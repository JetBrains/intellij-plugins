package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageDataAnnotationsManager
import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageDataManagerImpl
import com.intellij.coverage.CoverageEditorAnnotatorImpl
import com.intellij.coverage.CoverageSuiteListener
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.view.CoverageViewManager
import com.intellij.coverage.view.CoverageViewTreeStructure
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.markup.FillingLineMarkerRenderer
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.rt.coverage.data.LineCoverage
import com.intellij.testFramework.JavaModuleTestCase
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.util.concurrency.Invoker
import com.intellij.util.io.await
import com.intellij.util.ui.tree.TreeUtil
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.jetbrains.concurrency.await
import org.jetbrains.qodana.coverage.CHANGED_LINES_ARTIFACT_ID
import org.jetbrains.qodana.coverage.CHANGED_LINES_FILE_NAME
import org.jetbrains.qodana.coverage.ChangedLinesMetaDataArtifact
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.report.AggregatedReportMetadata
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.report.BrowserViewProvider
import org.jetbrains.qodana.report.LoadedReport
import org.jetbrains.qodana.report.NoProblemsContentProvider
import org.jetbrains.qodana.report.NotificationCallback
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.report.ValidatedSarif
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

@RunWith(JUnit4::class)
abstract class QodanaCoverageUiTestBase(private val sourceClass: String) : JavaModuleTestCase() {
  protected val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "core", "test-data")

  override fun runInDispatchThread(): Boolean = false

  protected val manager: CoverageDataManagerImpl
    get() = CoverageDataManager.getInstance(project) as CoverageDataManagerImpl

  protected open fun getProjectSourcesPath(): Path = testData.resolve(sourceClass).resolve("sources")

  override fun setUpProject() {
    myProject = PlatformTestUtil.loadAndOpenProject(getProjectSourcesPath(), testRootDisposable)
  }

  override fun setUp() {
    super.setUp()
    // Force-create the listener so it subscribes to the highlighted-report state before we drive it.
    CoverageListenerService.getInstance(project)
    registerCoverageToolWindow()
  }

  private fun registerCoverageToolWindow() {
    val toolWindowManager = ToolWindowManager.getInstance(project) as ToolWindowHeadlessManagerImpl
    toolWindowManager.doRegisterToolWindow(CoverageViewManager.TOOLWINDOW_ID)
  }

  /**
   * Copy a coverage artifact fixture to a temp file under the same name it would have once downloaded from the cloud,
   * and wrap it as a [CoverageMetaDataArtifact] keyed by [key].
   */
  protected fun coverageArtifact(scenario: String, fileName: String, key: String): CoverageMetaDataArtifact {
    return CoverageMetaDataArtifact(key, copyArtifactToTemp(scenario, fileName).toFile())
  }

  protected fun changedLinesArtifact(scenario: String): ChangedLinesMetaDataArtifact {
    return ChangedLinesMetaDataArtifact(CHANGED_LINES_ARTIFACT_ID, copyArtifactToTemp(scenario, CHANGED_LINES_FILE_NAME))
  }

  protected fun copyArtifactToTemp(scenario: String, fileName: String): Path {
    val golden = testData.resolve(sourceClass).resolve(scenario).resolve("artifacts").resolve(fileName)
    assertTrue("Missing coverage fixture: $golden", Files.isRegularFile(golden))
    val tempDir = Files.createTempDirectory("qodana-coverage-ui")
    val target = tempDir.resolve(fileName)
    Files.copy(golden, target)
    return target
  }

  protected suspend fun highlightCoverageReport(metadata: Map<String, ReportMetadata>) {
    val dataCollected = CompletableDeferred<Unit>()
    manager.addSuiteListener(object : CoverageSuiteListener {
      override fun coverageDataCalculated(bundle: CoverageSuitesBundle) {
        dataCollected.complete(Unit)
      }
    }, testRootDisposable)
    QodanaHighlightedReportService.getInstance(project).highlightReport(CoverageReportDescriptorMock(metadata))
    withTimeout(30.seconds) {
      dataCollected.await()
    }
  }

  protected suspend fun awaitGutterAnnotations(): Any? =
    CoverageDataAnnotationsManager.getInstance(project).allRequestsCompletion.await()

  protected suspend fun openFileInEditor(relativePath: String): VirtualFile {
    val file = readAction { projectFile(relativePath) }
    withContext(Dispatchers.EDT) {
      writeIntentReadAction { FileEditorManager.getInstance(project).openFile(file, true) }
    }
    awaitGutterAnnotations()
    return file
  }

  /**
   * The covered/uncovered line markers painted in the editor gutter for [relativePath], or `null` if none.
   * Lines are 1-based; values are [LineCoverage] bytes.
   */
  protected suspend fun gutterCoverage(relativePath: String): Map<Int, Byte>? = withContext(Dispatchers.EDT) {
    val file = projectFile(relativePath)
    val editor = FileEditorManager.getInstance(project).getEditors(file)
      .filterIsInstance<TextEditor>()
      .map { it.editor }
      .filterIsInstance<EditorImpl>()
      .firstOrNull() ?: return@withContext null
    val highlighters = editor.getUserData(CoverageEditorAnnotatorImpl.COVERAGE_HIGHLIGHTERS) ?: return@withContext null
    highlighters.associate { it.document.getLineNumber(it.startOffset) + 1 to coverageOf(it) }
  }

  protected suspend fun assertCoverageTree(bundle: CoverageSuitesBundle, expected: String) {
    CoverageViewManager.getInstance(project).stateBean.isShowOnlyModified = false
    val structure = CoverageViewTreeStructure(project, bundle)
    return withContext(Dispatchers.EDT) {
      val disposable = Disposer.newDisposable()
      try {
        val model = StructureTreeModel(structure, null, Invoker.forEventDispatchThread(disposable), disposable)
        val tree = writeIntentReadAction { javax.swing.JTree(model) }
        TreeUtil.promiseExpandAll(tree).await()
        PlatformTestUtil.assertTreeEqual(tree, expected)
      }
      finally {
        Disposer.dispose(disposable)
      }
    }
  }

  /** Absolute path of the opened project's base dir (where the reused `sources` were loaded from). */
  protected fun projectBasePath(): String = project.guessProjectDir()!!.path

  private fun projectFile(relativePath: String): VirtualFile =
    project.guessProjectDir()!!.findFileByRelativePath(relativePath)!!

  private fun coverageOf(highlighter: RangeHighlighter): Byte =
    when ((highlighter.lineMarkerRenderer as FillingLineMarkerRenderer).getTextAttributesKey()) {
      CodeInsightColors.LINE_FULL_COVERAGE -> LineCoverage.FULL
      CodeInsightColors.LINE_PARTIAL_COVERAGE -> LineCoverage.PARTIAL
      CodeInsightColors.LINE_NONE_COVERAGE -> LineCoverage.NONE
      else -> error("Unexpected gutter highlighting")
    }

  private inner class CoverageReportDescriptorMock(private val metadata: Map<String, ReportMetadata>) : ReportDescriptor {
    override val isReportAvailableFlow: Flow<NotificationCallback?> = emptyFlow()
    override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()
    override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()
    override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

    override suspend fun refreshReport(): ReportDescriptor = error("must not be invoked")

    override suspend fun loadReport(project: Project): LoadedReport =
      LoadedReport.Sarif(ValidatedSarif(SarifReport().withRuns(emptyList())), AggregatedReportMetadata(metadata), "")

    override fun hashCode(): Int = metadata.keys.hashCode()

    override fun equals(other: Any?): Boolean = other is CoverageReportDescriptorMock && other.metadata == metadata
  }
}
