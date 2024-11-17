package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.rt.coverage.report.XMLProjectData
import com.intellij.rt.coverage.report.XMLProjectData.*
import kotlin.math.max
import kotlin.math.min

fun LineInfo.merge(other: LineInfo): LineInfo {
  return LineInfo(
    lineNumber,
    min(missedInstructions, other.missedInstructions),
    max(coveredInstructions, other.coveredInstructions),
    min(missedBranches, other.missedBranches),
    max(coveredBranches, other.coveredBranches)
  )
}

fun FileInfo.merge(other: FileInfo): FileInfo {
  val mergedFileInfo = FileInfo(path)
  val lineInfoMap = mutableMapOf<Int, LineInfo>()

  lines.forEach { lineInfo ->
    lineInfoMap[lineInfo.lineNumber] = lineInfo
  }

  other.lines.forEach { otherLineInfo ->
    lineInfoMap[otherLineInfo.lineNumber]?.let { existingLineInfo ->
      lineInfoMap[otherLineInfo.lineNumber] = existingLineInfo.merge(otherLineInfo)
    } ?: run {
      lineInfoMap[otherLineInfo.lineNumber] = otherLineInfo
    }
  }

  mergedFileInfo.lines.addAll(lineInfoMap.values)
  return mergedFileInfo
}

fun ClassInfo.merge(other: ClassInfo): ClassInfo {
  return ClassInfo(
    name,
    fileName,
    min(missedLines, other.missedLines),
    max(coveredLines, other.coveredLines),
    min(missedInstructions, other.missedInstructions),
    max(coveredInstructions, other.coveredInstructions),
    min(missedBranches, other.missedBranches),
    max(coveredBranches, other.coveredBranches),
    min(missedMethods, other.missedMethods),
    max(coveredMethods, other.coveredMethods)
  )
}

fun XMLProjectData.merge(other: XMLProjectData) {
  val fileInfos = mutableListOf<FileInfo>()
  files.forEach { fileInfo ->
    other.getFile(fileInfo.path)?.let { otherFileInfo ->
      fileInfos.add(fileInfo.merge(otherFileInfo))
    }
  }
  fileInfos.forEach { addFile(it) }

  other.files.forEach { otherFileInfo ->
    if (getFile(otherFileInfo.path) == null) {
      addFile(otherFileInfo)
    }
  }

  val classInfos = mutableListOf<ClassInfo>()
  classes.forEach { classInfo ->
    other.getClass(classInfo.name)?.let { otherClassInfo ->
      classInfos.add(classInfo.merge(otherClassInfo))
    }
  }
  classInfos.forEach { addClass(it) }

  other.classes.forEach { otherClassInfo ->
    if (getClass(otherClassInfo.name) == null) {
      addClass(otherClassInfo)
    }
  }
}