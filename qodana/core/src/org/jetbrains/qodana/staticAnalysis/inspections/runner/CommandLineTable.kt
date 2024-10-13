package org.jetbrains.qodana.staticAnalysis.inspections.runner

import kotlin.math.max

class CommandLineTable(private val header: List<String>, private val rows: List<List<String>>, columnSize: List<Int>) {
  companion object {
    const val DEFAULT_COLUMN_SIZE = 50
  }

  private val columnSize: List<Int>

  init {
    this.columnSize = columnSize.mapIndexed { ind, size ->
      if (size != 0 || rows.isEmpty()) {
        return@mapIndexed size
      }

      max(
        rows.maxOfOrNull { if (ind < it.size) it[ind].length else 0 } ?: DEFAULT_COLUMN_SIZE,
        if (ind < header.size) header[ind].length else DEFAULT_COLUMN_SIZE
      )
    }
  }

  fun buildTable(): String {
    val format = "%-${columnSize[0]}s  " + columnSize.drop(1).joinToString(separator = "  ") { "%${it}s" }
    val tableHeader = String.format(format, *header.toTypedArray())

    val result = StringBuilder()
    result.appendLine("-".repeat(tableHeader.length))
    result.appendLine(tableHeader)
    result.appendLine("-".repeat(tableHeader.length))

    for (row in rows) {
      val splittedRow = row.mapIndexed { ind, s -> s.chunked(columnSize[ind]) }
      val maxLinesCount = splittedRow.maxOf { it.size }
      val alignedRow = splittedRow.map {
        val difWithMax = maxLinesCount - it.size
        List(difWithMax / 2) { "" } + it + List(difWithMax / 2 + difWithMax % 2) { "" }
      }
      for (i in 0 until maxLinesCount) {
        result.appendLine(String.format(format, *(alignedRow.map { it[i] }.toTypedArray())))
      }
    }
    result.appendLine("-".repeat(tableHeader.length))
    return result.toString()
  }
}