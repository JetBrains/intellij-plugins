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

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionProcessor;
import com.intellij.conversion.ProjectConverter;
import com.intellij.conversion.RunManagerSettings;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.util.ArrayList;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxRunConfigurationConverter extends ProjectConverter {
  @Override
  public ConversionProcessor<RunManagerSettings> createRunConfigurationsConverter() {
    return new ActualConverter();
  }

  private static final class ActualConverter extends ConversionProcessor<RunManagerSettings> {

    @Override
    public boolean isConversionNeeded(RunManagerSettings runManagerSettings) {
      for (Element element : runManagerSettings.getRunConfigurations()) {
        final String confType = element.getAttributeValue("type");
        if (EQUINOX_RUN_CONFIGURATION_TYPE.equals(confType)) {
          return true;
        }
      }

      return false;
    }

    @Override
    public void process(RunManagerSettings runManagerSettings) throws CannotConvertException {
      for (Element element : runManagerSettings.getRunConfigurations()) {
        final String confType = element.getAttributeValue("type");
        if (EQUINOX_RUN_CONFIGURATION_TYPE.equals(confType)) {

          String application = element.getAttributeValue(APPLICATION_ATTRIBUTE);
          String product = element.getAttributeValue(PRODUCT_ATTRIBUTE);
          String workingDir = element.getAttributeValue(WORKING_DIR_ATTRIBUTE);
          String configDir = element.getAttributeValue(CONFIG_DIR_ATTRIBUTE);
          String jvmArgs = element.getAttributeValue(JVM_ARGS_ATTRIBUTE);
          String additionalEquinoxArgs = element.getAttributeValue(ADDITIONAL_ARGS_ATTRIBUTE);
          boolean equinoxConsole = Boolean.valueOf(element.getAttributeValue(EQUINOX_CONSOLE, "false"));
          boolean equinoxDebug = Boolean.valueOf(element.getAttributeValue(EQUINOX_DEBUG, "false"));
          boolean clean = Boolean.valueOf(element.getAttributeValue(CLEAN, "true"));

          element.removeAttribute(APPLICATION_ATTRIBUTE);
          element.removeAttribute(PRODUCT_ATTRIBUTE);
          element.removeAttribute(WORKING_DIR_ATTRIBUTE);
          element.removeAttribute(CONFIG_DIR_ATTRIBUTE);
          element.removeAttribute(JVM_ARGS_ATTRIBUTE);
          element.removeAttribute(ADDITIONAL_ARGS_ATTRIBUTE);
          element.removeAttribute(EQUINOX_CONSOLE);
          element.removeAttribute(EQUINOX_DEBUG);
          element.removeAttribute(USE_UPDATE_CONFIGURATOR);
          element.removeAttribute(CLEAN);

          OsgiRunConfiguration osgiRunConfiguration = new OsgiRunConfiguration(null, null, null);

          osgiRunConfiguration.setWorkingDir(workingDir);
          osgiRunConfiguration.setVmParameters(jvmArgs);
          osgiRunConfiguration.setProgramParameters(additionalEquinoxArgs);

          EquinoxRunProperties runProperties = new EquinoxRunProperties(osgiRunConfiguration.getAdditionalProperties());
          runProperties.setEquinoxApplication(application);
          runProperties.setEquinoxProduct(product);
          runProperties.setStartConsole(equinoxConsole);
          runProperties.setDebugMode(equinoxDebug);

          osgiRunConfiguration.putAdditionalProperties(runProperties.getProperties());

          ArrayList<SelectedBundle> bundles = new ArrayList<SelectedBundle>();
          bundles.add(
            new SelectedBundle("legacyLoader", "org.osmorc.frameworkintegration.impl.equinox.LegacyEquinoxOsgiRunConfigurationLoader",
                               SelectedBundle.BundleType.FrameworkBundle));
          osgiRunConfiguration.setBundlesToDeploy(bundles);

          element.setAttribute("type", "#org.osmorc.OsgiConfigurationType");
          element.setAttribute("factoryName", "OSGi Bundles");

          try {
            osgiRunConfiguration.writeExternal(element);
          }
          catch (WriteExternalException e) {
            throw new CannotConvertException("Error while converting legacy Eclipse Equinox run configuration", e);
          }
        }
      }
    }

    private static final String EQUINOX_RUN_CONFIGURATION_TYPE = "#org.osmorc.EquinoxConfigurationType";
    @NonNls
    private static final String APPLICATION_ATTRIBUTE = "application";
    @NonNls
    private static final String PRODUCT_ATTRIBUTE = "product";
    @NonNls
    private static final String WORKING_DIR_ATTRIBUTE = "workingDir";
    @NonNls
    private static final String CONFIG_DIR_ATTRIBUTE = "configDir";
    @NonNls
    private static final String JVM_ARGS_ATTRIBUTE = "jvmArgs";
    @NonNls
    private static final String ADDITIONAL_ARGS_ATTRIBUTE = "additionalEquinoxArgs";
    @NonNls
    private static final String EQUINOX_CONSOLE = "equinoxConsole";
    @NonNls
    private static final String EQUINOX_DEBUG = "equinoxDebug";
    @NonNls
    private static final String USE_UPDATE_CONFIGURATOR = "useUpdateConfigurator";
    @NonNls
    private static final String CLEAN = "clean";
  }
}
