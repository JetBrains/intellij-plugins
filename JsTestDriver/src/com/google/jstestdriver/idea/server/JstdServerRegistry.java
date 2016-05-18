package com.google.jstestdriver.idea.server;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.UIUtil;
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
    final AsyncPromise<JstdServer> promise = new AsyncPromise<JstdServer>();
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

  @NotNull
  private Promise<JstdServer> doStart(@NotNull final JstdServerSettings settings, @NotNull final AsyncPromise<JstdServer> promise) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        final JstdServer server = new JstdServer(settings);
        UIUtil.invokeLaterIfNeeded(() -> {
          myServer = server;
          promise.setResult(server);
        });
      }
      catch (final Exception e) {
        UIUtil.invokeLaterIfNeeded(() -> promise.setError(e));
      }
    });
    return promise;
  }

  @NotNull
  public static JstdServerRegistry getInstance() {
    return INSTANCE;
  }

}
