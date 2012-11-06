package com.intellij.flex.model.run;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.*;
import com.intellij.openapi.util.io.FileUtil;
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

  @NotNull
  public JpsFlashRunnerParameters createCopy() {
    return new JpsFlashRunnerParameters(this);
  }

  public void applyChanges(@NotNull final JpsFlashRunnerParameters modified) {
    super.applyChanges(modified);

    myOverrideMainClass = modified.myOverrideMainClass;
    myOverriddenMainClass = modified.myOverriddenMainClass;
    myOverriddenOutputFileName = modified.myOverriddenOutputFileName;
  }

  // -----------------------

  public boolean isOverrideMainClass() {
    return myOverrideMainClass;
  }

  public void setOverrideMainClass(final boolean overrideMainClass) {
    myOverrideMainClass = overrideMainClass;
  }

  @NotNull
  public String getOverriddenMainClass() {
    return myOverriddenMainClass;
  }

  public void setOverriddenMainClass(@NotNull final String overriddenMainClass) {
    myOverriddenMainClass = overriddenMainClass;
  }

  @NotNull
  public String getOverriddenOutputFileName() {
    return myOverriddenOutputFileName;
  }

  public void setOverriddenOutputFileName(@NotNull final String overriddenOutputFileName) {
    myOverriddenOutputFileName = overriddenOutputFileName;
  }

  @Nullable
  public JpsFlexBuildConfiguration getBC(final JpsProject project) {
    final JpsFlexBuildConfiguration origBC = super.getBC(project);

    if (myOverrideMainClass && origBC != null) {
      final JpsFlexBuildConfiguration overriddenBC = origBC.getModule().getProperties().createTemporaryCopyForCompilation(origBC);

      overriddenBC.setOutputType(OutputType.Application);

      overriddenBC.setMainClass(myOverriddenMainClass);
      overriddenBC.setOutputFileName(myOverriddenOutputFileName);

      overriddenBC.setRLMs(Collections.<JpsFlexBuildConfiguration.RLMInfo>emptyList());

      if (origBC.getOutputType() != OutputType.Application) {
        overriddenBC.setUseHtmlWrapper(false);
        overriddenBC.setCssFilesToCompile(Collections.<String>emptyList());

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

      overriddenBC.getAndroidPackagingOptions().setPackageFileName(FileUtil.getNameWithoutExtension(myOverriddenOutputFileName));
      overriddenBC.getIosPackagingOptions().setPackageFileName(FileUtil.getNameWithoutExtension(myOverriddenOutputFileName));

      return overriddenBC;
    }

    return origBC;
  }
}
