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

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.FileGroup;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PerforceIntegrateEnvironment extends AbstractUpdateEnvironment {
  private static final @NonNls Map<String, String> ourPatternToGroupId = new HashMap<>();

  private static final @NonNls String INTEGRATED = "INTEGRATED";
  private static final @NonNls String CANT_INTEGRATE = "CANT_INTEGRATE";
  @VisibleForTesting public static final @NonNls String BRANCHED = "BRANCHED";

  static {
    ourPatternToGroupId.put(" - integrate from ", INTEGRATED);
    ourPatternToGroupId.put(" - sync/integrate from ", INTEGRATED);
    ourPatternToGroupId.put(" - can't integrate (already opened on this client)", CANT_INTEGRATE);
    ourPatternToGroupId.put(" - branch/sync from ", BRANCHED);
  }

  public PerforceIntegrateEnvironment(final Project project) {
    super(project);
  }

  @Override
  public void fillGroups(UpdatedFiles updatedFiles) {
    updatedFiles.registerGroup(new FileGroup(PerforceBundle.message("integrate.group.name.integrated"),
                                             PerforceBundle.message("integrate.group.name.integrated"), false, INTEGRATED, false));
    updatedFiles.registerGroup(new FileGroup(PerforceBundle.message("integrate.group.name.cant.integrate"),
                                             PerforceBundle.message("integrate.group.name.cant.integrate"), false, CANT_INTEGRATE, false));
    updatedFiles.registerGroup(new FileGroup(PerforceBundle.message("integrate.group.name.branched"),
                                             PerforceBundle.message("integrate.group.name.branched"), false, BRANCHED, false));
  }

  @Override
  protected boolean isTryToResolveAutomatically(PerforceSettings settings) {
    return settings.INTEGRATE_RUN_RESOLVE;
  }

  @Override
  protected Map<String, String> getPatternToGroupId() {
    return ourPatternToGroupId;
  }

  @Override
  protected boolean isRevertUnchanged(PerforceSettings settings) {
    return settings.INTEGRATE_REVERT_UNCHANGED;
  }

  @Override
  protected ExecResult performUpdate(P4File p4Dir, PerforceSettings settings) throws VcsException {
    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(p4Dir);
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.invalid.perforce.settings.for.0", p4Dir.getLocalPath()));
    }
    final ParticularConnectionSettings connectionSettings = settings.getSettings(connection);

    final String integrateChangeListNum = connectionSettings.INTEGRATE_CHANGE_LIST ? connectionSettings.INTEGRATED_CHANGE_LIST_NUMBER : null;

    // reset settings for the next Integrate invocation
    connectionSettings.INTEGRATE_CHANGE_LIST = false;
    connectionSettings.INTEGRATED_CHANGE_LIST_NUMBER = "";
    return PerforceRunner.getInstance(myProject).integrate(connectionSettings.INTEGRATE_BRANCH_NAME,
                                                           p4Dir,
                                                           connectionSettings.INTEGRATE_TO_CHANGELIST_NUM,
                                                           integrateChangeListNum, connectionSettings.INTEGRATE_REVERSE,
                                                           connection);
  }

  @Override
  public Configurable createConfigurable(Collection<FilePath> files) {
    final MultiMap<P4Connection, File> connectionToFiles = FileGrouper.distributeIoFilesByConnection(
      convertToFiles(files),
      PerforceSettings
        .getSettings(
          myProject)
        .getProject());
    return new PerforceUpdateConfigurable(PerforceSettings.getSettings(myProject)){
      @Override
      protected PerforcePanel createPanel() {
        return new PerforceIntegratePanel(myProject, new ArrayList<>(connectionToFiles.keySet()));
      }

      @Override
      public String getHelpTopic() {
        return "reference.dialogs.versionControl.integrate.project.perforce";
      }
    };
  }

  private static Collection<File> convertToFiles(final Collection<FilePath> files) {
    final ArrayList<File> result = new ArrayList<>();
    for (FilePath filePath : files) {
      result.add(filePath.getIOFile());
    }
    return result;
  }

  @Override
  public boolean validateOptions(final Collection<FilePath> roots) {
    return true;
  }
}
