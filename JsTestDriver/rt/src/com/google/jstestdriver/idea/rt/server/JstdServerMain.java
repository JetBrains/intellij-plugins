package com.google.jstestdriver.idea.rt.server;

import com.google.inject.Module;
import com.google.jstestdriver.FailureException;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.browser.BrowserPanicException;
import com.google.jstestdriver.config.*;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.guice.TestResultPrintingModule;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.util.RetryException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JstdServerMain {

  private static final Logger logger = Logger.getLogger(JstdServerMain.class.getName());

  public static void main(String[] args) {
    shutdownIfOrphan();
    try {
      // pre-parse parsing... These are the flags
      // that must be dealt with before we parse the flags.
      CmdLineFlags cmdLineFlags = new CmdLineFlagsFactory().create(args);
      List<Plugin> cmdLinePlugins = cmdLineFlags.getPlugins();

      // configure logging before we start seriously processing.
      LogManager.getLogManager().readConfiguration(cmdLineFlags.getRunnerMode().getLogConfig());


      final PluginLoader pluginLoader = new PluginLoader();

      // load all the command line plugins.
      final List<Module> pluginModules = pluginLoader.load(cmdLinePlugins);
      logger.log(Level.INFO, "loaded plugins {0}", pluginModules);

      JsTestDriverBuilder builder = new JsTestDriverBuilder();
      BasePaths basePath = cmdLineFlags.getBasePath();
      builder.addBasePaths(basePath);
      builder.setDefaultConfiguration(new DefaultConfiguration(basePath));

      builder.setConfigurationSource(cmdLineFlags.getConfigurationSource());
      builder.addPluginModules(pluginModules);
      builder.withPluginInitializer(TestResultPrintingModule.TestResultPrintingInitializer.class);
      builder.setRunnerMode(cmdLineFlags.getRunnerMode());
      builder.setFlags(cmdLineFlags.getUnusedFlagsAsArgs());
      builder.addServerListener(new JstdIntellijServerListener());
      JsTestDriver jstd = builder.build();
      jstd.runConfiguration();

      logger.info("Finished action run.");
    } catch (InvalidFlagException e) {
      e.printErrorMessages(System.out);
      CmdLineFlags.printUsage(System.out);
      System.exit(1);
    } catch (UnreadableFilesException e) {
      System.out.println("Configuration Error: \n" + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    } catch (ConfigurationException e) {
      System.out.println("Configuration Error: \n" + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    } catch (RetryException e) {
      System.out.println("Tests failed due to unexpected environment issue: "
                         + e.getCause().getMessage());
      System.exit(1);
    } catch (FailureException e) {
      System.out.println("Tests failed: " + e.getMessage());
      System.exit(1);
    } catch (BrowserPanicException e) {
      System.out.println("Test run failed due to unresponsive browser: " + e);
      System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Unexpected Runner Condition: " + e.getMessage()
                         + "\n Use --runnerMode DEBUG for more information.");
      System.exit(1);
    }
  }

  private static void shutdownIfOrphan() {
    Thread t = new Thread(() -> {
      int c = 0;
      while (c != -1) {
        try {
          c = System.in.read();
        }
        catch (IOException e) {
          //noinspection CallToPrintStackTrace
          e.printStackTrace();
        }
      }
      System.out.println("JsTestDriver server is an orphan process. It will exit now.");
      System.exit(123);
    }, "Orphan server killer");
    t.setDaemon(true);
    t.start();
  }
}
