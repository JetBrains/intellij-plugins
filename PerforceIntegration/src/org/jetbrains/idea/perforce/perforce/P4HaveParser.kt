package org.jetbrains.idea.perforce.perforce

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.VcsException
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.PerforceManager

open class P4HaveParser(private val perforceManager: PerforceManager, revisions: Object2LongMap<String>) :
  P4Parser(P4Command.have, revisions) {

  companion object {
    private const val HAVE_DELIMITER = " - ";
  }

  constructor(perforceManager: PerforceManager) : this(perforceManager, Object2LongOpenHashMap<String>())

  @Throws(VcsException::class)
  override fun consumeLine(outputLine: String): ParsedLine {
    val hashIndex = outputLine.indexOf('#')
    if (hashIndex < 0) {
      throw VcsException(PerforceBundle.message("error.unexpected.p4.have.output.format", outputLine))
    }
    val idx = outputLine.indexOf(HAVE_DELIMITER, hashIndex)
    if (idx < 0) {
      throw VcsException(PerforceBundle.message("error.unexpected.p4.have.output.format", outputLine))
    }
    var localPath = outputLine.substring(idx + HAVE_DELIMITER.length)
    localPath = perforceManager.convertP4ParsedPath(null, localPath)
    val revision = outputLine.substring(hashIndex + 1, idx).toLong()

    return ParsedLine(FileUtil.toSystemDependentName(localPath), revision)
  }
}
