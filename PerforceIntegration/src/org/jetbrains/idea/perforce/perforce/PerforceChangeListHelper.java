package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PerforceChangeListHelper {
  @NonNls private static final String DEFAULT_DESCRIPTION = "<none>";

  public static String createSpecification(final String description,
                                           final long changeListNumber,
                                           @Nullable final List<String> files,
                                           @Nullable final String clientName,
                                           @Nullable final String userName,
                                           final boolean forUpdate, boolean restricted) throws VcsException {
    @NonNls final StringBuilder result = new StringBuilder();
    result.append("Change:\t");
    if (changeListNumber == -1) {
      result.append("new");
    }
    else {
      result.append(changeListNumber);
    }
    if (clientName != null && (! forUpdate)) {
      result.append("\n\nClient:\t");
      result.append(clientName);
    }
    if (userName != null && (! forUpdate)) {
      result.append("\n\nUser:\t");
      result.append(userName);
    }
    if (! forUpdate) {
      result.append("\n\nStatus:\t");
      if (changeListNumber == -1) {
        result.append("new");
      }
      else {
        result.append("pending");
      }
    }
    if (restricted) {
      result.append("\n\nType:\trestricted");
    }
    result.append("\n\nDescription:");
    String[] lines = StringUtil.convertLineSeparators(description).split("\n");
    if (lines.length == 0) {
      lines = new String[] { DEFAULT_DESCRIPTION };
    }
    for(String line: lines) {
      result.append("\n\t").append(line);
    }

    if (changeListNumber != -1 || files != null) {
      result.append("\n\nFiles:\n");
      if (files != null) {
        for(String file: files) {
          result.append("\t").append(file).append("\n");
        }
      }
    }

    return result.toString();
  }

  public static long parseCreatedListNumber(final String output) {
    @NonNls final String prefix = "Change";
    String copy = output;
    if (copy.startsWith(prefix)) {
      copy = copy.substring(prefix.length()).trim();
    }
    int wsPos = copy.indexOf(' ');
    if (wsPos >= 0) {
      copy = copy.substring(0, wsPos).trim();
    }
    try {
      return Long.parseLong(copy);
    }
    catch (NumberFormatException e) {
      return -1;
    }
  }
}
