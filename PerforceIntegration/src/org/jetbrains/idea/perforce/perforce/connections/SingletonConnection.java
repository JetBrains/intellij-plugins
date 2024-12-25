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
package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.formove.FilePathComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FilterDescendantVirtualFileConvertible;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;

public class SingletonConnection extends AbstractP4Connection implements PerforceConnectionMapper {
  private static final Logger LOG = Logger.getInstance(SingletonConnection.class);

  private static final Key<SingletonConnection> KEY_IN_PROJECT = new Key<>("Connection per project");
  public static final ConnectionId SINGLETON_CONNECTION_ID = new ConnectionId();
  public static final File CURR_DIR = new File(".");
  private final PerforceSettings mySettings;
  private final Project myProject;

  private SingletonConnection(Project project) {
    this(project, PerforceSettings.getSettings(project));
  }

  public SingletonConnection(Project project, PerforceSettings settings) {
    myProject = project;
    mySettings = settings;
  }

  public static SingletonConnection getInstance(Project project){
    SingletonConnection result = project.getUserData(KEY_IN_PROJECT);
    if (result == null) {
      result = new SingletonConnection(project);
      project.putUserData(KEY_IN_PROJECT, result);
    }
    return result;
  }

  @Override
  public void runP4Command(PerforcePhysicalConnectionParametersI parameters,
                           String[] p4args,
                           ExecResult retVal,
                           @Nullable StringBuffer inputStream) {
    runP4CommandImpl(parameters, mySettings.getConnectArgs(), p4args, retVal, inputStream);
  }

  @Override
  public @NotNull File getWorkingDirectory() {
    VirtualFile[] roots = ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(PerforceVcs.getInstance(myProject));
    return roots.length > 0 ? VfsUtilCore.virtualToIoFile(roots[0]) : CURR_DIR;
  }

  @Override
  public @NotNull ConnectionKey getConnectionKey() {
    return new ConnectionKey(mySettings.port, mySettings.client, mySettings.user);
  }

  @Override
  public ConnectionId getId() {
    return SINGLETON_CONNECTION_ID;
  }

  @Override
  public boolean handlesFile(File file) {
    return true;
  }

  @Override
  public P4Connection getConnection(@NotNull VirtualFile file) {
    if (!file.isInLocalFileSystem()) {
      LOG.warn("Trying to get connection for non-local file " + file.getClass() + " " + file);
    }
    // todo check directories?
    // todo don't forget this optimization point
    return this;
  }

  @Override
  public Map<VirtualFile, P4Connection> getAllConnections() {
    final Project project = mySettings.getProject();
    final List<VirtualFile> files = ProjectLevelVcsManager.getInstance(project).getRootsUnderVcsWithoutFiltering(PerforceVcs.getInstance(project));
    new FilterDescendantVirtualFileConvertible<>(identity(), FilePathComparator.getInstance()).doFilter(new ArrayList<>(files));
    final HashMap<VirtualFile, P4Connection> map = new HashMap<>();
    for (VirtualFile file : files) {
      map.put(file, this);
    }
    return map;
  }
}
