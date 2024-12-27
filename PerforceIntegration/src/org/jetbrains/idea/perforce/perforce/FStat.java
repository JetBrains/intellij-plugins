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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.changesBrowser.FileChange;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


public final class FStat {
  private static final Logger LOG = Logger.getInstance(FStat.class);

  public Status status = STATUS_UNKNOWN;
  public Local local = LOCAL_NOT_LOCAL;

  public enum Status {
    UNKNOWN, NOT_IN_CLIENTSPEC, NOT_ADDED, ONLY_ON_SERVER, ON_SERVER_AND_LOCAL, ONLY_LOCAL, DELETED
  }

  // status values
  public static final Status STATUS_UNKNOWN = Status.UNKNOWN;
  public static final Status STATUS_NOT_IN_CLIENTSPEC = Status.NOT_IN_CLIENTSPEC;
  public static final Status STATUS_NOT_ADDED = Status.NOT_ADDED;
  public static final Status STATUS_ONLY_ON_SERVER = Status.ONLY_ON_SERVER;
  public static final Status STATUS_ON_SERVER_AND_LOCAL = Status.ON_SERVER_AND_LOCAL;
  public static final Status STATUS_ONLY_LOCAL = Status.ONLY_LOCAL;
  public static final Status STATUS_DELETED = Status.DELETED;

  public enum Local {
    NOT_LOCAL, CHECKED_IN, CHECKED_OUT, ADDING, BRANCHING, DELETING, INTEGRATING, MOVE_ADDING, MOVE_DELETING
  }
  // local status
  public static final Local LOCAL_NOT_LOCAL = Local.NOT_LOCAL;
  public static final Local LOCAL_CHECKED_IN = Local.CHECKED_IN;
  public static final Local LOCAL_CHECKED_OUT = Local.CHECKED_OUT;
  public static final Local LOCAL_ADDING = Local.ADDING;
  public static final Local LOCAL_BRANCHING = Local.BRANCHING;
  public static final Local LOCAL_DELETING = Local.DELETING;
  public static final Local LOCAL_INTEGRATING = Local.INTEGRATING;
  public static final Local LOCAL_MOVE_ADDING = Local.MOVE_ADDING;
  public static final Local LOCAL_MOVE_DELETING = Local.MOVE_DELETING;

  public long statTime = System.currentTimeMillis();
  public @Nullable String movedFile = "";
  public @NonNls String clientFile = "";
  public @NonNls String depotFile = "";
  public @NonNls String headAction = "";
  public @NonNls String headChange = "";
  public @NonNls String headRev = "";
  public @NonNls String headType = "";
  public @NonNls String headTime = "";
  public @NonNls String haveRev = "";
  public @NonNls String action = "";
  public @NonNls String actionOwner = "";
  public @NonNls String change = "";
  public @NonNls String unresolved = null;
  public P4File fromFile = null;
  static final @NonNls String MOVED_FILE_STATUS_FIELD = "movedFile";
  static final @NonNls String CLIENT_FILE_STATUS_FIELD = "clientFile ";
  static final @NonNls String DEPOT_FILE_STATUS_FIELD = "depotFile ";
  static final @NonNls String HEAD_ACTION_STATUS_FIELD = "headAction ";
  static final @NonNls String HEAD_CHANGE_STATUS_FIELD = "headChange ";
  static final @NonNls String HEAD_REV_STATUS_FIELD = "headRev ";
  static final @NonNls String HEAD_TYPE_STATUS_FIELD = "headType ";
  static final @NonNls String HEAD_TIME_STATUS_FIELD = "headTime ";
  static final @NonNls String HAVE_REV_STATUS_FIELD = "haveRev ";
  static final @NonNls String ACTION_STATUS_FIELD = "action ";
  static final @NonNls String ACTION_OWNER_STATUS_FIELD = "actionOwner ";
  static final @NonNls String CHANGE_STATUS_FIELD = "change ";
  static final @NonNls String UNRESOLVED_STATUS_FIELD = "unresolved ";

  @Override
  public @NonNls String toString() {
    return "org.jetbrains.idea.perforce.perforce.FStat{" + "status=" + status + ", local=" + local + ", statTime=" + statTime +
           ", \nclientFile='" + clientFile + "'" + ", \ndepotFile='" + depotFile + "'" + ", \nheadAction='" + headAction + "'" +
           ", headChange='" + headChange + "'" + ", headRev='" + headRev + "'" + ", headType='" + headType + "'" + ", headTime='" +
           headTime + "'" + ", haveRev='" + haveRev + "'" + ", \naction='" + action + "'" + ", actionOwner='" + actionOwner + "'" +
           ", change='" + change + "'" + ", unresolved='" + unresolved + "'" + ", branchedFrom='" + fromFile + "'" + "}";
  }


