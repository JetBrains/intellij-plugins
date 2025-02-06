package org.jetbrains.idea.perforce.perforce

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsKey
import com.intellij.openapi.vcs.VcsSharedChecker
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager
import java.nio.file.Path

internal class P4SharedInVcsChecker : VcsSharedChecker {
  companion object {
    private val LOG = logger<P4SharedInVcsChecker>()
  }

  override fun getSupportedVcs(): VcsKey = PerforceVcs.getKey()

  override fun isPathSharedInVcs(project: Project, path: Path): Boolean {
    val dir = P4File.create(path.toFile())
    val p4args = arrayOf<String?>(P4Command.changes.name, dir.getRecursivePath())
    val connection = PerforceConnectionManager.getInstance(project).getConnectionForFile(dir) ?: return false

    val execResult = PerforceRunner.getInstance(project).executeP4Command(p4args, connection)
    if (execResult.exitCode != 0) {
      LOG.debug("Failed to check changes under ${dir}, P4 command ${p4args.joinToString(" ")}, result $execResult")
    }

    return OutputMessageParser(execResult.stdout).myLines.isNotEmpty()
  }
}
