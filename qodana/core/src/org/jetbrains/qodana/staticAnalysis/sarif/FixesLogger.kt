package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.fragments.LineFragment
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue

@Serializable
private data class FixData(
  val inspectionId: String,
  val inspectionName: String,
  val problemOriginFilePath: String,
  val problemMessage: String,
  val fixText: String,
  val modifiedFilePath: String
)

@Serializable
private data class DiffChange(
  val beforeFix: String,
  val afterFix: String,
  val filePath: String,
  val problemMessage: String
)

private data class FileModification(
  val path: String,
  val problemMessage: String,
  val textBefore: CharSequence,
  val textAfter: CharSequence
)

private val jsonEncoder
  get() = Json { prettyPrint = true }

internal class FixesLogger {
  companion object {
    const val TEXT_NOT_CAPTURED_MESSAGE = "Text was not captured"
    const val FILE_NOT_CAPTURED_MESSAGE = "File was not captured"
    @VisibleForTesting
    const val INCLUDE_FIXES_DIFF_KEY = "qodana.fixes.log.include.diff"
  }

  private val fixesData: MutableList<FixData> = mutableListOf()
  val diffIncluded: Boolean = java.lang.Boolean.getBoolean(INCLUDE_FIXES_DIFF_KEY)

  fun logAppliedFix(
    messageReporter: QodanaMessageReporter,
    tool: InspectionToolWrapper<*, *>,
    problemMessage: String,
    problemOriginFilePath: String,
    problemDescriptor: ProblemDescriptor,
    modifiedFilePath: String
  ) {
    val fixes = problemDescriptor.fixes
    val fixText = when {
      fixes == null -> {
        "fix text was not captured"
      }
      fixes.size == 1 -> {
        fixes.first().name
      }
      else -> {
        "one of the following: " + fixes.joinToString("; ") { it.name }
      }
    }

    logAppliedFix(messageReporter, tool, problemMessage, problemOriginFilePath,  fixText, modifiedFilePath)
  }

  fun logAppliedFix(
    messageReporter: QodanaMessageReporter,
    tool: InspectionToolWrapper<*, *>,
    problemMessage: String,
    problemOriginFilePath: String,
    fixText: String,
    modifiedFilePath: String
  ) {
    val fixData = FixData(
      tool.id,
      tool.displayName,
      problemOriginFilePath,
      problemMessage,
      fixText,
      modifiedFilePath
    )
    messageReporter.reportMessage(1, "In the file $modifiedFilePath inspection ${fixData.inspectionName} performed fix: ${fixData.fixText}")
    fixesData.add(fixData)
  }

  suspend fun logFixesAsJson(logFileName: String) = withContext(StaticAnalysisDispatchers.IO) {
    val path = Path.of(PathManager.getLogPath(), "qodana", logFileName)
    path.parent.toFile().mkdirs()
    try {
      runInterruptible(StaticAnalysisDispatchers.IO) {
        Files.newBufferedWriter(path, StandardCharsets.UTF_8).use { writer ->
          writer.write(jsonEncoder.encodeToString(fixesData))
        }
      }
    }
    catch (e: IOException) {
      QodanaProjectDescriber.LOG.error("Error while saving fixes", e)
    }
  }

  private val fileDiffs = ConcurrentLinkedQueue<DiffChange>()
  private val fileModificationsQueue = ConcurrentLinkedQueue<FileModification>()

  fun addFileModificationToQueue(
    filePath: String,
    problemMessage: String,
    textBefore: CharSequence,
    textAfter: CharSequence,
  ) = fileModificationsQueue.add(FileModification(filePath, problemMessage, textBefore, textAfter))

  private fun addLineNumbers(text: String, startLine: Int): String {
    return text.split('\n')
      .filter { it.trimEnd('\n').trimStart('\n').trimIndent().isNotBlank() }
      .mapIndexed { i, line ->
        (i+startLine).toString() + line
      }.joinToString("\n")
  }

  private fun calculateDiff(fragment: LineFragment, textBefore: CharSequence, textAfter: CharSequence, filePath: String, problemMessage: String): DiffChange? {
    if (fragment.startOffset1 > fragment.endOffset1 || fragment.startOffset2 > fragment.endOffset2) {
      return null
    }
    val oldText = textBefore.substring(fragment.startOffset1, fragment.endOffset1)
    val newText = textAfter.substring(fragment.startOffset2, fragment.endOffset2)
    if (oldText.trimStart('\n').trimEnd('\n').trimIndent().isBlank()) {
      return DiffChange("", addLineNumbers(newText, fragment.startLine2), filePath, problemMessage)
    }
    if (newText.trimStart('\n').trimEnd('\n').trimIndent().isBlank()) {
      return DiffChange(addLineNumbers(oldText, fragment.startLine1), "Was deleted", filePath, problemMessage)
    }
    return DiffChange(addLineNumbers(oldText, fragment.startLine1), addLineNumbers(newText, fragment.startLine2), filePath, problemMessage)
  }

  // may have a long computational time
  fun commitFilesModificationsLog() {
    while (true) {
      val (modifiedFile, problemMessage, beforeFixText, afterFixText) = fileModificationsQueue.poll() ?: break
      if (beforeFixText == TEXT_NOT_CAPTURED_MESSAGE || afterFixText == TEXT_NOT_CAPTURED_MESSAGE) {
        fileDiffs.add(DiffChange(TEXT_NOT_CAPTURED_MESSAGE, TEXT_NOT_CAPTURED_MESSAGE, modifiedFile, problemMessage))
        continue
      }
      val diff = ComparisonManager.getInstance().compareLines(
        beforeFixText, afterFixText, ComparisonPolicy.DEFAULT, ProgressIndicatorBase()
      )
      if (diff.size == 0) {
        fileDiffs.add(DiffChange(TEXT_NOT_CAPTURED_MESSAGE, TEXT_NOT_CAPTURED_MESSAGE, modifiedFile, problemMessage))
        continue
      }
      diff.forEach { lineFragment ->
        fileDiffs.add(calculateDiff(lineFragment, beforeFixText, afterFixText, modifiedFile, problemMessage))
      }
    }
  }

  suspend fun logFileModificationsAsJson(logFileName: String) = withContext(StaticAnalysisDispatchers.IO) {
    val path = Path.of(PathManager.getLogPath(), "qodana", logFileName)
    path.parent.toFile().mkdirs()
    try {
      runInterruptible(StaticAnalysisDispatchers.IO) {
        Files.newBufferedWriter(path, StandardCharsets.UTF_8).use { writer ->
          writer.write(jsonEncoder.encodeToString(fileDiffs.toList()))
        }
      }
    }
    catch (e: IOException) {
      QodanaProjectDescriber.LOG.error("Error while saving file modifications", e)
    }
  }
}