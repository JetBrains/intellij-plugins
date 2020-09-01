// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.run;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.serialization.runConfigurations.JpsRunConfigurationPropertiesSerializer;

public final class JpsFlashRunConfigurationType extends JpsElementTypeBase<JpsFlashRunnerParameters> implements JpsRunConfigurationType<JpsFlashRunnerParameters> {

  public static final JpsFlashRunConfigurationType INSTANCE = new JpsFlashRunConfigurationType();
  public static final String ID = "FlashRunConfigurationType";

  private JpsFlashRunConfigurationType() {
  }

  public static JpsRunConfigurationPropertiesSerializer<JpsFlashRunnerParameters> createRunConfigurationSerializer() {
    return new JpsRunConfigurationPropertiesSerializer<JpsFlashRunnerParameters>(INSTANCE, ID) {
      @Override
      public JpsFlashRunnerParameters loadProperties(@Nullable final Element runConfigurationTag) {
        final JpsFlashRunnerParameters properties = runConfigurationTag != null
                                                    ? XmlSerializer.deserialize(runConfigurationTag, JpsFlashRunnerParameters.class)
                                                    : null;
        return properties != null ? properties : new JpsFlashRunnerParameters();
      }
    };
  }
}
