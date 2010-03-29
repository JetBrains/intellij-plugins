package org.osmorc.frameworkintegration.impl;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
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
      if (runConfiguration.getWorkingDir() == null ||
          "".equals(runConfiguration.getWorkingDir()) ||
          !new File(runConfiguration.getWorkingDir()).exists()) {
        throw new RuntimeConfigurationError(
          "The runtime directory is not specified or does not exist. Please set a runtime directory at the 'Parameters' tab or select 'Recreate each time'.");
      }
    }

    checkFrameworkSpecifics(runConfiguration);
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
