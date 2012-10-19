package com.intellij.flex.model.run;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.serialization.runConfigurations.JpsRunConfigurationPropertiesSerializer;

public class JpsFlashRunConfigurationType extends JpsRunConfigurationType<JpsFlashRunnerParameters> {

  public static final JpsFlashRunConfigurationType INSTANCE = new JpsFlashRunConfigurationType();
  public static final String ID = "FlashRunConfigurationType";

  private JpsFlashRunConfigurationType() {
  }

  public static JpsRunConfigurationPropertiesSerializer<JpsFlashRunnerParameters> createRunConfigurationSerializer() {
    return new JpsRunConfigurationPropertiesSerializer<JpsFlashRunnerParameters>(INSTANCE, ID) {
      public JpsFlashRunnerParameters loadProperties(@Nullable final Element runConfigurationTag) {
        final JpsFlashRunnerParameters properties = runConfigurationTag != null
                                                    ? XmlSerializer.deserialize(runConfigurationTag, JpsFlashRunnerParameters.class)
                                                    : null;
        return properties != null ? properties : new JpsFlashRunnerParameters();
      }

      public void saveProperties(final JpsFlashRunnerParameters properties, final Element runConfigurationTag) {
      }
    };
  }
}
