package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionProblemsNotifier;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Either synchronous or queued
 */
@Service
public final class PerforceBaseInfoWorker {
  private final static Logger LOG = Logger.getInstance(PerforceBaseInfoWorker.class);

  private long myLastValidTime = -1;
  private final PerforceConnectionManagerI myConnectionManager;
  private final PerforceSettings mySettings;
  private PerforceClientRootsChecker myChecker = new PerforceClientRootsChecker();

  /**
   * Sometimes the cache is reset while a background thread still works with previous connections and
   * wants to get their info. But if the connections have been recreated, the map won't contain old connections.
   * In this case, 'p4 info' is run explicitly for the given connection and cached in this map. Since this
   * "old" connection is unlikely to be needed after all current bg processes have finished, the map is weak.
   */
  private Map<P4Connection, ConnectionInfo> myInfos = ContainerUtil.createWeakMap();
  private final Object myInfoLock = new Object();
  private final Object myRefreshLock = new Object();
  private boolean myIsDirty;
  private boolean myStop;
  private final PerforceConnectionProblemsNotifier myNotifier;
  private final PerforceLoginManager myLoginManager;
  // todo don't like it
  private volatile boolean myInsideRefresh;

  public PerforceBaseInfoWorker(@NotNull Project project) {
    myNotifier = project.getService(PerforceConnectionProblemsNotifier.class);
    myLoginManager = PerforceLoginManager.getInstance(project);
    myLoginManager.addSuccessfulLoginListener(() -> {
      synchronized (myInfoLock) {
        if (myChecker.hasNotAuthorized()) {
          scheduleRefresh();
        }
      }
    });
    myConnectionManager = project.getService(PerforceConnectionManagerI.class);
    mySettings = PerforceSettings.getSettings(project);
  }

  private Map<P4Connection, ConnectionInfo> refreshInfo() {
    if (! mySettings.ENABLED) return Collections.emptyMap();

    final Map<P4Connection, ConnectionInfo> old;
    synchronized (myInfoLock) {
      old = myInfos;
      if (!myIsDirty) return old;
    }

    PerforceInfoAndClient.RefreshInfo refreshInfo;
    synchronized (myRefreshLock) {
      myInsideRefresh = true;
      try {
        refreshInfo = doRefreshInfo(old);
      } finally {
        myInsideRefresh = false;
      }
    }

    return refreshInfo.newInfo;
  }

  private PerforceInfoAndClient.RefreshInfo doRefreshInfo(Map<P4Connection, ConnectionInfo> old) {
    final Map<VirtualFile, P4Connection> allConnections = myConnectionManager.getAllConnections();
    PerforceInfoAndClient.RefreshInfo refreshInfo = recalculateInfo(allConnections, old);
    PerforceClientRootsChecker checker = new PerforceClientRootsChecker(refreshInfo.newInfo, allConnections);

    synchronized (myInfoLock) {
      myIsDirty = false;
      myInfos = ContainerUtil.createWeakMap();
      myInfos.putAll(refreshInfo.newInfo);
      myChecker = checker;
      if (!refreshInfo.hasAnyErrorsBesidesAuthentication) {
        myLastValidTime = System.currentTimeMillis();
      }
    }
    LOG.debug("info+client calculated: " + refreshInfo);

    notifyAboutErrors(checker, refreshInfo);
    return refreshInfo;
  }

  private PerforceInfoAndClient.RefreshInfo recalculateInfo(Map<VirtualFile, P4Connection> allConnections, Map<P4Connection, ConnectionInfo> old) {
    Project project = mySettings.getProject();
    PerforceRunner runner = PerforceRunner.getInstance(project);
    ClientRootsCache cache = ClientRootsCache.getClientRootsCache(project);
    return PerforceInfoAndClient.recalculateInfos(old, allConnections.values(), runner, cache);
  }

  private void notifyAboutErrors(PerforceClientRootsChecker checker, final PerforceInfoAndClient.RefreshInfo refreshInfo) {
    if (checker.isServerUnavailable()) {
      myNotifier.setProblems(true, true);
    } else {
      myNotifier.setProblems(false, checker.hasAnyErrors() || refreshInfo.hasAnyErrorsBesidesAuthentication);
    }
    if (checker.hasNotAuthorized()) {
      for (P4Connection connection : checker.getNotAuthorized()) {
        myLoginManager.getNotifier().ensureNotify(connection);
      }
    }
  }

  public void scheduleRefresh() {
    if (myInsideRefresh) return;
    LOG.debug("Schedule refresh: ", new Throwable());
    synchronized (myInfoLock) {
      myLastValidTime = -1;
      myIsDirty = true;
    }
  }

  @Nullable
  public Map<String, List<String>> getInfo(P4Connection connection) throws VcsException {
    ConnectionInfo info = getInfoAndClient(connection);
    return info == null ? null : info.getInfo();
  }

  @Nullable
  public ClientData getClient(P4Connection connection) throws VcsException {
    ConnectionInfo info = getInfoAndClient(connection);
    return info == null ? null : info.getClient();
  }

  @Nullable
  public Map<String, List<String>> getCachedInfo(P4Connection connection) throws VcsException {
    synchronized (myInfoLock) {
      if (myStop) return null;
      ConnectionInfo info = connection == null ? null : myInfos.get(connection);
      if (info == null) {
        LOG.debug("No info for " + connection + "; infos=" + myInfos);
        return null;
      }
      return info.getInfo();
    }
  }

  @Nullable
  public ClientData getCachedClient(P4Connection connection) throws VcsException {
    synchronized (myInfoLock) {
      if (myStop) return null;
      ConnectionInfo info = connection == null ? null : myInfos.get(connection);
      return info == null ? null : info.getClient();
    }
  }

  @Nullable
  private ConnectionInfo getInfoAndClient(P4Connection connection) throws VcsException {
    ConnectionInfo info;
    boolean dirty;
    synchronized (myInfoLock) {
      if (myStop) return null;

      info = connection == null ? null : myInfos.get(connection);
      dirty = myIsDirty;
      if (!dirty && info != null) {
        return info;
      }
    }

    if (dirty) {
      info = refreshInfo().get(connection);
    }
    if (info == null) {
      Project project = mySettings.getProject();
      info = PerforceInfoAndClient.calcInfo(connection, PerforceRunner.getInstance(project), ClientRootsCache.getClientRootsCache(project));
      synchronized (myInfoLock) {
        myInfos.put(connection, info);
      }
    }

    return info;
  }

  public void stop() {
    LOG.debug("stop", new Throwable());
    synchronized (myInfoLock) {
      myStop = true;
    }
  }

  public void start() {
    LOG.debug("start", new Throwable());
    synchronized (myInfoLock) {
      myStop = false;
      myLastValidTime = -1;
    }
  }

  public long getLastValidTime() {
    synchronized (myInfoLock) {
      return myLastValidTime;
    }
  }

  public P4RootsInformation getCheckerResults() {
    synchronized (myInfoLock) {
      return new P4RootsInformationHolder(myChecker.getErrors(), myChecker.getMap(), myChecker.getNotAuthorized());
    }
  }
}
