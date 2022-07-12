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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ClientVersion;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;
import org.jetbrains.idea.perforce.util.tracer.LongCallsParameters;
import org.jetbrains.idea.perforce.util.tracer.TracerManager;
import org.jetbrains.idea.perforce.util.tracer.TracerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public final class PerforceManager  {
  private final Project myProject;
  private final PerforceLoginManager myLoginManager;

  private static final Logger LOG = Logger.getInstance(PerforceManager.class);
  private static final Logger LOG_RELATIVE_PATH = Logger.getInstance("#Log_relative_path");
  private static final Logger TRACER_LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceManager_TRACER");

  private final Map<P4Connection, PerforceClient> myClientMap = new HashMap<>();

  private final static boolean ourTraceCalls = Boolean.TRUE.equals(Boolean.getBoolean("perforce.trace.calls"));
  private final static String ourTracerProperties = System.getProperty("perforce.trace.calls.properties");
  private TracerManager<P4Command> myTracer;

  private final ClientRootsCache myClientRootsCache;
  private final PerforceBaseInfoWorker myPerforceBaseInfoWorker;

  private final VirtualFileListener myListener;

  private volatile ClientVersion myClientVersion;
  private volatile boolean myActive;
  private final LocalFileSystem myLfs;
  private final PerforceShelf myShelf;

  public static PerforceManager getInstance(Project project) {
    return project.getService(PerforceManager.class);
  }

  public PerforceManager(@NotNull Project project) {
    myProject = project;
    myLoginManager = PerforceLoginManager.getInstance(project);
    myListener = new VirtualFileListener() {
      @Override
      public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        if (!event.isFromRefresh()) return;
        if (event.getPropertyName().equals(VirtualFile.PROP_WRITABLE)) {
          final boolean wasWritable = ((Boolean)event.getOldValue()).booleanValue();
          if (wasWritable) {
            event.getFile().putUserData(P4File.KEY, null);
          }
        }
      }

      @Override
      public void contentsChanged(@NotNull VirtualFileEvent event) {
        if (!event.isFromRefresh()) return;
        if (!event.getFile().isWritable()) {
          event.getFile().putUserData(P4File.KEY, null);
        }
      }
    };

    myClientRootsCache = project.getService(ClientRootsCache.class);
    myPerforceBaseInfoWorker = project.getService(PerforceBaseInfoWorker.class);
    if (ourTraceCalls) {
      myTracer = createTracer();
    }
    myLfs = LocalFileSystem.getInstance();
    myShelf = new PerforceShelf(project);
  }

  private static TracerManager<P4Command> createTracer() {
    if (ourTracerProperties != null) {
      final Properties properties = new Properties();
      try {
        properties.load(new FileInputStream(ourTracerProperties));

        final boolean logAverageTimes = Boolean.TRUE.equals(Boolean.parseBoolean(properties.getProperty(TracerProperties.GATHER_AVERAGE_TIMES)));
        final TracerParameters averageParameters;
        if (logAverageTimes) {
          averageParameters = new TracerParameters(TracerProperties.averageTimesInterval.getValue(properties),
                                                   (int) TracerProperties.averageTimesQueueSize.getValue(properties));
        } else {
          averageParameters = null;
        }
        final boolean logConcurrentThreads = Boolean.TRUE.equals(Boolean.parseBoolean(properties.getProperty(TracerProperties.GATHER_CONCURRENT_THREADS)));
        final TracerParameters concurrentThreadsParameters;
        if (logConcurrentThreads) {
          concurrentThreadsParameters = new TracerParameters(TracerProperties.numberConcurrentThreadsInterval.getValue(properties),
                                                             (int) TracerProperties.numberConcurrentThreadsQueueSize.getValue(properties));
        } else {
          concurrentThreadsParameters = null;
        }
        final boolean logLongCalls = Boolean.TRUE.equals(Boolean.parseBoolean(properties.getProperty(TracerProperties.GATHER_LONG_CALLS)));
        final LongCallsParameters longCallsParameters;
        if (logLongCalls) {
          longCallsParameters = new LongCallsParameters(TracerProperties.longCallsInterval.getValue(properties),
                                                        (int) TracerProperties.longCallsQueueSize.getValue(properties),
                                                        (int) TracerProperties.longCallsMaxKept.getValue(properties),
                                                        TracerProperties.longCallsLowerBound.getValue(properties));
        } else {
          longCallsParameters = null;
        }
        return new TracerManager<>(averageParameters, concurrentThreadsParameters, longCallsParameters, TRACER_LOG,
                                   TracerProperties.outputInterval.getValue(properties));
      }
      catch (IOException e) {
        //
      }
    }
    return new TracerManager<>(new TracerParameters(TracerProperties.averageTimesInterval.getDefault(),
                                                    (int)TracerProperties.averageTimesQueueSize.getDefault()),
                               new TracerParameters(TracerProperties.numberConcurrentThreadsInterval.getDefault(),
                                                    (int)TracerProperties.numberConcurrentThreadsQueueSize.getDefault()),
                               new LongCallsParameters(TracerProperties.longCallsInterval.getDefault(),
                                                       (int)TracerProperties.longCallsQueueSize.getDefault(),
                                                       (int)TracerProperties.longCallsMaxKept.getDefault(),
                                                       TracerProperties.longCallsLowerBound.getDefault()),
                               TRACER_LOG, TracerProperties.outputInterval.getDefault());
  }

  @Nullable
  public ClientVersion getClientVersion() {
    ClientVersion version = myClientVersion;
    if (version == null) {
      myClientVersion = version = PerforceRunner.getInstance(myProject).getClientVersion();
    }
    return version;
  }

  public void startListening(@NotNull Disposable parentDisposable) {
    myActive = true;
    VirtualFileManager.getInstance().addVirtualFileListener(myListener, parentDisposable);
    myLoginManager.startListening(parentDisposable);
    myPerforceBaseInfoWorker.start();

    Disposer.register(parentDisposable, () -> {
      myActive = false;
      myPerforceBaseInfoWorker.stop();
    });
  }

  @NotNull Map<String, List<String>> getCachedInfo(P4Connection connection) throws VcsException {
    final Map<String, List<String>> info = myPerforceBaseInfoWorker.getInfo(connection);
    if (info == null) {
      ProgressManager.checkCanceled();
      throw new VcsException(PerforceBundle.message("error.info.is.not.available"));
    }
    return info;
  }

  @NotNull ClientData getCachedClients(@Nullable P4Connection connection) throws VcsException {
    ClientData client = myPerforceBaseInfoWorker.getClient(connection);
    if (client == null) {
      ProgressManager.checkCanceled();
      throw new VcsException(PerforceBundle.message("error.info.is.not.available"));
    }
    return client;
  }

  @Nullable
  private Map<String, List<String>> getInfoOnlyCached(final P4Connection connection) throws VcsException {
    return myPerforceBaseInfoWorker.getCachedInfo(connection);
  }

  @Nullable ClientData getClientOnlyCached(final P4Connection connection) throws VcsException {
    return myPerforceBaseInfoWorker.getCachedClient(connection);
  }

  // todo: wrong. we should take all roots, since we can belong to any
  @Nullable
  public String getClientRoot(@Nullable final P4Connection connection) throws VcsException {
    return ContainerUtil.getFirstItem(getClientRoots(connection));
  }

  @NotNull
  public List<String> getClientRoots(@Nullable P4Connection connection) throws VcsException {
    return ContainerUtil.filter(getCachedClients(connection).getAllRoots(), mainRootValue -> {
      File file = new File(mainRootValue);
      VirtualFile vf = myLfs.findFileByIoFile(file);
      return vf != null && vf.isDirectory() || PerforceClientRootsChecker.isDirectory(file);
    });
  }

  public long getServerVersionYear(@Nullable final P4Connection connection) throws VcsException {
    final Map<String, List<String>> map = getCachedInfo(connection);
    final List<String> serverVersions = map.get(PerforceRunner.SERVER_VERSION);
    if (serverVersions == null || serverVersions.isEmpty()) return -1;
    return OutputMessageParser.parseServerVersion(serverVersions.get(0)).getVersionYear();
  }

  public long getServerVersionYearCached(@Nullable final P4Connection connection) throws VcsException {
    final Map<String, List<String>> map = getInfoOnlyCached(connection);
    if (map == null) return -1;
    final List<String> serverVersions = map.get(PerforceRunner.SERVER_VERSION);
    if (serverVersions == null || serverVersions.isEmpty()) return -1;
    return OutputMessageParser.parseServerVersion(serverVersions.get(0)).getVersionYear();
  }

  @Nullable
  public ServerVersion getServerVersion(@Nullable final P4Connection connection) throws VcsException {
    final List<String> serverVersions = getCachedInfo(connection).get(PerforceRunner.SERVER_VERSION);
    if (serverVersions == null || serverVersions.isEmpty()) return null;
    return OutputMessageParser.parseServerVersion(serverVersions.get(0));
  }

  public boolean isUnderPerforceRoot(@NotNull final VirtualFile virtualFile) throws VcsException {
    final P4Connection connection = PerforceSettings.getSettings(myProject).getConnectionForFile(virtualFile);
    return getClientRoots(connection).stream().anyMatch(path -> isUnderClientRoot(virtualFile, path));
  }

  private boolean isUnderClientRoot(final VirtualFile virtualFile, final String path) {
    final Application application = ApplicationManager.getApplication();
    return application.runReadAction((Computable<Boolean>)() -> {
      final VirtualFile root = myLfs.findFileByIoFile(new File(path));
      if (root != null && ((Comparing.equal(root, virtualFile)) || VfsUtilCore.isAncestor(root, virtualFile, false))) {
        return true;
      }
      return false;
    });
  }

  @Nullable
  private static String getRelativePath(String filePath, PerforceClient client) throws VcsException {
    return View.getRelativePath(P4File.unescapeWildcards(filePath), client.getName(), client.getViews());
  }

  @Nullable
  public static File getFileByDepotName(final String depotPath, PerforceClient client) throws VcsException {

    int revNumStart = depotPath.indexOf("#");

    final String relativePath;

    if (revNumStart >= 0) {
      relativePath = getRelativePath(depotPath.substring(0, revNumStart), client);

    }
    else {
      relativePath = getRelativePath(depotPath, client);
    }

    if (relativePath == null)  {
      if (LOG.isDebugEnabled()) {
        LOG.debug(missingLocalFileDiagnostics(depotPath, client));
      }

      return null;
    }

    Project project = client.getProject();
    List<String> roots = client.getRoots();
    List<File> resultCandidates = roots.isEmpty() ? Collections.singletonList(new File(FileUtil.toSystemDependentName(relativePath.trim())))
                                                  : ContainerUtil.map(roots, clientRoot -> new File(clientRoot, relativePath.trim()));
    File resultInProject = ContainerUtil.find(resultCandidates, result -> project == null || PerforceConnectionManager.getInstance(project).isUnderProjectConnections(result));
    if (LOG_RELATIVE_PATH.isDebugEnabled()) {
      LOG_RELATIVE_PATH.debug("depot: '" + depotPath + "' result: '" + resultInProject + "'" + (resultInProject == null ? " checked " + resultCandidates : ""));
    }
    return resultInProject;
  }

  private static String missingLocalFileDiagnostics(String depotPath, PerforceClient client) throws VcsException {
    final StringBuilder message = new StringBuilder();
    for (View view : client.getViews()) {
      message.append('\n');
      message.append("View ");
      message.append(view.toString());
    }
    message.append('\n');
    message.append("Cannot find local file for depot path: ").append(depotPath);
    return message.toString();
  }

  @NotNull
  public synchronized PerforceClient getClient(@NotNull final P4Connection connection) {
    PerforceClient client = myClientMap.get(connection);
    if (client == null) {
      client = new PerforceClientImpl(myProject, connection);
      myClientMap.put(connection, client);
    }
    return client;
  }

  public void configurationChanged() {
    //noinspection SynchronizeOnThis
    synchronized (this) {
      myClientMap.clear();
    }

    if (! myActive) return;
    myLoginManager.clearAll();
    clearInfoClientCache();
    ApplicationManager.getApplication().invokeLater(() -> {
      P4File.invalidateFstat(myProject);
      VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    }, myProject.getDisposed());
  }

  public void clearInfoClientCache() {
    myPerforceBaseInfoWorker.scheduleRefresh();
  }

  @Contract("null->null; !null->!null")
  @Nullable public String getRawRoot(@Nullable final String convertedRoot) {
    return myClientRootsCache.getRaw(convertedRoot);
  }

  public String convertP4ParsedPath(@Nullable String convertedClientRoot, @NotNull String s) {
    final String result = myClientRootsCache.convertPath(convertedClientRoot, s);
    LOG_RELATIVE_PATH.debug("convertion, s: '" + s + "' converted: '" + result + "' convertedRoot: '" + convertedClientRoot + "'");
    return result;
  }

  @Nullable
  public Object traceEnter(final P4Command command, final String commandPresentation) {
    if (myTracer != null) {
      return myTracer.start(command, commandPresentation);
    }
    return null;
  }

  public void traceExit(final Object context, final P4Command command, final String commandPresentation) {
    if (myTracer != null) {
      myTracer.stop(context, command, commandPresentation);
    }
  }

  public boolean isTraceEnabled() {
    return ourTraceCalls;
  }

  public void resetClientVersion() {
    myClientVersion = null;
  }

  public boolean isActive() {
    return myActive;
  }

  public static void ensureValidClient(@NotNull Project project, @NotNull P4Connection connection) throws VcsException {
    PerforceClient client = getInstance(project).getClient(connection);
    if (client.getName() == null) {
      throw new VcsException(PerforceBundle.message("error.missing.perforce.workspace"));
    }
    if (client.getUserName() == null) {
      throw new VcsException(PerforceBundle.message("error.missing.perforce.user.name"));
    }
    if (client.getServerPort() == null) {
      throw new VcsException(PerforceBundle.message("error.missing.perforce.server.port"));
    }
  }

  public PerforceShelf getShelf() {
    return myShelf;
  }
}
