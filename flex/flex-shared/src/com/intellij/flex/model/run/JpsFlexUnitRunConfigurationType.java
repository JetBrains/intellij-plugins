// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.run;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.serialization.runConfigurations.JpsRunConfigurationPropertiesSerializer;

public final class JpsFlexUnitRunConfigurationType extends JpsElementTypeBase<JpsFlexUnitRunnerParameters> implements JpsRunConfigurationType<JpsFlexUnitRunnerParameters> {

  public static final JpsFlexUnitRunConfigurationType INSTANCE = new JpsFlexUnitRunConfigurationType();
  public static final String ID = "FlexUnitRunConfigurationType";

  private JpsFlexUnitRunConfigurationType() {
  }

  public static JpsRunConfigurationPropertiesSerializer<JpsFlexUnitRunnerParameters> createRunConfigurationSerializer() {
    return new JpsRunConfigurationPropertiesSerializer<JpsFlexUnitRunnerParameters>(INSTANCE, ID) {
      @Override
      public JpsFlexUnitRunnerParameters loadProperties(@Nullable final Element runConfigurationTag) {
        final JpsFlexUnitRunnerParameters properties = runConfigurationTag != null
                                                       ? XmlSerializer.deserialize(runConfigurationTag, JpsFlexUnitRunnerParameters.class)
                                                       : null;
        return properties != null ? properties : new JpsFlexUnitRunnerParameters();
      }

      @Override
      public void saveProperties(final JpsFlexUnitRunnerParameters properties, final Element runConfigurationTag) {
      }
    };
  }
}
