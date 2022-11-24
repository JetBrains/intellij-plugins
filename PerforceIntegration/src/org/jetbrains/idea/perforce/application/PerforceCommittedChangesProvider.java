/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.changes.FilePathsHelper;
import com.intellij.openapi.vcs.changes.committed.RepositoryLocationGroup;
import com.intellij.openapi.vcs.changes.committed.VcsCommittedListsZipper;
import com.intellij.openapi.vcs.changes.committed.VcsCommittedListsZipperAdapter;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.ChangesBrowserSettingsEditor;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.AsynchConsumer;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.changesBrowser.PerforceChangeBrowserSettings;
import org.jetbrains.idea.perforce.changesBrowser.PerforceOnlyDatesVersionFilterComponent;
import org.jetbrains.idea.perforce.changesBrowser.PerforceVersionFilterComponent;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PerforceCommittedChangesProvider implements CachingCommittedChangesProvider<PerforceChangeList, PerforceChangeBrowserSettings> {
  private static final Logger LOG = Logger.getInstance(PerforceCommittedChangesProvider.class);

  private final Project myProject;
  private final PerforceRunner myRunner;
  private final MyZipper myZipper;
  private final ChangeListColumn[] myColumns = new ChangeListColumn[] {
    ChangeListColumn.NUMBER, ChangeListColumn.DATE, ChangeListColumn.NAME, new ClientColumn(), ChangeListColumn.DESCRIPTION };
  @NonNls private static final String IS_OPENED_SIGNATURE = "is opened and not being changed";
  @NonNls private static final String IS_OPENED_SIGNATURE2 = "- is opened for edit - not changed";
  @NonNls private static final String MUST_BE_RESOLVED = "- must resolve";

  public PerforceCommittedChangesProvider(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(myProject);
    myZipper = new MyZipper();
  }

  @Override
  @NotNull
  public PerforceChangeBrowserSettings createDefaultSettings() {
    return new PerforceChangeBrowserSettings();
  }

  @Override
  @NotNull
  public VcsCommittedListsZipper getZipper() {
    return myZipper;
  }

  private static class MyGroupCreator implements VcsCommittedListsZipperAdapter.GroupCreator {
    @Override
    public Object createKey(final RepositoryLocation location) {
      final String url = ((DefaultRepositoryLocation) location).getLocation();
      final int idx = url.indexOf("://");
      return (idx == -1) ? url : url.substring(0, idx);
    }

    @Override
    public RepositoryLocationGroup createGroup(final Object key, final Collection<RepositoryLocation> locations) {
      final RepositoryLocationGroup group = new RepositoryLocationGroup(key.toString());
      for (RepositoryLocation location : locations) {
        group.add(location);
      }
      return group;
    }
  }

  private static final class MyZipper extends VcsCommittedListsZipperAdapter {
    private MyZipper() {
      super(new MyGroupCreator());
    }
  }

  @NotNull
  @Override
  public ChangesBrowserSettingsEditor<PerforceChangeBrowserSettings> createFilterUI(boolean showDateFilter) {
    final Collection<P4Connection> connections = PerforceSettings.getSettings(myProject).getAllConnections();
    if (connections.size() == 1) {
      return new PerforceVersionFilterComponent(myProject, connections.iterator().next(), showDateFilter);
    }
    return new PerforceOnlyDatesVersionFilterComponent();
  }

  @NotNull
  @Override
  public RepositoryLocation getLocationFor(@NotNull FilePath root) {
    P4Connection connection = PerforceSettings.getSettings(myProject).getConnectionForFile(root.getIOFile());
    assert connection != null : "Null connection for " + root;
    String serverAddress = PerforceManager.getInstance(myProject).getClient(connection).getDeclaredServerPort();
    String location = serverAddress + "://" + root.getPresentableUrl();
    return PerforceRepositoryLocation.create(root, location, myProject);
  }

  @Override
  public void loadCommittedChanges(PerforceChangeBrowserSettings settings,
                                   @NotNull RepositoryLocation location,
                                   int maxCount,
                                   @NotNull AsynchConsumer<? super CommittedChangeList> consumer) throws VcsException {
    try {
      List<PerforceChangeList> changeLists = new ArrayList<>();
      PerforceSettings p4Settings = PerforceSettings.getSettings(myProject);
      if (!p4Settings.ENABLED) {
        return;
      }
      String url = ((DefaultRepositoryLocation)location).getURL();

      final String client = settings.getClientFilter();
      final String user = settings.getUserFilter();
      // todo make asynchronous later; Perforce is fast, maybe it's not worth doing
      final List<PerforceChangeList> changeListList = myRunner
        .getSubmittedChangeLists(client, user, P4File.create(new File(url)), settings, maxCount, p4Settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES);
      settings.filterChanges(changeLists);
      for (PerforceChangeList changeList : changeListList) {
        consumer.consume(changeList);
      }
    }
    finally {
      consumer.finished();
    }
  }

  @NotNull
  @Override
  public List<PerforceChangeList> getCommittedChanges(PerforceChangeBrowserSettings settings, RepositoryLocation location, int maxCount)
    throws VcsException {
    PerforceSettings p4Settings = PerforceSettings.getSettings(myProject);
    if (!p4Settings.ENABLED) {
      throw new VcsException(PerforceBundle.message("perforce.is.offline"));
    }
    String url = ((DefaultRepositoryLocation)location).getURL();

    final String client = settings.getClientFilter();
    final String user = settings.getUserFilter();
    List<PerforceChangeList> changeLists =
      new ArrayList<>(myRunner.getSubmittedChangeLists(client, user, P4File.create(new File(url)), settings, maxCount,
                                                       p4Settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES));
    LOG.debug("Changelists from Perforce: " + changeLists.size());
    settings.filterChanges(changeLists);
    LOG.debug("Changelists after filtering: " + changeLists.size());
    return changeLists;
  }

  @Override
  public ChangeListColumn @NotNull [] getColumns() {
    return myColumns;
  }

  @Override
  public int getUnlimitedCountValue() {
    return 0;
  }

  @Nullable
  @Override
  public Pair<PerforceChangeList, FilePath> getOneList(VirtualFile file, VcsRevisionNumber number) throws VcsException {
    PerforceSettings p4Settings = PerforceSettings.getSettings(myProject);
    if (!p4Settings.ENABLED) {
      return null;
    }
    FilePath filePath = VcsUtil.getFilePath(file.getPath(), false);
    final VirtualFile root;
    if (file.isInLocalFileSystem() || !file.isValid()) {
      filePath = ChangesUtil.getCommittedPath(myProject, filePath);
      final FilePath finalFilePath = filePath;
      final VirtualFile validParent = ChangesUtil.findValidParentAccurately(finalFilePath);
      if (validParent == null) return null;
      root = ProjectLevelVcsManager.getInstance(myProject).getVcsRootFor(validParent);
    }
    else {
      root = ProjectLevelVcsManager.getInstance(myProject).getVcsRootFor(file);
    }
    if (root == null) return null;
    final long changelistNumber;
    try {
      changelistNumber = Long.parseLong(number.asString());
    } catch (NumberFormatException e) {
      throw new VcsException(e);
    }
    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(root);
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.can.not.find.connection.for", root.getPath()));
    }

    final P4File rootP4File = P4File.create(root);
    ChangeBrowserSettings settings = new ChangeBrowserSettings();
    settings.USE_CHANGE_BEFORE_FILTER = true;
    settings.CHANGE_BEFORE = String.valueOf(changelistNumber);
    settings.USE_CHANGE_AFTER_FILTER = true;
    settings.CHANGE_AFTER = String.valueOf(changelistNumber);
    final List<PerforceChangeList> changeListList = myRunner.getSubmittedChangeLists(null, null, rootP4File, settings, 1, p4Settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES);
    //final FilePathImpl filePath = new FilePathImpl(file);
    if (changeListList.isEmpty()) {
      final List<PerforceChangeList> longerList = myRunner.getSubmittedChangeLists(null, null, rootP4File, settings, 0, true);
      for (PerforceChangeList list : longerList) {
        if (list.getNumber() == changelistNumber) {
          return correctNameIfNeeded(list, filePath, connection);
        }
      }
      return null;
    }

    return correctNameIfNeeded(changeListList.get(0), filePath, connection);
  }

  private Pair<PerforceChangeList, FilePath> correctNameIfNeeded(final PerforceChangeList changeList, @NotNull final FilePath filePath,
                                                                 final P4Connection connection)
    throws VcsException {
    for (Change change : changeList.getChanges()) {
      if (change.getAfterRevision() != null && FileUtil.filesEqual(filePath.getIOFile(), change.getAfterRevision().getFile().getIOFile())) {
        return Pair.create(changeList, filePath);
      }
    }

    final P4Revision[] revisions = myRunner.filelog(P4File.create(filePath), true);
    for (P4Revision revision : revisions) {
      if (revision.getChangeNumber() == changeList.getNumber()) {
        final P4WhereResult where = myRunner.where(revision.getDepotPath(), connection);
        return Pair.create(changeList, VcsUtil.getFilePath(where.getLocal(), false));
      }
    }
    return Pair.create(changeList, filePath);
  }

  @Override
  public int getFormatVersion() {
    return 2;
  }

  @Override
  public void writeChangeList(@NotNull DataOutput stream, @NotNull PerforceChangeList list) throws IOException {
    list.writeToStream(stream);
  }

  @NotNull
  @Override
  public PerforceChangeList readChangeList(@NotNull RepositoryLocation location, @NotNull DataInput stream) throws IOException {
    final P4Connection connection;
    final PerforceClient perforceClient;
    try {
      final PerforceRepositoryLocation perforceRepositoryLocation = (PerforceRepositoryLocation)location;
      connection = perforceRepositoryLocation.getConnection();
      perforceClient = perforceRepositoryLocation.getClient();
    }
    catch (VcsException e) {
      throw new IOException(e);
    }
    return new PerforceChangeList(myProject, stream, connection, perforceClient, new PerforceChangeCache(myProject));
  }

  @NotNull
  @Override
  public Collection<FilePath> getIncomingFiles(@NotNull RepositoryLocation location) throws VcsException {
    if (!PerforceSettings.getSettings(myProject).ENABLED) {
      throw new VcsException(PerforceBundle.message("perforce.is.offline"));
    }

    final DefaultRepositoryLocation repLocation = (DefaultRepositoryLocation)location;
    final P4File file = P4File.create(new File(repLocation.getURL()));
    ExecResult result = myRunner.previewSync(file);
    if (result.getExitCode() != 0 || result.getStderr().length() > 0) {
      if (result.getStderr().contains(PerforceRunner.FILES_UP_TO_DATE)) {
        return Collections.emptyList();
      }
      if (result.getStderr().contains(PerforceRunner.NO_SUCH_FILE_MESSAGE)) { // the root is not in clientspec or ignored
        return Collections.emptyList();
      }
      throw new VcsException(PerforceBundle.message("error.refreshing.incoming.changes", result.getExitCode(), result.getStderr()));
    }
    final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(file);
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.invalid.perforce.settings.for.0", file.getLocalPath()));
    }
    final PerforceClient client = PerforceManager.getInstance(myProject).getClient(connection);
    final PerforceManager perforceManager = PerforceManager.getInstance(myProject);
    String clientRoot = perforceManager.getClientRoot(connection);
    final P4WhereResult p4WhereResult = myRunner.whereDir(file, connection);
    String depotRoot = p4WhereResult.getDepot();

    clientRoot = FilePathsHelper.convertPath(clientRoot);
    depotRoot = FilePathsHelper.convertPath(depotRoot);

    List<FilePath> files = new ArrayList<>();
    String[] lines = LineTokenizer.tokenize(result.getStdout(), false);
    for (String line : lines) {
      String lineConverted = FilePathsHelper.convertPath(line);
      int pos = lineConverted.indexOf(clientRoot);
      if (pos >= 0) {
        final File localFile = new File(line.substring(pos));
        LOG.debug("Incoming file: " + line.substring(pos));
        files.add(VcsContextFactory.getInstance().createFilePathOn(localFile));
      }
      else if (line.contains(IS_OPENED_SIGNATURE)) {
        pos = line.indexOf(" - ");
        if (pos >= 0) {
          String depotPath = line.substring(0, pos);
          final File localPath = PerforceManager.getFileByDepotName(depotPath, client);
          if (localPath != null) {
            files.add(VcsContextFactory.getInstance().createFilePathOn(localPath));
          }
        }
      }
      else if (line.contains(IS_OPENED_SIGNATURE2)) {
        pos = line.indexOf(IS_OPENED_SIGNATURE2);
        if (pos >= 0) {
          String depotPath = line.substring(0, pos);
          final File localPath = PerforceManager.getFileByDepotName(depotPath, client);
          if (localPath != null) {
            files.add(VcsContextFactory.getInstance().createFilePathOn(localPath));
          }
        }
      }
      else if (line.contains(MUST_BE_RESOLVED)) {
        pos = lineConverted.indexOf(depotRoot);
        final int pathEnd = line.indexOf(MUST_BE_RESOLVED);
        if (pos >= 0) {
          final String depotPath = line.substring(pos, pathEnd).trim();
          final File localPath = PerforceManager.getFileByDepotName(depotPath, client);
          if (localPath != null) {
            files.add(VcsContextFactory.getInstance().createFilePathOn(localPath));
          }
          /*try {
            final P4WhereResult whereResult = myRunner.where(depotPath, connection);
            files.add(VcsContextFactory.getInstance().createFilePathOn(new File(whereResult.getLocal())));
          }
          catch (VcsException e) {
            LOG.info("Cannot parse 'must resolve': " + line);
          }*/
        }
      }
      else {
        LOG.info("Unknown line in incoming files: " + line);
      }
    }
    return files;
  }

  @Override
  public String getChangelistTitle() {
    return PerforceBundle.message("changes.browser.changelist.term");
  }

  @Override
  public boolean refreshIncomingWithCommitted() {
    return false;
  }

  private static class ClientColumn extends ChangeListColumn<PerforceChangeList> {
    @Override
    public String getTitle() {
      return PerforceBundle.message("changes.browser.client.column.name");
    }

    @Override
    public Object getValue(final PerforceChangeList changeList) {
      return changeList.getClient();
    }

    @Override
    public Comparator<PerforceChangeList> getComparator() {
      return Comparator.comparing(PerforceChangeList::getClient);
    }
  }
}
