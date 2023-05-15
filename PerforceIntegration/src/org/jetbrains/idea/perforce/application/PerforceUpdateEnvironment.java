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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.FileGroup;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PerforceUpdateEnvironment extends AbstractUpdateEnvironment{
  @NonNls private static final String DELETED_MESSAGE = " - deleted as ";
  @NonNls private static final String UPDATING_MESSAGE = " - updating ";
  @NonNls private static final String ADDED_MESSAGE = " - added as ";
  @NonNls private static final String MERGE_MESSAGE = " - is opened and not being changed";

  private final static Map<String, String> ourPatternToGroupId = new HashMap<>();

  static {
    ourPatternToGroupId.put(DELETED_MESSAGE, FileGroup.REMOVED_FROM_REPOSITORY_ID);
    ourPatternToGroupId.put(UPDATING_MESSAGE, FileGroup.UPDATED_ID);
    ourPatternToGroupId.put(ADDED_MESSAGE, FileGroup.CREATED_ID);
    ourPatternToGroupId.put(MERGE_MESSAGE, FileGroup.MERGED_ID);
  }

  public PerforceUpdateEnvironment(Project project) {
    super(project);
  }

  @Override
  public void fillGroups(UpdatedFiles updatedFiles) {

  }

  @Override
  protected boolean isTryToResolveAutomatically(final PerforceSettings settings) {
    return settings.SYNC_RUN_RESOLVE;
  }

  @Override
  protected Map<String, String> getPatternToGroupId() {
    return ourPatternToGroupId;
  }

  @Override
  protected boolean isRevertUnchanged(final PerforceSettings settings) {
    return settings.REVERT_UNCHANGED_FILES;
  }

  @Override
  protected ExecResult performUpdate(final P4File p4Dir, final PerforceSettings settings) throws VcsException {
    return PerforceRunner.getInstance(myProject).sync(p4Dir, settings.SYNC_FORCE);
  }

  @Override
  public Configurable createConfigurable(Collection<FilePath> files) {
    return new PerforceUpdateConfigurable(getSettings()){
      @Override
      protected PerforcePanel createPanel() {
        return PerforceSettings.getSettings(myProject).ENABLED ? new PerforceUpdatePanel() : new PerforceIsOfflinePanel();
      }

      @Override
      public String getHelpTopic() {
        return "reference.vcs.update.project.perforce";
      }
    };
  }

  @Override
  public boolean validateOptions(final Collection<FilePath> roots) {
    return PerforceCheckinHandlerFactory.beforeRemoteOperationCheck(myProject, "Update");
  }
}
