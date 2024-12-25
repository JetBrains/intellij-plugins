// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.run;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.*;
import com.intellij.openapi.util.io.FileUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsProject;

import java.util.Collections;

public class JpsFlashRunnerParameters extends JpsBCBasedRunnerParameters<JpsFlashRunnerParameters> {

  private boolean myOverrideMainClass = false;
  private @NotNull String myOverriddenMainClass = "";
  private @NotNull String myOverriddenOutputFileName = "";

  public JpsFlashRunnerParameters() {
  }

  private JpsFlashRunnerParameters(final JpsFlashRunnerParameters original) {
    myOverrideMainClass = original.myOverrideMainClass;
    myOverriddenMainClass = original.myOverriddenMainClass;
    myOverriddenOutputFileName = original.myOverriddenOutputFileName;
  }

  @Override
  public @NotNull JpsFlashRunnerParameters createCopy() {
    return new JpsFlashRunnerParameters(this);
  }

  // -----------------------

  public boolean isOverrideMainClass() {
    return myOverrideMainClass;
  }

  public void setOverrideMainClass(final boolean overrideMainClass) {
    myOverrideMainClass = overrideMainClass;
  }

  public @NotNull String getOverriddenMainClass() {
    return myOverriddenMainClass;
  }

  public void setOverriddenMainClass(final @NotNull String overriddenMainClass) {
    myOverriddenMainClass = overriddenMainClass;
  }

  public @NotNull String getOverriddenOutputFileName() {
    return myOverriddenOutputFileName;
  }

  public void setOverriddenOutputFileName(final @NotNull String overriddenOutputFileName) {
    myOverriddenOutputFileName = overriddenOutputFileName;
  }

  @Override
  public @Nullable JpsFlexBuildConfiguration getBC(final JpsProject project) {
    final JpsFlexBuildConfiguration origBC = super.getBC(project);

    if (myOverrideMainClass && origBC != null) {
      final JpsFlexBuildConfiguration overriddenBC = origBC.getModule().getProperties().createTemporaryCopyForCompilation(origBC);

      overriddenBC.setOutputType(OutputType.Application);

      overriddenBC.setMainClass(myOverriddenMainClass);
      overriddenBC.setOutputFileName(myOverriddenOutputFileName);

      overriddenBC.setRLMs(Collections.emptyList());

      if (origBC.getOutputType() != OutputType.Application) {
        overriddenBC.setUseHtmlWrapper(false);
        overriddenBC.setCssFilesToCompile(Collections.emptyList());

        overriddenBC.getDependencies().setFrameworkLinkage(LinkageType.Merged);

        for (JpsFlexDependencyEntry entry : overriddenBC.getDependencies().getEntries()) {
          if (entry.getLinkageType() == LinkageType.External) {
            entry.setLinkageType(LinkageType.Merged);
          }
        }

        overriddenBC.getAirDesktopPackagingOptions().setUseGeneratedDescriptor(true);

        final JpsAndroidPackagingOptions androidOptions = overriddenBC.getAndroidPackagingOptions();
        androidOptions.setEnabled(true);
        androidOptions.setUseGeneratedDescriptor(true);
        androidOptions.getSigningOptions().setUseTempCertificate(true);

        // impossible without extra user input: certificate and provisioning profile
        overriddenBC.getIosPackagingOptions().setEnabled(false);
      }

      if (FlexCommonUtils.canHaveResourceFiles(overriddenBC.getNature()) && !FlexCommonUtils.canHaveResourceFiles(origBC.getNature())) {
        overriddenBC.getCompilerOptions().setResourceFilesMode(JpsFlexCompilerOptions.ResourceFilesMode.None);
      }

      overriddenBC.getAndroidPackagingOptions().setPackageFileName(FileUtilRt.getNameWithoutExtension(myOverriddenOutputFileName));
      overriddenBC.getIosPackagingOptions().setPackageFileName(FileUtilRt.getNameWithoutExtension(myOverriddenOutputFileName));

      return overriddenBC;
    }

    return origBC;
  }
}
