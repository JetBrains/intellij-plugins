package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JobsOutputParser {
  private final static ParserLogger LOG = new ParserLogger("#org.jetbrains.idea.perforce.perforce.jobs.JobsOutputParser",
                                                           "'p4 jobs' output parsing error.");
  private final PerforceJobSpecification mySpecification;
  private final List<String> myLines;
  private final P4Connection myConnection;
  private final ConnectionKey myKey;

  public JobsOutputParser(PerforceJobSpecification specification, List<String> lines, P4Connection connection, ConnectionKey key) {
    mySpecification = specification;
    myLines = lines;
    myConnection = connection;
    myKey = key;
  }

  // without details (custom fields)
  public List<PerforceJob> parse() throws VcsException {
    final List<PerforceJob> result = new ArrayList<>();
    final List<FieldParser> fieldParsers = createStandardParsersBySpec();

    for (String line : myLines) {
      if (line.startsWith("\t")) {
        if (result.isEmpty()) LOG.generateParseException("Cannot parse line: " + line);
        PerforceJob lastJob = result.get(result.size() - 1);
        final PerforceJobFieldValue field = lastJob.getValueForStandardField(StandardJobFields.description);
        if (field != null) {
          field.setValue(field.getValue() + " " + line.trim());
        }
        else {
          LOG.generateParseException("Cannot append description line: " + line);
        }
      } else {
        result.add(parseLine(line, fieldParsers));
      }
    }
    return result;
  }

  private PerforceJob parseLine(final String line, final List<FieldParser> fieldParsers) throws VcsException {
    final List<PerforceJobFieldValue> values = new ArrayList<>(fieldParsers.size());
    final Ref<Integer> startAt = new Ref<>(0);
    for (FieldParser parser : fieldParsers) {
      parser.parse(line, startAt, values);
    }
    return new PerforceJob(values, Collections.emptyList(), myConnection, myKey);
  }

  private static class FieldParser {
    private final PerforceJobField myField;
    private final Parser myParser;

    protected FieldParser(@NotNull final PerforceJobField field, final Parser parser) {
      myField = field;
      myParser = parser;
    }

    protected void parse(final String line, final Ref<Integer> startAt, final List<PerforceJobFieldValue> fieldValues) throws VcsException {
      final String s = myParser.parse(line, startAt);
      if (s != null) {
        fieldValues.add(new PerforceJobFieldValue(myField, s));
      }
    }
  }

  private interface Parser {
    String BY = " by ";
    String ON = " on ";
    String STAR = "*";

    @NotNull StandardJobFields getField();

    @Nullable
    String parse(final String s, final Ref<Integer> startFrom) throws VcsException;
  }

  private List<FieldParser> createStandardParsersBySpec() {
    final List<FieldParser> result = new ArrayList<>();
    for (Parser parser : ourParsers) {
      final PerforceJobField field = mySpecification.getFieldByCode(parser.getField().getFixedCode());
      if (field != null) {
        result.add(new FieldParser(field, parser));
      }
    }

    return result;
  }

  //jobname on date by user *status* description
  private final static Parser[] ourParsers = new Parser[] {
    new Parser() {
      @Override
      @NotNull
      public StandardJobFields getField() {
        return StandardJobFields.name;
      }

      @Override
      @NotNull
      public String parse(String s, final Ref<Integer> startFrom) throws VcsException {
        int idx = s.indexOf(" ");
        idx = (idx == -1) ? s.length() : idx;
        final String result = s.substring(0, idx);
        startFrom.set(result.length());
        return result;
      }
    },
    new Parser() {
      @Override
      @NotNull
      public StandardJobFields getField() {
        return StandardJobFields.date;
      }

      @Override
      @Nullable
      public String parse(String s, final Ref<Integer> startFrom) throws VcsException {
        final int idx = s.indexOf(ON, startFrom.get());
        if (idx == -1) {
          return null;
        }
        int idxEnd = s.indexOf(" ", idx + ON.length());
        idxEnd = (idxEnd == -1) ? s.length() : idxEnd;
        final String result = s.substring(idx + ON.length(), idxEnd);
        startFrom.set(idxEnd);
        return result.trim();
      }
    },
    new Parser() {
      @Override
      @NotNull
      public StandardJobFields getField() {
        return StandardJobFields.user;
      }

      @Override
      @Nullable
      public String parse(String s, final Ref<Integer> startFrom) throws VcsException {
        final int idx = s.indexOf(BY, startFrom.get());
        if (idx == -1) {
          return null;
        }
        int idxEnd = s.indexOf(" ", idx + BY.length());
        idxEnd = (idxEnd == -1) ? s.length() : idxEnd;
        startFrom.set(idxEnd);
        return s.substring(idx + BY.length(), idxEnd).trim();
      }
    },
    new Parser() {
      @Override
      @NotNull
      public StandardJobFields getField() {
        return StandardJobFields.status;
      }

      @Override
      @Nullable
      public String parse(String s, final Ref<Integer> startFrom) throws VcsException {
        final int idx1 = s.indexOf(STAR, startFrom.get());
        if (idx1 == -1) {
          return null;
        }
        final int idx2 = s.indexOf(STAR, idx1 + STAR.length());
        if (idx2 == -1) LOG.generateParseException("Cannot find status in '" + s + "'");
        startFrom.set(idx2 + 1);
        return s.substring(idx1 + STAR.length(), idx2).trim();
      }
    },
    new Parser() {
      @Override
      @NotNull
      public StandardJobFields getField() {
        return StandardJobFields.description;
      }

      @Override
      @NotNull
      public String parse(String s, final Ref<Integer> startFrom) {
        String removeWrappers = s.substring(startFrom.get()).trim();
        removeWrappers = StringUtil.trimStart(removeWrappers, "'");
        if (removeWrappers.endsWith("'")) {
          removeWrappers = removeWrappers.substring(0, removeWrappers.length() - 2);
        }
        startFrom.set(s.length());
        return removeWrappers.trim();
      }
    }
  };
}
