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

import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkRunner;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;
import org.osmorc.run.ui.SelectedBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class EquinoxRunner extends AbstractFrameworkRunner {
  static final String MAIN_CLASS = "org.eclipse.core.runtime.adaptor.EclipseStarter";

  /**
   * See <a href="http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/reference/misc/runtime-options.html">Eclipse runtime options</a>.
   */
  @Override
  protected void setupParameters(@NotNull JavaParameters parameters) {
    ParametersList vmParameters = parameters.getVMParametersList();

    // bundles and start levels

    List<String> bundles = new ArrayList<>();

    for (SelectedBundle bundle : myBundles) {
      String bundlePath = bundle.getBundlePath();
      if (bundlePath == null) continue;
      boolean isFragment = CachingBundleInfoProvider.isFragmentBundle(bundlePath);
      String bundleUrl = toFileUri(bundlePath);

      if (!isFragment) {
        int startLevel = getBundleStartLevel(bundle);
        bundleUrl += "@" + startLevel;
        if (bundle.isStartAfterInstallation()) {
          bundleUrl += ":start";
        }
      }

      bundles.add(bundleUrl);
    }

    if (!bundles.isEmpty()) {
      vmParameters.addProperty("osgi.bundles", StringUtil.join(bundles, ","));
    }

    int startLevel = getFrameworkStartLevel();
    vmParameters.addProperty("osgi.startLevel", String.valueOf(startLevel));

    int defaultStartLevel = myRunConfiguration.getDefaultStartLevel();
    vmParameters.addProperty("osgi.bundles.defaultStartLevel", String.valueOf(defaultStartLevel));

    // framework-specific options

    if (GenericRunProperties.isStartConsole(myAdditionalProperties)) {
      vmParameters.addProperty("osgi.console");
      if (VersionComparatorUtil.compare(myInstance.getVersion(), "3.8") < 0) {
        vmParameters.addProperty("osgi.console.enable.builtin", "true");
      }
    }

    vmParameters.addProperty("osgi.clean", "true");

    if (GenericRunProperties.isDebugMode(myAdditionalProperties)) {
      vmParameters.addProperty("osgi.debug");
      vmParameters.addProperty("eclipse.consoleLog", "true");
    }

    String product = EquinoxRunProperties.getEquinoxProduct(myAdditionalProperties);
    String application = EquinoxRunProperties.getEquinoxApplication(myAdditionalProperties);
    if (!StringUtil.isEmptyOrSpaces(product)) {
      vmParameters.defineProperty("eclipse.product", product);
      vmParameters.defineProperty("eclipse.ignoreApp", "false");
    }
    else if (!StringUtil.isEmptyOrSpaces(application)) {
      vmParameters.defineProperty("eclipse.application", application);
      vmParameters.defineProperty("eclipse.ignoreApp", "false");
    }
    else {
      vmParameters.defineProperty("eclipse.ignoreApp", "true");
    }

    parameters.setMainClass(MAIN_CLASS);
  }
}
