// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManagerGate;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import static org.jetbrains.idea.perforce.perforce.PerforceChangeListHelper.findOrCreateDefaultList;

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
    return list != null ? list : findOrCreateDefaultList(myAddGate);
  }
}
