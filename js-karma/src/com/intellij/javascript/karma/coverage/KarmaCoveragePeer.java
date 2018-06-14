package com.intellij.javascript.karma.coverage;

import com.google.gson.JsonElement;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.StreamEventHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class KarmaCoveragePeer {

  private static final Logger LOG = Logger.getInstance(KarmaCoveragePeer.class);

  private final File myCoverageTempDir;
  private volatile KarmaCoverageSession myActiveCoverageSession;

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
          String path = JsonUtil.getString(eventBody);
          if (path != null) {
            File file = new File(path);
            if (file.isAbsolute() && file.isFile()) {
              coverageSession.onCoverageSessionFinished(file);
            }
            else {
              coverageSession.onCoverageSessionFinished(null);
            }
          }
          else {
            coverageSession.onCoverageSessionFinished(null);
          }
        }
      }
    });
  }
}
