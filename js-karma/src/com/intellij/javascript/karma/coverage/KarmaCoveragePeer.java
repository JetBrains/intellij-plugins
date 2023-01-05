// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.coverage;

import com.google.gson.JsonElement;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.StreamEventHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class KarmaCoveragePeer {

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
      File[] children = ObjectUtils.notNull(myCoverageTempDir.listFiles(), ArrayUtilRt.EMPTY_FILE_ARRAY);
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
            try {
              path = server.getTargetRun().convertTargetPathToLocalPath(path);
            }
            catch (IllegalArgumentException e) {
              Logger.getInstance(KarmaCoveragePeer.class).warn("Cannot read coverage file", e);
            }
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
