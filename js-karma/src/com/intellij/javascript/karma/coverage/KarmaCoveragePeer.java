package com.intellij.javascript.karma.coverage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.StreamEventHandler;
import com.intellij.javascript.karma.util.GsonUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoveragePeer {

  private final File myCoverageTempDir;
  private volatile KarmaCoverageSession myActiveCoverageSession;
  private volatile boolean myKarmaCoveragePluginInstalled;

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

  public boolean isKarmaCoveragePluginMissing() {
    return myKarmaCoveragePluginInstalled;
  }

  public void registerEventHandlers(@NotNull KarmaServer server) {
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
          String path = GsonUtil.asString(eventBody);
          coverageSession.onCoverageSessionFinished(new File(path));
        }
      }
    });
    server.registerStreamEventHandler(new StreamEventHandler() {
      @NotNull
      @Override
      public String getEventType() {
        return "coverageAvailability";
      }

      @Override
      public void handle(@NotNull JsonElement eventBody) {
        if (eventBody.isJsonObject()) {
          JsonObject object = eventBody.getAsJsonObject();
          JsonElement installedElement = object.get("plugin-installed");
          if (installedElement != null && installedElement.isJsonPrimitive()) {
            JsonPrimitive installedPrimitive = installedElement.getAsJsonPrimitive();
            if (installedPrimitive.isBoolean()) {
              myKarmaCoveragePluginInstalled = installedPrimitive.getAsBoolean();
            }
          }
          JsonElement reporterElement = object.get("reporter-configured");
          if (reporterElement != null && reporterElement.isJsonPrimitive()) {
            JsonPrimitive installedPrimitive = reporterElement.getAsJsonPrimitive();
            if (installedPrimitive.isBoolean()) {
              myKarmaCoveragePluginInstalled = installedPrimitive.getAsBoolean();
            }
          }
        }
      }
    });
  }

}
