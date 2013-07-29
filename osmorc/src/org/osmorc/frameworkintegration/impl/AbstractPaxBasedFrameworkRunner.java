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

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JdkUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.net.HttpConfigurable;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.run.ExternalVMFrameworkRunner;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Framework runner implementation for using the PAX runner. This is an abstract base class that can be extended for the
 * various frameworks.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public abstract class AbstractPaxBasedFrameworkRunner<P extends GenericRunProperties> implements ExternalVMFrameworkRunner {
  private static final String PAX_RUNNER_LIB = "pax-runner.jar";
  private static final String PAX_RUNNER_MAIN_CLASS = "org.ops4j.pax.runner.Run";

  protected OsgiRunConfiguration myRunConfiguration;
  protected P myAdditionalProperties;
  protected List<SelectedBundle> myBundles;
  private File myWorkingDir;

  @Override
  public JavaParameters createJavaParameters(@NotNull OsgiRunConfiguration runConfiguration,
                                             @NotNull List<SelectedBundle> bundles) throws ExecutionException {
    myRunConfiguration = runConfiguration;
    myAdditionalProperties = convertProperties(runConfiguration.getAdditionalProperties());
    myBundles = bundles;

    Sdk jdkForRun;
    if (runConfiguration.isUseAlternativeJre()) {
      String path = runConfiguration.getAlternativeJrePath();
      if (StringUtil.isEmpty(path) || !JdkUtil.checkForJre(path)) {
        jdkForRun = null;
      }
      else {
        jdkForRun = JavaSdk.getInstance().createJdk("", runConfiguration.getAlternativeJrePath());
      }
    }
    else {
      jdkForRun = ProjectRootManager.getInstance(runConfiguration.getProject()).getProjectSdk();
    }
    if (jdkForRun == null) {
      throw CantRunException.noJdkConfigured();
    }

    JavaParameters params = new JavaParameters();
    myWorkingDir = getWorkingDir(runConfiguration);
    if (!myWorkingDir.isDirectory() && !myWorkingDir.mkdirs()) {
      throw new CantRunException("Cannot create work directory '" + myWorkingDir.getPath() + "'");
    }
    params.setWorkingDirectory(myWorkingDir);

    // only add JDK classes to the classpath, the rest is to be provided by bundles
    params.configureByProject(runConfiguration.getProject(), JavaParameters.JDK_ONLY, jdkForRun);

    File pluginHome = PluginPathManager.getPluginHome("Osmorc");
    if (!pluginHome.isDirectory()) {
      pluginHome = PluginPathManager.getPluginHome("osmorc");
    }
    File paxLib = new File(pluginHome, "lib/" + PAX_RUNNER_LIB);
    if (!paxLib.exists()) {
      throw new CantRunException("Libraries required to start the framework not found - please check the installation");
    }
    params.getClassPath().add(paxLib);

    if (myRunConfiguration.isIncludeAllBundlesInClassPath()) {
      for (SelectedBundle bundle : myBundles) {
        String url = bundle.getBundleUrl();
        if (url != null) {
          params.getClassPath().add(org.osmorc.frameworkintegration.util.FileUtil.urlToPath(url));
        }
      }
    }

    ParametersList vmParameters = params.getVMParametersList();
    vmParameters.addAll(HttpConfigurable.convertArguments(HttpConfigurable.getJvmPropertiesList(false, null)));
    vmParameters.addParametersString(myRunConfiguration.getVmParameters());
    addAdditionalTargetVMProperties(vmParameters);

    params.setMainClass(PAX_RUNNER_MAIN_CLASS);

    ParametersList commandLineParameters = params.getProgramParametersList();
    commandLineParameters.add("--p=" + getOsgiFrameworkName().toLowerCase());
    commandLineParameters.add("--nologo=true");

    // Use the selected version if specified.
    FrameworkInstanceDefinition definition = myRunConfiguration.getInstanceToUse();
    String version = null;
    if (definition != null) {
      version = definition.getVersion();
    }
    if (!StringUtil.isEmptyOrSpaces(version)) {
      commandLineParameters.add("--v=" + version);
    }

    for (SelectedBundle bundle : myBundles) {
      String bundleUrl = bundle.getBundleUrl();
      String prefix = CachingBundleInfoProvider.isExploded(bundleUrl) ? "scan-bundle:" : "";
      boolean isFragment = CachingBundleInfoProvider.isFragmentBundle(bundleUrl);
      if (bundle.isStartAfterInstallation() && !isFragment) {
        int bundleStartLevel = getBundleStartLevel(bundle);
        commandLineParameters.add(prefix + bundleUrl + "@" + bundleStartLevel);
      }
      else if (isFragment) {
        commandLineParameters.add(prefix + bundleUrl + "@nostart");
      }
      else {
        commandLineParameters.add(prefix + bundleUrl);
      }
    }

    P frameworkProperties = myAdditionalProperties;
    String bootDelegation = frameworkProperties.getBootDelegation();
    if (bootDelegation != null && !(bootDelegation.trim().length() == 0)) {
      commandLineParameters.add("--bd=" + bootDelegation);
    }

    String systemPackages = frameworkProperties.getSystemPackages();
    if (systemPackages != null && !(systemPackages.trim().length() == 0)) {
      commandLineParameters.add("--sp=" + systemPackages);
    }

    int startLevel = getFrameworkStartLevel();
    commandLineParameters.add("--sl=" + startLevel);

    int defaultStartLevel = myRunConfiguration.getDefaultStartLevel();
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

    String additionalProgramParams = myRunConfiguration.getProgramParameters();
    if (!StringUtil.isEmptyOrSpaces(additionalProgramParams)) {
      commandLineParameters.addParametersString(additionalProgramParams);
    }

    params.setUseDynamicVMOptions(!myBundles.isEmpty());

    return params;
  }

  @NotNull
  protected abstract P convertProperties(@NotNull Map<String, String> properties);

  private static File getWorkingDir(OsgiRunConfiguration runConfiguration) {
    String path;
    if (runConfiguration.isGenerateWorkingDir()) {
      path = PathManager.getSystemPath() + File.separator + "osmorc" + File.separator + "run" + System.currentTimeMillis();
    }
    else {
      path = runConfiguration.getWorkingDir();
    }
    return new File(path);
  }

  private int getBundleStartLevel(@NotNull SelectedBundle bundle) {
    return bundle.isDefaultStartLevel() ? myRunConfiguration.getDefaultStartLevel() : bundle.getStartLevel();
  }

  private int getFrameworkStartLevel() {
    if (myRunConfiguration.isAutoStartLevel()) {
      int startLevel = 0;
      for (SelectedBundle bundle : myBundles) {
        int bundleStartLevel = getBundleStartLevel(bundle);
        startLevel = Math.max(bundleStartLevel, startLevel);
      }
      return startLevel;
    }
    else {
      return myRunConfiguration.getFrameworkStartLevel();
    }
  }

  @NotNull
  protected abstract String getOsgiFrameworkName();

  /**
   * Returns a list of additional VM parameters that should be given to the VM that is launched by PAX. For convenience this method
   * will return the empty string in this base class, so overriding classes do not need to call super.
   */
  protected void addAdditionalTargetVMProperties(@NotNull ParametersList vmParameters) { }

  @Override
  public void dispose() {
    if (myRunConfiguration.isGenerateWorkingDir() && myWorkingDir != null) {
      FileUtil.asyncDelete(myWorkingDir);
    }
  }
}
