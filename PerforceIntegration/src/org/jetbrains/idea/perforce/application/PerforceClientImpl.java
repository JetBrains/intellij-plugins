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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.View;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.P4ParametersConnection;
import org.jetbrains.idea.perforce.perforce.connections.SingletonConnection;

import java.util.List;
import java.util.Map;

class PerforceClientImpl implements PerforceClient {
  private final Project myProject;
  private final P4Connection myConnection;

  PerforceClientImpl(final Project project, P4Connection connection) {
    myProject = project;
    myConnection = connection;
  }

  private Map<String, List<String>> getInfo() throws VcsException {
    return PerforceManager.getInstance(myProject).getCachedInfo(myConnection);
  }

  @Override
  public @NotNull Project getProject() {
    return myProject;
  }

  @Override
  public String getName() throws VcsException {
    return getFieldValue(PerforceRunner.CLIENT_NAME);
  }

  @Override
  public @NotNull List<String> getRoots() throws VcsException {
    return PerforceManager.getInstance(myProject).getClientRoots(myConnection);
  }

  @Override
  public List<View> getViews() throws VcsException {
    return PerforceManager.getInstance(myProject).getCachedClients(myConnection).getViews();
  }

  @Override
  public @Nullable List<String> getCachedOptions() {
    try {
      ClientData clientSpec = PerforceManager.getInstance(myProject).getClientOnlyCached(myConnection);
      return clientSpec == null ? null : clientSpec.getOptions();
    }
    catch (VcsException e) {
      return null;
    }
  }

  private String getFieldValue(final String fieldName) throws VcsException {
    List<String> names = getInfo().get(fieldName);
    return names == null || names.isEmpty() ? null : names.get(0);
  }

  @Override
  public String getUserName() throws VcsException {
    return getFieldValue(PerforceRunner.USER_NAME);
  }

  @Override
  public @Nullable String getServerPort() throws VcsException {
    return getFieldValue(PerforceRunner.SERVER_ADDRESS);
  }

  @Override
  public @Nullable String getDeclaredServerPort() {
    if (myConnection instanceof P4ParametersConnection) {
      return ((P4ParametersConnection)myConnection).getParameters().getServer();
    }
    if (myConnection instanceof SingletonConnection) {
      return PerforceSettings.getSettings(myProject).port;
    }
    return null;
  }
}
