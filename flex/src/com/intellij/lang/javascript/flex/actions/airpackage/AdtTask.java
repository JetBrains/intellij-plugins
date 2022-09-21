package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.build.FlexCompilationUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  public static void appendSigningOptions(final List<? super String> command,
                                          final AirPackagingOptions packagingOptions,
                                          final String keystorePassword,
                                          final String keyPassword) {
    final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
    final boolean tempCertificate = !(packagingOptions instanceof IosPackagingOptions) && signingOptions.isUseTempCertificate();

    if (!tempCertificate && !signingOptions.getKeyAlias().isEmpty()) {
      command.add("-alias");
      command.add(signingOptions.getKeyAlias());
    }

    command.add("-storetype");
    command.add(tempCertificate ? "PKCS12" : signingOptions.getKeystoreType());

    command.add("-keystore");
    command.add(FileUtil.toSystemDependentName(tempCertificate ? AirPackageUtil.getTempKeystorePath() : signingOptions.getKeystorePath()));

    command.add("-storepass");
    command.add(tempCertificate ? AirPackageUtil.TEMP_KEYSTORE_PASSWORD : keystorePassword);

    if (tempCertificate) {
      if (packagingOptions instanceof AirDesktopPackagingOptions) {
        command.add("-tsa");
        command.add("none");
      }
    }
    else {
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
  }

  public static void appendPaths(final List<? super String> command,
                                 final Module module,
                                 final FlexBuildConfiguration bc,
                                 final AirPackagingOptions packagingOptions,
                                 final @Nullable String platformSdkPath,
                                 final String packageFileExtension) {
    final String outputFilePath = bc.getActualOutputFilePath();
    final String outputFolder = PathUtil.getParentPath(outputFilePath);

    command.add(FileUtil.toSystemDependentName(outputFolder + "/" + packagingOptions.getPackageFileName() + packageFileExtension));
    command.add(FileUtil.toSystemDependentName(FlexBaseRunner.getAirDescriptorPath(bc, packagingOptions)));

    if (platformSdkPath != null && !platformSdkPath.isEmpty()) {
      command.add("-platformsdk");
      command.add(FileUtil.toSystemDependentName(platformSdkPath));
    }

    appendANEPaths(command, module, bc);

    command.add("-C");
    command.add(FileUtil.toSystemDependentName(outputFolder));
    command.add(FileUtil.toSystemDependentName(PathUtil.getFileName(outputFilePath)));

    for (FilePathAndPathInPackage entry : packagingOptions.getFilesToPackage()) {
      final String fullPath = FileUtil.toSystemIndependentName(entry.FILE_PATH.trim());
      String relPathInPackage = FileUtil.toSystemIndependentName(entry.PATH_IN_PACKAGE.trim());
      relPathInPackage = StringUtil.trimStart(relPathInPackage, "/");

      final String pathEnd = "/" + relPathInPackage;
      if (fullPath.endsWith(pathEnd)) {
        command.add("-C");
        command.add(FileUtil.toSystemDependentName(fullPath.substring(0, fullPath.length() - pathEnd.length())));
        command.add(FileUtil.toSystemDependentName(relPathInPackage));
      }
      else if (".".equals(relPathInPackage)) {
        command.add("-C");
        command.add(FileUtil.toSystemDependentName(fullPath));
        command.add(FileUtil.toSystemDependentName("."));
      }
      else {
        command.add("-e");
        command.add(FileUtil.toSystemDependentName(fullPath));
        command.add(relPathInPackage);
      }
    }
  }

  private static void appendANEPaths(final List<? super String> command, final Module module, final FlexBuildConfiguration bc) {
    final Set<VirtualFile> extDirPaths = new HashSet<>();
    for (VirtualFile aneFile : FlexCompilationUtils.getANEFiles(ModuleRootManager.getInstance(module), bc.getDependencies())) {
      if (extDirPaths.add(aneFile.getParent())) {
        command.add("-extdir");
        command.add(FileUtil.toSystemDependentName(aneFile.getParent().getPath()));
      }
    }
  }
}
