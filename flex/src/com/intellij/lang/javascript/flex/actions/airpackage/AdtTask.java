package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.actions.AirSigningOptions;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AndroidPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.IosPackagingOptions;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtil;

import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions.FilePathAndPathInPackage;

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

  public static void appendSigningOptions(final List<String> command, final AirSigningOptions signingOptions,
                                          final String keystorePassword, final String keyPassword) {
    if (!signingOptions.getKeyAlias().isEmpty()) {
      command.add("-alias");
      command.add(signingOptions.getKeyAlias());
    }

    command.add("-storetype");
    command.add(signingOptions.getKeystoreType());

    command.add("-keystore");
    command.add(FileUtil.toSystemDependentName(signingOptions.getKeystorePath()));

    command.add("-storepass");
    command.add(keystorePassword);

    if (!signingOptions.getKeyAlias().isEmpty() && !keyPassword.isEmpty()) {
      command.add("-keypass");
      command.add(keyPassword);
    }

    if (!signingOptions.getProvider().isEmpty()) {
      command.add("-providerName");
      command.add(signingOptions.getProvider());
    }

    if (!signingOptions.getTsa().isEmpty()) {
      command.add("-tsa");
      command.add(signingOptions.getTsa());
    }
  }

  public static void appendPaths(final List<String> command,
                                 final Project project,
                                 final FlexIdeBuildConfiguration bc,
                                 final AirPackagingOptions packagingOptions) {
    final String extension = packagingOptions instanceof AndroidPackagingOptions
                             ? ".apk"
                             : packagingOptions instanceof IosPackagingOptions
                               ? ".ipa"
                               : AirPackageProjectParameters.getInstance(project).getDesktopPackageFileExtension();
    command.add(FileUtil.toSystemDependentName(bc.getOutputFolder() + "/" + packagingOptions.getPackageFileName() + extension));
    command.add(FileUtil.toSystemDependentName(FlexBaseRunner.getAirDescriptorPath(bc, bc.getAndroidPackagingOptions())));

    final String outputFilePath = bc.getOutputFilePath(true);
    command.add("-C");
    command.add(FileUtil.toSystemDependentName(PathUtil.getParentPath(outputFilePath)));
    command.add(FileUtil.toSystemDependentName(PathUtil.getFileName(outputFilePath)));

    for (FilePathAndPathInPackage entry : packagingOptions.getFilesToPackage()) {
      final String fullPath = FileUtil.toSystemIndependentName(entry.FILE_PATH.trim());
      String relPathInPackage = FileUtil.toSystemIndependentName(entry.PATH_IN_PACKAGE.trim());
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
