package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers

class DummyJenkinsConfigHandler {
  fun addStage(text: String, stageToAddText: String): String? {
    val pipelineNextTokenIndex = findNextTokenPositionOnLevel(text, "pipeline", 0)
    if (pipelineNextTokenIndex == null) {
      return text + "\npipeline {\n    stages {\n${stageToAddText.replaceIndent("        ")}\n    }\n}"
    }

    val afterPipelineText = text.substring(pipelineNextTokenIndex, text.lastIndex + 1)
    val pipelinesLBracketIndex = findFirstBracketMatchingEnd(afterPipelineText) ?: return null
    val pipelinesText = afterPipelineText.substring(0, pipelinesLBracketIndex + 1)
    val stagesNextTokenIndex = findNextTokenPositionOnLevel(pipelinesText, "stages", 1)

    if (stagesNextTokenIndex == null) {
      val (indent, offset) = getIndentByOffset(afterPipelineText, pipelinesLBracketIndex)
      val toAdd = stageToAddText.replaceIndent(indent)
      return text.substring(0, pipelineNextTokenIndex) +
             afterPipelineText.substring(0, offset + 1) +
             "stages {\n${toAdd}\n}".replaceIndent(indent) + "\n" +
             afterPipelineText.substring(offset + 1, afterPipelineText.lastIndex + 1)
    }

    val afterStagesText = pipelinesText.substring(stagesNextTokenIndex, pipelinesText.lastIndex + 1)
    val stagesLBracketIndex = findFirstBracketMatchingEnd(afterStagesText) ?: return null
    val (indent_, offset) = getIndentByOffset(afterStagesText, stagesLBracketIndex)
    val indent = if (indent_ != "") indent_ else getIndentByOffset(pipelinesText, stagesNextTokenIndex, false).first.repeat(2)

    return text.substring(0, pipelineNextTokenIndex + stagesNextTokenIndex) +
           afterStagesText.substring(0, offset + 1) +
           stageToAddText.replaceIndent(indent) + "\n" +
           afterStagesText.substring(offset + 1, afterStagesText.lastIndex + 1)
  }

  suspend fun isQodanaStagePresent(virtualFile: VirtualFile): Boolean {
    return withContext(QodanaDispatchers.IO) {
      virtualFile.readText().contains("jetbrains/qodana") }
  }

  private fun findFirstBracketMatchingEnd(text: String): Int? {
    var balance: Int? = null
    for ((index, c) in text.withIndex()) {
      if (c.isWhitespace()) continue
      if (c == '{') {
        if (balance == null) {
          balance = 1
          continue
        }
        else balance++
      }
      if (balance == null) return null
      if (c == '}') balance--
      if (balance == 0) return index
    }
    return null
  }

  private fun findNextTokenPositionOnLevel(text: String, toFind: String, level: Int = 0): Int? {
    val tokens = text.split("\\s+".toRegex())
    var balance = 0
    var position = 0
    for (token in tokens) {
      val nextIndex = text.indexOf(token, position) + token.length
      position += token.length
      position += if (nextIndex < text.length) text.substring(nextIndex).takeWhile { it.isWhitespace() }.length else 0
      if (token == "{") balance++
      if (token == "}") balance--
      if (token == toFind && balance == level) return position
    }
    return null
  }

  private fun getIndentByOffset(text: String, offset: Int, skipLast: Boolean = true): Pair<String, Int> {
    var curIndex = offset
    if (skipLast) {
      while (curIndex > 0 && text[curIndex] != '\n') {
        curIndex--
      }
    }
    val lines = text.substring(0, curIndex).split('\n')
    for (line in lines.reversed()) {
      if (line.all { it.isWhitespace() }) continue
      return Pair(line.takeWhile { it.isWhitespace() }, curIndex)
    }
    return Pair("", curIndex)
  }
}