package com.intellij.javascript.karma.server;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.CatchingConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Sergey Simonchik
 */
public class KarmaServerRegistry {

  private static final Logger LOG = Logger.getInstance(KarmaServerRegistry.class);
  private static ConcurrentMap<String, KarmaServer> myServers = new ConcurrentHashMap<String, KarmaServer>();
  private static ConcurrentMap<String, Object> myStartingServers = new ConcurrentHashMap<String, Object>();

  @Nullable
  public static KarmaServer getServerByConfigurationFile(@NotNull File configurationFile) {
    String path = configurationFile.getAbsolutePath();
    return myServers.get(path);
  }

  public static void startServer(@NotNull final File nodeInterpreter,
                                 @NotNull final File karmaPackageDir,
                                 @NotNull final File configurationFile,
                                 final CatchingConsumer<KarmaServer, Exception> consumer) {
    final String configPath = configurationFile.getAbsolutePath();
    if (myStartingServers.putIfAbsent(configPath, configPath) != null) {
      LOG.warn(new Throwable("Unexpected subsequent karma server starting:"
                             + "\n  nodeInterpreter: " + nodeInterpreter
                             + "\n  karmaPackageDir: " + karmaPackageDir
                             + "\n  configurationFile: " + configurationFile
      ));
      JobScheduler.getScheduler().schedule(new Runnable() {
        @Override
        public void run() {
          startServer(nodeInterpreter, karmaPackageDir, configurationFile, consumer);
        }
      }, 100, TimeUnit.MILLISECONDS);
    }
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          final KarmaServer server = new KarmaServer(nodeInterpreter, karmaPackageDir, configurationFile);
          myServers.put(configurationFile.getAbsolutePath(), server);
          consumer.consume(server);
          server.onTerminated(new KarmaServerTerminatedListener() {
            @Override
            public void onTerminated(int exitCode) {
              myServers.remove(configPath, server);
            }
          });
        }
        catch (Exception e) {
          consumer.consume(e);
        }
        finally {
          myStartingServers.remove(configPath);
        }
      }
    });
  }

}
