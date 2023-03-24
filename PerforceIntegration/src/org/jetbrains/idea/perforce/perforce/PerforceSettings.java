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
package org.jetbrains.idea.perforce.perforce;

import com.intellij.credentialStore.CredentialPromptDialog;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.messages.Topic;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigHelper;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;
import org.jetbrains.idea.perforce.perforce.login.PerforceOfflineNotification;

import java.io.File;
import java.util.*;

import static com.intellij.credentialStore.CredentialAttributesKt.CredentialAttributes;

@State(name = "PerforceDirect.Settings", storages = @Storage(StoragePathMacros.WORKSPACE_FILE), reportStatistic = false)
public final class PerforceSettings implements PersistentStateComponent<PerforceSettings>,
                                               PerforcePhysicalConnectionParametersI {
  private static final Logger LOG = Logger.getInstance(PerforceSettings.class);
  public static final Topic<Runnable> OFFLINE_MODE_EXITED = new Topic<>("Perforce.offline_mode_exited", Runnable.class);

  private static final String PERFORCE_SETTINGS_PASSWORD_KEY = "PERFORCE_SETTINGS_PASSWORD_KEY";

  private final Project myProject;
  private final PerforceOfflineNotification myOfflineNotification;

  // ------------------ persistent state start

  @Property(surroundWithTag = false)
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false, entryTagName = "CURRENT_CHANGE_LIST")
  public Map<ConnectionId, ParticularConnectionSettings> myConnectionSettings = new LinkedHashMap<>();

  public boolean useP4CONFIG = true;
  public boolean useP4IGNORE = true;
  public String port = "<perforce_server>:1666";
  public String client = "";
  public String user = "";
  public boolean showCmds = false;

  public @NlsSafe String pathToExec = "p4";
  public @NlsSafe String pathToIgnore = ".p4ignore";
  public @NlsSafe String PATH_TO_P4VC = "p4vc";

  public boolean myCanGoOffline = true;

  public boolean SYNC_FORCE = false;
  public boolean SYNC_RUN_RESOLVE = true;
  public boolean REVERT_UNCHANGED_FILES = true;
  public boolean REVERT_UNCHANGED_FILES_CHECKIN = false;
  public @NlsSafe String CHARSET = getCharsetNone();
  public boolean SHOW_BRANCHES_HISTORY = true;
  public boolean ENABLED = true;
  public boolean USE_LOGIN = true;
  public boolean INTEGRATE_RUN_RESOLVE = true;
  public boolean INTEGRATE_REVERT_UNCHANGED = true;
  public int SERVER_TIMEOUT = 20000;
  public boolean USE_PERFORCE_JOBS = false;
  public boolean SHOW_INTEGRATED_IN_COMMITTED_CHANGES = true;

  //
  // public PerforceSettings methods
  //

  public PerforceSettings() {
    myProject = null;
    myOfflineNotification = null;
  }

  public PerforceSettings(Project project) {
    myProject = project;
    myOfflineNotification = new PerforceOfflineNotification(myProject);
    myCanGoOffline = true;
  }

  public static PerforceSettings getSettings(final Project project) {
    return project.getService(PerforceSettings.class);
  }

  public PerforcePhysicalConnectionParameters getPhysicalSettings() {
    return new PerforcePhysicalConnectionParameters(getPathToExec(), getPathToIgnore(), myProject, getServerTimeout(), getCharsetName());
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String[] getConnectArgs() {
    assert !useP4CONFIG;
    final ArrayList<String> args = new ArrayList<>();

    if (!StringUtil.isEmptyOrSpaces(port)) {
      args.add("-p");
      args.add(port);
    }
    if (!StringUtil.isEmptyOrSpaces(client)) {
      args.add("-c");
      args.add(client);
    }
    if (!StringUtil.isEmptyOrSpaces(user)) {
      args.add("-u");
      args.add(user);
    }
    if (!USE_LOGIN) {
      final String pass = getPasswd();
      if (!StringUtil.isEmptyOrSpaces(pass)) {
        args.add("-P");
        args.add(pass);
      }
    }

    if (!isNoneCharset()) {
      args.add("-C");
      args.add(CHARSET);
    }

    return ArrayUtilRt.toStringArray(args);
  }

  @Override
  public String getPathToExec() {
    return pathToExec;
  }

  @Override
  public String getPathToIgnore() {
    if (useP4IGNORE) {
      return P4ConfigHelper.getP4IgnoreFileNameFromEnv();
    }

    return pathToIgnore;
  }

  @Override
  public Project getProject() {
    return myProject;
  }

  @Nullable
  @Transient
  public String getPasswd() {
    return PasswordSafe.getInstance().getPassword(CredentialAttributes(getClass(), PERFORCE_SETTINGS_PASSWORD_KEY));
  }

  @Nullable
  public String requestForPassword(P4Connection connection) {
    String prompt = connection == null
               ? PerforceBundle.message("message.text.perforce.command.failed.enter.password.v2")
               : PerforceBundle.message("message.text.perforce.command.failed.withdir.enter.password.v2", connection.getWorkingDir());
    String title = PerforceBundle.message("dialog.title.perforce.login");
    return CredentialPromptDialog.askPassword(myProject, title, prompt, CredentialAttributes(getClass(), PERFORCE_SETTINGS_PASSWORD_KEY), true);
  }

  public long getServerVersion(@Nullable final P4Connection connection) throws VcsException {
    return PerforceManager.getInstance(myProject).getServerVersionYear(connection);
  }

  public long getServerVersionCached(@Nullable final P4Connection connection) throws VcsException {
    return PerforceManager.getInstance(myProject).getServerVersionYearCached(connection);
  }

  @Nullable
  public ServerVersion getServerFullVersion(@Nullable final P4Connection connection) throws VcsException {
    return PerforceManager.getInstance(myProject).getServerVersion(connection);
  }

  public void ensureOfflineNotify() {
    if (myCanGoOffline && !ENABLED) {
      myOfflineNotification.ensureNotify(myOfflineNotification);
    }
  }

  @Override
  public void disable() {
    disable(false);
  }

  public void disable(boolean byUser) {
    if (!myCanGoOffline && !byUser) return;

    if (byUser) {
      saveUnchangedContentsForModifiedFiles();
    }

    doDisable();

    if (PerforceManager.getInstance(myProject).isActive()) {
      myOfflineNotification.ensureNotify(myOfflineNotification);
      PerforceManager.getInstance(myProject).configurationChanged();
    }
  }

  private void doDisable() {
    if (ENABLED) {
      LOG.info("Perforce going offline");
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("doDisable, " + ENABLED + "\n" + DebugUtil.currentStackTrace());
    }

    ENABLED = false;
  }

  private void saveUnchangedContentsForModifiedFiles() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      for (LocalChangeList list : ChangeListManager.getInstance(myProject).getChangeLists()) {
        for (Change change : list.getChanges()) {
          final VirtualFile file = change.getVirtualFile();
          final ContentRevision before = change.getBeforeRevision();
          if (file != null && before != null && !LastUnchangedContentTracker.hasSavedContent(file)) {
            try {
              final String content = before.getContent();
              if (content != null) {
                LastUnchangedContentTracker.forceSavedContent(file, content);
              }
            }
            catch (VcsException e) {
              LOG.info(e);
            }
          }
        }
      }
    }, PerforceBundle.message("file.caching.contents"), true, myProject);
  }

  public void enable() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("enable, " + ENABLED + "\n" + DebugUtil.currentStackTrace());
    }

    ENABLED = true;
    final PerforceManager perforceManager = PerforceManager.getInstance(myProject);
    if (!perforceManager.isActive()) return;

    PerforceConnectionManager.getInstance(myProject).updateConnections();

    if (!PerforceLoginManager.getInstance(myProject).checkAndRepairAll() || !ENABLED) {
      LOG.debug("enable failed 1 ");

      doDisable();
      myOfflineNotification.ensureNotify(myOfflineNotification);
      return;
    }

    final VcsException[] exc = {null};

    final Runnable process = () -> {
      final Collection<P4Connection> allConnections = getAllConnections();
      if (allConnections.isEmpty()) return;
      try {
        perforceManager.getClientRoots(allConnections.iterator().next());
      }
      catch (VcsException e) {
        LOG.info(e);
        exc[0] = e;
      }
    };
    if (ProgressManager.getInstance().runProcessWithProgressSynchronously(process, PerforceBundle.message("connection.going.online"), true, myProject)) {
      if (exc[0] != null && !ApplicationManager.getApplication().isUnitTestMode()) {
        Messages.showWarningDialog(myProject, exc[0].getMessage(), PerforceBundle.message("connection.cannot.connect"));
        doDisable();
        myOfflineNotification.ensureNotify(myOfflineNotification);
        return;
      }

      // could have changed when checking authorization
      if (ENABLED) {
        VcsOperationLog.getInstance(myProject).replayLog();
        if (!myOfflineNotification.isEmpty()) {
          myOfflineNotification.clear();
        }
        myProject.getMessageBus().syncPublisher(OFFLINE_MODE_EXITED).run();
      }
      else {
        LOG.debug("enable failed 2");
        myOfflineNotification.ensureNotify(myOfflineNotification);
      }
    }
  }

  @Nullable
  public P4Connection getConnectionForFile(final File file) {
    return PerforceConnectionManager.getInstance(getProject()).getConnectionForFile(file);
  }

  @Nullable
  public P4Connection getConnectionForFile(final VirtualFile file) {
    return PerforceConnectionManager.getInstance(getProject()).getConnectionForFile(file);
  }

  /**
   * For cases when connection working dir doesn't matter and all connections with the same server/client/user can be treated as one
   */
  public Map<ConnectionKey, P4Connection> getConnectionsByKeys() {
    final Map<ConnectionKey, P4Connection> key2connection = new HashMap<>();
    for (P4Connection connection : getAllConnections()) {
      key2connection.put(connection.getConnectionKey(), connection);
    }
    return key2connection;
  }


  public Collection<P4Connection> getAllConnections() {
    return Collections
      .unmodifiableCollection(new HashSet<>(PerforceConnectionManager.getInstance(getProject()).getAllConnections().values()));
  }

  public ParticularConnectionSettings getSettings(@NotNull final P4Connection connection) {
    ConnectionId id = connection.getId();
    if (!myConnectionSettings.containsKey(id)) {
      myConnectionSettings.put(id, new ParticularConnectionSettings());
    }
    return myConnectionSettings.get(id);
  }

  public boolean isNoneCharset() {
    return StringUtil.isEmptyOrSpaces(CHARSET) || getCharsetNone().equals(CHARSET);
  }

  @Override
  public PerforceSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull PerforceSettings object) {
    XmlSerializerUtil.copyBean(object, this);
  }

  public void setCanGoOffline(boolean canGoOffline) {
    myCanGoOffline = canGoOffline;
  }

  @Override
  public int getServerTimeout() {
    return SERVER_TIMEOUT;
  }

  @NotNull
  @Override
  public String getCharsetName() {
    return CHARSET;
  }

  public static @NlsSafe String getCharsetNone() {
    return PerforceBundle.message("none.charset.presentation");
  }
}
