/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;

import java.util.List;

/**
 * Note: the result could be like that:
 * "//depot/p4Trunk/src/com/spaces here also/sss/a b.txt <no line break>
 * //unit-206/p4Trunk/src/com/spaces here also_/sss/a b.txt <no line break>
 * c:/depot3666/p4Trunk\src\com\spaces here also_\sss\a b.txt"
 *
 * so it is not that simple
 */
public class WhereParser {
  private String myIn;
  private final List<String> myClientRoots;
  private final String myClientName;
  private final @NlsSafe String myAnyPath;

  private String myLocal;
  private String myLocalRootRelative;
  private String myDepot;

  // client name case - can it be an issue?
  WhereParser(@NotNull String in, List<String> clientRoots, @NotNull String clientName, @NotNull String anyPath) {
    myIn = in;
    myClientRoots = clientRoots;
    myClientName = clientName;
    myAnyPath = anyPath;
  }

  public void execute() throws VcsException {
    String preparedAnyPath = myAnyPath.replace('\\', '/');
    final int lastIdx = preparedAnyPath.lastIndexOf('/');
    preparedAnyPath = (lastIdx == -1) ? preparedAnyPath : preparedAnyPath.substring(lastIdx + 1);

    myIn = myIn.replace('\\', '/');

    final int idxOfLocalRootRelative = myIn.indexOf(" //" + myClientName + "/");
    if (idxOfLocalRootRelative == -1) throw new VcsException(createErrorMessage(preparedAnyPath));

    final int idxLocal = findRelativeEnd(idxOfLocalRootRelative + 1);
    if ((idxLocal == -1) || (idxLocal <= idxOfLocalRootRelative)) throw new VcsException(createErrorMessage(preparedAnyPath));

    myDepot = myIn.substring(0, idxOfLocalRootRelative).trim();
    myLocalRootRelative = myIn.substring(idxOfLocalRootRelative, idxLocal).trim();
    myLocal = myIn.substring(idxLocal).trim();
  }

  private int findRelativeEnd(int from) {
    for (String root : myClientRoots) {
      int index = myIn.indexOf(" " + root, from);
      if (index >= 0) {
        return index;
      }
    }

    if (SystemInfo.isWindows) {
      int colon = myIn.indexOf(":", from);
      while (colon > from + 2) {
        if (myIn.charAt(colon - 2) == ' ' &&
            Character.isLetter(myIn.charAt(colon - 1)) &&
            colon + 1 < myIn.length() &&
            (myIn.charAt(colon + 1) == '/' || myIn.charAt(colon + 1) == '\\')) {
          return colon - 2;
        }
        colon = myIn.indexOf(":", colon + 1);
      }
    }

    return myIn.indexOf(" ", from);
  }

  public String getLocal() {
    return myLocal;
  }

  public String getLocalRootRelative() {
    return myLocalRootRelative;
  }

  public String getDepot() {
    return myDepot;
  }

  private String createErrorMessage(final String preparedAnyPath) {
    String details = myIn + ", myAnyPath = '" + myAnyPath + "', anyPathFile = '" + preparedAnyPath + "', myClientName = '" +
               myClientName + "', myClientRoots = '" + myClientRoots + "'";
    return PerforceBundle.message("error.p4.where.wrong.result", details);
  }
}
