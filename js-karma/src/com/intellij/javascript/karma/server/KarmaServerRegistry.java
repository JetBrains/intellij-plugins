package com.intellij.javascript.karma.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sergey Simonchik
 */
public class KarmaServerRegistry {

  private static Map<String, KarmaServer> myServers = new ConcurrentHashMap<String, KarmaServer>();

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
