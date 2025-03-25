package org.jetbrains.qodana.staticAnalysis.inspections.incorrectFormatting

import com.intellij.codeInsight.daemon.impl.ProblemRelatedLocation
import com.intellij.codeInsight.daemon.impl.withRelatedLocations
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingInspectionHelper
import com.intellij.codeInspection.incorrectFormatting.IncorrectFormattingResultHandler
import org.jetbrains.qodana.staticAnalysis.sarif.CONTEXT_MAX_LINES_MARGIN
import kotlin.collections.isNotEmpty

class IncorrectFormattingResultHandlerQodana: IncorrectFormattingResultHandler {
  companion object {
    private const val MAXIMUM_SNIPPET_SIZE = 10
    private const val CONTEXT_SIZE_AROUND_PROBLEM = 2 * CONTEXT_MAX_LINES_MARGIN + 1
    private const val SELECTED_SNIPPETS_AMOUNT = 3
  }
  override fun getResults(reportPerFile: Boolean, helper: IncorrectFormattingInspectionHelper): Array<ProblemDescriptor>? {
    val allProblems = helper.createAllReports()
    if (!allProblems.isNullOrEmpty()) {
      val selectedDescriptors = selectDescriptorsForSnippets(allProblems, SELECTED_SNIPPETS_AMOUNT)
      return arrayOf(
        helper.createGlobalReport().withRelatedLocations(
          selectedDescriptors.map { problemDescriptor ->
            ProblemRelatedLocation(problemDescriptor as ProblemDescriptorBase)
          }
        )
      )
    }
    return arrayOf(helper.createGlobalReport())
  }

  private fun selectDescriptorsForSnippets(
    descriptors: Array<ProblemDescriptor>,
    snippetsAmount: Int
  ): List<ProblemDescriptor> {
    val sortedDescriptors = descriptors.sortedBy { it.lineNumber }

    val groups = mutableListOf<MutableList<ProblemDescriptor>>()
    var currentGroup = mutableListOf<ProblemDescriptor>()

    var minimalLineInCurrentGroup = 0
    var lastLineInCurrentGroup = 0
    for (descriptor in sortedDescriptors) {
      if (currentGroup.isEmpty()) {
        currentGroup.add(descriptor)
        minimalLineInCurrentGroup = getProblemFirstLineNumber(descriptor)
        lastLineInCurrentGroup = getProblemLastLineNumber(descriptor)
        continue
      }
      if (getProblemFirstLineNumber(descriptor) - minimalLineInCurrentGroup > MAXIMUM_SNIPPET_SIZE ||
          getProblemFirstLineNumber(descriptor) - lastLineInCurrentGroup > CONTEXT_SIZE_AROUND_PROBLEM) {
        groups.add(currentGroup)
        currentGroup = mutableListOf(descriptor)
        minimalLineInCurrentGroup = getProblemFirstLineNumber(descriptor)
        lastLineInCurrentGroup = getProblemLastLineNumber(descriptor)
      } else {
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