package org.jetbrains.idea.perforce.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import java.nio.file.FileSystems
import java.nio.file.PathMatcher

class P4IgnoresMappingsHelper(project : Project, connection: P4Connection) {
  private val mappings : Array<MappingEntry>

  init {
    val perforceRunner = PerforceRunner.getInstance(project);
    val result = perforceRunner.ignores(connection)
    val mappingsList = ArrayList<MappingEntry>();
    for (line in result.stdout.split("\n".toRegex())) {
      if (line.isEmpty())
        continue
      val excluded = line[0] == '!'
      val trimmedLine = if (excluded) {
        line.substring(1)
      }
      else {
        line
      }

      val entry = MappingEntry(trimmedLine.replace("...", "**"), excluded)
      mappingsList.add(entry)
    }

    mappings = mappingsList.toTypedArray()
  }

  public fun isIgnored(file: VirtualFile): Boolean {
    val path = file.toNioPath()

    for (i in mappings.indices) {
      val rule = mappings[i]
      val res = rule.pathMatcher.matches(path)
      if (res) {
        return !rule.isExcluded
      }
    }

    return false
  }

  private class MappingEntry(val pattern: String, val isExcluded: Boolean) {
    val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:${pattern}")

    override fun toString(): String {
      return pattern
    }
  }
}