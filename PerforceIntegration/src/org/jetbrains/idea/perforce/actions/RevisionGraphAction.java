/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.idea.perforce.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.ide.impl.TrustedProjects;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;


public class RevisionGraphAction extends DumbAwareAction {
  private static final Logger LOG = Logger.getInstance(RevisionGraphAction.class);

  private static void addCommandParameters(@NonNls GeneralCommandLine cmd, final PerforceClient client) throws VcsException {
    String port = client.getDeclaredServerPort();
    if (StringUtil.isEmptyOrSpaces(port)) {
      port = client.getServerPort();
    }
    String userName = client.getUserName();
    String clientName = client.getName();

    if (port == null || userName == null || clientName == null) {
      throw new VcsException(PerforceBundle.message("error.failed.to.retrieve.perforce.client.settings", port, userName, clientName));
    }

    //create the command
    cmd.addParameters("-p", port, "-u", userName, "-c", clientName);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getData(CommonDataKeys.PROJECT);
    final VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
    assert project != null && virtualFile != null;

    if (!project.isDefault() && !TrustedProjects.isTrusted(project)) {
      throw new IllegalStateException("Shouldn't be possible to run a P4 command in the safe mode");
    }

    final P4Connection connection = PerforceConnectionManager.getInstance(project).getConnectionForFile(virtualFile);
    if (connection == null) {
      Messages.showInfoMessage(project, PerforceBundle.message("connection.cannot.determine.settings"), PerforceBundle.message("connection.problem"));
      return;
    }

    final PerforceClient client = PerforceManager.getInstance(project).getClient(connection);
    final PerforceSettings settings = PerforceSettings.getSettings(project);

    final GeneralCommandLine cmd = new GeneralCommandLine(settings.PATH_TO_P4VC);

    try {
      addCommandParameters(cmd, client);
    } catch (VcsException e1) {
      Messages.showErrorDialog(project, e1.getMessage(), PerforceBundle.message("perforce"));
      return;
    }

    cmd.addParameters(getCommandName());

    FStat fStat = null;
    if (settings.ENABLED) {
      final P4File p4File = P4File.create(virtualFile);
      try {
        fStat = p4File.getFstat(project, false);
      }
      catch (VcsException ex) {
        Messages.showErrorDialog(project, PerforceBundle.message("failed.to.retrieve.p4.status.information",
                                                                 FileUtil.toSystemDependentName(virtualFile.getPath()),
                                                                 ex.getMessage()),
                                 PerforceBundle.message("perforce"));
        return;
      }
    }
    if (fStat != null && !StringUtil.isEmpty(fStat.depotFile)) {
      cmd.addParameters(fStat.depotFile);
    }
    else {
      cmd.addParameters(FileUtil.toSystemDependentName(virtualFile.getPath()));
    }

    LOG.debug("Invoking p4v with command line " + cmd.getCommandLineString());

    try {
      new OSProcessHandler(cmd) {
        private final StringBuilder output = new StringBuilder();

        @Override
        public void notifyTextAvailable(@NotNull String text, @NotNull Key outputType) {
          super.notifyTextAvailable(text, outputType);
          output.append(text);
        }

        @Override
        protected void onOSProcessTerminated(int exitCode) {
          if (exitCode != 0) {
            Notifications.Bus.notify(
              new Notification(PerforceVcs.getKey().getName(),
                               PerforceBundle.message("p4vc.running.problems"),
                               PerforceBundle.message("p4vc.running.problems.message", cmd, exitCode, (!output.isEmpty() ? PerforceBundle
                                 .message("p4vc.running.problems.output", output) : "")),
                               NotificationType.ERROR));
          }
          super.onOSProcessTerminated(exitCode);
        }
      }.startNotify();
    }
    catch (ExecutionException ex) {
      Messages.showErrorDialog(project, PerforceBundle.message("p4vc.run.failed", ex.getMessage()), PerforceBundle.message("p4vc"));
    }
  }

  protected @NonNls String getCommandName() {
    return "revgraph";
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(final @NotNull AnActionEvent e) {
    final Project project = e.getData(CommonDataKeys.PROJECT);
    final VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (project == null || virtualFile == null || !virtualFile.isInLocalFileSystem() || virtualFile.isDirectory() ||
        !PerforceSettings.getSettings(project).ENABLED ||
        !(VcsUtil.getVcsFor(project, virtualFile) instanceof PerforceVcs)) {
      e.getPresentation().setEnabled(false);
      return;
    }

    FileStatus fileStatus = FileStatusManager.getInstance(project).getStatus(virtualFile);
    e.getPresentation().setEnabled(fileStatus != FileStatus.ADDED && fileStatus != FileStatus.UNKNOWN && fileStatus != FileStatus.IGNORED);
  }
}
