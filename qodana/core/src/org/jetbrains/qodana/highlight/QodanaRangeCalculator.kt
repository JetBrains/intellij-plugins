package org.jetbrains.qodana.highlight

import com.intellij.codeInspection.LanguageInspectionSuppressors
import com.intellij.diff.comparison.ByWordRt
import com.intellij.diff.comparison.CancellationChecker
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.fragments.DiffFragment
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.util.diff.Diff
import org.jetbrains.qodana.problem.SarifProblem
import kotlin.math.min

internal class QodanaRangeCalculator(
  private val project: Project,
  private val file: PsiFile,
  private val document: Document,
  private val highlightedReportData: HighlightedReportData
) {
  internal fun calculateTextRanges(relevantProblems: List<SarifProblem>, data: QodanaRevisionData): List<ProblemWithRange> {
    val ranges = when (data) {
      is QodanaRevisionData.VCSInfo -> {
        val problemsByRevision = relevantProblems.groupBy { it.revisionId }
        problemsByRevision.flatMap { (revisionId, problems) ->
          getTextRangesForRevision(problems, data.revisionPsiFiles[revisionId]?.viewProvider?.document)
        }
      }
      is QodanaRevisionData.LocalInfo -> {
        getTextRangesForRevision(relevantProblems, data.localDocumentData.document)
      }
    }
    return ranges
  }

  private fun getTextRangesForRevision(problems: List<SarifProblem>, revisionDocument: Document?): List<ProblemWithRange> {
    if (problems.isEmpty()) return emptyList()

    val rangesInCurrentDocument = problems.map { ProblemWithRange(it, it.getTextRangeInDocument(document)) } //ranges in current state of file

    //TODO: show ui element with error - can't find revision
    if (revisionDocument == null) {
      return rangesInCurrentDocument
    }
    val changes = Diff.buildChanges(revisionDocument.text, document.text)

    val diffs = ByWordRt.compare(revisionDocument.text, document.text, ComparisonPolicy.DEFAULT, CancellationChecker.EMPTY)

    return List(problems.size) { index ->
      val problem = problems[index]
      val problemWithNullRange = ProblemWithRange(problem, null)
      val localProblem = rangesInCurrentDocument[index]

      if (problem.startLine == null || problem.startLine >= revisionDocument.lineCount || problem.startColumn == null) return@List localProblem
      if (problem.snippetText == null && Diff.translateLine(changes, problem.startLine) == -1) return@List problemWithNullRange

      val problemLength =
        problem.charLength ?:
        if (problem.endLine != null && problem.endLine < revisionDocument.lineCount && problem.endColumn != null) {
          revisionDocument.getLineStartOffset(problem.endLine) + problem.endColumn - revisionDocument.getLineStartOffset(problem.startLine) - problem.startColumn
        } else {
          return@List localProblem
        }
      val startOffsetInRevisionFile = revisionDocument.getLineStartOffset(problem.startLine) + problem.startColumn

      var lastRelatedIndex = diffs.binarySearch {
        it.endOffset1 - startOffsetInRevisionFile
      }
      if (lastRelatedIndex < 0) lastRelatedIndex = - lastRelatedIndex - 1
      val relatedDiffs = diffs.subList(0, min(lastRelatedIndex + 1, diffs.size))

      val textRange = tryTranslateOffsetsByDiffs(startOffsetInRevisionFile, startOffsetInRevisionFile + problemLength, relatedDiffs)
                      ?: return@List localProblem

      if (textRange.endOffset > document.textLength) return@List localProblem
      val textEqualToSnippet = problem.isEqualToSnippet(document.getText(textRange))
      if (!textEqualToSnippet) return@List problemWithNullRange
      if (isProblemSuppressed(problems[index], textRange, highlightedReportData)) return@List problemWithNullRange
      ProblemWithRange(problems[index], textRange)
    }
  }

  private fun tryTranslateOffsetsByDiffs(startOffset: Int, endOffset: Int, diffs: List<DiffFragment>): TextRange? {
    var newStartOffset = startOffset
    var newEndOffset = endOffset
    diffs.forEach {
      if (it.endOffset1 <= startOffset) {
        newStartOffset += (it.endOffset2 - it.startOffset2) - (it.endOffset1 - it.startOffset1)
        newEndOffset += (it.endOffset2 - it.startOffset2) - (it.endOffset1 - it.startOffset1)
      }
    }
    for (diff in diffs) {
      if (diff.startOffset1 <= startOffset && startOffset < diff.endOffset1) {
        newStartOffset = diff.startOffset2 + startOffset - diff.startOffset1
        newEndOffset = diff.startOffset2 + endOffset - diff.startOffset1
        break
      }
    }
    for (diff in diffs) {
      if (diff.startOffset1 < endOffset && endOffset <= diff.endOffset1 && startOffset <= diff.startOffset1) {
        newEndOffset = diff.endOffset2 + endOffset - diff.endOffset1
        break
      }
    }
    if (newEndOffset < newStartOffset) return null

    return TextRange(newStartOffset, newEndOffset)
  }

  private fun isProblemSuppressed(problem: SarifProblem, textRange: TextRange, highlightedReportData: HighlightedReportData): Boolean {
    if (DumbService.isDumb(project)) return false
    val element = file.findElementAt(textRange.startOffset) ?: return false
    val language = LanguageUtil.getFileLanguage(element.containingFile.virtualFile) ?: return false
    val inspectionSuppressors = LanguageInspectionSuppressors.INSTANCE.allForLanguage(language)
    val toolId = highlightedReportData.inspectionsInfoProvider.getSuppressIdByInspection(problem.inspectionId) ?: return false

    return !inspectionSuppressors.all { !it.isSuppressedFor(element, toolId) }
  }
}