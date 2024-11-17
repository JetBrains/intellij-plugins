package org.jetbrains.idea.perforce.perforce

import com.intellij.openapi.vcs.VcsException
import it.unimi.dsi.fastutil.objects.Object2LongMap
import org.jetbrains.idea.perforce.PerforceBundle

internal class P4OpenedParser(revisions: Object2LongMap<String>) : P4Parser(P4Command.opened, revisions) {

  companion object {
    private const val OPENED_DELIMITER = " - ";
  }

  override fun consumeLine(outputLine: String): ParsedLine {
    val hashIndex = outputLine.indexOf('#');
    if (hashIndex < 0) {
      throw VcsException(PerforceBundle.message("error.unexpected.p4.opened.output.format", outputLine))
    }
    val idx = outputLine.indexOf(OPENED_DELIMITER, hashIndex);
    if (idx < 0) {
      throw VcsException(PerforceBundle.message("error.unexpected.p4.opened.output.format", outputLine))
    }
    val depotPath = outputLine.substring(0, hashIndex)
    val revision = outputLine.substring(hashIndex + 1, idx).toLong()

    return ParsedLine(depotPath, revision)
  }
}
