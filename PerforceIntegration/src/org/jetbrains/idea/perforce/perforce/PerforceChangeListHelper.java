package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PerforceChangeListHelper {
  private static final @NonNls String DEFAULT_DESCRIPTION = "<none>";

  public static String createSpecification(final String description,
                                           final long changeListNumber,
                                           final @Nullable List<String> files,
                                           final @Nullable String clientName,
                                           final @Nullable String userName,
                                           final boolean forUpdate, boolean restricted) throws VcsException {
    final @NonNls StringBuilder result = new StringBuilder();
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
    final @NonNls String prefix = "Change";
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

  public static ChangeList findOrCreateDefaultList(ChangeListManagerGate addGate) {
    for (String name : LocalChangeList.getAllDefaultNames()) {
      LocalChangeList list = addGate.findChangeList(name);
      if (list != null) return list;
    }
    return addGate.addChangeList(LocalChangeList.getDefaultName(), "");
  }
}
