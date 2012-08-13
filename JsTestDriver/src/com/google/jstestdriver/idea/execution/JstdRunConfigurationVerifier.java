package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager;
import com.google.jstestdriver.idea.server.ui.ServerStartAction;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JstdRunConfigurationVerifier {

  private JstdRunConfigurationVerifier() {}

  public static void verify(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    if (runSettings.getTestType() == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      verifyAllInDirectory(runSettings);
    } else if (runSettings.getTestType() == TestType.CONFIG_FILE) {
      verifyConfigFile(runSettings);
    } else if (runSettings.getTestType() == TestType.JS_FILE) {
      verifyJSFileType(runSettings);
    } else if (runSettings.getTestType() == TestType.TEST_CASE) {
      verifyTestCase(runSettings);
    } else if (runSettings.getTestType() == TestType.TEST_METHOD) {
      verifyTestMethod(runSettings);
    }
    verifyServer(runSettings);
  }

  private static void verifyAllInDirectory(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    String directory = runSettings.getDirectory();
    if (directory.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Directory name is empty");
    }
    File dirFile = new File(directory);
    if (!dirFile.exists()) {
      throw new RuntimeConfigurationError("Specified directory '" + directory + "' does not exists");
    }
    if (dirFile.isFile()) {
      throw new RuntimeConfigurationError("You have specified file, but directory was expected.");
    }
    if (!dirFile.isDirectory()) {
      throw new RuntimeConfigurationError("Please specify directory correctly.");
    }
  }

  private static void verifyConfigFile(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    String fileStr = runSettings.getConfigFile();
    if (fileStr.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Configuration file name is empty");
    }
    File configFile = new File(fileStr);
    if (!configFile.exists()) {
      throw new RuntimeConfigurationError("Configuration file does not exists");
    }
    if (configFile.isDirectory()) {
      throw new RuntimeConfigurationError("You have specified directory, but file was expected.");
    }
    if (!configFile.isFile()) {
      throw new RuntimeConfigurationError("Please specify configuration file correctly.");
    }
  }

  private static void verifyJSFileType(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    verifyConfigFile(runSettings);
    verifyJSFilePath(runSettings);
  }

  private static void verifyJSFilePath(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    String fileStr = runSettings.getJsFilePath();
    if (fileStr.trim().isEmpty()) {
      throw new RuntimeConfigurationError("JavaScript file name is empty");
    }
    File configFile = new File(fileStr);
    if (!configFile.exists()) {
      throw new RuntimeConfigurationError("JavaScript file does not exists");
    }
    if (configFile.isDirectory()) {
      throw new RuntimeConfigurationError("You have specified directory, but file was expected.");
    }
    if (!configFile.isFile()) {
      throw new RuntimeConfigurationError("Please specify JavaScript file correctly.");
    }
  }

  private static void verifyTestCase(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    verifyJSFileType(runSettings);
    if (runSettings.getTestCaseName().isEmpty()) {
      throw new RuntimeConfigurationError("Test case name is empty");
    }
  }

  private static void verifyTestMethod(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    verifyTestCase(runSettings);
    if (runSettings.getTestMethodName().isEmpty()) {
      throw new RuntimeConfigurationError("Test method name is empty");
    }
  }

  private static void verifyServer(@NotNull JstdRunSettings runSettings) throws RuntimeConfigurationError {
    if (runSettings.getServerType() == ServerType.EXTERNAL) {
      String serverAddressStr = runSettings.getServerAddress();
      if (serverAddressStr.trim().isEmpty()) {
        throw new RuntimeConfigurationError("Server address is empty");
      }
      try {
        new URL(serverAddressStr);
      } catch (MalformedURLException e) {
        throw new RuntimeConfigurationError("Please specify server address correctly");
      }
    }
  }

  public static void isJstdLocalServerReady(@NotNull Project project,
                                            @NotNull JstdRunSettings settings,
                                            boolean debug) throws ExecutionException {
    if (settings.isExternalServerType()) {
      return;
    }
    JstdServerState jstdServerState = JstdServerState.getInstance();
    if (!jstdServerState.isServerRunning()) {
      String browserMessage = debug ? "Firefox or Chrome" : "a browser";
      throw new MyExecutionException(project, "JsTestDriver server is not running.<br>" +
                                              "<a href=\"\">Start a local server</a> and capture " + browserMessage);
    }
  }

  private static class MyExecutionException extends ExecutionException implements HyperlinkListener {

    private final Project myProject;

    public MyExecutionException(Project project, String s) {
      super(s);
      myProject = project;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        JstdServerState jstdServerState = JstdServerState.getInstance();
        if (!jstdServerState.isServerRunning()) {
          ServerStartAction.asyncStartServer(new Runnable() {
            @Override
            public void run() {
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                  JstdToolWindowManager manager = JstdToolWindowManager.getInstance(myProject);
                  ToolWindow toolWindow = manager.registerToolWindowIfNeeded();
                  if (toolWindow != null) {
                    toolWindow.show(null);
                  }
                }
              });
            }
          });
        }
      }
    }
  }

}
