package org.jetbrains.qodana.highlight

import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil
import com.intellij.codeInsight.daemon.impl.analysis.DaemonTooltipsUtil
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.history.LocalHistory
import com.intellij.navigation.JBProtocolRevisionResolver
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.actions.QodanaRootIgnoreIntention
import org.jetbrains.qodana.actions.QodanaSuppressableProblemGroup
import org.jetbrains.qodana.actions.StopShowingReportAction
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.extensions.QodanaHighlightInfoComparator
import org.jetbrains.qodana.extensions.QodanaHighlightInfoTypeProvider
import org.jetbrains.qodana.extensions.QodanaHighlightingSupportInfoProvider
import org.jetbrains.qodana.extensions.RepositoryRevisionProvider
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.buildDescription
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.Path

internal class QodanaReportHighlightingPass(
  private val myFile: PsiFile,
  private val editor: Editor,
  private val reportService: QodanaHighlightedReportService,
  private val passState: QodanaHighlightingPassState
) : TextEditorHighlightingPass(myFile.project, editor.document), DumbAware {

  private val myHighlightInfos = AtomicReference<List<HighlightInfo>>(emptyList())

  private val enabledInspections = AtomicReference<Set<String>>(emptySet())

  override fun doCollectInformation(progress: ProgressIndicator) {
    val virtualFile = myFile.virtualFile ?: return
    val projectDir = myProject.guessProjectDir()?.let { Path(it.canonicalPath ?: it.path) } ?: return

    myHighlightInfos.set(emptyList())

    val highlights = mutableListOf<HighlightInfo>()

    val highlightedReportData = reportService.highlightedReportState.value.highlightedReportDataIfSelected
                                ?: return
    val virtualFilePath = virtualFile.canonicalPath ?: virtualFile.path
    val relevantProblems = highlightedReportData.getRelevantProblemsByFilePath(projectDir, Path(virtualFilePath))
      .filter { it.hasRange }
      .filter { problem ->
        !highlightedReportData.excludedDataFlow.value.any { data ->
          data.isRelatedToProblem(problem)
        }
      }

    if (relevantProblems.isEmpty()) return

    val revisionIds = relevantProblems.map { it.revisionId }.toSet()
    val localIdeRunTimestamp = highlightedReportData.ideRunData?.ideRunTimestamp

    val data = editor.document.getUserData(QODANA_REVISION_DATA)
    if (!isDataCorrect(data, localIdeRunTimestamp, revisionIds)) {
      passState.scope.launch(QodanaDispatchers.Default) {
        withBackgroundProgress(myProject, QodanaBundle.message("progress.title.qodana.highlight.calc", myFile.name)) {
          if (localIdeRunTimestamp != null) {
            putLocalRunData(localIdeRunTimestamp)
          }
          else {
            putRevisionData(revisionIds)
          }
          DaemonCodeAnalyzer.getInstance(myProject).restart(myFile)
        }
      }
      return
    }

    val rangesCalculator = QodanaRangeCalculator(myProject, myFile, document, highlightedReportData)

    val relevantProblemsWithRanges = rangesCalculator.calculateTextRanges(
      relevantProblems, data!!
    )
    highlights.addAll(relevantProblemsWithRanges.mapNotNull { (problem, range) ->
      val toolId = highlightedReportData.inspectionsInfoProvider.getSuppressIdByInspection(problem.inspectionId)
      buildHighlightInfo(problem, range, toolId)
    })

    if (virtualFile.isInLocalFileSystem) {
      updateProblemsRanges(highlightedReportData, relevantProblemsWithRanges)
    }

    runBlockingCancellable {
      val inspections = QodanaHighlightingSupportInfoProvider.getEnabledInspections(myProject)
      enabledInspections.set(inspections)
    }

    myHighlightInfos.set(highlights)
  }

  private fun isDataCorrect(data: QodanaRevisionData?, localIdeRunTimestamp: Long?, revisionIds: Set<String?>): Boolean {
    return when (data) {
      null -> false
      is QodanaRevisionData.VCSInfo -> {
        localIdeRunTimestamp == null &&
        data.revisionPsiFiles.keys.toSet() == revisionIds &&
        data.revisionPsiFiles.values.toSet().filterNotNull().all { it.isValid }
      }
      is QodanaRevisionData.LocalInfo -> {
        data.localDocumentData.timestamp == localIdeRunTimestamp
      }
    }
  }

  private fun putLocalRunData(localIdeRunTimestamp: Long) {
    val revisionFileContent = LocalHistory.getInstance().getByteContent(myFile.virtualFile) { t -> t <= localIdeRunTimestamp } ?: byteArrayOf()
    val text = LoadTextUtil.getTextByBinaryPresentation(revisionFileContent, myFile.virtualFile, false, false).toString()
    val createdDocument = EditorFactory.getInstance().createDocument(text)
    editor.document.putUserData(QODANA_REVISION_DATA, QodanaRevisionData.LocalInfo(
      QodanaLocalDocumentData(localIdeRunTimestamp, createdDocument))
    )
  }

  private fun putRevisionData(revisionIds: Set<String?>) {
    val psiFiles = revisionIds.associateWith {
      getPsiFileByRevision(myFile, it) ?: getPsiFileForLastRevision(myFile)
    }
    editor.document.putUserData(QODANA_REVISION_DATA, QodanaRevisionData.VCSInfo(psiFiles))
  }

  private fun updateProblemsRanges(highlightedReportData: HighlightedReportData,
                                   problemsWithRanges: List<ProblemWithRange>) {
    val propertiesUpdaters = problemsWithRanges.map { (problem, textRange) ->
      if (textRange == null) return@map SarifProblemPropertiesUpdater(problem) { it.copy(isMissing = true) }

      val line = document.getLineNumber(textRange.startOffset)
      val lineStartOffset = document.getLineStartOffset(line)

      val column = textRange.startOffset - lineStartOffset
      SarifProblemPropertiesUpdater(problem) { it.copy(isMissing = false, line = line, column = column) }
    }
    highlightedReportData.updateProblemsProperties(propertiesUpdaters)
  }

  private fun getPsiFileByRevision(myFile: PsiFile, revisionId: String?): PsiFile? {
    revisionId ?: return null
    val path = myFile.virtualFile.canonicalPath ?: return null
    val revisionFile = JBProtocolRevisionResolver.processResolvers(myProject, path, revisionId) ?: return null
    val psiFile = runReadAction { PsiManager.getInstance(myProject).findFile(revisionFile) }
    return if (psiFile?.isValid == false) null else psiFile
  }

  private fun getPsiFileForLastRevision(myFile: PsiFile): PsiFile? {
    val currentRevision = RepositoryRevisionProvider.getRepositoryRevision(myProject, myFile.virtualFile)
    return getPsiFileByRevision(myFile, currentRevision)
  }

  private fun buildHighlightInfo(problem: SarifProblem, textRange: TextRange?, suppressToolId: String?): HighlightInfo? {
    textRange ?: return null

    val description = problem.buildDescription(useQodanaPrefix = true, showSeverity = true)
    val tooltip = DaemonTooltipsUtil.getWrappedTooltipWithCustomReference(
      description,
      SarifInspectionDescriptionLinkHandler.getLinkReferenceToInspection(problem.inspectionId),
      true
    )

    val element = myFile.findElementAt(textRange.startOffset)
    val infoType = QodanaHighlightInfoTypeProvider.getHighlightTypeInfo()
    val builder = HighlightInfo.newHighlightInfo(QodanaHighlightingInfoType(problem, infoType)) //not using level right now

    val ignoreActions = mutableListOf<IntentionAction>()
    if (suppressToolId != null) {
      val problemGroup = QodanaSuppressableProblemGroup(suppressToolId, myFile.virtualFile)
      builder.problemGroup(problemGroup)
      val suppressActions = problemGroup.getSuppressActions(element)
      ignoreActions.addAll(suppressActions)
    }

    ignoreActions.add(StopShowingReportAction())

    builder.registerFix(QodanaRootIgnoreIntention(), ignoreActions, null, null, null)

    val highlight = builder
      .range(textRange)
      .needsUpdateOnTyping(true)
      .description(description)
      .escapedToolTip(tooltip)
      .create()

    require(highlight?.inspectionToolId == null) {
      "Inspection id must not be passed via HighlightInfo, otherwise inspections pass may clean it up"
    }
    return highlight
  }

  private fun updateWasAnalyzedOnceStatus(): Boolean {
    val codeAnalyzer = (DaemonCodeAnalyzerEx.getInstance(myProject) as? DaemonCodeAnalyzerImpl)
    val firstAnalysisPassed = QodanaHighlightingSupportInfoProvider.getPrecedingPassesIds().all {
      codeAnalyzer?.fileStatusMap?.getFileDirtyScope(document, context, myFile, it) == null
    }
    return passState.updateWasAnalysedOnce(firstAnalysisPassed)
  }

  override fun doApplyInformationToEditor() {
    val markupModel = DocumentMarkupModel.forDocument(document, myProject, false)
    val qodanaHighlights = myHighlightInfos.get()

    val qodanaHighlightersDuplicatingIde = markupModel.allHighlighters.flatMap { getQodanaHighlightsDuplicatingIde(it, qodanaHighlights) }.toSet()
    val onlyQodanaHighlights = qodanaHighlights - qodanaHighlightersDuplicatingIde

    val analyzedOnce = updateWasAnalyzedOnceStatus()

    val enabledInspections = enabledInspections.get()

    val toUpdate = onlyQodanaHighlights.mapNotNull { qodanaHighlight ->
      val type = qodanaHighlight.type as? QodanaHighlightingInfoType ?: return@mapNotNull null
      val isFixed = analyzedOnce && type.sarifProblem.inspectionId in enabledInspections
      SarifProblemPropertiesUpdater(type.sarifProblem) { it.copy(isFixed = isFixed) }
    } + qodanaHighlightersDuplicatingIde.mapNotNull { qodanaHighlight ->
      val type = qodanaHighlight.type as? QodanaHighlightingInfoType ?: return@mapNotNull null
      SarifProblemPropertiesUpdater(type.sarifProblem) { it.copy(isFixed = false) }
    }

    val highlightedReportData = reportService.highlightedReportState.value.highlightedReportDataIfSelected
    highlightedReportData?.updateProblemsProperties(toUpdate)
    val onlyQodanaNotFixedHighlights = onlyQodanaHighlights
      .filter {
        val inspectionId = (it.type as? QodanaHighlightingInfoType)?.sarifProblem?.inspectionId
        return@filter inspectionId !in enabledInspections
      }

    UpdateHighlightersUtil.setHighlightersToEditor(
      myProject, editor.document, 0, myFile.textLength, onlyQodanaNotFixedHighlights, colorsScheme, id
    )
    passState.setInfosFromPass(onlyQodanaHighlights)
  }
}

internal data class ProblemWithRange(val problem: SarifProblem, val range: TextRange?)

internal fun getQodanaHighlightsDuplicatingIde(ideHighlighter: RangeHighlighter, qodanaHighlights: List<HighlightInfo>): List<HighlightInfo> {
  val ideHighlight = HighlightInfo.fromRangeHighlighter(ideHighlighter) ?: return emptyList()
  if (ideHighlight.type is QodanaHighlightingInfoType ||
      ideHighlight.description == null ||
      ideHighlight.inspectionToolId == null && ideHighlight.externalSourceId == null) return emptyList()

  return qodanaHighlights.filter { qodanaHighlight: HighlightInfo ->
    val duplicate = QodanaHighlightInfoComparator.equals(ideHighlight, qodanaHighlight)
    duplicate
  }
}