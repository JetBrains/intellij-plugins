package org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting

import com.intellij.codeInsight.daemon.impl.ProblemRelatedLocation
import com.intellij.codeInsight.daemon.impl.withRelatedLocations
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingInspectionHelper
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandler
import org.jetbrains.qodana.staticAnalysis.sarif.CONTEXT_MAX_LINES_MARGIN
import org.jetbrains.qodana.staticAnalysis.sarif.MAX_CONTEXT_CHARS_LENGTH

class IncorrectFormattingResultHandlerQodana : IncorrectFormattingResultHandler {
  companion object {
    private const val CONTEXT_SIZE_AROUND_PROBLEM = 2 * CONTEXT_MAX_LINES_MARGIN + 1
    const val QODANA_INCORRECT_FORMATTING_MAXIMUM_SNIPPET_SIZE_PROPERTY: String = "qodana.incorrect.formatting.maximum.snippet.size"
    const val QODANA_INCORRECT_FORMATTING_SELECTED_SNIPPETS_AMOUNT_PROPERTY: String = "qodana.incorrect.formatting.selected.snippets.amount"
  }

  override fun getResults(reportPerFile: Boolean, helper: IncorrectFormattingInspectionHelper): Array<ProblemDescriptor>? {
    val allProblems = helper.createAllReports()
    if (allProblems.isNullOrEmpty()) return arrayOf(helper.createGlobalReport())
    val maximumSnippetSize = Integer.getInteger(QODANA_INCORRECT_FORMATTING_MAXIMUM_SNIPPET_SIZE_PROPERTY, 10)
    val selectedSnippetsAmount = Integer.getInteger(QODANA_INCORRECT_FORMATTING_SELECTED_SNIPPETS_AMOUNT_PROPERTY, 3)
    val selectedDescriptors = selectDescriptorsForSnippets(allProblems, selectedSnippetsAmount, maximumSnippetSize)
    return arrayOf(
      helper.createGlobalReport().withRelatedLocations(
        selectedDescriptors
          .mapNotNull { problemDescriptor ->
            (problemDescriptor as? ProblemDescriptorBase)?.let { ProblemRelatedLocation(it) }
          }
          .groupBy { it.getLineNumber() }
          .flatMap { (_, descriptors) ->
            val minOffsetInGroup = descriptors.minByOrNull { it.getOffset() ?: Int.MAX_VALUE }?.getOffset() ?: 0
            // for long lines only the first problems are added, so the report is not very big
            // added to the report problems are located in the margin of the MAX_CONTEXT_CHARS_LENGTH
            // from the first problem in line
            descriptors.filter { descriptor ->
              val currentOffset = descriptor.getOffset() ?: return@filter false
              currentOffset - minOffsetInGroup <= MAX_CONTEXT_CHARS_LENGTH
            }
          }
      )
    )
  }

  private fun selectDescriptorsForSnippets(
    descriptors: Array<ProblemDescriptor>,
    snippetsAmount: Int,
    maximumSnippetSize: Int,
  ): List<ProblemDescriptor> {
    val sortedDescriptors = descriptors.sortedBy { it.lineNumber }

    val groups = mutableListOf<MutableList<ProblemDescriptor>>()
    var currentGroup = mutableListOf<ProblemDescriptor>()
    var minimalLineInCurrentGroup = 0
    var lastLineInCurrentGroup = 0

    fun resetCurrentGroup(descriptor: ProblemDescriptor) {
      currentGroup = mutableListOf(descriptor)
      minimalLineInCurrentGroup = getProblemFirstLineNumber(descriptor)
      lastLineInCurrentGroup = getProblemLastLineNumber(descriptor)
    }

    for (descriptor in sortedDescriptors) {
      if (currentGroup.isEmpty()) {
        resetCurrentGroup(descriptor)
        continue
      }
      if (getProblemFirstLineNumber(descriptor) - minimalLineInCurrentGroup > maximumSnippetSize ||
          getProblemFirstLineNumber(descriptor) - lastLineInCurrentGroup > CONTEXT_SIZE_AROUND_PROBLEM) {
        groups.add(currentGroup)
        resetCurrentGroup(descriptor)
      }
      else {
        currentGroup.add(descriptor)
        lastLineInCurrentGroup = getProblemLastLineNumber(descriptor)
      }
    }

    if (currentGroup.isNotEmpty()) {
      groups.add(currentGroup)
    }
    val groupsBiggerThanMinSize = groups.filter { group ->
      group.last().lineNumber - group.first().lineNumber + 1 >= CONTEXT_SIZE_AROUND_PROBLEM
    }
    if (groupsBiggerThanMinSize.size >= snippetsAmount)
      return evenlyDistributedItems(groupsBiggerThanMinSize, snippetsAmount).flatten()
    val groupsSmallerThanMinSize = groups.filter { group ->
      group.last().lineNumber - group.first().lineNumber + 1 < CONTEXT_SIZE_AROUND_PROBLEM
    }
    return (groupsBiggerThanMinSize +
            evenlyDistributedItems(groupsSmallerThanMinSize, snippetsAmount - groupsBiggerThanMinSize.size)).flatten()
  }

  private fun getProblemFirstLineNumber(descriptor: ProblemDescriptor) = descriptor.lineNumber + 1

  private fun getProblemLastLineNumber(descriptor: ProblemDescriptor): Int {
    return descriptor.lineNumber + descriptor.psiElement.text.lines().size - 1
  }

  private fun <T> evenlyDistributedItems(list: List<T>, n: Int): List<T> {
    if (n <= 0) return emptyList()
    if (list.isEmpty() || n >= list.size) return list

    val step = list.size.toDouble() / n
    return (0 until n).map { i -> list[(i * step).toInt()] }
  }
}