// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.coverage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.StreamEventHandler;
import com.intellij.javascript.nodejs.execution.NodeTargetRun;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.NioFiles;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

public class KarmaCoveragePeer {

  private final Path myCoverageTempDir;
  private volatile KarmaCoverageSession myActiveCoverageSession;

  public KarmaCoveragePeer() throws IOException {
    myCoverageTempDir = Files.createTempDirectory("karma-intellij-coverage-");
  }

  public @NotNull Path getCoverageTempDir() {
    return myCoverageTempDir;
  }

  public void startCoverageSession(@NotNull KarmaCoverageSession coverageSession) {
    // clear directory
    if (Files.isDirectory(myCoverageTempDir)) {
      try {
        List<Path> children = NioFiles.list(myCoverageTempDir);
        for (Path child : children) {
          NioFiles.deleteRecursively(child);
        }
      }
      catch (IOException e) {
        Logger.getInstance(KarmaCoveragePeer.class).warn("Failed to delete content of " + myCoverageTempDir);
      }
    }
    else {
      try {
        Files.createDirectory(myCoverageTempDir);
      }
      catch (IOException e) {
        Logger.getInstance(KarmaCoveragePeer.class).warn("Failed to create " + myCoverageTempDir);
      }
    }
    myActiveCoverageSession = coverageSession;
  }

  public void registerEventHandlers(final @NotNull KarmaServer server) {
    server.registerStreamEventHandler(new StreamEventHandler() {
      @Override
      public @NotNull String getEventType() {
        return "coverageFinished";
      }

      @Override
      public void handle(@NotNull JsonElement eventBody) {
        KarmaCoverageSession coverageSession = myActiveCoverageSession;
        myActiveCoverageSession = null;
        if (coverageSession != null) {
          JsonObject coverageData = ObjectUtils.tryCast(eventBody, JsonObject.class);
          String lcovFilePath = JsonUtil.getChildAsString(coverageData, "lcovFilePath");
          String projectRoot = JsonUtil.getChildAsString(coverageData, "projectRoot");
          KarmaCoverageResultPaths coverageResultPaths = null;
          if (StringUtil.isNotEmpty(lcovFilePath) && StringUtil.isNotEmpty(projectRoot)) {
            Path localLcovFilePath = convertTargetPathToLocal(server.getTargetRun(), lcovFilePath, "lcovFilePath");
            Path localProjectRoot = convertTargetPathToLocal(server.getTargetRun(), projectRoot, "projectRoot");
            if (localLcovFilePath != null && localProjectRoot != null) {
              coverageResultPaths = new KarmaCoverageResultPaths(localLcovFilePath, localProjectRoot);
            }
          }
          coverageSession.onCoverageSessionFinished(coverageResultPaths);
        }
      }
    });
  }

  private static @Nullable Path convertTargetPathToLocal(@NotNull NodeTargetRun targetRun,
                                                         @NotNull String targetFilePath,
                                                         @NotNull String pathName) {
    String localPath;
    try {
      localPath = targetRun.convertTargetPathToLocalPath(targetFilePath);
    }
    catch (IllegalArgumentException e) {
      Logger.getInstance(KarmaCoveragePeer.class).warn("Cannot convert " + pathName, e);
      return null;
    }
    Path path;
    try {
      path = Path.of(localPath);
    }
    catch (InvalidPathException e) {
      Logger.getInstance(KarmaCoveragePeer.class).warn("Cannot find path for " + localPath + " (" + pathName + ")", e);
      return null;
    }
    if (!Files.exists(path)) {
      Logger.getInstance(KarmaCoveragePeer.class).warn("File " + path + " doesn't exists (" + pathName + ")");
      return null;
    }
    return path;
  }
}
