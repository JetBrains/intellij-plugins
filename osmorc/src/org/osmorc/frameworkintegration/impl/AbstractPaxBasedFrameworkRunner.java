/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.frameworkintegration.impl;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.net.HttpConfigurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.run.ExternalVMFrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Framework runner implementation for using the PAX runner. This is an abstract base class that can be extended for the
 * various frameworks.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public abstract class AbstractPaxBasedFrameworkRunner<P extends GenericRunProperties>
                extends AbstractFrameworkRunner<P>
                implements ExternalVMFrameworkRunner {

  public static final String PaxRunnerLib = "pax-runner.jar";
  public static final String PaxRunnerMainClass = "org.ops4j.pax.runner.Run";

  protected AbstractPaxBasedFrameworkRunner() { }

  @NotNull
  @Override
  public final List<String> getFrameworkStarterLibraries() throws ExecutionException {
    return getPaxLibraries();
  }

  public static List<String> getPaxLibraries() throws ExecutionException {
    File pluginHome = PluginPathManager.getPluginHome("Osmorc");
    if (!pluginHome.isDirectory()) {
      pluginHome = PluginPathManager.getPluginHome("osmorc");
    }

    File paxLib = new File(pluginHome, "lib/" + PaxRunnerLib);
    if (!paxLib.exists()) {
      throw new ExecutionException("PAX Runner (" + paxLib + ") missing");
    }

    return Collections.singletonList(paxLib.getPath());
  }

  @Override
  public void fillCommandLineParameters(@NotNull ParametersList commandLineParameters, @NotNull SelectedBundle[] bundlesToInstall) {
    commandLineParameters.add("--p=" + getOsgiFrameworkName().toLowerCase());
    commandLineParameters.add("--nologo=true");

    // Use the selected version if specified.
    FrameworkInstanceDefinition definition = getRunConfiguration().getInstanceToUse();
    String version = null;
    if (definition != null) {
      version = definition.getVersion();
    }
    if (!StringUtil.isEmptyOrSpaces(version)) {
      commandLineParameters.add("--v=" + version);
    }

    for (SelectedBundle bundle : bundlesToInstall) {
      String bundleUrl = bundle.getBundleUrl();
      String prefix = CachingBundleInfoProvider.isExploded(bundleUrl) ? "scan-bundle:" : "";
      if (bundle.isStartAfterInstallation() && !CachingBundleInfoProvider.isFragmentBundle(bundleUrl)) {
        int bundleStartLevel = bundle.isDefaultStartLevel() ? getRunConfiguration().getDefaultStartLevel() : bundle.getStartLevel();
        commandLineParameters.add(prefix + bundleUrl + "@" + bundleStartLevel);
      }
      else {
        if (CachingBundleInfoProvider.isFragmentBundle(bundleUrl)) {
          commandLineParameters.add(prefix + bundleUrl + "@nostart");
        }
        else {
          commandLineParameters.add(prefix + bundleUrl);
        }
      }
    }

    P frameworkProperties = getFrameworkProperties();
    String bootDelegation = frameworkProperties.getBootDelegation();
    if (bootDelegation != null && !(bootDelegation.trim().length() == 0)) {
      commandLineParameters.add("--bd=" + bootDelegation);
    }

    String systemPackages = frameworkProperties.getSystemPackages();
    if (systemPackages != null && !(systemPackages.trim().length() == 0)) {
      commandLineParameters.add("--sp=" + systemPackages);
    }

    int startLevel = getFrameworkStartLevel(bundlesToInstall);
    commandLineParameters.add("--sl=" + startLevel);

    int defaultStartLevel = getRunConfiguration().getDefaultStartLevel();
    commandLineParameters.add("--bsl=" + defaultStartLevel);

    if (frameworkProperties.isDebugMode()) {
      commandLineParameters.add("--log=DEBUG");
    }

    if (frameworkProperties.isStartConsole()) {
      commandLineParameters.add("--console");
    }
    else {
      commandLineParameters.add("--noConsole");
    }

    commandLineParameters.add("--executor=inProcess");
    commandLineParameters.add("--keepOriginalUrls");
    commandLineParameters.add("--skipInvalidBundles");

    String additionalProgramParams = getRunConfiguration().getProgramParameters();
    if (!StringUtil.isEmptyOrSpaces(additionalProgramParams)) {
      commandLineParameters.addParametersString(additionalProgramParams);
    }
  }

  @Override
  public void fillVmParameters(ParametersList vmParameters, @NotNull SelectedBundle[] bundlesToInstall) {
    vmParameters.addAll(HttpConfigurable.convertArguments(HttpConfigurable.getJvmPropertiesList(false, null)));

    String vmParamsFromConfig = getRunConfiguration().getVmParameters();
    vmParameters.addParametersString(vmParamsFromConfig);

    addAdditionalTargetVMProperties(vmParameters, bundlesToInstall);
  }

  @Override
  public void runCustomInstallationSteps(@NotNull SelectedBundle[] bundlesToInstall) throws ExecutionException {
    // nothing to do here either...
  }

  /**
   * Needs to be implemented by subclasses.
   *
   * @return the name of the osgi framework that the PAX runner should run.
   */
  @NotNull
  protected abstract String getOsgiFrameworkName();

  /**
   * Returns a list of additional VM parameters that should be given to the VM that is launched by PAX. For convenience this method
   * will return the empty string in this base class, so overriding classes do not need to call super.
   *
   * @param vmParameters
   * @param urlsOfBundlesToInstall the list of bundles to install
   * @return a string with VM parameters.
   */
  protected void addAdditionalTargetVMProperties(@NotNull ParametersList vmParameters,
                                                 @NotNull SelectedBundle[] urlsOfBundlesToInstall) {
  }

  @NotNull
  @NonNls
  @Override
  public final String getMainClass() {
    return PaxRunnerMainClass;
  }

  @Override
  protected final Pattern getFrameworkStarterClasspathPattern() {
    return null;
  }
}
