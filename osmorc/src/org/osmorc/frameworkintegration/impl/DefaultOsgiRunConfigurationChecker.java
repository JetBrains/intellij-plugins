package org.osmorc.frameworkintegration.impl;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.OsgiRunConfigurationChecker;

import java.io.File;

/**
 * Default implementation for the run configuration checker.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class DefaultOsgiRunConfigurationChecker implements OsgiRunConfigurationChecker {


  public final void checkConfiguration(OsgiRunConfiguration runConfiguration) throws RuntimeConfigurationException {
    // make sure that if the user wants to re-use a runtime directory that it exists 
    if (!runConfiguration.isGenerateWorkingDir()) {
      if (runConfiguration.getWorkingDir() == null || "".equals(runConfiguration.getWorkingDir())) {
        throw new RuntimeConfigurationError(
          "The runtime directory is not specified. Please set a runtime directory at the 'Parameters' tab or select 'Recreate each time'.");
      }
      final File dir = new File(runConfiguration.getWorkingDir());
      if (!dir.exists()) {
        // try to create it
        if (!dir.mkdirs()) {
          throw new RuntimeConfigurationError("The runtime directory could not be created. Please check the path at the 'Parameters' tab.");
        }
      }
    }

    checkFrameworkSpecifics(runConfiguration);

    final FrameworkInstanceDefinition frameworkInstanceDefinition = runConfiguration.getInstanceToUse();
    if (frameworkInstanceDefinition != null) {
      String version = frameworkInstanceDefinition.getVersion();
      if (version == null || version.length() == 0 && // no version set in framework definition
                             // and also no version specified in the program parameters.
                             !(runConfiguration.getProgramParameters().contains("--v=") ||
                               runConfiguration.getProgramParameters().contains("--version="))) {
        throw new RuntimeConfigurationWarning("You did not specify a version to be used for '" +
                                              frameworkInstanceDefinition.getName() +
                                              "'. The runner will download and use the latest available version for this framework.");
      }
    }
  }

  /**
   * Method which can be overridden by subclasses to do framework-specific configuration checks. Subclasses overriding this method
   * do not need to call the implementation of the superclass.
   *
   * @param runConfiguration the run configuration to be checked.
   */
  protected void checkFrameworkSpecifics(OsgiRunConfiguration runConfiguration) throws RuntimeConfigurationException {

  }
}