  private void resolveStatus() throws VcsException {
    //
    // resolve the status and local
    //
    if (!headRev.isEmpty() && haveRev.isEmpty()) {
      local = LOCAL_NOT_LOCAL;
      if (headAction.equals(FileChange.DELETE_ACTION)) {
        status = STATUS_DELETED;
      }
      else {
        status = STATUS_ONLY_ON_SERVER;
      }
    }
    else {
      if (headRev.isEmpty()) {
        status = STATUS_ONLY_LOCAL;
      }
      else {
        status = STATUS_ON_SERVER_AND_LOCAL;
      }
    }

    if (action.isEmpty()) {
      local = LOCAL_CHECKED_IN;
    }
    else if (FileChange.ADD_ACTION.equals(action)) {
      local = LOCAL_ADDING;
    }
    else if (FileChange.EDIT_ACTION.equals(action)) {
      local = LOCAL_CHECKED_OUT;
    }
    else if (FileChange.BRANCH_ACTION.equals(action)) {
      local = LOCAL_BRANCHING;
    }
    else if (FileChange.INTEGRATE_ACTION.equals(action)) {
      local = LOCAL_INTEGRATING;
    }
    else if (FileChange.DELETE_ACTION.equals(action)) {
      local = LOCAL_DELETING;
    } else if (FileChange.MOVE_DELETE_ACTION.equals(action)) {
      local = LOCAL_MOVE_DELETING;
    } else if (FileChange.MOVE_ADD_ACTION.equals(action)) {
      local = LOCAL_MOVE_ADDING;
    } else {
      throw new VcsException(PerforceBundle.message("exception.text.unknown.action", action));
    }
  }

  static FStat parseFStat(BufferedReader rdr) throws VcsException {
    FStat result = null;
    String s;
    try {
      while ((s = rdr.readLine()) != null) {
        if (s.isEmpty()) {
          break; // empty lines separate multiple fstat results
        }
        // check first "... "
        if (s.indexOf("... ") != 0) {
          throw new VcsException(PerforceBundle.message("exception.text.unexpected.fstat.line.syntax", s));
        }
        if (result == null) {
          result = new FStat();
        }
        final String line = s.substring(4);

        if (line.startsWith(CLIENT_FILE_STATUS_FIELD)) {
          result.clientFile = line.substring(11);
        }
        else if (line.startsWith(DEPOT_FILE_STATUS_FIELD)) {
          result.depotFile = line.substring(10);
        }
        else if (line.startsWith(HEAD_ACTION_STATUS_FIELD)) {
          result.headAction = line.substring(11);
        }
        else if (line.startsWith(HEAD_CHANGE_STATUS_FIELD)) {
          result.headChange = line.substring(11);
        }
        else if (line.startsWith(HEAD_REV_STATUS_FIELD)) {
          result.headRev = line.substring(8);
        }
        else if (line.startsWith(HEAD_TYPE_STATUS_FIELD)) {
          result.headType = line.substring(9);
        }
        else if (line.startsWith(HEAD_TIME_STATUS_FIELD)) {
          result.headTime = line.substring(9);
        }
        else if (line.startsWith(HAVE_REV_STATUS_FIELD)) {
          result.haveRev = line.substring(8);
        }
        else if (line.startsWith(ACTION_STATUS_FIELD)) {
          result.action = line.substring(7);
        }
        else if (line.startsWith(ACTION_OWNER_STATUS_FIELD)) {
          result.actionOwner = line.substring(12);
        }
        else if (line.startsWith(CHANGE_STATUS_FIELD)) {
          result.change = line.substring(7);
        }
        else if (line.startsWith(UNRESOLVED_STATUS_FIELD)) {
          result.unresolved = line.substring(11);
        } else if (line.startsWith(MOVED_FILE_STATUS_FIELD)) {
          result.movedFile = line.substring(10);
        }
        else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Unparsed fstat field: \"" + s + "\"");
          }
        }
      }
    }
    catch (IOException ex) {
      throw new VcsException(PerforceBundle.message("exception.text.cannot.parse.fstat.stdout"));
    }

    if (result != null) {
      result.resolveStatus();
    }
    return result;
  }

  public static Map<File, String> splitOutputForEachFile(final String stdOut) throws IOException {
    final BufferedReader reader = new BufferedReader(new StringReader(stdOut));
    String line;
    File file = null;
    StringBuilder currentBuffer = new StringBuilder();
    final HashMap<File, String> result = new HashMap<>();
    while ((line = reader.readLine()) != null) {
      if (line.isEmpty()) {
        if (file != null) {
          result.put(file, currentBuffer.toString());
          currentBuffer = new StringBuilder();
          file = null;
        }
      }
      else {
        currentBuffer.append(line);
        if (line.startsWith(PerforceRunner.CLIENT_FILE_PREFIX)) {
          file = new File(line.substring(PerforceRunner.CLIENT_FILE_PREFIX.length()).trim());
        }
        currentBuffer.append("\n");
      }
    }

    if (file != null) {
      result.put(file, currentBuffer.toString());
    }

    return result;
  }

  public boolean isOpenedOrAdded() {
    return LOCAL_CHECKED_OUT == local || LOCAL_ADDING == local || LOCAL_BRANCHING == local || LOCAL_INTEGRATING == local ||
           LOCAL_MOVE_ADDING == local;
  }
}
