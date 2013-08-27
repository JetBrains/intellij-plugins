package com.intellij.javascript.karma.coverage;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.StreamEventHandler;
import com.intellij.javascript.karma.util.GsonUtil;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoveragePeer {

  private static final Logger LOG = Logger.getInstance(KarmaCoveragePeer.class);

  private final File myCoverageTempDir;
  private volatile KarmaCoverageSession myActiveCoverageSession;
  private KarmaCoverageStartupStatus myStartupStatus;
  private List<KarmaCoverageInitializationListener> myInitListeners = Lists.newCopyOnWriteArrayList();
  private volatile boolean myDisposed = false;

  public KarmaCoveragePeer() throws IOException {
    myCoverageTempDir = FileUtil.createTempDirectory("karma-intellij-coverage-", null);
  }

  @NotNull
  public File getCoverageTempDir() {
    return myCoverageTempDir;
  }

  public void startCoverageSession(@NotNull KarmaCoverageSession coverageSession) {
    // clear directory
    if (myCoverageTempDir.isDirectory()) {
      File[] children = ObjectUtils.notNull(myCoverageTempDir.listFiles(), ArrayUtil.EMPTY_FILE_ARRAY);
      for (File child : children) {
        FileUtil.delete(child);
      }
    }
    else {
      FileUtil.createDirectory(myCoverageTempDir);
    }
    myActiveCoverageSession = coverageSession;
  }

  @Nullable
  public KarmaCoverageStartupStatus getStartupStatus() {
    return myStartupStatus;
  }

  public void dispose() {
    myDisposed = true;
  }

  /**
   * Should be called in EDT
   */
  public void doWhenCoverageInitialized(@NotNull KarmaCoverageInitializationListener listener, @NotNull Disposable parent) {
    if (myStartupStatus != null) {
      listener.onCoverageInitialized(myStartupStatus);
    }
    else {
      final int timeoutMillis = 10000;
      new Alarm(Alarm.ThreadToUse.SWING_THREAD, parent).addRequest(new Runnable() {
        @Override
        public void run() {
          if (myStartupStatus == null) {
            if (myDisposed) {
              LOG.info("Karma coverage was already disposed");
            }
            else {
              LOG.error("Karma coverage hasn't been initialized in " + timeoutMillis + " ms");
              myInitListeners.clear();
            }
          }
        }
      }, timeoutMillis, ModalityState.any());
      myInitListeners.add(listener);
    }
  }

  private void fireOnCoverageInitialized(@NotNull final KarmaCoverageStartupStatus initStatus) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        myStartupStatus = initStatus;
        for (KarmaCoverageInitializationListener listener : myInitListeners) {
          listener.onCoverageInitialized(initStatus);
        }
        myInitListeners.clear();
      }
    });
  }

  private void onCoverageInitialized(@NotNull final KarmaServer server,
                                     boolean coverageReporterSpecifiedInConfig,
                                     boolean coverageReporterFound) {
    if (!coverageReporterSpecifiedInConfig) {
      fireOnCoverageInitialized(new KarmaCoverageStartupStatus(false, true, true));
    }
    if (coverageReporterFound) {
      fireOnCoverageInitialized(new KarmaCoverageStartupStatus(true, true, true));
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
              checkCoveragePlugin(server.getKarmaPackageDir());
            }
          });
        }
      });
    }
  }

  private void checkCoveragePlugin(@NotNull File karmaPackageDir) {
    VirtualFile karmaPackageVirtualDir = VfsUtil.findFileByIoFile(karmaPackageDir, false);
    boolean coveragePluginInstalled = true;
    if (karmaPackageVirtualDir != null && karmaPackageVirtualDir.isValid()) {
      List<CompletionModuleInfo> modules = Lists.newArrayList();
      NodeModuleSearchUtil.findModulesWithName(modules, "karma-coverage", karmaPackageVirtualDir, null, false);
      coveragePluginInstalled = !modules.isEmpty();
    }
    fireOnCoverageInitialized(new KarmaCoverageStartupStatus(true, false, coveragePluginInstalled));
  }

  public void registerEventHandlers(@NotNull final KarmaServer server) {
    server.registerStreamEventHandler(new StreamEventHandler() {
      @NotNull
      @Override
      public String getEventType() {
        return "coverageFinished";
      }

      @Override
      public void handle(@NotNull JsonElement eventBody) {
        KarmaCoverageSession coverageSession = myActiveCoverageSession;
        myActiveCoverageSession = null;
        if (coverageSession != null) {
          String path = GsonUtil.getAsString(eventBody);
          coverageSession.onCoverageSessionFinished(new File(path));
        }
      }
    });
    server.registerStreamEventHandler(new StreamEventHandler() {

      private AtomicBoolean myCoverageInitialized = new AtomicBoolean(true);

      @NotNull
      @Override
      public String getEventType() {
        return "coverageStartupStatus";
      }

      @Override
      public void handle(@NotNull JsonElement eventBody) {
        if (myCoverageInitialized.compareAndSet(true, false)) {
          Boolean coverageReporterFound = null;
          Boolean coverageReporterSpecifiedInConfig = null;
          if (eventBody.isJsonObject()) {
            JsonObject eventObj = eventBody.getAsJsonObject();
            coverageReporterSpecifiedInConfig = GsonUtil.getBooleanProperty(eventObj, "coverageReporterSpecifiedInConfig");
            coverageReporterFound = GsonUtil.getBooleanProperty(eventObj, "coverageReporterFound");
          }
          if (coverageReporterSpecifiedInConfig == null) {
            LOG.warn("Malformed '" + getEventType()
                     + "' event: can not found boolean property 'coverageReporterSpecifiedInConfig'!");
            coverageReporterSpecifiedInConfig = true;
          }
          if (coverageReporterFound == null) {
            LOG.warn("Malformed '" + getEventType()
                     + "' event: can not found boolean property 'coverageReporterFound'!");
            coverageReporterFound = true;
          }
          onCoverageInitialized(server, coverageReporterSpecifiedInConfig, coverageReporterFound);
        }
      }
    });
  }

}
