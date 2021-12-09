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

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

class PerforceChangeListCalculator {
  private final ChangeListManagerGate myAddGate;
  private final PerforceNumberNameSynchronizer mySynchronizer;
  private final ConnectionKey myConnectionKey;

  PerforceChangeListCalculator(Project project, P4Connection connection, ChangeListManagerGate addGate) {
    myConnectionKey = connection.getConnectionKey();
    myAddGate = addGate;
    mySynchronizer = PerforceNumberNameSynchronizer.getInstance(project);
  }

  @NotNull
  ChangeList convert(PerforceChange perforceChange) {
    LocalChangeList list = myAddGate.findChangeList(mySynchronizer.getName(myConnectionKey, perforceChange.getChangeList()));
    return list != null ? list : findOrCreateDefaultList();
  }

  private ChangeList findOrCreateDefaultList() {
    for (String name : LocalChangeList.getAllDefaultNames()) {
      LocalChangeList list = myAddGate.findChangeList(name);
      if (list != null) return list;
    }
    return myAddGate.addChangeList(LocalChangeList.getDefaultName(), "");
  }
}
