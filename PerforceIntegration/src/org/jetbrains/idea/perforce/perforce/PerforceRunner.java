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
package org.jetbrains.idea.perforce.perforce;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.impl.ContentRevisionCache;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.*;
import com.intellij.util.text.SyncDateFormat;
import com.intellij.vcsUtil.VcsUtil;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.ClientVersion;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.application.*;
import org.jetbrains.idea.perforce.application.annotation.AnnotationInfo;
import org.jetbrains.idea.perforce.changesBrowser.FileChange;
import org.jetbrains.idea.perforce.merge.BaseRevision;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;
import org.jetbrains.idea.perforce.perforce.connections.PerforceLocalConnection;
import org.jetbrains.idea.perforce.perforce.jobs.JobsSearchSpecificator;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceJob;
import org.jetbrains.idea.perforce.perforce.login.LoginSupport;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public final class PerforceRunner implements PerforceRunnerI {
  private final Project myProject;
  private final PerforceConnectionManagerI myConnectionManager;
  private final PerforceSettings mySettings;
  private final PerforceRunnerProxy myProxy;

  private static final int MAX_LOG_LENGTH = 10*1000*1000;
  private static final int OPENED_SIZE = 50;

  @NonNls static final String PASSWORD_INVALID_MESSAGE = "Perforce password (P4PASSWD) invalid or unset";
  @NlsSafe public static final String PASSWORD_INVALID_MESSAGE2 = "Password invalid.";
  @NonNls private static final String SESSION_EXPIRED_MESSAGE = "Your session has expired";
  @NonNls public static final String FILES_UP_TO_DATE = "file(s) up-to-date.";
  @NonNls private static final String PASSWORD_NOT_ALLOWED_MESSAGE = "Password not allowed at this server security level";
  @NonNls public static final String NO_SUCH_FILE_MESSAGE = " - no such file(s)";
  @NonNls private static final String STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE = "file(s) not opened on this client";
  @NonNls private static final String YET_ANOTHER_STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE = "file(s) not opened for edit.";
  @NonNls private static final String NO_FILES_TO_RESOLVE_MESSAGE = "no file(s) to resolve";
  @NonNls private static final String UBINARY_MERGING_MESSAGE = "- ubinary/ubinary merge ";
  @NonNls private static final String BINARY_MERGING_MESSAGE = "- binary/binary merge ";
  @NonNls private static final String MERGING_MESSAGE = "- merging ";
  @NonNls private static final String USING_BASE_MESSAGE = "using base";
  @NonNls public static final String NOT_UNDER_CLIENT_ROOT_MESSAGE = "is not under client's root";
  @NonNls public static final String NOT_IN_CLIENT_VIEW_MESSAGE = " - file(s) not in client view";
  @NonNls public static final String NOT_ON_CLIENT_MESSAGE = "file(s) not on client";
  @NonNls private static final String NO_FILES_RESOLVED_MESSAGE = "no file(s) resolved";
  @NonNls private static final String INVALID_REVISION_NUMBER = "Invalid revision number";

  @NonNls public static final String CHANGE = "Change:";
  @NonNls public static final String DATE = "Date:";
  @NonNls public static final String CLIENT = "Client:";
  @NonNls public static final String USER = "User:";
  @NonNls public static final String STATUS = "Status:";
  @NonNls public static final String DESCRIPTION = "Description:";
  @NonNls public static final String JOB = "Job:";
  @NonNls public static final String JOBS = "Jobs:";
  @NonNls public static final String FILES = "Files:";
  @NonNls public static final String OWNER = "Owner:";
  @NonNls public static final String VIEW = "View:";
  @NonNls public static final String TYPE = "Type:";

  @NonNls public static final String CLIENTSPEC_ROOT = "Root:";
  @NonNls public static final String CLIENTSPEC_ALTROOTS = "AltRoots:";

  @NonNls public static final String USER_NAME = "User name:";
  @NonNls public static final String CLIENT_NAME = "Client name:";
  @NonNls public static final String CLIENT_HOST = "Client host:";
  @NonNls public static final String CLIENT_ROOT = "Client root:";
  @NonNls public static final String CLIENT_UNKNOWN = "Client unknown.";
  @NonNls public static final String CURRENT_DIRECTORY = "Current directory:";
  @NonNls public static final String CLIENT_ADDRESS = "Client address:";
  @NonNls public static final String PEER_ADDRESS = "Client address:";
  @NonNls public static final String CLIENT_OPTIONS = "Options:";
  @NonNls public static final String SERVER_ADDRESS = "Server address:";
  @NonNls public static final String SERVER_ROOT = "Server root:";
  @NonNls public static final String SERVER_DATE = "Server date:";
  @NonNls public static final String SERVER_LICENSE = "Server license:";
  @NonNls public static final String SERVER_VERSION = "Server version:";

  @NonNls private static final SyncDateFormat DATESPEC_DATE_FORMAT = new SyncDateFormat(new SimpleDateFormat("yyyy/MM/dd:HH:mm:ss", Locale.US));
  @NonNls private static final String NOW = "now";
  @NonNls private static final String DEFAULT_CHANGELIST_NUMBER = "default";

  @NonNls public static final String CLIENT_FILE_PREFIX = "... clientFile ";

  private static final int CHUNK_SIZE = SystemProperties.getIntProperty("p4.chunk.size", 2000);

  public static final String[] CHANGE_FORM_FIELDS = new String[]{CHANGE,
    DATE,
    CLIENT,
    USER,
    STATUS,
    TYPE,
    DESCRIPTION,
    JOBS,
    FILES};

  private static final String[] AVAILABLE_INFO = new String[]{USER_NAME,
    CLIENT_NAME,
    CLIENT_HOST,
    CLIENT_ROOT,
    CLIENT_UNKNOWN,
    CURRENT_DIRECTORY,
    CLIENT_ADDRESS,
    SERVER_ADDRESS,
    PEER_ADDRESS,
    SERVER_ROOT,
    SERVER_DATE,
    SERVER_LICENSE,
    SERVER_VERSION};

  private static final Logger LOG = Logger.getInstance(PerforceRunner.class);
  private static final Logger SPECIFICATION_LOG = Logger.getInstance("#PerforceJobSpecificationLogging");
  @NonNls private static final String DEFAULT_DESCRIPTION = "<none>";
  @NonNls private static final String DUMP_FILE_NAME = "p4output.log";

  private static final String CLIENT_VERSION_REV = "Rev.";

  private final PerforceManager myPerforceManager;
  private final LoginSupport myLoginManager;

  public static PerforceRunner getInstance(Project project) {
    return project.getService(PerforceRunner.class);
  }

  public PerforceRunner(Project project) {
    this(PerforceConnectionManager.getInstance(project),
         PerforceSettings.getSettings(project),
         PerforceLoginManager.getInstance(project));
  }

  @NonInjectable
  public PerforceRunner(final PerforceConnectionManagerI connectionManager, final PerforceSettings settings, final LoginSupport loginManager) {
    myProject = settings.getProject();
    myConnectionManager = connectionManager;
    mySettings = settings;
    myPerforceManager = PerforceManager.getInstance(myProject);
    myLoginManager = loginManager;
    myProxy = new PerforceRunnerProxy(myProject, this);
  }

  public PerforceRunnerI getProxy() {
    return myProxy.getProxy();
  }

  public Map<String, List<String>> getInfo(@NotNull final P4Connection connection) throws VcsException {
    @NonNls final String[] p4args = {"info"};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult, connection);
    return FormParser.execute(execResult.getStdout(), AVAILABLE_INFO, CLIENT_UNKNOWN);
  }

  @Nullable
  public String getClient(@Nullable final P4Connection connection) throws VcsException {
    if (connection == null) return null;
    List<String> clientNames = getInfo(connection).get(CLIENT_NAME);
    return clientNames == null ? null : clientNames.get(0);
  }


  @Override
  public void edit(P4File file) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    edit(file, getActiveListNumber(connection));
  }

  public void edit(final P4File file, final long changeListNumber) throws VcsException {
    edit(file, changeListNumber, false);
  }

  public void edit(final P4File file, final long changeListNumber, final boolean keepWorkspace) throws VcsException {
    P4Connection connection = getNotNullConnection(file);
    editAll(List.of(file), changeListNumber, keepWorkspace, connection);
  }

  public void editAll(final List<P4File> files, final long changeListNumber, final boolean keepWorkspace, @NotNull P4Connection connection) throws VcsException {
    List<String> paths = new ArrayList<>();
    for (P4File file : files) {
      file.invalidateFstat();
      paths.add(file.getEscapedPath());
    }

    final CommandArguments arguments = CommandArguments.createOn(P4Command.edit);
    if (keepWorkspace) {
      arguments.append("-k");
    }
    appendChangeListNumber(changeListNumber, arguments);

    final ExecResult execResult = executeP4Command(arguments.getArguments(), paths, null, new PerforceContext(connection));
    checkError(execResult, connection);
  }

  @NotNull
  private P4Connection getNotNullConnection(P4File file) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    if (connection == null) {
      throw new VcsException(PerforceBundle.message("error.no.connection.for.file", file.getLocalPath()));
    }
    return connection;
  }

  private static void appendChangeListNumber(long changeListNumber, CommandArguments arguments) {
    if (changeListNumber > 0) {
      arguments.append("-c").append(String.valueOf(changeListNumber));
    }
  }

  public Map<P4File, FStat> fstatBulk(final List<P4File> files) throws VcsException {
    if (files.isEmpty()) return Collections.emptyMap();

    P4Connection connection = getNotNullConnection(files.get(0));
    Set<String> p4Args = new LinkedHashSet<>();
    for (P4File file : files) {
      p4Args.add(file.getEscapedPath());
    }
    ExecResult execResult = executeP4Command(new String[]{"fstat"}, p4Args, null, new PerforceContext(connection));

    final Map<P4File, FStat> result = new LinkedHashMap<>();
    String stderr = execResult.getStderr();

    final Map<String, P4File> path2File = CollectionFactory.createFilePathMap();
    for (P4File file : files) {
      path2File.put(FileUtil.toSystemIndependentName(file.getLocalPath()), file);
    }

    for (String line : StringUtil.splitByLines(stderr)) {
      int index = line.indexOf(NO_SUCH_FILE_MESSAGE);
      if (index < 0) index = line.indexOf(NOT_IN_CLIENT_VIEW_MESSAGE);
      if (index >= 0) {
        FStat fStat = new FStat();
        fStat.status = line.contains(NO_SUCH_FILE_MESSAGE) ? FStat.Status.NOT_ADDED : FStat.Status.NOT_IN_CLIENTSPEC;
        result.put(getP4FileByPath(line.substring(0, index), path2File), fStat);
      } else {
        checkError(execResult, connection);
      }
    }

    try {
      execResult.allowSafeStdoutUsage(stream -> {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        while (true) {
          try {
            FStat fStat = FStat.parseFStat(reader);
            if (fStat == null) {
              break;
            }

            result.put(getP4FileByPath(fStat.clientFile, path2File), fStat);
          }
          catch (VcsException e) {
            throw new IOException(e);
          }
        }
      });
    }
    catch (IOException e) {
      if (e.getCause() instanceof VcsException) {
        throw (VcsException)e.getCause();
      }
      throw new VcsException(e);
    }
    return result;
  }

  private static P4File getP4FileByPath(String path, Map<String, P4File> path2File) throws VcsException {
    String clientFile = P4File.unescapeWildcards(FileUtil.toSystemIndependentName(path));
    P4File p4File = path2File.get(clientFile);
    if (p4File == null) {
      throw new VcsException(PerforceBundle.message("error.invalid.file.fstat.mapping", clientFile, new ArrayList<>(path2File.keySet())));
    }
    return p4File;
  }

  @Override
  public FStat fstat(final P4File p4File) throws VcsException {
    Map<P4File, FStat> map = fstatBulk(Collections.singletonList(p4File));
    FStat result = map.get(p4File);
    if (result == null) {
      throw new VcsException(PerforceBundle.message("error.no.fstat.for.file", p4File, map));
    }
    return result;
  }

  public void revertAll(List<String> files, @NotNull P4Connection connection) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.revert);
    checkError(executeP4Command(arguments.getArguments(), files, null, new PerforceContext(connection)), connection);
  }

  @Override
  public void revert(final P4File p4File, final boolean justTry) throws VcsException {
    p4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.revert).append(p4File.getEscapedPath());
    P4Connection connection = getNotNullConnection(p4File);
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    if (!justTry) {
      checkError(execResult, connection);
    }
  }

  public void revertUnchanged(@NotNull final P4Connection connection, final Collection<String> files) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{"revert", "-a"}, files, null, new PerforceContext(connection));
    if (!execResult.getStderr().contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) &&
        !execResult.getStderr().contains(YET_ANOTHER_STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE)) {
      checkError(execResult, connection);
    }
  }

  public void revertUnchanged(final P4Connection connection, final long changeListNumber) throws VcsException {
    String changeListNum = changeListNumber == -1L ? DEFAULT_CHANGELIST_NUMBER : String.valueOf(changeListNumber);
    final ExecResult execResult = executeP4Command(new String[]{"revert", "-a", "-c", changeListNum}, connection);
    if (!execResult.getStderr().contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) &&
        !execResult.getStderr().contains(YET_ANOTHER_STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE)) {
      checkError(execResult, connection);
    }
  }

  private long getActiveListNumber(@Nullable final P4Connection connection) throws VcsException {
    if (connection == null) return -1;
    PerforceManager.ensureValidClient(myProject, connection);
    final LocalChangeList activeList = ChangeListManager.getInstance(myProject).getDefaultChangeList();
    final ConnectionKey connectionKey = connection.getConnectionKey();
    final Long number = PerforceNumberNameSynchronizer.getInstance(myProject).getNumber(connectionKey, activeList.getName());
    return number == null ? -1 : number;
  }

  public void add(final P4File p4File) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);
    add(p4File, getActiveListNumber(connection));
  }

  public void add(final P4File p4File, final long changeListNumber) throws VcsException {
    p4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.add);
    if (PerforceVcs.getFileNameComplaint(p4File) != null) {
      arguments.append("-f");
    }
    P4Connection connection = getNotNullConnection(p4File);
    appendChangeListNumber(changeListNumber, arguments);
    arguments.append(p4File.getLocalPath());
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult, connection);
  }

  public ExecResult previewAdd(@NotNull P4Connection connection, Collection<VirtualFile> files) {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.add).append("-f").append("-n");

    List<String> paths = new ArrayList<>();
    for (VirtualFile file : files) {
      paths.add(VfsUtilCore.virtualToIoFile(file).getPath());
    }

    return executeP4Command(arguments.getArguments(), paths, null, new PerforceContext(connection));
  }

  public ExecResult ignores(@NotNull P4Connection connection, Collection<VirtualFile> files) {
    CommandArguments arguments = CommandArguments.createOn(P4Command.ignores).append("-i");

    File cwd = connection.getWorkingDirectory();
    for (VirtualFile file : files) {
      String relativePath = FileUtil.getRelativePath(cwd, VfsUtilCore.virtualToIoFile(file));
      if (relativePath != null) {
        arguments.append(relativePath);
      }
    }

    return executeP4Command(arguments.getArguments(), connection);
  }

  @Override
  public ExecResult sync(final P4File p4File, boolean forceSync) throws VcsException {
    return doSync(p4File, forceSync ? "-f" : null);
  }

  public ExecResult previewSync(final P4File p4File) throws VcsException {
    return doSync(p4File, "-n");
  }

  private ExecResult doSync(final P4File p4File, @Nullable @NonNls final String arg) throws VcsException {
    p4File.invalidateFstat();
    P4Connection connection = getNotNullConnection(p4File);
    final CommandArguments arguments = CommandArguments.createOn(P4Command.sync);
    if (arg != null) {
      arguments.append(arg);
    }
    arguments.append(p4File.getRecursivePath());

    return executeP4Command(arguments.getArguments(), Collections.emptyList(), null, new PerforceContext(connection, true, false));
  }

  public ExecResult sync(P4File p4File, String revision) throws VcsException {
    p4File.invalidateFstat();
    P4Connection connection = getNotNullConnection(p4File);
    CommandArguments arguments = CommandArguments.createOn(P4Command.sync).append(p4File.getRecursivePath() + "@" + revision);
    return executeP4Command(arguments.getArguments(), Collections.emptyList(), null, new PerforceContext(connection, true, false));
  }

  // todo ? not sure for move+add/delete cases
  public void assureDel(final P4File p4File, @Nullable final Long changeList) throws VcsException {
    // reverting the edit of a file at the old path in a renamed directory will recreate both the file
    // and its parent dir => need to delete both
    final List<File> filesToDelete = new ArrayList<>();
    File f = p4File.getLocalFile();
    while (f != null && !f.exists()) {
      filesToDelete.add(f);
      f = f.getParentFile();
    }

    // can't use cached fstat because we can just have performed some operation affecting the status of the file
    final FStat fstat = p4File.getFstat(myProject, true);

    if (fstat.status == FStat.STATUS_NOT_ADDED || fstat.status == FStat.STATUS_NOT_IN_CLIENTSPEC || fstat.local == FStat.LOCAL_DELETING ||
        fstat.local == FStat.LOCAL_MOVE_DELETING) {
      // this is OK, that's what we want
    }
    else {
      // we have to do something about it

      // first, if revert is enough
      if (fstat.local == FStat.LOCAL_ADDING || fstat.local == FStat.LOCAL_MOVE_ADDING) {
        revert(p4File, false);
      }
      else if (fstat.local == FStat.LOCAL_CHECKED_IN) {
        if (changeList == null) {
          delete(p4File);
        }
        else {
          delete(p4File, changeList.longValue());
        }
      }
      else {
        revert(p4File, false);
        try {
          if (changeList == null) {
            delete(p4File);
          }
          else {
            delete(p4File, changeList.longValue());
          }
        }
        catch (Exception ex) {
          // TODO: now we ignore, but we should really check the status after revert
        }
      }
    }

    // remove file
    for (File file : filesToDelete) {
      if (file.exists()) {
        final boolean res = file.delete();
        if (!res) {
          throw new VcsException(PerforceBundle.message("exception.text.cannot.delete.local.file", file));
        }
      }
    }
  }

  private void delete(final P4File p4File) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(p4File);
    delete(p4File, getActiveListNumber(connection));
  }

  private void delete(final P4File p4File, final long changeListNumber) throws VcsException {
    p4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.delete);
    P4Connection connection = getNotNullConnection(p4File);
    appendChangeListNumber(changeListNumber, arguments);
    arguments.append(p4File.getEscapedPath());
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult, connection);
  }

  public void integrate(final P4File oldP4File,final P4File newP4File) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(oldP4File);
    integrate(oldP4File, newP4File, getActiveListNumber(connection));
  }

  public void integrate(final P4File oldP4File, final P4File newP4File, final long changeListNumber) throws VcsException {
    P4Connection connection = getNotNullConnection(oldP4File);
    oldP4File.invalidateFstat();
    newP4File.invalidateFstat();
    final CommandArguments arguments = CommandArguments.createOn(P4Command.integrate);
    appendChangeListNumber(changeListNumber, arguments);
    arguments.append("-d");
    arguments.append(oldP4File.getEscapedPath()).append(newP4File.getEscapedPath());

    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult, connection);
  }

  public void reopen(final File[] selectedFiles, final long changeListNumber) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.reopen);
    arguments.append("-c");
    if (changeListNumber > 0) {
      arguments.append(String.valueOf(changeListNumber));
    }
    else {
      arguments.append("default");
    }

    MultiMap<P4Connection, File> connectionToFile = FileGrouper.distributeIoFilesByConnection(Arrays.asList(selectedFiles),
                                                                                               mySettings.getProject());

    executeFileCommands(arguments, connectionToFile);
  }

  private void executeFileCommands(final CommandArguments arguments,
                                   final MultiMap<P4Connection, File> connectionToFile) throws VcsException {
    for (P4Connection connection : connectionToFile.keySet()) {
      CommandArguments connectionArguments = arguments.createCopy();
      for (File selectedFile : connectionToFile.get(connection)) {
        connectionArguments.append(P4File.escapeWildcards(selectedFile.getPath()));
      }
      final ExecResult execResult = executeP4Command(connectionArguments.getArguments(), connection);
      checkError(execResult, connection);
    }
  }

  public void reopen(@NotNull P4Connection connection, long targetChangeListNumber, List<String> paths) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.reopen);
    arguments.append("-c");
    if (targetChangeListNumber > 0) {
      arguments.append(String.valueOf(targetChangeListNumber));
    }
    else {
      arguments.append("default");
    }

    checkError(executeP4Command(arguments.getArguments(), paths, null, new PerforceContext(connection)), connection);
  }

  public List<String> getClients(P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{P4Command.clients.getName()}, connection);
    checkError(execResult, connection);
    return OutputMessageParser.processClientsOutput(execResult.getStdout());
  }

  public List<String> getUsers(P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{P4Command.users.getName()}, connection);
    checkError(execResult, connection);
    return OutputMessageParser.processUsersOutput(execResult.getStdout());
  }

  public List<PerforceChange> getChanges(P4Connection connection, final long changeListNumber) throws VcsException {
    final PerforceClient client = myPerforceManager.getClient(connection);

    if (LOG.isDebugEnabled()) {
      LOG.debug("connection = " + connection + ", changeListNumber = " + changeListNumber);
    }

    if (changeListNumber != -1) {
      Pair<ChangeListData, List<FileChange>> pair = describeAll(connection, Collections.singletonList(changeListNumber), false).values().iterator().next();
      return createPerforceChanges(client, pair.first, pair.second);
    }

    final List<PerforceChange> result = new ArrayList<>();
    // 'p4 describe' doesn't work for the default changelist
    Map<String, List<String>> form = getChangeSpec(connection, changeListNumber);
    List<String> strings = form.get(FILES);
    if (strings != null) {
      for (String s : strings) {
        ContainerUtil.addIfNotNull(result, PerforceChange.createOn(s, client, changeListNumber, getDescription(form)));
      }
    }
    return result;
  }

  private Map<Long, Pair<ChangeListData, List<FileChange>>> describeAll(P4Connection connection, List<Long> lists, boolean shelved) throws VcsException {
    CommandArguments args = CommandArguments.createOn(P4Command.describe);
    args.append("-s");

    if (shelved) {
      args.append("-S");
    }

    for (Long list : lists) {
      args.append(list);
    }
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    checkError(execResult, connection);

    ProgressManager.checkCanceled();

    final String stdout = execResult.getStdout();
    Map<ChangeListData, List<FileChange>> map = OutputMessageParser.processMultiDescriptionOutput(stdout, shelved);
    Map<Long, Pair<ChangeListData, List<FileChange>>> result = new HashMap<>();
    for (ChangeListData data : map.keySet()) {
      result.put(data.NUMBER, Pair.create(data, map.get(data)));
    }
    return result;
  }

  @NotNull
  private static List<PerforceChange> createPerforceChanges(PerforceClient client,
                                                            ChangeListData data,
                                                            List<FileChange> changes)
    throws VcsException {
    List<PerforceChange> converted = new ArrayList<>();
    for (FileChange fileChange : changes) {
      ProgressManager.checkCanceled();
      final File localFile = PerforceManager.getFileByDepotName(fileChange.getDepotPath(), client);
      converted.add(new PerforceChange(fileChange.getType(), localFile, fileChange.getDepotPath(),
                                       fileChange.getRevisionAfter(), data.NUMBER, data.DESCRIPTION));
    }
    return converted;
  }

  private Map<String, List<String>> getChangeSpec(P4Connection connection, long changeListNumber) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.change);
    arguments.append("-o");
    if (changeListNumber > 0) {
      arguments.append(String.valueOf(changeListNumber));
    }
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult, connection);

    final String stdout = execResult.getStdout();
    return FormParser.execute(stdout, CHANGE_FORM_FIELDS);
  }

  public List<PerforceChangeList> getPendingChangeLists(final P4Connection connection) throws VcsException {
    final PerforceClient client = myPerforceManager.getClient(connection);
    final CommandArguments args = CommandArguments.createOn(P4Command.changes);
    args.append("-i");
    appendTArg(args, connection);
    args.append("-l")
      .append("-s").append("pending");

    appendUserName(client, args);
    appendClientName(client, args);
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    checkError(execResult, connection);
    return parsePerforceChangeLists(execResult.getStdout(), connection, new PerforceChangeCache(myProject));
  }

  public void setChangeRevisionsFromHave(P4Connection connection, List<PerforceChange> result) throws VcsException {
    final List<FilePath> files = new ArrayList<>();
    for (PerforceChange perforceChange : result) {
      File file = perforceChange.getFile();
      if (file != null) {
        files.add(VcsUtil.getFilePath(file, false));
      }
    }

    Object2LongMap<String> haveRevisions =
      new Object2LongOpenCustomHashMap<>(FastUtilHashingStrategies.getStringStrategy(SystemInfoRt.isFileSystemCaseSensitive));

    final PathsHelper pathsHelper = new PathsHelper(myPerforceManager);
    pathsHelper.addAllPaths(files);
    haveMultiple(pathsHelper, connection, new P4HaveParser.RevisionCollector(myPerforceManager, haveRevisions));

    for (PerforceChange change : result) {
      File file = change.getFile();
      if (file != null) {
        final String path = file.getAbsolutePath();
        final long revision = haveRevisions.getLong(FileUtil.toSystemDependentName(path));
        if (revision != 0) {
          change.setRevision(revision);
        }
      }
    }
  }

  public List<PerforceChangeList> getPendingChangeLists(final P4Connection connection,
                                                        PerforceChangeCache changeCache) throws VcsException {
    final PerforceClient client = myPerforceManager.getClient(connection);
    final CommandArguments args = CommandArguments.createOn(P4Command.changes);
    args.append("-i");
    appendTArg(args, connection);
    args.append("-l")
      .append("-s").append("pending");

    appendUserName(client, args);
    appendClientName(client, args);

    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    checkError(execResult, connection);

    return parsePerforceChangeLists(execResult.getStdout(), connection, changeCache);
  }

  public void fillChangeCache(P4Connection connection,
                              PerforceChangeCache changeCache,
                              PerforceShelf shelf,
                              List<PerforceChangeList> lists) throws VcsException {
    List<Long> numbers = ContainerUtil.map2List(lists, list -> list.getNumber());
    final PerforceClient client = myPerforceManager.getClient(connection);

    Map<Long, Pair<ChangeListData, List<FileChange>>> changeMap = describeAll(connection, numbers, false);
    for (PerforceChangeList list : lists) {
      Pair<ChangeListData, List<FileChange>> changes = changeMap.get(list.getNumber());
      if (changes != null) {
        changeCache.setChanges(connection, list.getNumber(), createPerforceChanges(client, changes.first, changes.second));
      }
    }

    ServerVersion serverVersion = PerforceManager.getInstance(myProject).getServerVersion(connection);
    if (serverVersion != null && serverVersion.supportsShelve()) {
      Map<Long, Pair<ChangeListData, List<FileChange>>> shelveMap = describeAll(connection, numbers, true);
      for (final Long cl : shelveMap.keySet()) {
        for (FileChange c : shelveMap.get(cl).second) {
          File localFile = PerforceManager.getFileByDepotName(c.getDepotPath(), client);
          shelf.addShelvedChange(connection, cl, new ShelvedChange(c.getType(), c.getDepotPath(), c.getRevisionAfter(), connection, localFile, cl));
        }
      }
    }
  }

  private void appendTArg(final CommandArguments arguments, @Nullable final P4Connection connection) throws VcsException {
    if (mySettings.getServerVersion(connection) >= 2003) {
      arguments.append("-t");
    }
  }

  private static CommandArguments appendUserName(final PerforceClient client, CommandArguments args) throws VcsException {
    final String userName = client.getUserName();
    return userName != null ? args.append("-u").append(userName) : args;
  }

  private static CommandArguments appendClientName(final PerforceClient client, CommandArguments args) throws VcsException {
    final String userName = client.getName();
    return userName != null ? args.append("-c").append(userName) : args;
  }

  private List<PerforceChangeList> parsePerforceChangeLists(final String stdout, P4Connection connection,
                                                            @NotNull PerforceChangeCache changeCache) {
    final ArrayList<PerforceChangeList> result = new ArrayList<>();
    for (ChangeListData data : OutputMessageParser.processChangesOutput(stdout)) {
      result.add(new PerforceChangeList(data, myProject, connection, changeCache));
    }
    return result;
  }

  public boolean deleteChangeList(@NotNull P4Connection connection, long number,
                               boolean acceptUnknown, boolean acceptNonEmpty, boolean acceptShelved) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.change);
    arguments.append("-d");
    arguments.append(String.valueOf(number));
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    if (execResult.getExitCode() == 0) {
      return true;
    }

    final String stderr = execResult.getStderr();
    if (acceptUnknown && stderr.contains("Change " + number + " unknown.") ||
        acceptNonEmpty && stderr.contains("open file(s) associated with it and can't be deleted") ||
        acceptShelved && stderr.contains("shelved files associated with it and can't be deleted")) {
      return false;
    }
    checkError(execResult, connection);
    return false;
  }

  private static void adjustJobs(final P4Connection connection,
                                 final Map<String, List<String>> changeForm,
                                 @Nullable final List<PerforceJob> p4jobs) {
    changeForm.remove(JOBS);
    if (p4jobs != null && (! p4jobs.isEmpty())) {
      final List<String> values = new ArrayList<>();
      for (PerforceJob p4job : p4jobs) {
        if (connection.getId().equals(p4job.getConnection().getId())) {
          values.add(p4job.getName());
        }
      }
      changeForm.put(JOBS, values);
    }
  }

  public long submitForConnection(@NotNull final P4Connection connection,
                                  final List<PerforceChange> changesForConnection,
                                  final long changeListNumber,
                                  final String preparedComment, @Nullable final List<PerforceJob> p4jobs) throws VcsException {
    List<String> excludedChanges = new ArrayList<>();
    Map<String, List<String>> changeForm = getChangeSpec(connection, changeListNumber);
    String originalDescription = getDescription(changeForm);
    adjustJobs(connection, changeForm, p4jobs);
    final StringBuffer changeData = createChangeData(preparedComment, changesForConnection, changeForm, excludedChanges);
    long submittedRevision;
    long newNumber = -1;
    if (changeListNumber == -1) {
      final CommandArguments arguments = CommandArguments.createOn(P4Command.submit);
      appendChangeListNumber(changeListNumber, arguments);
      arguments.append("-i");
      final ExecResult execResult = executeP4Command(arguments.getArguments(), changeData, connection);
      checkError(execResult, connection);
      submittedRevision = getSubmittedRevisionNumber(execResult, "Change (\\d+) created with ", 1);
    }
    else {
      CommandArguments arguments = CommandArguments.createOn(P4Command.change);
      arguments.append("-i");
      checkError(connection.runP4CommandLine(mySettings, arguments.getArguments(), changeData), connection);
      arguments = CommandArguments.createOn(P4Command.submit);
      appendChangeListNumber(changeListNumber, arguments);
      ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
      checkError(execResult, connection);
      submittedRevision = getSubmittedRevisionNumber(execResult, "Change (\\d+) renamed change (\\d+) and ", 2);

      if (!excludedChanges.isEmpty()) {
        LOG.debug("Reopening excluded changes in new changelist");
        newNumber = createChangeList(originalDescription, connection, excludedChanges);
      }
    }
    PerforceNumberNameSynchronizer.getInstance(myProject).handleChangeListSubmitted(connection, changeListNumber, newNumber);
    return submittedRevision;
  }

  private static long getSubmittedRevisionNumber(ExecResult execResult, final String regex, final int groupNumber) {
    Matcher matcher = Pattern.compile(regex).matcher(execResult.getStdout());
    if (matcher.find()) {
      String group = matcher.group(groupNumber);
      try {
        return Long.parseLong(group);
      }
      catch (NumberFormatException ignore) {
      }
    }
    return -1;
  }

  public long createChangeList(String description, @NotNull final P4Connection connection, @Nullable final List<String> files)
    throws VcsException {
    final String changeListForm = createChangeListForm(description, -1, connection, files, myPerforceManager.getClient(connection));
    if (mySettings.showCmds) {
      logMessage(changeListForm);
    }
    final ExecResult execResult = executeP4Command(new String[]{P4Command.change.getName(), "-i"}, new StringBuffer(changeListForm), connection);
    checkError(execResult, connection);
    return PerforceChangeListHelper.parseCreatedListNumber(execResult.getStdout());
  }

  public void renameChangeList(long number, String description, @NotNull P4Connection connection) throws VcsException {
    final Map<String, List<String>> oldSpec = getChangeSpec(connection, number);
    oldSpec.put(DESCRIPTION, processDescription(description));
    final ExecResult execResult = executeP4Command(new String[]{P4Command.change.getName(), "-i"}, createStringFormRepresentation(oldSpec), connection);
    checkError(execResult, connection);
  }

  public static List<String> processDescription(final String description) {
    final List<String> result = new ArrayList<>();
    final String[] lines = StringUtil.convertLineSeparators(description).split("\n");
    if (lines.length == 0) {
      result.add(DEFAULT_DESCRIPTION);
    } else {
      Collections.addAll(result, lines);
    }
    return result;
  }

  private String createChangeListForm(final String description,
                                      final long changeListNumber, final P4Connection connection,
                                      @Nullable final List<String> files, final PerforceClient client) throws VcsException {
    @NonNls final StringBuilder result = new StringBuilder();
    result.append("Change:\t");
    if (changeListNumber == -1) {
      result.append("new");
    }
    else {
      result.append(changeListNumber);
    }
    result.append("\n\nClient:\t");
    result.append(client.getName());
    result.append("\n\nUser:\t");
    result.append(client.getUserName());
    result.append("\n\nStatus:\t");
    if (changeListNumber == -1) {
      result.append("new");
    }
    else {
      result.append("pending");
    }
    result.append("\n\nDescription:");
    final List<String> descriptionLines = processDescription(description);
    for (String line : descriptionLines) {
      result.append("\n\t").append(line);
    }
    if (changeListNumber != -1 || files != null) {
      result.append("\n\nFiles:\n");
      if (files != null) {
        for(String file: files) {
          result.append("\t").append(file).append("\n");
        }
      }
      else {
        final List<PerforceChange> list = openedInList(connection, changeListNumber);
        for(PerforceChange openedFile: list) {
          result.append("\t").append(openedFile.getDepotPath()).append("\n");
        }
      }
    }

    return result.toString();
  }

  public List<PerforceChangeList> getSubmittedChangeLists(@Nullable String client, @Nullable String user, @NotNull final P4File rootP4File,
                                                          @NotNull ChangeBrowserSettings settings,
                                                          final int maxCount, final boolean showIntegrated) throws VcsException {
    String interval = dateSpec(settings.getDateAfterFilter(), settings.getDateBeforeFilter(), settings.getChangeAfterFilter(),
                        settings.getChangeBeforeFilter(), settings.STRICTLY_AFTER);
    final List<String> fileSpecs = List.of(rootP4File.getRecursivePath() + interval);
    return getSubmittedChangeLists(getNotNullConnection(rootP4File), client, user, maxCount, showIntegrated, fileSpecs);
  }

  public List<PerforceChangeList> getSubmittedChangeLists(@NotNull P4Connection connection,
                                                           @Nullable String client,
                                                           @Nullable String user,
                                                           int maxCount, boolean showIntegrated, List<String> fileSpecs)
    throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.changes);
    arguments.append("-s").append("submitted");
    if (showIntegrated) {
      arguments.append("-i");
    }
    appendTArg(arguments, connection);
    arguments.append("-l");

    if (client != null && client.length() > 0) {
      arguments.append("-c").append(client);
    }
    if (user != null && user.length() > 0) {
      arguments.append("-u").append(user);
    }
    if (maxCount > 0) {
      arguments.append("-m").append(maxCount);
    }

    for (String spec : fileSpecs) {
      arguments.append(spec);
    }

    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult, connection);
    return parsePerforceChangeLists(execResult.getStdout(), connection, new PerforceChangeCache(myProject));
  }

  private static String dateSpec(final Date after, final Date before, final Long afterChange, final Long beforeChange, boolean strictlyAfter) {

    if (after == null && before == null && afterChange == null && beforeChange == null) {
      return "";
    }

    final StringBuilder result = new StringBuilder();
    result.append('@');
    if (after != null) {
      result.append(DATESPEC_DATE_FORMAT.format(after));
    }
    else if (afterChange != null) {
      result.append(afterChange.longValue() + (strictlyAfter ? 1 : 0));
    }
    else {
      result.append(DATESPEC_DATE_FORMAT.format(new Date(0)));
    }
    result.append(',');
    result.append('@');
    if (before != null) {
      result.append(DATESPEC_DATE_FORMAT.format(before));
    }
    else if (beforeChange != null) {
      result.append(beforeChange.longValue());
    }
    else {
      result.append(NOW);
    }

    return result.toString();
  }

  public List<PerforceChange> openedInList(final P4Connection connection, final long number) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.opened);
    args.append("-c").append(number);
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    if (StringUtil.toLowerCase(execResult.getStderr()).contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE)) {
      // no files opened
      return new ArrayList<>();
    }
    checkError(execResult, connection);
    try {
      return PerforceOutputMessageParser.processOpenedOutput(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<PerforceChange> opened(final P4Connection connection, final Collection<FilePath> paths, boolean throwIfNotUnderClient) throws VcsException {
    if (paths.size() > OPENED_SIZE) {
      List<PerforceChange> result = new ArrayList<>();
      for (List<FilePath> filePaths : JBIterable.from(paths).split(OPENED_SIZE, false)) {
        result.addAll(openedImpl(connection, filePaths, throwIfNotUnderClient));
      }
      return result;
    }
    return openedImpl(connection, paths, throwIfNotUnderClient);
  }

  private List<PerforceChange> openedImpl(final P4Connection connection, final Collection<FilePath> paths, final boolean throwIfNotUnderClient) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.opened);
    for (FilePath path : paths) {
      args.append(P4File.create(path).getEscapedPath());
    }
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    final String err = StringUtil.toLowerCase(execResult.getStderr());
    if ((! throwIfNotUnderClient) && (err.contains(STANDARD_REVERT_UNCHANGED_ERROR_MESSAGE) ||
        err.contains(NOT_UNDER_CLIENT_ROOT_MESSAGE) || err.contains(NOT_IN_CLIENT_VIEW_MESSAGE))) {
    } else {
      checkError(execResult, connection);
    }
    try {
      return PerforceOutputMessageParser.processOpenedOutput(execResult.getStdout());
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  private static String getDescription(final Map<String, List<String>> changeForm) {
    final List<String> strings = changeForm.get(DESCRIPTION);
    if (strings == null) return "";
    return StringUtil.join(strings, "\n");
  }

  private static StringBuffer createChangeData(String preparedComment,
                                               List<PerforceChange> actualChanges,
                                               Map<String, List<String>> changeForm,
                                               List<String> excludedChanges) {
    setDescriptionToForm(changeForm, preparedComment);

    List<String> changes = changeForm.get(FILES);

    if (changes != null) {
      for (String changeString : changes) {
        String fileString = changeString.trim();
        int typeIndex = fileString.indexOf("#");
        String depotPath = fileString.substring(0, typeIndex - 1);
        if (findChangeByDepotPath(actualChanges, depotPath) == null) {
          excludedChanges.add(changeString);
        }
      }
      changes.removeAll(excludedChanges);
    }

    return createStringFormRepresentation(changeForm);

  }

  public static void setDescriptionToForm(Map<String, List<String>> changeForm, String preparedComment) {
    List<String> description = changeForm.get(DESCRIPTION);
    if (description != null) {
      description.clear();
      description.addAll(getAllLines(preparedComment));
    }
    else {
      changeForm.put(DESCRIPTION, getAllLines(preparedComment));
    }
  }

  private static List<String> getAllLines(String preparedComment) {
    List<String> result = new ArrayList<>();
    for (String line1 : LineTokenizer.tokenize(preparedComment.trim(), false)) {
      result.add(line1.trim());
    }
    return result;
  }

  @Nullable
  private static PerforceChange findChangeByDepotPath(List<PerforceChange> actualChanges, String depotPath) {
    for (PerforceChange change : actualChanges) {
      if (change.getDepotPath().equals(depotPath)) {
        return change;
      }
    }
    return null;
  }

  public static StringBuffer createStringFormRepresentation(Map<String, List<String>> changeForm) {
    StringBuffer result = new StringBuffer();
    for (String field : changeForm.keySet()) {
      result.append("\n");
      result.append(field);

      List<String> values = changeForm.get(field);
      result.append("\n");
      for (String value : values) {
        result.append("\t");
        result.append(value);
        result.append("\n");
      }
    }

    return result;
  }

  public List<String> getBranches(final P4Connection connection) throws VcsException {
    final ExecResult result = executeP4Command(new String[]{"branches"}, connection);
    checkError(result, connection);
    return OutputMessageParser.processBranchesOutput(result.getStdout());
  }

  @NotNull
  public BranchSpec loadBranchSpec(@NotNull final String branchName, @NotNull final P4Connection connection) throws VcsException {
    @NonNls final String[] p4args = {"branch",
      "-o",
      branchName};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult, connection);
    final Map<String, List<String>> branchSpecForm = FormParser.execute(execResult.getStdout(), new String[]{OWNER,
      DESCRIPTION,
      VIEW});
    return new BranchSpec(branchSpecForm);
  }

  public Map<String, List<String>> loadClient(final String clientName, final P4Connection connection) throws VcsException {
    @NonNls final String[] p4args = {"client",
      "-o",
      clientName};
    final ExecResult execResult = executeP4Command(p4args, connection);
    checkError(execResult, connection);

    return FormParser.execute(execResult.getStdout(), new String[]{"Client:",
      "Owner:",
      "Update:",
      "Access:",
      "Host:",
      "Description:",
      CLIENTSPEC_ROOT,
      CLIENTSPEC_ALTROOTS,
      CLIENT_OPTIONS,
      "LineEnd:",
      VIEW});
  }

  public byte @NotNull [] getByteContent(final P4File file, @Nullable final String revisionNumber) throws VcsException {
    return getByteContent(file.getEscapedPath(), revisionNumber, getNotNullConnection(file));
  }

  public byte @NotNull [] getByteContent(BaseRevision baseRevision, @NotNull P4Connection connection) throws VcsException {
    return getByteContent(baseRevision.getDepotPath(), baseRevision.getRevisionNum(), connection);
  }
  public byte @NotNull [] getByteContent(final String depotPath, @Nullable final String revisionNumber, @NotNull P4Connection connection) throws VcsException {
    File tempFile = null;
    try {
      tempFile = FileUtil.createTempFile("ijP4Print", "");
      String[] p4args = {"print", "-q", "-o", tempFile.getPath(), depotPath + (revisionNumber == null ? "" : revisionNumber)};
      final ExecResult execResult = executeP4Command(p4args, connection);
      checkError(execResult, connection);
      ContentRevisionCache.checkContentsSize(depotPath, tempFile.length());
      return FileUtil.loadFileBytes(tempFile);
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
    finally {
      if (tempFile != null) {
        FileUtil.delete(tempFile);
      }
    }
  }

  public byte @NotNull [] getContent(final FilePath file, final String revisionNumber) throws VcsException {
    P4File p4File = P4File.create(file);
    return getByteContent(getDepotPath(p4File), revisionNumber, getNotNullConnection(p4File));
  }

  private String getDepotPath(P4File p4File) throws VcsException {
    return p4File.getFstat(myProject, true).depotFile;
  }

  public LocalPathsSet getResolvedWithConflictsMap(@NotNull final P4Connection connection, final Collection<VirtualFile> roots) throws VcsException {
    final List<String> args = new ArrayList<>();
    for (VirtualFile root : roots) {
      args.add(P4File.create(root).getRecursivePath());
    }

    final ExecResult execResult = executeP4Command(new String[]{P4Command.resolve.getName(), "-n", "-t"}, args, null, new PerforceContext(connection));
    return new LocalPathsSet(processResolveOutput(execResult.getStdout()).keySet());
  }

  public LinkedHashSet<VirtualFile> getResolvedWithConflicts(final P4Connection connection, @Nullable final VirtualFile root) throws VcsException {
    return getResolvedWithConflicts(connection,
                                    ContainerUtil.createMaybeSingletonList(root == null ? null : P4File.create(root).getRecursivePath()));
  }

  @NotNull
  public LinkedHashSet<VirtualFile> getResolvedWithConflicts(P4Connection connection, Collection<String> fileSpecs) throws VcsException {
    final CommandArguments args = CommandArguments.createOn(P4Command.resolve);
    args.append("-n");
    args.append("-t");
    for (String spec : fileSpecs) {
      args.append(spec);
    }
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    final LinkedHashSet<VirtualFile> result = new LinkedHashSet<>();
    if (StringUtil.toLowerCase(execResult.getStderr()).contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      return result;
    }

    LinkedHashMap<String, BaseRevision> map = processResolveOutput(execResult.getStdout());
    for (String path : map.keySet()) {
      ContainerUtil.addIfNotNull(result, LocalFileSystem.getInstance().findFileByPath(path));
    }
    return result;
  }

  @Nullable
  public BaseRevision getBaseRevision(P4File file) throws VcsException {
    final P4Connection connection = getNotNullConnection(file);
    final ExecResult execResult = executeP4Command(new String[]{"resolve",
      "-n", "-o", "-t",
      file.getEscapedPath()}, connection);
    final String stdout = execResult.getStdout();
    if (StringUtil.toLowerCase(stdout).contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      return null;
    }
    checkError(execResult, connection);
    Map<String, BaseRevision> result = processResolveOutput(stdout);
    return result.isEmpty() ? null : result.values().iterator().next();
  }

  public static LinkedHashMap<String, BaseRevision> processResolveOutput(final String output) {
    final LinkedHashMap<String, BaseRevision> result = new LinkedHashMap<>();
    for (String line : StringUtil.splitByLines(output)) {
      String separator = MERGING_MESSAGE;
      int index = line.indexOf(separator);
      if (index < 0) {
        index = line.indexOf(separator = UBINARY_MERGING_MESSAGE);
      }
      if (index < 0) {
        index = line.indexOf(separator = BINARY_MERGING_MESSAGE);
      }
      if (index >= 0) {
        String file = line.substring(0, index).trim();
        BaseRevision revision = createBaseRevision(line.substring(index + separator.length()), result.get(file));
        result.put(file, revision);
      }
    }
    return result;
  }

  @Nullable
  private static BaseRevision createBaseRevision(String line, @Nullable BaseRevision existing) {
    int usingBasePosition = line.indexOf(USING_BASE_MESSAGE);
    if (usingBasePosition < 0) return null;
    String sourcePath = line.substring(0, usingBasePosition);

    int sourceRevPosition = StringUtil.indexOfAny(sourcePath, "#@");
    String sourceRevision = sourceRevPosition >= 0 ? sourcePath.substring(sourceRevPosition).trim() : null;
    int revPosition = StringUtil.indexOfAny(line, "#@", usingBasePosition, line.length());

    if (revPosition < 0) return null;
    String basePath = line.substring(usingBasePosition + USING_BASE_MESSAGE.length(), revPosition).trim();

    final String revision = line.substring(revPosition).trim();
    try {
      final String revisionNum = existing == null ? revision : existing.getRevisionNum();
      return new BaseRevision(revisionNum, sourceRevision, basePath);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  public void resolveToYours(final P4File file) throws VcsException {
    resolve(file, "-ay");
  }

  public void resolveAutomatically(final P4File file) throws VcsException {
    resolve(file, "-am");
  }

  private void resolve(final P4File file, @NonNls final String resolveMode) throws VcsException {
    @NonNls String[] p4args = {"resolve", resolveMode, file.getRecursivePath()};
    final P4Connection connection = getNotNullConnection(file);
    final ExecResult execResult = executeP4Command(p4args, connection);
    final String stdErr = StringUtil.toLowerCase(execResult.getStderr());
    if (!stdErr.contains(NO_FILES_TO_RESOLVE_MESSAGE)) {
      checkError(execResult, connection);
    }
  }

  public ExecResult integrate(final String branchName,
                              final P4File path,
                              final long changeListNum,
                              @Nullable final String integrateChangeListNum,
                              final boolean reverse,
                              final P4Connection connection) {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.integrate);
    appendChangeListNumber(changeListNum, arguments);

    if (reverse) {
      arguments.append("-r");
    }

    arguments.append("-b").append(branchName);

    boolean insideBranch;
    String depotPath;
    try {
      depotPath = where(path, connection).getDepot() + "/";
      insideBranch = isInsideBranch(branchName, connection, depotPath);
    }
    catch (VcsException e) {
      ExecResult result = new ExecResult();
      result.setException(e);
      return result;
    }

    if (insideBranch == reverse) {
      arguments.append("-s");
    }

    if (integrateChangeListNum == null) {
      arguments.append(depotPath + "...");
    }
    else {
      arguments.append(depotPath + "...@" + integrateChangeListNum + ",@" + integrateChangeListNum);
    }

    return executeP4Command(arguments.getArguments(), connection);
  }

  public boolean have(P4File file) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    if (connection == null) return false;

    Object2LongMap<String> haveRevisions = new Object2LongOpenHashMap<>();
    final P4HaveParser haveParser = new P4HaveParser.RevisionCollector(myPerforceManager, haveRevisions);
    doHave(Collections.singletonList(getP4FilePath(file, file.isDirectory(), file.isDirectory())), connection, haveParser, false);
    return !haveRevisions.isEmpty();
  }

  public long haveRevision(P4File file) throws VcsException {
    P4Connection connection = myConnectionManager.getConnectionForFile(file);
    if (connection == null) return -1;

    Object2LongMap<String> haveRevisions = new Object2LongOpenHashMap<>();
    final P4HaveParser haveParser = new P4HaveParser.RevisionCollector(myPerforceManager, haveRevisions);
    doHave(Collections.singletonList(getP4FilePath(file, file.isDirectory(), false)), connection, haveParser, false);
    return haveRevisions.isEmpty() ? -1 : haveRevisions.values().iterator().nextLong();
  }

  public VcsRevisionNumber getCurrentRevision(final P4File p4File) {
    try {
      final long curRev = haveRevision(p4File);
      // cached
      FStat fstat = p4File.getFstat(myProject, false);
      final long cachedRev = Long.parseLong(fstat.haveRev);
      if (cachedRev != curRev) {
        // go for head change also
        fstat = p4File.getFstat(myProject, true);
      }
      return PerforceVcsRevisionNumber.createFromFStat(fstat);
    }
    catch (VcsException | NumberFormatException e) {
      return null;
    }
  }

  public void haveMultiple(final PathsHelper helper, @NotNull final P4Connection connection, final P4HaveParser consumer) throws VcsException {
    if (helper.isEmpty()) return;
    final List<String> args = helper.getRequestString();

    doHave(args, connection, consumer, true);
  }

  static String getP4FilePath(final P4File file, boolean isDirectory, final boolean recursively) {
    String escapedPath = file.getEscapedPath();
    return isDirectory ? escapedPath + "/" + (recursively ? "..." : "*") : escapedPath;
  }

  private void doHave(final List<String> filesSpec,
                      @NotNull final P4Connection connection,
                      final P4HaveParser consumer,
                      boolean longTimeout) throws VcsException {
    // See http://www.perforce.com/perforce/doc.052/manuals/cmdref/have.html#1040665
    // According to Perforce docs output will be presented patterned like: depot-file#revision-number - local-path
    // One line per file

    PerforceContext context = new PerforceContext(connection, longTimeout, false);

    for (List<String> chunk : Lists.partition(new ArrayList<>(new LinkedHashSet<>(filesSpec)), CHUNK_SIZE)) {
      final ExecResult execResult = executeP4Command(new String[]{"have"}, chunk, null, context);
      final String stderr = execResult.getStderr();
      final boolean notUnderRoot = stderr.contains(NOT_ON_CLIENT_MESSAGE) || stderr.contains(NOT_UNDER_CLIENT_ROOT_MESSAGE);
      if (! notUnderRoot) {
        // Perforce bug: if ask "p4 have <local path>/*" and in <local path> directory it would be unversioned file with symbols
        // that should be escaped, Perforce reports "Invalid revision number" somewhy
        // since we do NOT pass revision number in have string, we can filter out this message and use other strings of output
        if (! stderr.contains(INVALID_REVISION_NUMBER)) {
          checkError(execResult, connection);
        }
      } else {
        LOG.debug("Problem while doing 'have': " + stderr);
      }
      final Ref<VcsException> vcsExceptionRef = new Ref<>();
      try {
        execResult.allowSafeStdoutUsage(inputStream -> {
          try {
            consumer.readHaveOutput(inputStream);
          }
          catch (VcsException e) {
            vcsExceptionRef.set(e);
          }
        });
      }
      catch (IOException e) {
        throw new VcsException(e);
      }
      if (! vcsExceptionRef.isNull()) {
        throw vcsExceptionRef.get();
      }
    }
  }

  public P4Revision[] filelog(final P4File file, boolean showBranches) throws VcsException {
    return filelog(getNotNullConnection(file), file.getRecursivePath(), showBranches);
  }

  public P4Revision[] filelog(@NotNull P4Connection connection, @NotNull String path, boolean showBranches) throws VcsException {
    CommandArguments arguments = createFilelogArgs(showBranches, connection).append(path);
    final ExecResult execResult = executeP4Command(arguments.getArguments(), connection);
    checkError(execResult, connection);

    return parseLogOutput(execResult, isFilelogNewDateVersion(connection));
  }

  public List<String> files(final Collection<String> escapedPaths, final P4Connection connection) throws VcsException {
    if (escapedPaths.isEmpty()) return Collections.emptyList();

    final ExecResult execResult = executeP4Command(CommandArguments.createOn(P4Command.files).getArguments(), escapedPaths, null, new PerforceContext(connection));
    if (execResult.getExitCode() != 0) {
      checkError(execResult, connection);
    }
    return new OutputMessageParser(execResult.getStdout()).myLines;
  }

  private CommandArguments createFilelogArgs(boolean showBranches, @Nullable final P4Connection connection) throws VcsException {
    final CommandArguments arguments = CommandArguments.createOn(P4Command.filelog);
    if (showBranches) {
      arguments.append("-i");
    }
    arguments.append("-l");
    if (isFilelogNewDateVersion(connection)) {
      arguments.append("-t");
    }
    int limit = VcsConfiguration.getInstance(myProject).LIMIT_HISTORY ? VcsConfiguration.getInstance(myProject).MAXIMUM_HISTORY_ROWS : -1;
    if (limit > 0) {
      arguments.append("-m");
      arguments.append(limit);
    }
    return arguments;
  }

  private boolean isFilelogNewDateVersion(@Nullable final P4Connection connection) throws VcsException {
    final ServerVersion serverVersion = mySettings.getServerFullVersion(connection);
    if (serverVersion == null) return false;
    return serverVersion.getVersionYear() >= 2003 || serverVersion.getVersionYear() == 2002 && serverVersion.getVersionNum() > 1;
  }

  private static P4Revision[] parseLogOutput(final ExecResult execResult, boolean newDateFormat) throws VcsException {
    try {
      final List<P4Revision> p4Revisions = OutputMessageParser.processLogOutput(execResult.getStdout(), newDateFormat);
      return p4Revisions.toArray(new P4Revision[0]);
    }
    catch (ParseException e) {
      throw new VcsException(e);
    }
  }

  public AnnotationInfo annotate(P4Connection connection, String filePath, long revision) throws VcsException {
    if (revision != -1) {
      filePath += "#" + revision;
    }
    List<String> commands = new ArrayList<>();
    commands.add("annotate");
    commands.add("-q");
    boolean useChangelistNumbers = false;
    if (mySettings.SHOW_BRANCHES_HISTORY && isAnnotateBranchSupported(connection)) {
      commands.add("-i");
      useChangelistNumbers = true;
    }
    if (isAnnotateIgnoringWhitespaceSupported(connection)) {
      commands.add("-dw");
    } else {
      commands.add("-dl");
    }
    commands.add(filePath);
    final ExecResult execResult = executeP4Command(ArrayUtilRt.toStringArray(commands), connection);
    checkError(execResult, connection);
    try {
      return new AnnotationInfo(execResult.getStdout(), useChangelistNumbers);
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  private boolean isAnnotateBranchSupported(@Nullable final P4Connection connection) throws VcsException {
    ServerVersion version = myPerforceManager.getServerVersion(connection);
    if (version == null) return false;
    return version.getVersionYear() > 2005 || version.getVersionYear() == 2005 && version.getVersionNum() >= 2;
  }

  private boolean isAnnotateIgnoringWhitespaceSupported(@Nullable final P4Connection connection) throws VcsException {
    return isAnnotateBranchSupported(connection);
  }

  public List<ResolvedFile> getResolvedFiles(@NotNull final P4Connection connection, final Collection<VirtualFile> roots) throws VcsException {
    final List<String> args = new ArrayList<>();
    for (VirtualFile root : roots) {
      args.add(P4File.create(root).getRecursivePath());
    }
    final ExecResult execResult = executeP4Command(new String[]{P4Command.resolved.getName()}, args, null, new PerforceContext(connection));
    checkError(execResult, connection);
    try {
      final String clientRoot = myPerforceManager.getClientRoot(connection);
      return PerforceOutputMessageParser.processResolvedOutput(execResult.getStdout(),
                                                               // this convertor will replace possibly not canonic
                                                               // client root in a path (client root returned by perforce manager is already canonicalized)
                                                               o -> myPerforceManager.convertP4ParsedPath(clientRoot, o));
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  public List<String> getJobSpecification(final P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{"jobspec", "-o"}, connection);
    checkError(execResult, connection);
    final String stdout = execResult.getStdout();
    SPECIFICATION_LOG.debug(stdout);
    return new OutputMessageParser(stdout).myLines;
  }

  public List<String> getJobs(final P4Connection connection, final JobsSearchSpecificator specificator) throws VcsException {
    final String[] strings = specificator.addParams(new String[]{"jobs", "-l"});
    final ExecResult execResult = executeP4Command(strings, connection);
    checkError(execResult, connection);
    return new OutputMessageParser(execResult.getStdout()).myLines;
  }

  public List<String> getJobDetails(final PerforceJob job) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"job", "-o", job.getName()}, job.getConnection());
    checkError(execResult, job.getConnection());
    return new OutputMessageParser(execResult.getStdout()).myLines;
  }

  public List<String> getJobsForChange(@NotNull final P4Connection connection, final long number) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"fixes", "-c", String.valueOf(number)}, connection);
    checkError(execResult, connection);
    return new OutputMessageParser(execResult.getStdout()).myLines;
  }

  public void addJobForList(@NotNull final P4Connection connection, final long number, final String name) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"fix", "-c", String.valueOf(number), name}, connection);
    checkError(execResult, connection);
  }

  public void removeJobFromList(@NotNull final P4Connection connection, final long number, final String name) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[] {"fix", "-d", "-c", String.valueOf(number), name}, connection);
    checkError(execResult, connection);
  }

  // The method is used in "Upsource Integration" plugin
  public ExecResult executeP4Command(@NonNls final String[] p4args, @NotNull final P4Connection connection) {
    return executeP4Command(p4args, null, connection);
  }

  private ExecResult executeP4Command(@NonNls final String[] p4args, @Nullable final StringBuffer inputStream, @NotNull final P4Connection connection) {
    return executeP4Command(p4args, Collections.emptyList(), inputStream, new PerforceContext(connection));
  }

  private ExecResult executeP4Command(@NonNls String[] p4cmd, Collection<String> args, @Nullable final StringBuffer inputStream, @NotNull PerforceContext ctx) {
    // construct the command-line
    final ExecResult retVal = new ExecResult();
    if (!mySettings.ENABLED) {
      retVal.setException(new VcsException(PerforceBundle.message("exception.text.perforce.integration.is.disabled")));
      retVal.setStderr(PerforceBundle.message("exception.text.perforce.integration.is.disabled"));
      return retVal;
    }

    File tempFile = null;
    try {
      String presentableCmdLine = StringUtil.join(p4cmd, " ") + " " + StringUtil.join(args, " ");
      final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
      if (progressIndicator != null) {
        progressIndicator.setText2(PerforceBundle.message("progress.text2.p4.status", StringUtil.trimLog(presentableCmdLine, 239).trim()));
        progressIndicator.setText(PerforceBundle.message("progress.text.perforce.command"));
      }

      if (mySettings.showCmds) {
        logMessage(StringUtil.trimLog(presentableCmdLine, 10 * 1000));
      }

      final String[] p4args;
      if (shouldPassArgumentsViaFile(args)) {
        tempFile = createArgumentFile(args);
        p4args = ArrayUtil.mergeArrays(new String[]{"-x", tempFile.getPath()}, p4cmd);
      } else {
        p4args = ArrayUtil.mergeArrays(p4cmd, ArrayUtilRt.toStringArray(args));
      }

      ctx.runP4Command(mySettings, p4args, retVal, inputStream);
    }
    catch (PerforceTimeoutException | VcsException | InterruptedException | IOException e) {
      retVal.setException(e);
    }
    finally {
      if (tempFile != null) {
        FileUtil.asyncDelete(tempFile);
      }
    }

    if (mySettings.showCmds) {
      logMessage("\n" + retVal);
    }

    if (mySettings.USE_LOGIN && (retVal.getStderr().contains(SESSION_EXPIRED_MESSAGE) || retVal.getStderr().contains(PASSWORD_INVALID_MESSAGE))) {
      myLoginManager.notLogged(ctx.connection);
      try {
        if (!ctx.justLogged && myLoginManager.silentLogin(ctx.connection)) {
          retVal.cleanup();
          return executeP4Command(p4cmd, args, inputStream, new PerforceContext(ctx.connection, ctx.longTimeout, true));
        }
      }
      catch (VcsException e) {
        retVal.setException(createCorrectException(e.getMessage(), mySettings, ctx.connection));
        return retVal;
      }
      return retVal;
    }

    if (!mySettings.USE_LOGIN &&
        (retVal.getStderr().contains(PASSWORD_INVALID_MESSAGE) || retVal.getStderr().contains(PASSWORD_NOT_ALLOWED_MESSAGE))) {
      myLoginManager.notLogged(ctx.connection);
    }

    return retVal;
  }

  private static File createArgumentFile(Collection<String> args) throws VcsException {
    try {
      File tempFile = FileUtil.createTempFile("p4batch", ".txt");
      FileUtil.writeToFile(tempFile, StringUtil.join(args, System.lineSeparator()));
      tempFile.deleteOnExit();
      return tempFile;
    }
    catch (IOException e) {
      throw new VcsException(e);
    }
  }

  private static boolean shouldPassArgumentsViaFile(Collection<String> args) {
    int totalLength = 0;
    for (String arg : args) {
      totalLength += arg.length() + 1;
    }
    return totalLength > 2000;
  }

  private static @Nls String createPasswordNotAllowedButSetMessage(@NotNull final P4Connection connection) {
    return PerforceBundle.message("connection.password.not.allowed", connection.getWorkingDir());
  }

  public static File getDumpFile() {
    return new File(PathManager.getLogPath(), DUMP_FILE_NAME);
  }

  private static void logMessage(final String message) {
    File file = getDumpFile();
    String s = "\n" + new Time(System.currentTimeMillis()) + " " + message;
    try {
      if (file.length() > MAX_LOG_LENGTH) {
        FileUtil.delete(file);
      }
      FileUtil.writeToFile(file, s.getBytes(StandardCharsets.UTF_8), true);
    }
    catch (Exception e) {
      //ignore
    }
  }

  private static VcsException createCorrectException(@NlsSafe String stderr, final PerforceSettings settings, @Nullable final P4Connection connection) {
    if (connection != null) {
      if (stderr.contains(PASSWORD_NOT_ALLOWED_MESSAGE)) {
        return new PerforcePasswordNotAllowedException(settings.USE_LOGIN && settings.useP4CONFIG ? createPasswordNotAllowedButSetMessage(connection) : stderr,
                                                       settings.getProject(), connection);
      }
      if (stderr.contains(PASSWORD_INVALID_MESSAGE) || stderr.contains(PASSWORD_INVALID_MESSAGE2) || stderr.contains(SESSION_EXPIRED_MESSAGE)) {
        return new PerforceAuthenticationException(stderr, connection, settings.getProject());
      }
    }
    if (stderr.contains("must create client") && stderr.contains("to access local files") || stderr.contains("Password must be set before access can be granted")) {
      return new VcsConnectionProblem(stderr);
    }
    if (stderr.contains("Connect to server failed")) {
      return new PerforceServerUnavailable(stderr);
    }
    return new VcsException(stderr);
  }

  public ExecResult checkError(final ExecResult result, @Nullable P4Connection connection) throws VcsException {
    checkError(result, mySettings, connection);
    return result;
  }

  public static void checkError(final ExecResult result, final PerforceSettings settings, @Nullable P4Connection connection) throws VcsException {
    final Throwable exception = result.getException();
    final boolean error = exception != null || containsErrorOutput(result) || result.getExitCode() < 0;
    if (error) {
      final String errorOutput = result.getStderr();
      if (exception != null) {
        result.cleanup();
        if (exception instanceof PerforceAuthenticationException) {
          throw (PerforceAuthenticationException)exception;
        }
        throw new VcsException(exception);
      }
      else {
        String stdErr = errorOutput.trim();
        if (stdErr.length() > 0) {
          boolean hasErrors = false;
          for (String s : stdErr.split("\n")) {
            if (StringUtil.isNotEmpty(s) && !StringUtil.toLowerCase(s).contains(NO_FILES_RESOLVED_MESSAGE)) {
              hasErrors = true;
              break;
            }
          }
          if (hasErrors) {
            String stdOut = result.getStdout().trim();
            String message = StringUtil.isNotEmpty(stdOut) ? errorOutput + "\n" + stdOut : errorOutput;
            throw createCorrectException(message, settings, connection);
          }
        }
        else {
          throw new VcsException(PerforceBundle.message("error.p4.returned.error.code", result.getExitCode()));
        }
      }
    }
  }

  public static VcsException[] checkErrors(final ExecResult result, final PerforceSettings settings, P4Connection connection) {
    final Throwable exception = result.getException();
    final boolean error = result.getExitCode() != 0 || exception != null || containsErrorOutput(result);
    if (error) {
      final String errorOutput = result.getStderr();
      result.cleanup();
      if (exception != null) {
        return new VcsException[] { new VcsException(exception) };
      }
      else {
        String[] lines = errorOutput.split("\n");
        VcsException[] errors = new VcsException[lines.length];
        for(int i=0; i<lines.length; i++) {
          errors [i] = createCorrectException(lines [i], settings, connection);
        }
        return errors;
      }
    }
    return VcsException.EMPTY_ARRAY;
  }

  private static boolean containsErrorOutput(final ExecResult result) {
    String errorOutput = result.getStderr().trim();
    return !errorOutput.contains(FILES_UP_TO_DATE) && errorOutput.length() > 2;
  }

  @NotNull
  public P4WhereResult where(final P4File file, final P4Connection connection) throws VcsException {
    return where(file.getEscapedPath(), connection);
  }

  public P4WhereResult whereDir(final P4File dir, final P4Connection connection) throws VcsException {
    final P4WhereResult result = where(dir.getRecursivePath(), connection);
    return new P4WhereResult(removeTail(result.getLocal()), removeTail(result.getLocalRootDependent()), removeTail(result.getDepot()));
  }

  private static String removeTail(final String s) {
    return StringUtil.trimEnd(s, "/...");
  }

  @NotNull
  public P4WhereResult where(final String escapedPath, final P4Connection connection) throws VcsException {
    final ExecResult execResult = executeP4Command(new String[]{"where", escapedPath}, connection);
    checkError(execResult, connection);

    String out = getLastLine(execResult);

    final PerforceClient client = myPerforceManager.getClient(connection);

    List<String> roots = ContainerUtil.map(client.getRoots(), root -> myPerforceManager.getRawRoot(root).replace('\\', '/'));
    WhereParser parser = new WhereParser(out, roots, client.getName(), escapedPath);
    parser.execute();

    return new P4WhereResult(myPerforceManager.convertP4ParsedPath(null, parser.getLocal()), parser.getLocalRootRelative(),
                             parser.getDepot());
  }

  private static String getLastLine(ExecResult execResult) throws VcsException {
    final String result = execResult.getStdout();
    final String[] lines = result.trim().split("\n");
    for (int i = lines.length - 1; i >= 0; i--) {
      String line = lines[i];
      if (line.startsWith("-")) continue;
      return line;
    }
    throw new VcsException(PerforceBundle.message("error.p4.where.wrong.result", result));
  }

  public ClientVersion getClientVersion() {
    final P4Connection connection = new PerforceLocalConnection(myProject.getBaseDir().getPath());
    // just "p4 -V"
    try {
      final ExecResult result = executeP4Command(new String[]{"-V"}, connection);
      checkError(result, connection);
      final String out = result.getStdout();
      final Map<String, List<String>> map = FormParser.execute(out, new String[]{CLIENT_VERSION_REV});
      final List<String> versionString = map.get(CLIENT_VERSION_REV);
      if ((versionString != null) && (! versionString.isEmpty())) {
        return OutputMessageParser.parseClientVersion(versionString.get(0));
      }
    }
    catch (VcsException e) {
      //
    }
    return ClientVersion.UNKNOWN;
  }

  public void move(final P4File from, final P4File to, final P4Connection connection, boolean keepWorkspace, long changeList) throws VcsException {
    final CommandArguments command = CommandArguments.createOn(P4Command.move);
    appendChangeListNumber(changeList, command);

    if (keepWorkspace) {
      command.append("-k");
    }
    command.append(from.getEscapedPath());
    command.append(to.getEscapedPath());

    final ExecResult result = executeP4Command(command.getArguments(), connection);
    checkError(result, connection);
    if (result.getStdout().contains("can't move to an existing file")) {
      throw new VcsException(PerforceBundle.message("exception.text.cannot.assure.no.file.being.on.server", to.getLocalPath()));
    }
  }

  public boolean isValidPendingNumber(P4Connection connection, long number) {
    LOG.debug("connection = [" + connection + "], changeListNumber = [" + number + "]");
    final CommandArguments args = CommandArguments.createOn(P4Command.describe);
    args.append("-s").append(number);
    final ExecResult execResult = executeP4Command(args.getArguments(), connection);
    final Throwable exception = execResult.getException();
    final boolean error = exception != null || containsErrorOutput(execResult) || execResult.getExitCode() < 0;
    return !error;
  }

  public List<String> getBranchViews(@NotNull String branchName, boolean target, @NotNull final P4Connection connection) throws VcsException {
    List<String> fileSpecs = new ArrayList<>();
    for (String view : loadBranchSpec(branchName, connection).getViews()) {
      String[] split = view.split("\\s");
      if (split.length == 2) {
        fileSpecs.add(split[target ? 1 : 0]);
      }
    }
    return fileSpecs;
  }

  private boolean isInsideBranch(@NotNull String branchName, @NotNull final P4Connection connection, final String depotPath) throws VcsException {
    for (String view : getBranchViews(branchName, true, connection)) {
      if (depotPath.startsWith(StringUtil.trimEnd(view, "..."))) {
        return true;
      }
    }
    return false;
  }

  public void unshelve(P4Connection connection, long changeList, Collection<String> paths) throws VcsException {
    ExecResult result = executeP4Command(new String[]{"unshelve", "-s", String.valueOf(changeList), "-c", String.valueOf(changeList)}, paths, null, new PerforceContext(connection));
    checkError(result, connection);
  }

  public void shelve(P4Connection connection, long changeList, Collection<String> paths) throws VcsException {
    ExecResult result = executeP4Command(new String[]{"shelve", "-f", "-c", String.valueOf(changeList)}, paths, null, new PerforceContext(connection));
    if (result.getExitCode() != 0) {
      throw new VcsException(result.getStdout() + result.getStderr());
    }
    checkError(result, connection);
  }

  public void deleteFromShelf(P4Connection connection, long changeList, Collection<String> paths) throws VcsException {
    ExecResult result = executeP4Command(new String[]{"shelve", "-d", "-c", String.valueOf(changeList)}, paths, null, new PerforceContext(connection));
    checkError(result, connection);
    deleteChangeList(connection, changeList, false, true, true);
  }
}
