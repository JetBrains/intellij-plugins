package org.jetbrains.qodana.highlight

import com.intellij.codeInsight.hint.TooltipLinkHandlerEP
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.mock.MockDocument
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.ImaginaryEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.MultiformatMessageString
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.ReportingDescriptor
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import com.jetbrains.qodana.sarif.model.Tool
import com.jetbrains.qodana.sarif.model.ToolComponent
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.reinstansiateService
import org.jetbrains.qodana.report.AggregatedReportMetadata
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.report.BrowserViewProvider
import org.jetbrains.qodana.report.LoadedReport
import org.jetbrains.qodana.report.NoProblemsContentProvider
import org.jetbrains.qodana.report.NotificationCallback
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.report.ValidatedSarif
import org.jetbrains.qodana.runDispatchingOnUi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@TestApplication
class SarifInspectionDescriptionLinkHandlerTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private lateinit var inspectionFromPlatformWithDescription: InspectionToolWrapper<*, *>

  @BeforeEach
  fun setUp() {
    qodanaFixture.reinstansiateService(qodanaFixture.project, QodanaHighlightedReportService(qodanaFixture.project, qodanaFixture.scope))
    initPlatformInspections()
  }

  private fun initPlatformInspections() {
    val inspectionProfileManager = (InspectionProfileManager.getInstance(qodanaFixture.project) as ProjectInspectionProfileManager)
    val previousProfile = inspectionProfileManager.currentProfile

    InspectionProfileImpl.INIT_INSPECTIONS = true
    val profile = InspectionProfileImpl("profile_with_platform_inspections")
    inspectionProfileManager.setCurrentProfile(profile)

    Disposer.register(qodanaFixture.testRootDisposable) {
      InspectionProfileImpl.INIT_INSPECTIONS = false
      inspectionProfileManager.setCurrentProfile(previousProfile)
      inspectionProfileManager.deleteProfile(profile)
    }
    inspectionFromPlatformWithDescription =
      profile.tools.asSequence().map { it.tool }.first { it.id == it.shortName && it.loadDescription() != null }
  }

  private val highlightedReportService: QodanaHighlightedReportService
    get() = QodanaHighlightedReportService.getInstance(qodanaFixture.project)

  private val editorMock: Editor
    get() = ImaginaryEditor(qodanaFixture.project, MockDocument())

  @Test
  fun `test inspection available from platform get description from platform`(): Unit = runDispatchingOnUi {
    val inspectionShortName = inspectionFromPlatformWithDescription.shortName
    val expectedInspectionDescription = inspectionFromPlatformWithDescription.loadDescription()

    val reportDescriptor = SingleInspectionProblemReportDescriptor(
      inspectionId = inspectionShortName,
      markdownDescription = "instead of this must be loaded platform inspection description",
      textDescription = "instead of this must be loaded platform inspection description"
    )
    highlightedReportService.highlightReport(reportDescriptor)
    dispatchAllTasksOnUi()

    assertThat(TooltipLinkHandlerEP.getDescription(SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(inspectionShortName), editorMock))
      .isEqualTo(expectedInspectionDescription)
  }

  @Test
  fun `test inspection not available from platform with sarif markdown get description from markdown`(): Unit = runDispatchingOnUi {
    val inspectionId = UUID.randomUUID().toString()
    @Language("HTML")
    val expectedInspectionDescription = """
      <html>
      <head></head>
      <body>
      <p>Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases.</p>
      <p>Types of function parameters can be specified in docstrings or in Python 3 function annotations.</p>
      <p><strong>Example:</strong></p>
      <pre><code>def foo() -&gt; int:
          return "abc" # Expected int, got str
      
      
      a: str
      a = foo() # Expected str, got int
      </code></pre>
      <p>With the quick-fix, you can modify the problematic types:</p>
      <pre><code>def foo() -&gt; str:
          return "abc"
      
      
      a: str
      a = foo()
      </code></pre>
      </body>
      </html>
    """.trimIndent()

    @Language("MarkDown")
    val markdownDescription = """
      Reports type errors in function call expressions, targets, and return values. In a dynamically typed language, this is possible in a limited number of cases.
      
      Types of function parameters can be specified in
      docstrings or in Python 3 function annotations.
      
      **Example:**
      
      ```
      def foo() -> int:
          return "abc" # Expected int, got str
      
      
      a: str
      a = foo() # Expected str, got int
      ```
      
      With the quick-fix, you can modify the problematic types:
      
      ```
      def foo() -> str:
          return "abc"
      
      
      a: str
      a = foo()
      ```
    """.trimIndent()

    val reportDescriptor = SingleInspectionProblemReportDescriptor(
      inspectionId = inspectionId,
      markdownDescription = markdownDescription,
      textDescription = "instead of this must be loaded from markdown"
    )
    highlightedReportService.highlightReport(reportDescriptor)
    dispatchAllTasksOnUi()

    assertThat(TooltipLinkHandlerEP.getDescription(SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(inspectionId), editorMock))
      .isEqualTo(expectedInspectionDescription)
  }

  @Test
  fun `test inspection not available from platform only with plain text get description as plain text`(): Unit = runDispatchingOnUi {
    val inspectionId = UUID.randomUUID().toString()
    val expectedInspectionDescription = "description in plain text"

    val reportDescriptor = SingleInspectionProblemReportDescriptor(
      inspectionId = inspectionId,
      markdownDescription = null,
      textDescription = expectedInspectionDescription
    )
    highlightedReportService.highlightReport(reportDescriptor)
    dispatchAllTasksOnUi()

    assertThat(TooltipLinkHandlerEP.getDescription(SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(inspectionId), editorMock))
      .isEqualTo(expectedInspectionDescription)
  }

  @Test
  fun `test inspection not available from platform empty description in sarif`(): Unit = runDispatchingOnUi {
    val inspectionId = UUID.randomUUID().toString()

    val reportDescriptor = SingleInspectionProblemReportDescriptor(
      inspectionId = inspectionId,
      markdownDescription = null,
      textDescription = null
    )
    highlightedReportService.highlightReport(reportDescriptor)
    dispatchAllTasksOnUi()

    assertThat(TooltipLinkHandlerEP.getDescription(SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(inspectionId), editorMock))
      .isEqualTo(null)
  }

  @Test
  fun `test unknown inspection not present in platform and sarif`(): Unit = runDispatchingOnUi {
    val inspectionId = UUID.randomUUID().toString()

    val reportDescriptor = SingleInspectionProblemReportDescriptor(
      inspectionId = "some other inspection id",
      markdownDescription = "this must not be loaded",
      textDescription = "this must not be loaded"
    )
    highlightedReportService.highlightReport(reportDescriptor)
    dispatchAllTasksOnUi()

    assertThat(TooltipLinkHandlerEP.getDescription(SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(inspectionId), editorMock))
      .isEqualTo(null)
  }

  @Test
  fun `test inspection from platform no description because no report is highlighted`(): Unit = runDispatchingOnUi {
    val inspectionShortName = inspectionFromPlatformWithDescription.shortName
    assertThat(TooltipLinkHandlerEP.getDescription(SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(inspectionShortName), editorMock))
      .isEqualTo(null)
  }

  @Test
  fun `test inspection from platform no description during loading of report`(): Unit = runDispatchingOnUi {
    val inspectionShortName = inspectionFromPlatformWithDescription.shortName

    qodanaFixture.scope.launch(QodanaDispatchers.Default) {
      highlightedReportService.highlightReport(InfiniteLoadingReportDescriptorMock)
    }
    dispatchAllTasksOnUi()

    assertThat(TooltipLinkHandlerEP.getDescription(SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(inspectionShortName), editorMock))
      .isEqualTo(null)
  }
}

