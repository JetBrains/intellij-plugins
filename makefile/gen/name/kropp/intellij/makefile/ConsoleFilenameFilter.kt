package name.kropp.intellij.makefile

import com.intellij.execution.filters.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*

class ConsoleFilenameFilter(private val project: Project) : Filter {
  private val posixFilename = Regex("(/[^: ]+)(:(\\d+))?(:(\\d+))?")
  private val windowsFilename = Regex("([a-z]:\\[^: ]+)(:(\\d+))?(:(\\d+))?", RegexOption.IGNORE_CASE)

  override fun applyFilter(line: String, entireLength: Int): Filter.Result {
    val start = entireLength - line.length
    val match = posixFilename.findAll(line).lastOrNull() ?: windowsFilename.findAll(line).lastOrNull()
    if (match != null) {
      val filename = match.groups[1]?.value ?: return Filter.Result(start, entireLength, null, TextAttributes())
      val file = project.baseDir.fileSystem.findFileByPath(filename) ?: return Filter.Result(start, entireLength, null, TextAttributes())

      val row = match.groups[3]?.value?.toInt() ?: 0
      val column = match.groups[5]?.value?.toInt() ?: 0

      val descriptor = OpenFileDescriptor(project, file, row, column)
      return Filter.Result(start + match.range.start, start + match.range.endInclusive + 1, OpenFileHyperlinkInfo(descriptor))
    }
    return Filter.Result(start, entireLength, null, TextAttributes())
  }
}
