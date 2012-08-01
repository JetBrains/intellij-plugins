package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.execution.configurations.RuntimeConfigurationError;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JstdRunConfigurationVerifier {
  private JstdRunConfigurationVerifier() {
  }

  public static void verify(JstdRunSettings runSettings) throws RuntimeConfigurationError {
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

  private static void verifyTestMethod(JstdRunSettings runSettings) throws RuntimeConfigurationError {
    verifyTestCase(runSettings);
    if (runSettings.getTestMethodName().isEmpty()) {
      throw new RuntimeConfigurationError("Test method name is empty");
    }
  }

  private static void verifyTestCase(JstdRunSettings runSettings) throws RuntimeConfigurationError {
    verifyJSFileType(runSettings);
    if (runSettings.getTestCaseName().isEmpty()) {
      throw new RuntimeConfigurationError("Test case name is empty");
    }
  }

  private static void verifyAllInDirectory(JstdRunSettings runSettings) throws RuntimeConfigurationError {
    String directory = runSettings.getDirectory();
    if (directory.trim().isEmpty()) {
      throw new RuntimeConfigurationError("Directory name is empty");
    }
    File dirFile = new File(directory);
    if (!dirFile.exists()) {
      throw new RuntimeConfigurationError("Specified directory '" + directory + "' does not exists");
    }
    if (dirFile.isFile()) {
      throw new RuntimeConfigurationError("You have specified '" + directory + "' file, but directory was expected.");
    }
    if (!dirFile.isDirectory()) {
      throw new RuntimeConfigurationError("Please specify directory correctly.");
    }
  }

  private static void verifyConfigFile(JstdRunSettings runSettings) throws RuntimeConfigurationError {
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

  private static void verifyJSFileType(JstdRunSettings runSettings) throws RuntimeConfigurationError {
    verifyConfigFile(runSettings);
    verifyJSFilePath(runSettings);
  }

  private static void verifyJSFilePath(JstdRunSettings runSettings) throws RuntimeConfigurationError {
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

  private static void verifyServer(JstdRunSettings runSettings) throws RuntimeConfigurationError {
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

}
