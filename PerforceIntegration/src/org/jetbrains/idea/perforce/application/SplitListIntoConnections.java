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
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SplitListIntoConnections<T> {
  private final MultiMap<ConnectionKey, FilePath> myPaths = MultiMap.createSet();
  private final PerforceConnectionManagerI myConnectionManager;
  private final Convertor<? super P4Connection, ? extends T> myFactory;
  private final Map<ConnectionKey, T> myByConnectionMap = new HashMap<>();

  public SplitListIntoConnections(final Project project, final Convertor<? super P4Connection, ? extends T> factory) {
    myFactory = factory;
    myConnectionManager = PerforceConnectionManager.getInstance(project);
  }

  private void processRevision(final ContentRevision contentRevision) throws VcsException {
    final FilePath filePath = contentRevision.getFile();
    final P4Connection connection = myConnectionManager.getConnectionForFile(filePath.getIOFile());
    if (connection == null) return;
    final ConnectionKey key = connection.getConnectionKey();
    myPaths.putValue(key, filePath);
    myByConnectionMap.put(key, myFactory.convert(connection));
  }

  public void execute(final Collection<? extends Change> incomingChanges) throws VcsException {
    for (Change incomingChange : incomingChanges) {
      if (incomingChange.getBeforeRevision() != null) {
        processRevision(incomingChange.getBeforeRevision());
      }
      if (incomingChange.getAfterRevision() != null) {
        processRevision(incomingChange.getAfterRevision());
      }
    }
  }

  public MultiMap<ConnectionKey, FilePath> getPaths() {
    return myPaths;
  }

  public Map<ConnectionKey, T> getByConnectionMap() {
    return myByConnectionMap;
  }
}
