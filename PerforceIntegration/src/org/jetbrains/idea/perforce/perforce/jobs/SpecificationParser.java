package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.vcs.VcsException;

import java.util.ArrayList;
import java.util.List;

public class SpecificationParser {
  private static final String PARSE_ERROR = "Perforce Job Specification parse error.";
  private static final ParserLogger LOG = new ParserLogger("#org.jetbrains.idea.perforce.perforce.jobs.SpecificationParser", PARSE_ERROR);

  private final List<String> myLines;

  public SpecificationParser(List<String> lines) {
    myLines = lines;
  }

  public PerforceJobSpecification parse() throws VcsException {
    final List<PerforceJobField> fields = new ArrayList<>();
    boolean inFields = false;
    for (String line : myLines) {
      if (inFields) {
        if (fieldsFinished(line)) break;
        final PerforceJobField field = parseField(line);
        fields.add(field);
      } else {
        inFields = fieldsStarted(line);
      }
    }

    return new PerforceJobSpecification(fields);
  }

  private static boolean fieldsFinished(final String line) {
    String trimmed = line.trim();
    return trimmed.startsWith("Values:") || trimmed.startsWith("Comments:") || trimmed.startsWith("Presets:");
  }

  private static boolean fieldsStarted(final String line) {
    return line.trim().startsWith("Fields:");
  }

  private static PerforceJobField parseField(final String line) throws VcsException {
    final PerforceJobField result = new PerforceJobField();
    final String trimmed = line.trim();
    // code name datatype length persistence
    final String[] words = trimmed.split(" ");
    for (int i = 0; i < words.length; i++) {
      final String word = words[i];
      ourFieldParsers[i].parseAndFill(word, result);
    }
    if (! result.filled()) {
      LOG.generateParseException("Not all fields specified");
    }
    return result;
  }

  private static final FieldParser[] ourFieldParsers = new FieldParser[] {
    new FieldParser("code") {
      @Override
      protected void parseAndFill(String s, PerforceJobField field) throws VcsException {
        int value = -1;
        try {
          value = Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
          LOG.generateParseException("Code could not be parsed: " + s);
        }
        if ((value <= 100) || (value > 199)) {
          LOG.generateParseException("Code is out of [101,199] interval: " + value);
        }
        field.setCode(value);
      }
    },
    new FieldParser("name") {
      @Override
      protected void parseAndFill(String s, PerforceJobField field) {
        field.setName(s);
      }
    },
    new FieldParser("datatype") {
      @Override
      protected void parseAndFill(String s, PerforceJobField field) {
        final PerforceJobFieldType type = PerforceJobFieldType.valueOf(s);
        field.setType(type);
      }
    },
    new FieldParser("length") {
      @Override
      protected void parseAndFill(String s, PerforceJobField field) {
      }
    },
    new FieldParser("persistence") {
      @Override
      protected void parseAndFill(String s, PerforceJobField field) throws VcsException {
        final PerforceJobPersistenceType persistenceType = PerforceJobPersistenceType.parse(s);
        if (persistenceType == null) {
          LOG.generateParseException("Cannot parse persistence: " + s);
        }
        field.setPersistence(persistenceType);
      }
    },
  };

  private abstract static class FieldParser {
    // for debug
    private final String myName;

    protected FieldParser(String name) {
      myName = name;
    }

    protected abstract void parseAndFill(final String s, final PerforceJobField field) throws VcsException;

    @Override
    public String toString() {
      return myName;
    }
  }
}
