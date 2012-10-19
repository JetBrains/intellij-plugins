package com.intellij.flex.model.run;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.serialization.runConfigurations.JpsRunConfigurationPropertiesSerializer;

public class JpsFlexUnitRunConfigurationType extends JpsRunConfigurationType<JpsFlexUnitRunnerParameters> {

  public static final JpsFlexUnitRunConfigurationType INSTANCE = new JpsFlexUnitRunConfigurationType();
  public static final String ID = "FlexUnitRunConfigurationType";

  private JpsFlexUnitRunConfigurationType() {
  }

  public static JpsRunConfigurationPropertiesSerializer<JpsFlexUnitRunnerParameters> createRunConfigurationSerializer() {
    return new JpsRunConfigurationPropertiesSerializer<JpsFlexUnitRunnerParameters>(INSTANCE, ID) {
      public JpsFlexUnitRunnerParameters loadProperties(@Nullable final Element runConfigurationTag) {
        final JpsFlexUnitRunnerParameters properties = runConfigurationTag != null
                                                       ? XmlSerializer.deserialize(runConfigurationTag, JpsFlexUnitRunnerParameters.class)
                                                       : null;
        return properties != null ? properties : new JpsFlexUnitRunnerParameters();
      }

      public void saveProperties(final JpsFlexUnitRunnerParameters properties, final Element runConfigurationTag) {
      }
    };
  }
}
