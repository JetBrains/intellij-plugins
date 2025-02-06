package org.jetbrains.qodana.highlight

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.navigation.JBProtocolRevisionResolver
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.psi.PsiManager
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.jetbrains.qodana.sarif.model.*
import com.siyeh.ig.bugs.EmptyStatementBodyInspection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.report.*
import org.jetbrains.qodana.run.RUN_TIMESTAMP
import org.jetbrains.qodana.staticAnalysis.sarif.withSuppressToolId
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString

private const val QODANA_PLUGIN_HIGHLIGHTING_KEY = "qodana_problem"
private const val REVISIONS_DIR = "revisions"

/**
 * Since our highlighting depends on presence of file's git revision, we simulate the git history in the following way:
 *
 * All project revisions must be stored in project directory in directory [REVISIONS_DIR]

 * For example, if you want to have project with files "A.java" and "B.java"
 * and revisions of this project with names "initial" and "latest"
 * your project file structure must look like this:
 * ```
 * root
 * ├── A.java
 * ├── B.java
 * └── [REVISIONS_DIR]
 *     ├── initial
 *     │   └── A.java
 *     └── latest
 *         ├── A.java
 *         └── B.java
 * ```
 */
class QodanaReportHighlightingPassTest : QodanaPluginLightTestBase() {
  override fun getBasePath() = Path(super.getBasePath(), "highlight").pathString

  override fun setUp() {
    super.setUp()

    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
    setStressTestToEnablePsiCachingInTests()

    copyProjectTestData("project")
    configureGit()
    initPlatformInspections()

    reinstansiateService(project, QodanaHighlightedReportService(project, scope))
  }

  private val reportLatestRevision = ReportDescriptorTestMock(
    "report_latest",
    listOf(
      ProblemTestDescription("main", "src/Main.java", 3, 22, 4,
                             "latest", snippet = "main"),
      ProblemTestDescription("code in functionWithSpacesIndent", "src/Main.java", 8, 7, 4,
                             "latest", snippet = "code"),
      ProblemTestDescription("code in functionWithTabulationIndent", "src/Main.java", 12, 5, 4,
                             "latest", snippet = "code")
    )
  )

