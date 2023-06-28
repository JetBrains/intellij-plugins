package org.jetbrains.idea.perforce.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import java.nio.file.FileSystems
import java.nio.file.PathMatcher

class P4IgnoresMappingsHelper private constructor(private val mappings : Array<MappingEntry>) {
  companion object {
    @JvmStatic
    fun create(project: Project, connection: P4Connection): P4IgnoresMappingsHelper {
      val perforceRunner = PerforceRunner.getInstance(project)
      val result = perforceRunner.ignores(connection)
      val mappingsList = ArrayList<MappingEntry>()
      for (line in result.stdout.split("\n".toRegex())) {
        if (line.isEmpty())
          continue
        val excluded = line[0] == '!'
        val trimmedLine = line.removePrefix("!")

        val entry = MappingEntry(trimmedLine.replace("...", "**"), excluded)
        mappingsList.add(entry)
      }

      return P4IgnoresMappingsHelper(mappingsList.toTypedArray())
    }
  }

  fun isIgnored(file: VirtualFile): Boolean {
    val path = file.toNioPath()

    for (rule in mappings) {
      if (rule.pathMatcher.matches(path)) {
        return !rule.isExcluded
      }
    }

    return false
  }

  private data class MappingEntry(val pattern: String, val isExcluded: Boolean) {
    val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:${pattern}")
  }
}