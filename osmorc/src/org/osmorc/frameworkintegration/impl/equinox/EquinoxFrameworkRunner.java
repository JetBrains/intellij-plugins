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
package org.osmorc.frameworkintegration.impl.equinox;

import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.ConfigurationMethod;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Framework runner for the Equinox OSGi framework.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class EquinoxFrameworkRunner extends AbstractFrameworkRunner<EquinoxRunProperties>
{


  public EquinoxFrameworkRunner()
  {
    createTempFolder();
  }

  @NotNull
  public ConfigurationMethod[] getConfigurationMethods()
  {
    return new ConfigurationMethod[0];
  }

  @NotNull
  public String[] getCommandlineParameters(@NotNull SelectedBundle[] urlsOfBundlesToInstall,
                                           @NotNull EquinoxRunProperties runProperties)
  {
    List<String> result = new ArrayList<String>();
    String configurationDir = getWorkingDirectory() + File.separator + "fwDir";
    result.add("-configuration");
    result.add(configurationDir);

    if (runProperties.isStartEquinoxOSGIConsole())
    {
      result.add("-console");
    }
    if (runProperties.isDebugMode())
    {
      result.add("-debug");
      result.add("-consoleLog");
    }
    return result.toArray(new String[result.size()]);
  }

  @NotNull
  public Map<String, String> getSystemProperties(@NotNull SelectedBundle[] bundlesToInstall,
                                                 @NotNull EquinoxRunProperties runProperties)
  {
    Map<String, String> result = new HashMap<String, String>();
    StringBuilder bundleUrls = new StringBuilder();
    int level = 0;
    for (SelectedBundle selectedBundle : bundlesToInstall)
    {
      String bundleUrl = selectedBundle.getBundleUrl();
      bundleUrls.append(bundleUrl);
      if (selectedBundle.shouldBeStarted())
      {
        // keep start level even for fragment bundles
        bundleUrls.append("@").append(selectedBundle.getStartLevel());
        // dont attach "start" on fragment bundles
        if (!CachingBundleInfoProvider.isFragmentBundle(selectedBundle.getBundleUrl()))
        {
          bundleUrls.append(":start");
        }
      }
      bundleUrls.append(",");
      level = Math.max(level, selectedBundle.getStartLevel());
    }
    if (bundleUrls.length() > 0)
    {
      bundleUrls.delete(bundleUrls.length() - 1, bundleUrls.length());
    }

    result.put("osgi.bundles", bundleUrls.toString());
    result.put("osgi.framework.beginningstartlevel", String.valueOf(level));
    result.put("osgi.startLevel", String.valueOf(level));
    String systemPackages = runProperties.getSystemPackages();
    if (systemPackages != null && !(systemPackages.trim().length() == 0))
    {
      String pkg = result.get("org.osgi.framework.system.packages") + "," + systemPackages;
      result.put("org.osgi.framework.system.packages", pkg);
    }
    String bootDelegation = runProperties.getBootDelegation();
    if (bootDelegation != null && !(bootDelegation.trim().length() == 0))
    {
      result.put("org.osgi.framework.bootdelegation", bootDelegation);
    }
    result.put("eclipse.ignoreApp", "true");
    return result;
  }

  public void runCustomInstallationSteps(@NotNull SelectedBundle[] urlsOfBundlesToInstall,
                                         @NotNull EquinoxRunProperties runProperties)
  {
  }

  public boolean supportsExplodedBundles()
  {
    return false;
  }

  @NotNull
  public String getMainClass()
  {
    return "org.eclipse.core.runtime.adaptor.EclipseStarter";
  }

  @NotNull
  public EquinoxRunProperties convertProperties(Map<String, String> properties)
  {
    return new EquinoxRunProperties(properties);
  }

  public Pattern getFrameworkStarterClasspathPattern()
  {
    return _frameworkStarterJarNamePattern;
  }

  private static final Pattern _frameworkStarterJarNamePattern = Pattern.compile("^org\\.eclipse\\.osgi_.*\\.jar");
}
