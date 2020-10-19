package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.vcs.VcsException;

import java.util.ArrayList;
import java.util.List;

public class FixesOutputParser {
  private final List<String> myLines;

  public FixesOutputParser(List<String> lines) {
    myLines = lines;
  }

  public List<String> parseJobNames() throws VcsException {
    final List<String> result = new ArrayList<>();
    for (String line : myLines) {
      if (! line.startsWith("\t")) {
        int idx = line.indexOf(" ");
        idx = (idx == -1) ? line.length() : idx;
        result.add(line.substring(0, idx));
      }
    }
    return result;
  }
}
