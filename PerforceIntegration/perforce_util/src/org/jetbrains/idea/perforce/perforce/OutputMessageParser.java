/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.ClientVersion;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.changesBrowser.FileChange;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputMessageParser {
  private static final Logger LOG = Logger.getInstance(OutputMessageParser.class);
  protected LinkedList<String> myLines = new LinkedList<>();
  private String myDepotPath;
  protected String myCurrentLine;
  private boolean myIsBranched = false;
  private String myBranch = null;
  private final ProgressIndicator myProgressIndicator;

  public static final DateTimeFormatter NEW_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.US);
  public static final DateTimeFormatter OLD_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.US);

  private static final @NonNls String CLIENT_PREFIX = "Client";
  private static final String FILE_PREFIX = "...";
  protected final String myOutput;

  private static class ChangeReadingPolicy {

    private static final @NonNls String CHANGE_REGEX = "(Change )(.*)( on )(.*)( by )(.*)(@)(.*)";
    public static final Pattern CHANGE_PATTERN = Pattern.compile(CHANGE_REGEX);

    private static final @NonNls String CHANGE_DESCRIPTION_REGEX = "(Change )(.*)( by )(.*)(@)(.*)( on )(.*)";
    public static final Pattern CHANGE_DESCRIPTION_PATTERN = Pattern.compile(CHANGE_DESCRIPTION_REGEX);

    private final Pattern myPattern;
    private final int myUserGroup;
    private final int myDateGroupGroup;

    ChangeReadingPolicy(final Pattern pattern, final int userGroup, final int dateGroupGroup) {
      myPattern = pattern;
      myUserGroup = userGroup;
      myDateGroupGroup = dateGroupGroup;
    }

    public Pattern getPattern() {
      return myPattern;
    }

    public int getUserGroup() {
      return myUserGroup;
    }

    public int getDateGroup() {
      return myDateGroupGroup;
    }
  }


  private static final @NonNls String LOG_REGEX = "(... #)(.*)( change )(.*)( )(.*)( on )(.*)( by )(.*)(@)(.*)( \\()(.*)(\\))";
  public static final Pattern LOG_PATTERN = Pattern.compile(LOG_REGEX);
  public static final Pattern DEPOT_PATTERN = Pattern.compile("(//)(.*)");

  public static final @NonNls Pattern SERVER_VERSION_PATTERN = Pattern.compile("(.*\\/.*\\/)(.*)(\\.)(\\d+)([^\\d]*\\/.* \\(.*\\/.*\\/.*\\))");
  public static final @NonNls Pattern CLIENT_VERSION_PATTERN = Pattern.compile("(.*/.*/)(.*)(\\.)(\\d+)[^\\d]*/(.*) (\\(.*/.*/.*\\)).");
  private static final @NonNls String BRANCH_FROM_PREFIX = "... ... branch from ";
  private static final @NonNls String AFFECTED_FILES_PREFIX = "Affected files ...";
  private static final @NonNls String SHELVED_FILES_PREFIX = "Shelved files ...";
  private static final @NonNls String BRANCH_PREFIX = "Branch";

  protected OutputMessageParser(final String output) {
    myOutput = output;

    if (ApplicationManager.getApplication() != null) {
      myProgressIndicator = ProgressManager.getInstance().getProgressIndicator();
    } else {
      myProgressIndicator = null;
    }

    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(new StringReader(output));
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty()) {
          myLines.add(line);
        }
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    }
  }

  public static List<P4Revision> processLogOutput(String output, final boolean newDateFormat) throws DateTimeParseException {
    ArrayList<P4Revision> result = new ArrayList<>();
    final OutputMessageParser parser = new OutputMessageParser(output);

    P4Revision revision;
    while ((revision = parser.readNextRevision(newDateFormat)) != null) {
      result.add(revision);
    }
    return result;
  }

  private @Nullable P4Revision readNextRevision(final boolean newDateFormat) throws DateTimeParseException {
    if (myLines.isEmpty()) return null;
    myCurrentLine = myLines.remove(0);
    final Matcher logMatcher = LOG_PATTERN.matcher(myCurrentLine);
    if (logMatcher.matches() && myDepotPath != null) {
      String dateString = logMatcher.group(8);
      Date date = parseDate(newDateFormat, dateString);
      final P4Revision result =
        new P4Revision(myDepotPath, Long.parseLong(logMatcher.group(2)), Long.parseLong(logMatcher.group(4)),logMatcher.group(6),
                       date, logMatcher.group(10), logMatcher.group(12), logMatcher.group(14),
                       myIsBranched);
      final StringBuffer messages = new StringBuffer();
      readMessages(messages);
      result.setDescription(messages.toString());
      return result;
    } else {
      final Matcher depotMatcher = DEPOT_PATTERN.matcher(myCurrentLine);
      if (depotMatcher.matches()) {
        myDepotPath = myCurrentLine;
        return readNextRevision(newDateFormat);
      } else {
        if (myCurrentLine.startsWith(BRANCH_FROM_PREFIX)) {
          myIsBranched = true;
          myBranch = myCurrentLine;
        }
        return readNextRevision(newDateFormat);
      }
    }
  }

  private static Date parseDate(boolean newDateFormat, String dateString) throws DateTimeParseException {
    TemporalAccessor parsed;
    try {
      parsed = (newDateFormat ? NEW_DATE_FORMAT : OLD_DATE_FORMAT).parse(dateString);
    }
    catch (DateTimeParseException e) {
      parsed = OLD_DATE_FORMAT.parse(dateString);
    }
    if (!parsed.isSupported(ChronoField.HOUR_OF_DAY)) {
      parsed = LocalDate.from(parsed).atStartOfDay();
    }
    return Date.from(LocalDateTime.from(parsed).atZone(ZoneId.systemDefault()).toInstant());
  }

  private void readMessages(final StringBuffer result) {
    if (myBranch != null) {
      result.append("[");
      result.append(myBranch);
      result.append("]");
      myBranch = null;
    }
    while (!myLines.isEmpty()) {
      String line = myLines.get(0);
      if (!line.startsWith("\t")) return;
      myLines.remove(0);
      line = line.substring(1).trim();
      if (!result.isEmpty()) {
        result.append("\n");
      }
      result.append(line);
    }
  }

  protected void skip(final String s) {
    if (myCurrentLine.startsWith(s)) {
      myCurrentLine = myCurrentLine.substring(s.length()).trim();
    }
  }

  protected @Nullable String readTo(final String s) {
    final int position = myCurrentLine.indexOf(s);
    if (position < 0) return null;
    String result = myCurrentLine.substring(0, position).trim();
    myCurrentLine = myCurrentLine.substring(position).trim();
    return result;
  }

  public static List<ChangeListData> processChangesOutput(final String output) {
    final ArrayList<ChangeListData> result = new ArrayList<>();
    final OutputMessageParser parser = new OutputMessageParser(output);
    ChangeListData change;
    while ((change = parser.readNextChange(new ChangeReadingPolicy(ChangeReadingPolicy.CHANGE_PATTERN, 6, 4))) != null) {
      result.add(change);
    }
    return result;
  }

  private @Nullable ChangeListData readNextChange(final ChangeReadingPolicy changePattern) {
    if (myLines.isEmpty()) return null;
    myCurrentLine = myLines.remove(0);
    final Matcher matcher = changePattern.getPattern().matcher(myCurrentLine);
    if (matcher.matches()) {
      String number = matcher.group(2);
      long revisionNumber = Long.parseLong(number);
      String date = matcher.group(changePattern.getDateGroup());
      final String user = matcher.group(changePattern.getUserGroup());
      String client = matcher.group(changePattern.getUserGroup() + 2);
      final ChangeListData result = new ChangeListData();
      result.CLIENT = client;
      result.USER = user;
      result.DATE = date;
      result.NUMBER = revisionNumber;
      final StringBuffer messages = new StringBuffer();
      readMessages(messages);
      result.DESCRIPTION = messages.toString();
      return result;

    } else {
      return readNextChange(changePattern);
    }

  }

  public static List<String> processClientsOutput(final String output) {
    final ArrayList<String> result = new ArrayList<>();

    final OutputMessageParser parser = new OutputMessageParser(output);
    while (!parser.myLines.isEmpty()) {
      parser.myCurrentLine = parser.myLines.remove(0);
      if (parser.myCurrentLine.startsWith(CLIENT_PREFIX)) {
        parser.skip(CLIENT_PREFIX);
        result.add(parser.readTo(" "));
      }
    }
    return result;
  }

  public static List<String> processUsersOutput(final String output) {
    final ArrayList<String> result = new ArrayList<>();

    final OutputMessageParser parser = new OutputMessageParser(output);
    while (!parser.myLines.isEmpty()) {
      parser.myCurrentLine = parser.myLines.remove(0);
      result.add(parser.readTo("<"));
    }
    return result;

  }

  public static Map<ChangeListData, List<FileChange>> processMultiDescriptionOutput(final String output, boolean shelved) {
    LinkedHashMap<ChangeListData, List<FileChange>> result = new LinkedHashMap<>();
    final OutputMessageParser parser = new OutputMessageParser(output);
    while (!parser.myLines.isEmpty()) {
      ChangeListData data = parser.loadChangeListDescription();
      if (data != null) {
        result.put(data, parser.processSingleDescriptionOutput(shelved ? SHELVED_FILES_PREFIX : AFFECTED_FILES_PREFIX));
      }
    }
    return result;
  }

  private List<FileChange> processSingleDescriptionOutput(final String prefix) {
    final ArrayList<FileChange> result = new ArrayList<>();
    while (!myLines.isEmpty()) {
      myCurrentLine = myLines.removeFirst();
      if (myCurrentLine.startsWith(prefix)) {
        break;
      }
    }

    while (!myLines.isEmpty()) {
      if (!myLines.getFirst().startsWith(FILE_PREFIX)) {
        break;
      }

      myCurrentLine = myLines.removeFirst();
      result.add(createFileChange());
    }

    return result;
  }

  public static ServerVersion parseServerVersion(String str) {
    final Matcher matcher = SERVER_VERSION_PATTERN.matcher(str);
    if (!matcher.matches()) {
      return new ServerVersion(-1, -1);
    } else {
      final String year = matcher.group(2);
      final String version = matcher.group(4);
      try {
        return new ServerVersion(Long.parseLong(year), Long.parseLong(version));
      }
      catch (NumberFormatException e) {
        return new ServerVersion(-1, -1);
      }
    }

  }

  /*
   * Rev. P4/NTX86/2009.1/205670 (2009/06/29).
   * Rev. P4/NTX86/2006.2/112639 (2006/12/14).
   * Rev. P4/LINUX26X86/2008.2/179173 (2008/12/05).
   * Rev. P4/NTX86/2004.2/68597 (2004/09/03).
   */
  // server pattern is the same as client's
  public static ClientVersion parseClientVersion(final String str) {
    final Matcher matcher = CLIENT_VERSION_PATTERN.matcher(str);
    if (!matcher.matches()) {
      return ClientVersion.UNKNOWN;
    } else {
      final String year = matcher.group(2);
      final String version = matcher.group(4);
      final String build = matcher.group(5);
      final long yearLong;
      final long versionLong;
      try {
        yearLong = Long.parseLong(year);
        versionLong = Long.parseLong(version);
      }
      catch (NumberFormatException e) {
        return ClientVersion.UNKNOWN;
      }
      try {
        return new ClientVersion(yearLong, versionLong, Long.parseLong(build));
      }
      catch (NumberFormatException e) {
        // not that important
        return new ClientVersion(yearLong, versionLong, -1);
      }
    }
  }

  /*
  my
  Perforce - The Fast Software Configuration Management System.
Copyright 1995-2009 Perforce Software.  All rights reserved.
Rev. P4/NTX86/2009.1/205670 (2009/06/29).

Ira's
Perforce - The Fast Software Configuration Management System.
Copyright 1995-2006 Perforce Software. All rights reserved.
Rev. P4/NTX86/2006.2/112639 (2006/12/14).

ubuntu
Perforce - The Fast Software Configuration Management System.
Copyright 1995-2008 Perforce Software.  All rights reserved.
Rev. P4/LINUX26X86/2008.2/179173 (2008/12/05).

Jeka
Perforce - The Fast Software Configuration Management System.
Copyright 1995, 2004 Perforce Software. All rights reserved.
Rev. P4/NTX86/2004.2/68597 (2004/09/03).

*/

  private @NotNull FileChange createFileChange() {
    if (myProgressIndicator != null && myProgressIndicator.isCanceled()) {
      throw new ProcessCanceledException();
    }

    myCurrentLine = myCurrentLine.substring(FILE_PREFIX.length()).trim();
    final String repositoryFilePath =  readTo("#");
    skip("#");
    String fileRev = readTo(" ");
    String type = myCurrentLine.trim();
    final File file = new File(repositoryFilePath);
    long revision = fileRev != null && !"none".equals(fileRev) ? Long.parseLong(fileRev) : -1;
    return new FileChange(repositoryFilePath, file, revision, type);
  }

  public static List<String> processBranchesOutput(final String output) {
    final ArrayList<String> result = new ArrayList<>();

    final OutputMessageParser parser = new OutputMessageParser(output);

    String branch;
    while ((branch = parser.readNextBranch()) != null) {
      result.add(branch);
    }

    return result;
  }

  private @Nullable String readNextBranch() {

    if (myLines.isEmpty()) return null;
    myCurrentLine = myLines.remove(0);

    if (myCurrentLine != null) {
      final String[] strings = myCurrentLine.split(" ");
      if (strings.length > 2 && strings[0].equals(BRANCH_PREFIX)) {
        return strings[1];
      }
    }
    return null;
  }

  @Nullable
  ChangeListData loadChangeListDescription() {
    return readNextChange(new ChangeReadingPolicy(ChangeReadingPolicy.CHANGE_DESCRIPTION_PATTERN, 4, 8));
  }
}
