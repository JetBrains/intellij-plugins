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
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.run.ExternalVMFrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Framework runner implementation for using the PAX runner. This is an abstract base class that can be extended for the
 * various frameworks.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public abstract class AbstractPaxBasedFrameworkRunner<P extends GenericRunProperties> extends AbstractFrameworkRunner<P> implements ExternalVMFrameworkRunner {
  private static final String PaxRunnerLib = "pax-runner-1.5.0-SNAPSHOT.jar";

  protected AbstractPaxBasedFrameworkRunner() {
  }


  @NotNull
  @Override
  public final List<VirtualFile> getFrameworkStarterLibraries() {
    // pax does it's own magic, so the only lib we need, is the pax lib.
    // XXX: ask anton if there is some better way to do this..
    @SuppressWarnings({"ConstantConditions"}) final String paxLib =
      PluginManager.getPlugin(PluginId.getId("Osmorc")).getPath().getPath() + "/lib/" + PaxRunnerLib;
    List<VirtualFile> libs = new ArrayList<VirtualFile>(1);
    VirtualFile path = LocalFileSystem.getInstance().findFileByPath(paxLib);
    if (path == null) {
      // hmm not good... try get it from the classpath - this is a hack...
      String[] classpath = System.getProperty("java.class.path").split(File.pathSeparator);
      for (String s : classpath) {
        if (s.contains(PaxRunnerLib)) {
          path = LocalFileSystem.getInstance().findFileByPath(s);
          if (path != null) {
            libs.add(path);
            break;
          }
        }
      }
    }
    else {
      libs.add(path);
    }
    return libs;
  }


  public void fillCommandLineParameters(@NotNull ParametersList commandLineParameters, @NotNull SelectedBundle[] bundlesToInstall) {
    commandLineParameters.add("--p=" + getOsgiFrameworkName().toLowerCase());

    for (SelectedBundle bundle : bundlesToInstall) {
      if (bundle.isStartAfterInstallation() && !CachingBundleInfoProvider.isFragmentBundle(bundle.getBundleUrl())) {
        commandLineParameters.add(bundle.getBundleUrl() + "@" + bundle.getStartLevel());
      }
      else {
        if ( CachingBundleInfoProvider.isFragmentBundle(bundle.getBundleUrl())) {
          commandLineParameters.add(bundle.getBundleUrl() + "@nostart");
        }
        else{
          commandLineParameters.add(bundle.getBundleUrl());
        }
      }
    }
    final P frameworkProperties = getFrameworkProperties();
    String bootDelegation = frameworkProperties.getBootDelegation();
    if (bootDelegation != null && !(bootDelegation.trim().length() == 0)) {
      commandLineParameters.add("--bd="+bootDelegation);
    }

    String systemPackages = frameworkProperties.getSystemPackages();
    if (systemPackages != null && !(systemPackages.trim().length() == 0)) {
      commandLineParameters.add("--sp=" + systemPackages);
    }

    int startLevel = getFrameworkStartLevel(bundlesToInstall);
    commandLineParameters.add("--sl="+startLevel);

    if (frameworkProperties.isDebugMode()) {
      commandLineParameters.add("--log=DEBUG");
    }

    if (frameworkProperties.isStartConsole()) {
      commandLineParameters.add("--console");
    }
    else {
      commandLineParameters.add("--noConsole");
    }


    StringBuilder vmOptionsParam = new StringBuilder();
    vmOptionsParam.append("--vmOptions=");
    String vmParameters = getRunConfiguration().getVmParameters();

    if (vmParameters.length() > 0) {
      vmOptionsParam.append(vmParameters);
    }
    String additionalVmOptions = getAdditionalTargetVMProperties(bundlesToInstall);
    if (additionalVmOptions.length() > 0) {
      vmOptionsParam.append(" ").append(additionalVmOptions);
    }

    if (isDebugRun()) {
      String debugParams =
        "-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=n,suspend=n,address=" + getDebugPort();
      vmOptionsParam.append(" ").append(debugParams);

    }
    commandLineParameters.add(vmOptionsParam.toString());
    commandLineParameters.add("--keepOriginalUrls");
    commandLineParameters.add("--skipInvalidBundles");
  }

  public void fillVmParameters(ParametersList vmParameters, @NotNull SelectedBundle[] bundlesToInstall) {
    // nothing to do here..
  }

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
   * Returns a list of additional VM parameters that should be given to the VM that is launched by PAX. For convencience this method
   * will return the empty string in this base class, so overriding classes do not need to call super.
   *
   * @param urlsOfBundlesToInstall the list of bundles to install
   * @return a string with VM parameters.
   */
  @NotNull
  protected String getAdditionalTargetVMProperties(@NotNull SelectedBundle[] urlsOfBundlesToInstall) {
    return "";
  }


  @NotNull
  @NonNls
  public final String getMainClass() {
    return "org.ops4j.pax.runner.Run";
  }


  protected final Pattern getFrameworkStarterClasspathPattern() {
    return null;
  }


}
