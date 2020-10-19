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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.update.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.WaitForProgressToShow;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;

import java.io.File;
import java.util.*;

abstract class AbstractUpdateEnvironment implements UpdateEnvironment {
  protected final Project myProject;
  protected final PerforceRunner myRunner;

  AbstractUpdateEnvironment(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
  }

  protected static void processOutput(final String output,
                                      final UpdatedFiles updatedFiles,
                                      final Map<String, String> patternToGroupId,
                                      final PerforceClient client) throws VcsException {
    String[] lines = LineTokenizer.tokenize(output, false);
    for (String line : lines) {
      fillUpdateInformationFromTheLine(line, updatedFiles, patternToGroupId, client);
    }
  }

  private static void fillUpdateInformationFromTheLine(String line,
                                                       UpdatedFiles updatedFiles,
                                                       Map<String, String> patternToGroupId,
                                                       final PerforceClient client) throws VcsException {
    for (String pattern : patternToGroupId.keySet()) {
      if (processLine(line, updatedFiles, pattern, patternToGroupId.get(pattern), client)) {
        return;
      }
    }
  }

  private static boolean processLine(String line, UpdatedFiles updatedFiles,
                                     String message, String fileGroupId, final PerforceClient client) throws VcsException {
    int messageStart = line.indexOf(message);
    if (messageStart < 0) return false;

    String depotFilePath = line.substring(0, messageStart).trim();
    final File fileByDepotName = PerforceManager.getFileByDepotName(depotFilePath, client);
    final String filePath = fileByDepotName != null ? fileByDepotName.getPath() : depotFilePath;

    int revNumPos = depotFilePath.indexOf('#');
    VcsRevisionNumber revision = revNumPos > 0 ? new PerforceOnlyRevisionNumber(Long.parseLong(depotFilePath.substring(revNumPos + 1))) : null;

    updatedFiles.getGroupById(fileGroupId).add(filePath, PerforceVcs.getKey(), revision);
    return true;
  }

  protected PerforceSettings getSettings() {
    return PerforceSettings.getSettings(myProject);
  }

  private void resolveAutomatically(final P4File contentRoot) throws VcsException {
    myRunner.resolveAutomatically(contentRoot);
  }

  @Override
  @NotNull
  public UpdateSession updateDirectories(FilePath @NotNull [] contentRoots, UpdatedFiles updatedFiles, ProgressIndicator progressIndicator,
                                         @NotNull final Ref<SequentialUpdatesContext> context)
    throws ProcessCanceledException {
    PerforceSettings settings = getSettings();
    if (!settings.ENABLED) {
      return new UpdateSessionAdapter(Collections.singletonList(new VcsException(PerforceBundle.message("perforce.is.offline"))), false);
    }
    final ArrayList<VcsException> vcsExceptions = new ArrayList<>();

    try {
      MultiMap<P4Connection, FilePath> map = FileGrouper.distributePathsByConnection(Arrays.asList(contentRoots), myProject);

      final PerforceManager perforceManager = PerforceManager.getInstance(myProject);
      for (P4Connection connection : map.keySet()) {
        if (isRevertUnchanged(settings)) {
          List<String> paths = ContainerUtil.map2List(map.get(connection), path -> P4File.create(path).getRecursivePath());
          try {
            myRunner.revertUnchanged(connection, paths);
          }
          catch (final VcsConnectionProblem e) {
            vcsExceptions.add(e);
            ApplicationManager.getApplication().invokeLater(() -> e.attemptQuickFix(true));
            return new UpdateSessionAdapter(vcsExceptions, true);
          }
          catch (VcsException e) {
            vcsExceptions.add(e);
          }
        }

        try {
          final PerforceClient client = perforceManager.getClient(connection);
          for (FilePath path : map.get(connection)) {
            P4File p4Dir = P4File.create(path);
            final ExecResult execResult = performUpdate(p4Dir, settings);
            processOutput(execResult.getStdout(), updatedFiles, getPatternToGroupId(), client);
            VcsException[] updateExceptions = PerforceRunner.checkErrors(execResult, settings, connection);
            if (updateExceptions.length > 0) {
              Collections.addAll(vcsExceptions, updateExceptions);
            }
            else {
              if (isTryToResolveAutomatically(settings)) {
                resolveAutomatically(p4Dir);
              }
            }
          }
        }
        catch (final VcsConnectionProblem e) {
          vcsExceptions.add(e);
          ApplicationManager.getApplication().invokeLater(() -> e.attemptQuickFix(true));
          return new UpdateSessionAdapter(vcsExceptions, true);
        }
        catch (VcsException e) {
          vcsExceptions.add(e);
        }
      }

      handleResolveConflicts(map);
    }
    finally {
      PerforceSettings.getSettings(myProject).SYNC_FORCE = false;
    }
    return new UpdateSessionAdapter(vcsExceptions, false);
  }

  private void handleResolveConflicts(MultiMap<P4Connection, FilePath> map) {
    try {
      final List<VirtualFile> filesToResolve = new LinkedList<>();
      for (P4Connection connection : map.keySet()) {
        for (FilePath root : map.get(connection)) {
          VirtualFile file = root.getVirtualFile();
          if (file != null) {
            filesToResolve.addAll(filterByServerVersion(myRunner.getResolvedWithConflicts(connection, file)));
          }
        }
      }
      if (! filesToResolve.isEmpty()) {
        WaitForProgressToShow.runOrInvokeAndWaitAboveProgress(() -> new PerforceMergeProvider(myProject).showMergeDialog(filesToResolve));
      }
    }
    catch (VcsException e) {
       //ignore
    }
  }

  private List<VirtualFile> filterByServerVersion(final Collection<VirtualFile> allFilesToResolve) throws VcsException {
    final PerforceConnectionManagerI connectionManager = PerforceConnectionManager.getInstance(myProject);
    final ArrayList<VirtualFile> result = new ArrayList<>();
    for (VirtualFile virtualFile : allFilesToResolve) {
      if (PerforceSettings.getSettings(myProject).getServerVersion(connectionManager.getConnectionForFile(virtualFile)) >= 2004) {
        result.add(virtualFile);
      }
    }
    return result;
  }

  protected abstract boolean isTryToResolveAutomatically(PerforceSettings settings);

  protected abstract Map<String, String> getPatternToGroupId();

  protected abstract boolean isRevertUnchanged(PerforceSettings settings);

  protected abstract ExecResult performUpdate(P4File p4Dir, PerforceSettings settings) throws VcsException;
}
