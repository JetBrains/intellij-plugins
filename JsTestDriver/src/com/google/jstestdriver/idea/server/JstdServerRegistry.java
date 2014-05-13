package com.google.jstestdriver.idea.server;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.CatchingConsumer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdServerRegistry {

  private static final JstdServerRegistry INSTANCE = new JstdServerRegistry();

  private JstdServer myServer;

  @Nullable
  public JstdServer getServer() {
    return myServer;
  }

  public void restartServer(@NotNull final JstdServerSettings settings, @NotNull final CatchingConsumer<JstdServer, Exception> consumer) {
    JstdServer server = myServer;
    if (server != null && server.isProcessRunning()) {
      server.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
        @Override
        public void onServerStopped() {
          myServer = null;
          doStart(settings, consumer);
        }
      }, ApplicationManager.getApplication());
      server.shutdownAsync();
    }
    else {
      doStart(settings, consumer);
    }
  }

  private void doStart(@NotNull final JstdServerSettings settings, @NotNull final CatchingConsumer<JstdServer, Exception> consumer) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          final JstdServer server = new JstdServer(settings);
          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
              myServer = server;
              consumer.consume(server);
            }
          });
        }
        catch (final Exception e) {
          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
              consumer.consume(e);
            }
          });
        }
      }
    });
  }

  @NotNull
  public static JstdServerRegistry getInstance() {
    return INSTANCE;
  }

}