  fun `test no highlight when report is not selected`() = runDispatchingOnUi {
    openFileInEditorByRelativePath("src/Main.java")

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test no highlight when report is loading`() = runDispatchingOnUi {
    reportLatestRevision.setStatusLoading()
    reportLatestRevision.problems[0].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision delete line before`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, _ ->
      document.deleteString(document.getLineStartOffset(1), document.getLineEndOffset(1) + 1)
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision delete line after`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, _ ->
      document.deleteString(document.getLineStartOffset(5), document.getLineEndOffset(5) + 1)
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision delete lines before and after`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, _ ->
      document.deleteString(document.getLineStartOffset(12), document.getLineEndOffset(12) + 1)
      document.deleteString(document.getLineStartOffset(9), document.getLineEndOffset(9) + 1)
      document.deleteString(document.getLineStartOffset(5), document.getLineEndOffset(5) + 1)
      document.deleteString(document.getLineStartOffset(1), document.getLineEndOffset(1) + 1)
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision insert line before`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, _ ->
      document.insertString(document.getLineStartOffset(1), "  //inserted line\n")
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision insert line after`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, _ ->
      document.insertString(document.getLineStartOffset(5), "  //inserted line\n")
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision insert lines before and after`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, _ ->
      document.insertString(document.getLineStartOffset(12), "\t\t//inserted line\n")
      document.insertString(document.getLineStartOffset(9), "  //inserted line\n")
      document.insertString(document.getLineStartOffset(5), "  //inserted line\n")
      document.insertString(document.getLineStartOffset(1), "  //inserted line\n")
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision insert text before problem with snippet`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[2].openFileInEditor()
    reportLatestRevision.problems[2].editDocument { document, problemRange ->
      document.insertString(problemRange.startOffset, "ADDED_TEXT ")
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision insert text after problem with snippet`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[2].openFileInEditor()
    reportLatestRevision.problems[2].editDocument { document, problemRange ->
      document.insertString(problemRange.endOffset, " ADDED_TEXT")
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }


  fun `test highlight report latest revision delete text after problem with snippet`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[2].openFileInEditor()
    reportLatestRevision.problems[2].editDocument { document, problemRange ->
      document.deleteString(problemRange.endOffset, problemRange.endOffset + 3)
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision delete text after problem without snippet`() = runDispatchingOnUi {
    val reportFeatureRevision = ReportDescriptorTestMock(
      "report_latest",
      listOf(
        ProblemTestDescription("main", "src/Main.java", 3, 22, 4, "latest"),
        ProblemTestDescription("code in functionWithSpacesIndent", "src/Main.java", 8, 7, 4, "latest"),
        ProblemTestDescription("code in functionWithTabulationIndent", "src/Main.java", 12, 5, 4, "latest")
      )
    )

    reportFeatureRevision.setStatusHighlighted()
    reportFeatureRevision.problems[2].openFileInEditor()
    reportLatestRevision.problems[2].editDocument { document, problemRange ->
      document.deleteString(problemRange.endOffset, problemRange.endOffset + 3)
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision complex editing`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[2].openFileInEditor()
    reportLatestRevision.problems[2].editDocument { document, _ ->
      document.deleteString(document.getLineStartOffset(3), document.getLineEndOffset(3) + 1)
      document.deleteString(document.getLineStartOffset(4), document.getLineEndOffset(4) + 1)
      document.insertString(document.getLineStartOffset(5), "    //new comment\n")
      document.insertString(document.getLineStartOffset(6), "    int i = 0;\n")
      document.insertString(document.getLineStartOffset(7), "    if (true) {\n")
      document.insertString(document.getLineStartOffset(8), "  ")
      document.insertString(document.getLineStartOffset(9), "    }\n")
      document.insertString(document.getLineStartOffset(10), "    int j = i;\n")
      document.insertString(document.getLineStartOffset(13), "  void empty() {};\n")
      document.insertString(document.getLineStartOffset(14), "\n")
      document.insertString(document.getLineEndOffset(16), " more text")
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report latest revision another complex editing`() = runDispatchingOnUi {
    reportLatestRevision.setStatusHighlighted()
    reportLatestRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, problemRange ->
      document.deleteString(problemRange.startOffset - 19, problemRange.startOffset - 6)
      document.insertString(document.getLineStartOffset(2) + 2, "private")
      document.insertString(document.getLineStartOffset(2) + 33, ", int a")
      document.insertString(document.getLineStartOffset(7), "\n")
      document.insertString(document.getLineStartOffset(8), "\n")
      document.insertString(document.getLineStartOffset(10), "\n")
      document.insertString(document.getLineStartOffset(11), "    //new comment\n")
      document.insertString(document.getLineStartOffset(15), "\t\t//new comment\n")
      document.insertString(document.getLineStartOffset(16) + 2, "int beforeComment = -1; ")
      document.deleteString(document.getLineStartOffset(17), document.getLineEndOffset(17) + 1)
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report feature revision problem only in feature revision`() = runDispatchingOnUi {
    val reportFeatureRevision = ReportDescriptorTestMock(
      "report_feature",
      listOf(
        ProblemTestDescription("main", "src/Main.java", 3, 22, 4, "feature"),
        ProblemTestDescription("code in functionWithSpacesIndent", "src/Main.java", 12, 7, 4, "feature"),
        ProblemTestDescription("code in functionWithTabulationIndent", "src/Main.java", 16, 5, 4, "feature"),
        ProblemTestDescription("code in functionOnlyInFeatureRevision", "src/Main.java", 8, 7, 4, "feature")
      )
    )

    reportFeatureRevision.setStatusHighlighted()
    reportFeatureRevision.problems[3].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report old revision ide run problems`() = runDispatchingOnUi {
    val reportFeatureRevision = ReportDescriptorTestMock(
      "report_feature",
      listOf(
        ProblemTestDescription("comments", "src/Main.java", 4, 5, 23, "old"),
        ProblemTestDescription("comments", "src/Main.java", 8, 5, 9, "old"),
        ProblemTestDescription("comments", "src/Main.java", 12, 3, 9, "old")
      ),
      isIdeRun = true
    )

    reportFeatureRevision.setStatusHighlighted()
    reportFeatureRevision.problems[1].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report old revision ide run problems then modified`() = runDispatchingOnUi {
    val reportFeatureRevision = ReportDescriptorTestMock(
      "report_feature",
      listOf(
        ProblemTestDescription("comments", "src/Main.java", 4, 5, 23, "old"),
        ProblemTestDescription("comments", "src/Main.java", 8, 5, 9, "old"),
        ProblemTestDescription("comments", "src/Main.java", 12, 3, 9, "old")
      ),
      isIdeRun = true
    )

    reportFeatureRevision.setStatusHighlighted()
    reportFeatureRevision.problems[0].openFileInEditor()
    reportLatestRevision.problems[0].editDocument { document, _ ->
      document.insertString(document.getLineStartOffset(1), "\n")
    }

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report old revision not ide run still highlighted`() = runDispatchingOnUi {
    val reportFeatureRevision = ReportDescriptorTestMock(
      "report_feature",
      listOf(
        ProblemTestDescription("comments", "src/Main.java", 4, 5, 23, "old"),
        ProblemTestDescription("comments", "src/Main.java", 8, 5, 9, "old"),
        ProblemTestDescription("comments", "src/Main.java", 12, 3, 9, "old")
      ),
      isIdeRun = false
    )

    reportFeatureRevision.setStatusHighlighted()
    reportFeatureRevision.problems[1].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report invalid revision valid problems`() = runDispatchingOnUi {
    val reportInvalidRevisionValidProblems = ReportDescriptorTestMock(
      "report_invalid_revision_valid_problems",
      listOf(
        ProblemTestDescription("main", "src/Main.java", 3, 22, 4, "invalid"),
        ProblemTestDescription("code in functionWithSpacesIndent", "src/Main.java", 8, 7, 4, "invalid"),
        ProblemTestDescription("code in functionWithTabulationIndent", "src/Main.java", 12, 5, 4, "invalid")
      ),
    )

    reportInvalidRevisionValidProblems.setStatusHighlighted()
    reportInvalidRevisionValidProblems.problems[0].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report invalid revision invalid problems`() = runDispatchingOnUi {
    val reportInvalidRevisionValidProblems = ReportDescriptorTestMock(
      "report_invalid_revision_valid_problems",
      listOf(
        ProblemTestDescription("line value is too big", "src/Main.java", 50, 1, 1, "invalid"),
        ProblemTestDescription("column value is too big", "src/Main.java", 3, 50, 1, "invalid"),
        ProblemTestDescription("invalid file", "src/NotExistingFile.java", 1, 1, 1, "invalid")
      )
    )

    reportInvalidRevisionValidProblems.setStatusHighlighted()
    reportInvalidRevisionValidProblems.problems[0].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test highlight report problems with range of multiple lines`() = runDispatchingOnUi {
    val reportWithProblemsMultipleLinesRange = ReportDescriptorTestMock(
      "multiple_lines_report",
      listOf(
        ProblemTestDescription("lines 1-5", "src/Main.java", 1, 1, 95, "latest"),
        ProblemTestDescription("lines 11-15", "src/Main.java", 11, 1, 113, "latest")
      )
    )

    reportWithProblemsMultipleLinesRange.setStatusHighlighted()
    reportWithProblemsMultipleLinesRange.problems[0].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test multiple runs all highlighted`() = runDispatchingOnUi {
    val reportWithProblemsMultipleLinesRange = ReportDescriptorTestMock(
      "multiple_lines_report",
      listOf(
        ProblemTestDescription("lines 1-5", "src/Main.java", 1, 1, 95, "latest"),
        ProblemTestDescription("lines 11-15", "src/Main.java", 11, 1, 113, "feature"),
        ProblemTestDescription("line value is too big", "src/Main.java", 50, 1, 1, "invalid"),
        )
    )

    reportWithProblemsMultipleLinesRange.setStatusHighlighted()
    reportWithProblemsMultipleLinesRange.problems[0].openFileInEditor()

    checkHighlighting("$name/MainExpectedHighlight.java")
  }

  fun `test one statement suppressed other not`() = runDispatchingOnUi {
    val reportWithEmptyMethodProblems = ReportDescriptorTestMock(
      "suppress_test",
      listOf(
        ProblemTestDescription("error_1", "src/Logic.java", 4, 3, 21, "latest", inspectionId = "EmptyMethod"),
        ProblemTestDescription("error_2", "src/Logic.java", 8, 3, 21, "latest", inspectionId = "EmptyMethod")
      )
    )

    reportWithEmptyMethodProblems.setStatusHighlighted()
    reportWithEmptyMethodProblems.problems[0].openFileInEditor()

    checkHighlighting("$name/LogicExpectedHighlight.java")
  }

  fun `test don't suppress other inspections`() = runDispatchingOnUi {
    val reportWithUnusedProblems = ReportDescriptorTestMock(
      "suppress_test",
      listOf(
        ProblemTestDescription("error_1", "src/Logic.java", 4, 3, 21, "latest", inspectionId = "unused"),
        ProblemTestDescription("error_2", "src/Logic.java", 8, 3, 21, "latest", inspectionId = "unused")
      )
    )

    reportWithUnusedProblems.setStatusHighlighted()
    reportWithUnusedProblems.problems[0].openFileInEditor()

    checkHighlighting("$name/LogicExpectedHighlight.java")
  }

  fun `test suppress all for class`() = runDispatchingOnUi {
    val reportWithConstantValueProblems = ReportDescriptorTestMock(
      "suppress_test",
      listOf(
        ProblemTestDescription("error_1", "src/Logic.java", 4, 3, 21,  "latest", inspectionId = "ConstantValue"),
        ProblemTestDescription("error_2", "src/Logic.java", 8, 3, 21, "latest", inspectionId = "ConstantValue")
      )
    )

    reportWithConstantValueProblems.setStatusHighlighted()
    reportWithConstantValueProblems.problems[0].openFileInEditor()

    checkHighlighting("$name/LogicExpectedHighlight.java")
  }

  fun `test suppress with tool id in sarif`() = runDispatchingOnUi {
    val reportWithUnusedProblems = ReportDescriptorTestMock(
      "suppress_test",
      listOf(
        ProblemTestDescription("error_1", "src/Logic.java", 4, 3, 21, "latest", inspectionId = "unused"),
        ProblemTestDescription("error_2", "src/Logic.java", 8, 3, 21, "latest", inspectionId = "unused")
      ),
      listOf(
        RuleTestDescription("unused", "ConstantValue")
      )
    )

    reportWithUnusedProblems.setStatusHighlighted()
    reportWithUnusedProblems.problems[0].openFileInEditor()

    checkHighlighting("$name/LogicExpectedHighlight.java")
  }

  fun `test clashed highlightings when file opened`() = runDispatchingOnUi {
    val reportWithProblemsMultipleLinesRange = ReportDescriptorTestMock(
      "multiple_lines_report",
      listOf(
        ProblemTestDescription("Empty body", "src/Main.java", 13, 3, 2, "latest", inspectionId = "EmptyStatementBody"),
      )
    )

    reportWithProblemsMultipleLinesRange.setStatusHighlighted()
    reportWithProblemsMultipleLinesRange.problems[0].openFileInEditor()

    val currentFile = myFixture.file!!

    val expectedDocument1 = loadAdditionalTestDataFile("$name/MainExpectedHighlight1.java").getDocument()!!
    val expectedHighlightingData1 = expectedDocument1.extractHighlightingData()
    prepareForHighlight()
    val realHighlightingInfo1 = myFixture.doHighlighting()
    expectedHighlightingData1.checkResult(currentFile, realHighlightingInfo1, currentFile.text)

    myFixture.enableInspections(EmptyStatementBodyInspection::class.java)

    val expectedDocument2 = loadAdditionalTestDataFile("$name/MainExpectedHighlight2.java").getDocument()!!
    val expectedHighlightingData2 = expectedDocument2.extractHighlightingData()
    prepareForHighlight()
    val realHighlightingInfo2 = myFixture.doHighlighting()
    assertThat(realHighlightingInfo1.size).isEqualTo(realHighlightingInfo2.size)
    expectedHighlightingData2.checkResult(currentFile, realHighlightingInfo2, currentFile.text)
    myFixture.disableInspections()
  }

  fun `test highlightings with enabled local inspection`() = runDispatchingOnUi {
    val reportWithProblemsMultipleLinesRange = ReportDescriptorTestMock(
      "multiple_lines_report",
      listOf(
        ProblemTestDescription("Empty body", "src/Main.java", 13, 3, 2, "latest", inspectionId = "EmptyStatementBody"),
        ProblemTestDescription("Empty body", "src/Main.java", 14, 3, 2, "latest", inspectionId = "EmptyStatementBody"),
      )
    )

    reportWithProblemsMultipleLinesRange.setStatusHighlighted()
    reportWithProblemsMultipleLinesRange.problems[0].openFileInEditor()

    val currentFile = myFixture.file!!

    val expectedDocument1 = loadAdditionalTestDataFile("$name/MainExpectedHighlight1.java").getDocument()!!
    val expectedHighlightingData1 = expectedDocument1.extractHighlightingData()
    prepareForHighlight()
    val realHighlightingInfo1 = myFixture.doHighlighting()
    expectedHighlightingData1.checkResult(currentFile, realHighlightingInfo1, currentFile.text)

    myFixture.enableInspections(EmptyStatementBodyInspection::class.java)

    val expectedDocument2 = loadAdditionalTestDataFile("$name/MainExpectedHighlight2.java").getDocument()!!
    val expectedHighlightingData2 = expectedDocument2.extractHighlightingData()
    prepareForHighlight()
    myFixture.doHighlighting()
    val realHighlightingInfo2 = myFixture.doHighlighting()
    assertThat(realHighlightingInfo1.size).isEqualTo(realHighlightingInfo2.size + 1)
    expectedHighlightingData2.checkResult(currentFile, realHighlightingInfo2, currentFile.text)
  }

  private fun setStressTestToEnablePsiCachingInTests() {
    val oldStressTestStatus = ApplicationManagerEx.isInStressTest()
    ApplicationManagerEx.setInStressTest(true)
    Disposer.register(testRootDisposable) {
      ApplicationManagerEx.setInStressTest(oldStressTestStatus)
    }
  }

  private fun configureGit() {
    JBProtocolRevisionResolver.EP_NAME.point.registerExtension(
      JBProtocolRevisionResolver { _, absolutePath, revision ->
        val projectDirectory = projectDir
        val projectPath = Path(projectDirectory!!.path)
        val pathToFile = Path(absolutePath)
        if (!pathToFile.startsWith(projectPath))
          return@JBProtocolRevisionResolver null

        val relativePathFromProjectDir = projectPath.relativize(pathToFile).pathString
        projectDirectory.findFileByRelativePath(Path(REVISIONS_DIR, revision, relativePathFromProjectDir).invariantSeparatorsPathString)
      },
      testRootDisposable
    )
  }

  private fun initPlatformInspections() {
    val inspectionProfileManager = (InspectionProfileManager.getInstance(project) as ProjectInspectionProfileManager)
    val previousProfile = inspectionProfileManager.currentProfile

    InspectionProfileImpl.INIT_INSPECTIONS = true
    val profile = InspectionProfileImpl("profile_with_platform_inspections")
    profile.disableAllTools(project)
    inspectionProfileManager.addProfile(profile)
    inspectionProfileManager.setCurrentProfile(profile)

    Disposer.register(testRootDisposable) {
      InspectionProfileImpl.INIT_INSPECTIONS = false
      inspectionProfileManager.setCurrentProfile(previousProfile)
      inspectionProfileManager.deleteProfile(profile)
    }
  }

  private fun checkHighlighting(pathToExpectedFileInTestData: String) {
    val expectedDocument = loadAdditionalTestDataFile(pathToExpectedFileInTestData).getDocument()!!
    val expectedHighlightingData = expectedDocument.extractHighlightingData()

    prepareForHighlight()
    val realHighlightingInfo = myFixture.doHighlighting()
    val currentFile = myFixture.file!!
    expectedHighlightingData.checkResult(currentFile, realHighlightingInfo, currentFile.text)
  }

  private fun Document.extractHighlightingData(): ExpectedHighlightingData {
    val expectedHighlightingData = ExpectedHighlightingData(this, false, false, false).apply {
      registerHighlightingType(
        CodeInsightTestFixture.ERROR_MARKER,
        ExpectedHighlightingData.ExpectedHighlightingSet(HighlightSeverity.ERROR, false, false)
      )
      registerHighlightingType(
        QODANA_PLUGIN_HIGHLIGHTING_KEY,
        ExpectedHighlightingData.ExpectedHighlightingSet(HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING, false, true)
      )
      init()
    }
    return expectedHighlightingData
  }

  private fun prepareForHighlight() {
    val revisionIds = QodanaHighlightedReportService.getInstance(project)
                        .highlightedReportState.value.highlightedReportDataIfSelected?.allProblems?.map { it.revisionId } ?: return
    val revisionId = revisionIds[0] ?: return

    val filePath = myFixture.file.virtualFile.canonicalPath
    val revisionFile = filePath?.let { revisionId.let { JBProtocolRevisionResolver.processResolvers(project!!, filePath, revisionId) } }
    val revisionPsiFile = revisionFile?.let { PsiManager.getInstance(project!!).findFile(revisionFile) }

    val data = revisionIds.associateWith { revisionPsiFile }

    val ideRunTimestamp = QodanaHighlightedReportService.getInstance(project)
                        .highlightedReportState.value.highlightedReportDataIfSelected?.ideRunData?.ideRunTimestamp

    if (ideRunTimestamp == null) {
      myFixture.editor.document.putUserData(QODANA_REVISION_DATA, QodanaRevisionData.VCSInfo(data))
    } else {
      val createdDocument = EditorFactory.getInstance().createDocument(myFixture.file.text)
      myFixture.editor.document.putUserData(QODANA_REVISION_DATA,QodanaRevisionData.LocalInfo(
        QodanaLocalDocumentData(ideRunTimestamp, createdDocument))
      )
    }
  }

  private inner class ReportDescriptorTestMock(
    val id: String,
    val problems: List<ProblemTestDescription>,
    val rules: List<RuleTestDescription> = emptyList(),
    val isIdeRun: Boolean = false
  ) : ReportDescriptor {
    override val isReportAvailableFlow: Flow<NotificationCallback?> = emptyFlow()

    override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

    override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

    override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

    private val reportDeferred = CompletableDeferred<SarifReport>()

    override suspend fun refreshReport(): ReportDescriptor? = error("must not be invoked")

    override suspend fun loadReport(project: Project) = LoadedReport.Sarif(ValidatedSarif(reportDeferred.await()), AggregatedReportMetadata(emptyMap()), "")

    fun setStatusLoading() {
      scope.launch(QodanaDispatchers.Default) {
        QodanaHighlightedReportService.getInstance(project).highlightReport(this@ReportDescriptorTestMock)
      }
      dispatchAllTasksOnUi()
    }

    fun setStatusHighlighted() {
      completeLoading()
      scope.launch(QodanaDispatchers.Default) {
        QodanaHighlightedReportService.getInstance(project).highlightReport(this@ReportDescriptorTestMock)
      }
      dispatchAllTasksOnUi()
    }

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
      if (other !is ReportDescriptorTestMock) return false

      return id == other.id
    }

    private fun completeLoading() {
      val problemsToRevision = problems.groupBy { it.revisionId }
      val results = problemsToRevision.mapValues { (_, problems) ->
        problems.map {
          Result(Message().withText(it.message))
            .withLocations(listOf(
              Location()
                .withPhysicalLocation(
                  PhysicalLocation()
                    .withArtifactLocation(
                      ArtifactLocation().withUri(it.file).withUriBaseId("SRCROOT"))
                    .withRegion(
                      Region().withStartColumn(it.column).withStartLine(it.line).withCharLength(it.charLength)
                        .withSnippet(ArtifactContent().withText(it.snippet))
                    )
                )
            ))
            .withRuleId(it.inspectionId)
        }
      }

      val rulesDescriptors = rules.map {
        ReportingDescriptor(it.name)
          .withDefaultConfiguration(
            ReportingConfiguration()
              .withEnabled(true).withParameters(PropertyBag().withSuppressToolId(it.suppressToolId))
          )
      }

      val tool = Tool().withExtensions(setOf(ToolComponent().withRules(rulesDescriptors)))

      val runs = results.map {(revision, results) ->
        val run = Run().withResults(results).withTool(tool)
        if (revision != null)
          run.withVersionControlProvenance(setOf(VersionControlDetails().withRevisionId(revision)))
        run
      }

      val report = SarifReport()
      if (isIdeRun) {
        report.withProperties(PropertyBag().also {
          it[RUN_TIMESTAMP] = System.currentTimeMillis()
        })
      }

      reportDeferred.complete(report.withRuns(runs))
    }
  }

  private data class ProblemTestDescription(
    val message: String,
    val file: String,
    val line: Int,
    val column: Int,
    val charLength: Int,
    val revisionId: String? = null,
    val snippet: String? = null,
    val inspectionId: String? = "INSPECTION_ID"
  )

  private fun ProblemTestDescription.openFileInEditor() = openFileInEditorByRelativePath(file)

  private fun ProblemTestDescription.editDocument(editAction: (Document, TextRange) -> Unit) {
    WriteCommandAction.runWriteCommandAction(project) {
      editAction.invoke(myFixture.editor.document, toTextRange())
    }
  }

  private fun ProblemTestDescription.toTextRange(): TextRange {
    val lineOffset = myFixture.editor.document.getLineStartOffset(line - 1)
    val problemOffset = lineOffset + (column - 1)
    return TextRange(problemOffset, problemOffset + charLength)
  }

  private data class RuleTestDescription(
    val name: String,
    val suppressToolId: String
  )

  private fun openFileInEditorByRelativePath(path: String) = myFixture.openFileInEditor(projectDir!!.findFileByRelativePath(path)!!)
}
