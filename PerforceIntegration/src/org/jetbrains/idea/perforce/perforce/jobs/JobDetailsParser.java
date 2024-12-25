package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JobDetailsParser {
  private static final ParserLogger LOG = new ParserLogger("#org.jetbrains.idea.perforce.perforce.jobs.JobDetailsParser", "'p4 job' output parse error.");
  private final List<String> myLines;

  public JobDetailsParser(List<String> lines) {
    myLines = lines;
  }

  public @NotNull List<Pair<String, String>> parse() throws VcsException {
    final List<Pair<String, String>> result = new ArrayList<>();

    for (String line : myLines) {
      if (line.startsWith("#")) {
        // comment
        continue;
      }
      if (line.startsWith("\t")) {
        // a value
        if (result.isEmpty()) LOG.generateParseException("Cannot parse line: '" + line + "'");
        final int lastIdx = result.size() - 1;
        final Pair<String, String> prevVal = result.get(lastIdx);
        result.set(lastIdx, Pair.create(prevVal.getFirst(), prevVal.getSecond() + "\n" + line.trim()));
      } else {
        final int columnIdx = line.indexOf(":");
        if (columnIdx == -1) {
          // try match comment second time; didnt do as first version since specification is too vague
          if (line.trim().startsWith("#")) {
            continue;
          }
          LOG.generateParseException("Cannot find field name in: '" + line + "'");
        }
        result.add(Pair.create(line.substring(0, columnIdx), line.substring(columnIdx + 1).trim()));
      }
    }

    return result;
  }
}
