package com.intellij.javascript.karma.server;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.CatchingConsumer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sergey Simonchik
 */
public class KarmaServerRegistry {

  private static Map<String, KarmaServer> myServers = new ConcurrentHashMap<String, KarmaServer>();
  private static Set<String> myStartingServers = Collections.synchronizedSet(ContainerUtil.<String>newHashSet());

  public static void startServer(@NotNull final File nodeInterpreter,
                                 @NotNull final File karmaPackageDir,
                                 @NotNull final File configurationFile,
                                 final CatchingConsumer<KarmaServer, IOException> consumer) {
    final String key = configurationFile.getAbsolutePath();
    if (myStartingServers.contains(key)) {
      return;
    }
    myStartingServers.add(key);
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          KarmaServer server = new KarmaServer(nodeInterpreter, karmaPackageDir, configurationFile);
          myServers.put(configurationFile.getAbsolutePath(), server);
          myStartingServers.remove(key);
          consumer.consume(server);
        }
        catch (IOException e) {
          consumer.consume(e);
        }
      }
    });
  }

  @Nullable
  public static KarmaServer getServerByConfigurationFile(@NotNull File configurationFile) {
    String path = configurationFile.getAbsolutePath();
    return myServers.get(path);
  }

  public static void registerServer(@NotNull KarmaServer karmaServer) {
    String path = karmaServer.getConfigurationFile().getAbsolutePath();
    myServers.put(path, karmaServer);
  }

  public static void serverTerminated(@NotNull KarmaServer server) {
    String path = server.getConfigurationFile().getAbsolutePath();
    myServers.remove(path);
  }
}
