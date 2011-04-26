package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;

import java.io.File;
import java.util.List;

public abstract class AdtTask extends ExternalTask {

  public AdtTask(Project project, Sdk flexSdk) {
    super(project, flexSdk);
  }

  @Override
  protected List<String> createCommandLine() {
    final List<String> command = FlexSdkUtils.getCommandLineForSdkTool(myProject, myFlexSdk, null, "com.adobe.air.ADT", "adt.jar");
    appendAdtOptions(command);
    return command;
  }

  protected abstract void appendAdtOptions(final List<String> command);

  public static void appendSigningOptions(List<String> command, AirInstallerParametersBase parameters) {
    if (parameters.KEY_ALIAS.length() > 0) {
      command.add("-alias");
      command.add(parameters.KEY_ALIAS);
    }

    command.add("-storetype");
    command.add(parameters.KEYSTORE_TYPE);

    command.add("-keystore");
    command.add(parameters.KEYSTORE_PATH);

    if (parameters.getKeystorePassword().length() > 0) {
      command.add("-storepass");
      command.add(parameters.getKeystorePassword());
    }

    if (parameters.getKeyPassword().length() > 0) {
      command.add("-keypass");
      command.add(parameters.getKeyPassword());
    }

    if (parameters.PROVIDER_CLASS.length() > 0) {
      command.add("-providerName");
      command.add(parameters.PROVIDER_CLASS);
    }

    if (parameters.TSA.length() > 0) {
      command.add("-tsa");
      command.add(parameters.TSA);
    }
  }

  public static void appendPaths(final List<String> command, final AirInstallerParametersBase parameters) {
    command.add(parameters.INSTALLER_FILE_LOCATION + File.separatorChar + parameters.INSTALLER_FILE_NAME);
    command.add(parameters.AIR_DESCRIPTOR_PATH);

    for (AirInstallerParametersBase.FilePathAndPathInPackage path : parameters.FILES_TO_PACKAGE) {
      final String fullPath = FileUtil.toSystemIndependentName(path.FILE_PATH.trim());
      String relPathInPackage = FileUtil.toSystemIndependentName(path.PATH_IN_PACKAGE.trim());
      if (relPathInPackage.startsWith("/")) {
        relPathInPackage = relPathInPackage.substring(1);
      }

      final String pathEnd = "/" + relPathInPackage;
      if (fullPath.endsWith(pathEnd)) {
        command.add("-C");
        command.add(FileUtil.toSystemDependentName(fullPath.substring(0, fullPath.length() - pathEnd.length())));
        command.add(FileUtil.toSystemDependentName(relPathInPackage));
      }
      else {
        command.add("-e");
        command.add(FileUtil.toSystemDependentName(fullPath));
        command.add(relPathInPackage);
      }
    }
  }
}