private class SingleInspectionProblemReportDescriptor(
  private val inspectionId: String,
  private val markdownDescription: String?,
  private val textDescription: String?
) : ReportDescriptor {
  override val isReportAvailableFlow: Flow<NotificationCallback?> = emptyFlow()

  override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

  override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

  override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

  override suspend fun refreshReport(): ReportDescriptor? = error("must not be invoked")

  override suspend fun loadReport(project: Project) = LoadedReport.Sarif(ValidatedSarif(createSarifWithSignleInspectionProblem()), AggregatedReportMetadata(emptyMap()), "")

  override fun hashCode(): Int = System.identityHashCode(this)

  override fun equals(other: Any?): Boolean = (this === other)

  private fun createSarifWithSignleInspectionProblem(): SarifReport {
    val fullDescription = MultiformatMessageString()
      .withMarkdown(markdownDescription)
      .withText(textDescription)
    val rule = ReportingDescriptor()
      .withId(inspectionId)
      .withFullDescription(fullDescription)
    val extension = ToolComponent()
      .withRules(listOf(rule))
      .withFullDescription(fullDescription)
    val tool = Tool()
      .withExtensions(setOf(extension))

    val artifactLocation = ArtifactLocation()
      .withUri("some_path")
      .withUriBaseId("SRCROOT")
    val physicalLocation = PhysicalLocation()
      .withArtifactLocation(artifactLocation)
      .withRegion(Region())
    val location = Location()
      .withPhysicalLocation(physicalLocation)

    val result = Result()
      .withRuleId(inspectionId)
      .withLocations(listOf(location))

    val run = Run()
      .withTool(tool)
      .withResults(listOf(result))

    return SarifReport().withRuns(listOf(run))
  }
}


private object InfiniteLoadingReportDescriptorMock : ReportDescriptor {
  override val isReportAvailableFlow: Flow<NotificationCallback?> = emptyFlow()

  override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

  override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

  override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

  override suspend fun refreshReport(): ReportDescriptor = error("Must not be invoked")

  override suspend fun loadReport(project: Project): LoadedReport = awaitCancellation()

  override fun hashCode(): Int = System.identityHashCode(this)

  override fun equals(other: Any?): Boolean {
    return this === other
  }
}
