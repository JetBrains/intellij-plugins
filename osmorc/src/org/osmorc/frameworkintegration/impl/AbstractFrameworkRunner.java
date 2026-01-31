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
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SmartList;
import com.intellij.util.net.HttpConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceManager;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.intellij.openapi.util.Pair.pair;
import static org.osmorc.frameworkintegration.FrameworkInstanceManager.FrameworkBundleType;
import static org.osmorc.i18n.OsmorcBundle.message;

/**
 * This class provides a default implementation for a part of the FrameworkRunner interface.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public abstract class AbstractFrameworkRunner implements FrameworkRunner {
  protected OsgiRunConfiguration myRunConfiguration;
  protected FrameworkInstanceDefinition myInstance;
  protected FrameworkIntegrator myIntegrator;
  protected FrameworkInstanceManager myInstanceManager;
  protected Map<String, String> myAdditionalProperties;
  protected List<SelectedBundle> myBundles;
  protected File myWorkingDir;

  @Override
  public JavaParameters createJavaParameters(@NotNull OsgiRunConfiguration runConfiguration,
                                             @NotNull List<SelectedBundle> bundles) throws ExecutionException {
    myRunConfiguration = runConfiguration;
    myInstance = myRunConfiguration.getInstanceToUse();
    assert myInstance != null : myRunConfiguration;
    myIntegrator = FrameworkIntegratorRegistry.getInstance().findIntegratorByInstanceDefinition(myInstance);
    if (myIntegrator == null) {
      throw new CantRunException(message("run.configuration.missing.integrator", myInstance));
    }
    myInstanceManager = myIntegrator.getFrameworkInstanceManager();
    myAdditionalProperties = myRunConfiguration.getAdditionalProperties();
    myBundles = bundles;

    JavaParameters params = new JavaParameters();

    // working directory and JVM

    if (myRunConfiguration.isGenerateWorkingDir()) {
      myWorkingDir = new File(PathManager.getSystemPath(), "osmorc/run." + System.currentTimeMillis());
    }
    else {
      myWorkingDir = new File(myRunConfiguration.getWorkingDir());
    }
    if (!myWorkingDir.isDirectory() && !myWorkingDir.mkdirs()) {
      throw new CantRunException(message("run.configuration.working.dir.create.failed", myWorkingDir.getPath()));
    }
    params.setWorkingDirectory(myWorkingDir);

    // only add JDK classes to the classpath, the rest is to be provided by bundles
    String jreHome = myRunConfiguration.isUseAlternativeJre() ? myRunConfiguration.getAlternativeJrePath() : null;
    JavaParametersUtil.configureProject(myRunConfiguration.getProject(), params, JavaParameters.JDK_ONLY, jreHome);

    // class path

    Collection<SelectedBundle> systemBundles = myInstanceManager.getFrameworkBundles(myInstance, FrameworkBundleType.SYSTEM);
    if (systemBundles.isEmpty()) {
      throw new CantRunException(message("run.configuration.no.system.libs"));
    }
    for (SelectedBundle bundle : systemBundles) {
      String path = bundle.getBundlePath();
      assert path != null : bundle;
      params.getClassPath().add(path);
    }

    if (GenericRunProperties.isStartConsole(myAdditionalProperties)) {
      Collection<SelectedBundle> shellBundles = myInstanceManager.getFrameworkBundles(myInstance, FrameworkBundleType.SHELL);
      if (shellBundles.isEmpty()) {
        throw new CantRunException(message("run.configuration.no.shell.bundles"));
      }
      List<SelectedBundle> allBundles = new ArrayList<>(shellBundles);
      allBundles.addAll(myBundles);
      myBundles = allBundles;
    }

    if (myRunConfiguration.isIncludeAllBundlesInClassPath()) {
      for (SelectedBundle bundle : myBundles) {
        String path = bundle.getBundlePath();
        if (path != null) {
          params.getClassPath().add(path);
        }
      }
    }

    // runner options

    params.setUseDynamicVMOptions(!myBundles.isEmpty());
    params.setUseDynamicParameters(!myBundles.isEmpty());

    HttpConfigurable.getInstance().getJvmProperties(false, null).forEach(p -> params.getVMParametersList().addProperty(p.first, p.second));
    params.getVMParametersList().addParametersString(myRunConfiguration.getVmParameters());

    String additionalProgramParams = myRunConfiguration.getProgramParameters();
    if (!StringUtil.isEmptyOrSpaces(additionalProgramParams)) {
      params.getProgramParametersList().addParametersString(additionalProgramParams);
    }

    String bootDelegation = GenericRunProperties.getBootDelegation(myAdditionalProperties);
    if (!StringUtil.isEmptyOrSpaces(bootDelegation)) {
      params.getVMParametersList().addProperty("org.osgi.framework.bootdelegation", bootDelegation);
    }

    String systemPackages = GenericRunProperties.getSystemPackages(myAdditionalProperties);
    if (!StringUtil.isEmptyOrSpaces(systemPackages)) {
      params.getVMParametersList().addProperty("org.osgi.framework.system.packages.extra", systemPackages);
    }

    // framework-specific options

    try {
      setupParameters(params);
    }
    catch (ConfigurationException e) {
      throw new CantRunException(e.getMessage(), e.getCause());
    }

    return params;
  }

  protected static class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  protected abstract void setupParameters(@NotNull JavaParameters parameters) throws ConfigurationException;

  protected Map<Integer, Pair<List<String>, List<String>>> collectBundles() {
    Map<Integer, Pair<List<String>, List<String>>> bundles = new TreeMap<>();

    for (SelectedBundle bundle : myBundles) {
      String bundlePath = bundle.getBundlePath();
      if (bundlePath == null) continue;

      String bundleUrl = toFileUri(bundlePath);
      int startLevel = getBundleStartLevel(bundle);
      Pair<List<String>, List<String>> lists = bundles.computeIfAbsent(startLevel, k -> pair(new SmartList<>(), new SmartList<>()));
      boolean start = bundle.isStartAfterInstallation() && !CachingBundleInfoProvider.isFragmentBundle(bundlePath);
      (start ? lists.first : lists.second).add(bundleUrl);
    }

    return bundles;
  }

  protected int getBundleStartLevel(@NotNull SelectedBundle bundle) {
    return bundle.isDefaultStartLevel() ? myRunConfiguration.getDefaultStartLevel() : bundle.getStartLevel();
  }

  protected int getFrameworkStartLevel() {
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

  @Override
  public void dispose() {
    if (myRunConfiguration.isGenerateWorkingDir() && myWorkingDir != null) {
      FileUtil.asyncDelete(myWorkingDir);
    }
  }

  protected @NotNull String toFileUri(@NotNull String path) {
    return new File(path).toURI().toString();
  }
}
