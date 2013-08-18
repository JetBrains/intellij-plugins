package com.intellij.javascript.karma.server;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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

  private final Project myProject;
  private final ConcurrentMap<String, KarmaServer> myServerByConfigFile = new ConcurrentHashMap<String, KarmaServer>();
  private final ConcurrentMap<Key, KarmaServer> myServers = new ConcurrentHashMap<Key, KarmaServer>();
  private final ConcurrentMap<Key, Object> myStartingServers = new ConcurrentHashMap<Key, Object>();

  public KarmaServerRegistry(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  public static KarmaServerRegistry getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, KarmaServerRegistry.class);
  }

  @Nullable
  public KarmaServer getServer(@NotNull File nodeInterpreter,
                                      @NotNull File karmaPackageDir,
                                      @NotNull File configurationFile) {
    Key key = new Key(nodeInterpreter, karmaPackageDir, configurationFile);
    return myServers.get(key);
  }

  public void startServer(@NotNull final File nodeInterpreter,
                          @NotNull final File karmaPackageDir,
                          @NotNull final File configurationFile,
                          final CatchingConsumer<KarmaServer, Exception> consumer) {
    final Key key = new Key(nodeInterpreter, karmaPackageDir, configurationFile);
    KarmaServer prevServer = myServerByConfigFile.get(key.getConfigurationFilePath());
    if (prevServer != null) {
      prevServer.onTerminated(new KarmaServerTerminatedListener() {
        @Override
        public void onTerminated(int exitCode) {
          doStartServer(key, consumer);
        }
      });
      prevServer.shutdownAsync();
    }
    else {
      doStartServer(key, consumer);
    }
  }

  private void doStartServer(@NotNull final Key key,
                             @NotNull final CatchingConsumer<KarmaServer, Exception> consumer) {
    if (myStartingServers.putIfAbsent(key, key) != null) {
      LOG.warn(new Throwable("Unexpected subsequent karma server starting:" + key.toString()));
      JobScheduler.getScheduler().schedule(new Runnable() {
        @Override
        public void run() {
          startServer(key.getNodeInterpreter(), key.getKarmaPackageDir(), key.getConfigurationFile(), consumer);
        }
      }, 100, TimeUnit.MILLISECONDS);
    }
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          final KarmaServer server;
          try {
            server = new KarmaServer(myProject,
                                     key.getNodeInterpreter(),
                                     key.getKarmaPackageDir(),
                                     key.getConfigurationFile());
            myServers.put(key, server);
            myServerByConfigFile.put(key.getConfigurationFilePath(), server);
          }
          finally {
            myStartingServers.remove(key);
          }
          consumer.consume(server);
          server.onTerminated(new KarmaServerTerminatedListener() {
            @Override
            public void onTerminated(int exitCode) {
              myServers.remove(key, server);
              myServerByConfigFile.remove(key.getConfigurationFilePath(), server);
            }
          });
        }
        catch (Exception e) {
          consumer.consume(e);
        }
      }
    });

  }

  private static class Key {

    private final File myNodeInterpreter;
    private final File myKarmaPackageDir;
    private final File myConfigurationFile;
    private final String myNodeInterpreterPath;
    private final String myKarmaPackageDirPath;
    private final String myConfigurationFilePath;

    private Key(@NotNull File nodeInterpreter,
                @NotNull File karmaPackageDir,
                @NotNull File configurationFile) {
      myNodeInterpreter = nodeInterpreter;
      myKarmaPackageDir = karmaPackageDir;
      myConfigurationFile = configurationFile;
      myNodeInterpreterPath = nodeInterpreter.getAbsolutePath();
      myKarmaPackageDirPath = karmaPackageDir.getAbsolutePath();
      myConfigurationFilePath = configurationFile.getAbsolutePath();
    }

    private File getNodeInterpreter() {
      return myNodeInterpreter;
    }

    private File getKarmaPackageDir() {
      return myKarmaPackageDir;
    }

    private File getConfigurationFile() {
      return myConfigurationFile;
    }

    private String getConfigurationFilePath() {
      return myConfigurationFilePath;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Key key = (Key)o;

      if (!myConfigurationFilePath.equals(key.myConfigurationFilePath)) return false;
      if (!myKarmaPackageDirPath.equals(key.myKarmaPackageDirPath)) return false;
      if (!myNodeInterpreterPath.equals(key.myNodeInterpreterPath)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = myNodeInterpreterPath.hashCode();
      result = 31 * result + myKarmaPackageDirPath.hashCode();
      result = 31 * result + myConfigurationFilePath.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "interpreter: " + myNodeInterpreterPath
             + ", karmaPackageDir: " + myKarmaPackageDirPath
             + ", configurationFile: " + myConfigurationFilePath;
    }
  }

}
