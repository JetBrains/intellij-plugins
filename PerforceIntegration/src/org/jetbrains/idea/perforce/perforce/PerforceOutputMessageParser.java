/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.containers.Convertor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class PerforceOutputMessageParser extends OutputMessageParser {
  private static final Logger LOG = Logger.getInstance(PerforceOutputMessageParser.class);
  @NonNls private static final String CHANGE_PREFIX = "change";

  private PerforceOutputMessageParser(final String output) throws IOException {
    super(output);
  }

  public static List<PerforceChange> processOpenedOutput(final String opened) throws IOException {
    final ArrayList<PerforceChange> result = new ArrayList<>();
    final PerforceOutputMessageParser parser = new PerforceOutputMessageParser(opened);
    PerforceChange change;
    while((change = parser.readNextOpened()) != null) {
      result.add(change);
    }

    return result;
  }

  @Nullable
  private PerforceChange readNextOpened() {
    if (myLines.isEmpty()) return null;
    myCurrentLine = myLines.remove(0);
    String originalLine = myCurrentLine;

    if (myCurrentLine != null) {
      String depotPath = readTo("#");
      skip("#");
      String revisionNumber = readTo(" - ");
      if (revisionNumber == null) {
        throw new RuntimeException("Invalid 'p4 opened' line format: " + originalLine);
      }
      skip("-");
      String changeType = readTo(" ");

      long changeListNumber = -1;
      if (myCurrentLine.startsWith(CHANGE_PREFIX)) {
        skip(CHANGE_PREFIX);
        changeListNumber = Long.parseLong(readTo(" "));
      }

      return new PerforceChange(PerforceAbstractChange.convertToType(changeType), null, depotPath, Long.parseLong(revisionNumber),
                                changeListNumber, null);
    }
    return null;
  }

  public static List<ResolvedFile> processResolvedOutput(final String resolved,
                                                         @NotNull final Convertor<String, String> pathConvertor)
    throws IOException, VcsException {
    final ArrayList<ResolvedFile> result = new ArrayList<>();
    final PerforceOutputMessageParser parser = new PerforceOutputMessageParser(resolved);
    ResolvedFile file;
    while((file = parser.readNextResolved(pathConvertor)) != null) {
      result.add(file);
    }

    return result;
  }

  @Nullable
  private ResolvedFile readNextResolved(@NotNull final Convertor<String, String> pathConvertor) throws IOException, VcsException {
    if (myLines.isEmpty()) return null;
    myCurrentLine = myLines.remove(0);

    if (myCurrentLine != null) {
      String wholeLine = myCurrentLine;
      String localPath = readTo(" - ");
      if (localPath == null) {
        throw unexpectedFormat(wholeLine);
      }

      File localFile = new File(pathConvertor.convert(localPath));
      skip("-");
      String operation = readTo("//");
      String depotPath = readTo("#");
      if (depotPath == null) {
        depotPath = readTo("@=");
        if (depotPath != null) {
          return new ResolvedFile(localFile, operation, depotPath, -1, -1);
        }
        if (operation.startsWith("resolved ")) {
          return null;
        }
      }

      skip("#");
      long revision1;
      long revision2 = -1;
      try {
        if (myCurrentLine.indexOf(',') >= 0) {
          revision1 = Long.parseLong(readTo(","));
          skip(",#");
          revision2 = Long.parseLong(myCurrentLine);
        }
        else {
          revision1 = Long.parseLong(myCurrentLine);
        }
      }
      catch (NumberFormatException e) {
        throw unexpectedFormat(wholeLine);
      }

      return new ResolvedFile(localFile, operation, depotPath, revision1, revision2);
    }
    return null;
  }

  private VcsException unexpectedFormat(String wholeLine) {
    LOG.debug("Cannot parse p4 resolved output:\n" + myOutput + "\n\n current=" + wholeLine);
    return new VcsException(PerforceBundle.message("error.cannot.parse.p4.resolved.output"));
  }
}