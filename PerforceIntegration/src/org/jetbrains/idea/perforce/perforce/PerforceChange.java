/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;

import java.io.File;


public class PerforceChange extends PerforceAbstractChange {
  private static final Logger LOG = Logger.getInstance(PerforceChange.class);
  private final String myDepotPath;
  private long myRevision;
  private final long myChangeListNumber;
  private final String myChangeListDescription;

  public static @Nullable PerforceChange createOn(String fileString, PerforceClient client) throws VcsException {
    // must be called only in tests
    return createOn(fileString, client, -1, null);
  }

  public static @Nullable PerforceChange createOn(String fileString,
                                                  PerforceClient client,
                                                  long changeListNumber,
                                                  @Nullable String changeListDescription) throws VcsException {
    fileString = fileString.trim();
    int typeIndex = fileString.indexOf("#");
    if (typeIndex < 0) {
      throw new VcsException(PerforceBundle.message("error.cannot.parse.change.line", fileString));
    }

    String depotPath = fileString.substring(0, typeIndex - 1);
    final File localFile = PerforceManager.getFileByDepotName(depotPath, client);
    if (localFile != null) {
      String type = fileString.substring(typeIndex + 1).trim();
      return new PerforceChange(PerforceAbstractChange.convertToType(type), localFile, depotPath, -1,
                                changeListNumber, changeListDescription);
    }
    else {
      return null;
    }
  }

  public PerforceChange(int type, File localFile, String depotPath, long revision,
                        final long changeListNumber, final String changeListDescription) {
    myChangeListNumber = changeListNumber;
    myChangeListDescription = changeListDescription;
    myDepotPath = depotPath;
    myRevision = revision;
    setFile(localFile);
    myType = type;
  }

  public long getChangeList() {
    return myChangeListNumber;
  }

  public String getDepotPath() {
    return myDepotPath;
  }

  public String getChangeListDescription() {
    return myChangeListDescription;
  }

  public long getRevision() {
    if (myRevision == -1) {
      File file = getFile();
      LOG.info(new Throwable("revision == -1, type: " + myType + " path: " + (file == null ? null : file.getAbsolutePath())));
    }
    // details of default changelist are retrieved via 'p4 change -o', which does not return revision numbers
    /*if (myRevision == -1) {
      throw new UnsupportedOperationException("Unknown revision number");
    }*/
    return myRevision;
  }

  @Override
  public @NonNls String toString() {
    return "PerforceChange[" + myDepotPath + "," + myType + "]";
  }

  public void setRevision(long revision) {
    myRevision = revision;
  }
}
