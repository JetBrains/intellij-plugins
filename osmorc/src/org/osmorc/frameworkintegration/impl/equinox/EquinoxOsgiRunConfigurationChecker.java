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

import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.openapi.util.SystemInfo;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.impl.DefaultOsgiRunConfigurationChecker;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.OsgiRunConfiguration;

/**
 * Runconfiguration checker for the Equinox framework.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxOsgiRunConfigurationChecker extends DefaultOsgiRunConfigurationChecker {


  public EquinoxOsgiRunConfigurationChecker() {
  }


  @Override
  protected void checkFrameworkSpecifics(OsgiRunConfiguration runConfiguration) throws RuntimeConfigurationWarning {
    FrameworkInstanceDefinition frameworkInstanceDefinition = runConfiguration.getInstanceToUse();
    assert frameworkInstanceDefinition != null;

    EquinoxRunProperties runProperties = new EquinoxRunProperties(runConfiguration.getAdditionalProperties());

    if (runProperties.getEquinoxApplication() != null && runProperties.getEquinoxApplication().length() > 0 ||
        runProperties.getEquinoxProduct() != null && runProperties.getEquinoxProduct().length() > 0) {
      if (SystemInfo.isMac && !runConfiguration.getVmParameters().contains("-XstartOnFirstThread")) {
        throw new RuntimeConfigurationWarning(
          "Using the JVM option -XstartOnFirstThread for running SWT apps on Mac OS X is highly recommended.");
      }

      if (runProperties.isStartConsole()) {
        throw new RuntimeConfigurationWarning(OsmorcBundle.getTranslation("runconfiguration.equinox.runningWithConsole"));
      }
    }
  }
}
