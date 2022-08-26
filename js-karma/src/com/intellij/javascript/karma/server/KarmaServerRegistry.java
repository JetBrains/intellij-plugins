// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.server;

import com.intellij.javascript.karma.execution.KarmaServerSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KarmaServerRegistry {

  private final Project myProject;
  private final ConcurrentMap<String, KarmaServer> myServerByConfigFile = new ConcurrentHashMap<>();
  private final ConcurrentMap<KarmaServerSettings, Promise<KarmaServer>> myServers = new ConcurrentHashMap<>();

  public KarmaServerRegistry(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  public static KarmaServerRegistry getInstance(@NotNull Project project) {
    return project.getService(KarmaServerRegistry.class);
  }

  public @Nullable KarmaServer getServer(@NotNull KarmaServerSettings serverSettings) {
    Promise<KarmaServer> promise = myServers.get(serverSettings);
    if (promise != null && promise.isSucceeded()) {
      try {
        return promise.blockingGet(0);
      }
      catch (Exception e) {
        throw new RuntimeException("Unexpected", e);
      }
    }
    return null;
  }

  public @NotNull Promise<KarmaServer> startServer(@NotNull KarmaServerSettings serverSettings) {
    AsyncPromise<KarmaServer> promise = new AsyncPromise<>();
    Promise<KarmaServer> prevPromise = myServers.putIfAbsent(serverSettings, promise);
    if (prevPromise != null) {
      return prevPromise;
    }
    KarmaServer prevServer = myServerByConfigFile.get(serverSettings.getConfigurationFilePath());
    if (prevServer != null) {
      prevServer.onTerminated(new KarmaServerTerminatedListener() {
        @Override
        public void onTerminated(int exitCode) {
          doStartServer(serverSettings, promise);
        }
      });
      prevServer.shutdownAsync();
    }
    else {
      doStartServer(serverSettings, promise);
    }
    return promise;
  }

  private void doStartServer(@NotNull KarmaServerSettings serverSettings, @NotNull AsyncPromise<KarmaServer> promise) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        KarmaServer server = new KarmaServer(myProject, serverSettings);
        myServerByConfigFile.put(serverSettings.getConfigurationFilePath(), server);
        server.onTerminated(new KarmaServerTerminatedListener() {
          @Override
          public void onTerminated(int exitCode) {
            myServers.remove(serverSettings, promise);
            myServerByConfigFile.remove(serverSettings.getConfigurationFilePath(), server);
          }
        });
        promise.setResult(server);
      }
      catch (Exception e) {
        promise.setError(e);
      }
    });
  }
}
