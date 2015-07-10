package com.intellij.javascript.karma.coverage;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.StreamEventHandler;
import com.intellij.javascript.karma.util.GsonUtil;
import com.intellij.javascript.karma.util.NodeInstalledPackage;
import com.intellij.javascript.karma.util.NodeInstalledPackagesLocator;
import com.intellij.javascript.nodejs.NodePathSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
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
  private List<KarmaCoverageInitializationCallback> myListeners = Lists.newCopyOnWriteArrayList();
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
  public void onCoverageInitialized(@NotNull KarmaCoverageInitializationCallback callback) {
    if (myStartupStatus != null) {
      callback.onCoverageInitialized(myStartupStatus);
    }
    else {
      final int timeoutMillis = 10000;
      final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
      alarm.addRequest(new Runnable() {
        @Override
        public void run() {
          if (myStartupStatus == null) {
            if (myDisposed) {
              LOG.info("Karma coverage was already disposed");
            }
            else {
              LOG.error("Karma coverage hasn't been initialized in " + timeoutMillis + " ms");
              myListeners.clear();
            }
          }
          Disposer.dispose(alarm);
        }
      }, timeoutMillis, ModalityState.any());
      myListeners.add(callback);
    }
  }

  private void fireOnCoverageInitialized(@NotNull final KarmaCoverageStartupStatus startupStatus) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        myStartupStatus = startupStatus;
        for (KarmaCoverageInitializationCallback listener : myListeners) {
          listener.onCoverageInitialized(startupStatus);
        }
        myListeners.clear();
      }
    });
  }

  private void onCoverageInitialized(@NotNull final KarmaServer server,
                                     boolean coveragePreprocessorSpecifiedInConfig,
                                     boolean coverageReporterFound) {
    // optimization: if 'coveragePreprocessorSpecifiedInConfig' is false, report about it and skip karma-coverage plugin checking
    if (!coveragePreprocessorSpecifiedInConfig || coverageReporterFound) {
      fireOnCoverageInitialized(new KarmaCoverageStartupStatus(coveragePreprocessorSpecifiedInConfig,
                                                               coverageReporterFound,
                                                               true));
    }
    else {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
              checkCoveragePlugin(server);
            }
          });
        }
      });
    }
  }

  private void checkCoveragePlugin(@NotNull KarmaServer server) {
    NodeInstalledPackagesLocator locator = NodeInstalledPackagesLocator.getInstance();
    NodePathSettings nodeSettings = new NodePathSettings(server.getNodeInterpreterPath());
    NodeInstalledPackage pkg = locator.findInstalledPackages("karma-coverage", server.getKarmaPackageDir(), nodeSettings);
    fireOnCoverageInitialized(new KarmaCoverageStartupStatus(true, false, pkg != null));
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
          if (path != null) {
            coverageSession.onCoverageSessionFinished(new File(path));
          }
        }
      }
    });
    server.registerStreamEventHandler(new StreamEventHandler() {

      private AtomicBoolean myCoverageInitialized = new AtomicBoolean(true);
      private static final String COVERAGE_PREPROCESSOR_SPECIFIED_IN_CONFIG = "coveragePreprocessorSpecifiedInConfig";
      private static final String COVERAGE_REPORTER_FOUND = "coverageReporterFound";

      @NotNull
      @Override
      public String getEventType() {
        return "coverageStartupStatus";
      }

      @Override
      public void handle(@NotNull JsonElement eventBody) {
        if (myCoverageInitialized.compareAndSet(true, false)) {
          Boolean coverageReporterFound = null;
          Boolean coveragePreprocessorSpecifiedInConfig = null;
          if (eventBody.isJsonObject()) {
            JsonObject eventObj = eventBody.getAsJsonObject();
            coveragePreprocessorSpecifiedInConfig = GsonUtil.getBooleanProperty(eventObj, COVERAGE_PREPROCESSOR_SPECIFIED_IN_CONFIG);
            coverageReporterFound = GsonUtil.getBooleanProperty(eventObj, COVERAGE_REPORTER_FOUND);
          }
          if (coveragePreprocessorSpecifiedInConfig == null) {
            warnAboutMissingProperty(COVERAGE_PREPROCESSOR_SPECIFIED_IN_CONFIG);
            coveragePreprocessorSpecifiedInConfig = true;
          }
          if (coverageReporterFound == null) {
            warnAboutMissingProperty(COVERAGE_REPORTER_FOUND);
            coverageReporterFound = true;
          }
          onCoverageInitialized(server, coveragePreprocessorSpecifiedInConfig, coverageReporterFound);
        }
      }

      private void warnAboutMissingProperty(@NotNull String propertyName) {
        LOG.warn("Malformed event '" + getEventType() + "': can not found boolean property '" + propertyName + "'!");
      }
    });
  }

}
