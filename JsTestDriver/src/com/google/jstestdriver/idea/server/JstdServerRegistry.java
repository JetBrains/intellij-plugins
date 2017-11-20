package com.google.jstestdriver.idea.server;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

public class JstdServerRegistry {

  private static final JstdServerRegistry INSTANCE = new JstdServerRegistry();

  private JstdServer myServer;

  @Nullable
  public JstdServer getServer() {
    return myServer;
  }

  @NotNull
  public Promise<JstdServer> restartServer(@NotNull final JstdServerSettings settings) {
    JstdServer server = myServer;
    final AsyncPromise<JstdServer> promise = new AsyncPromise<>();
    if (server != null && server.isProcessRunning()) {
      server.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
        @Override
        public void onServerStopped() {
          myServer = null;
          doStart(settings, promise);
        }
      }, ApplicationManager.getApplication());
      server.shutdownAsync();
      return promise;
    }
    else {
      doStart(settings, promise);
    }
    return promise;
  }

  private void doStart(@NotNull JstdServerSettings settings, @NotNull AsyncPromise<JstdServer> promise) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        JstdServer server = new JstdServer(settings);
        ApplicationManager.getApplication().invokeLater(() -> {
          myServer = server;
          promise.setResult(server);
        });
      }
      catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(() -> promise.setError(e));
      }
    });
  }

  @NotNull
  public static JstdServerRegistry getInstance() {
    return INSTANCE;
  }

}
