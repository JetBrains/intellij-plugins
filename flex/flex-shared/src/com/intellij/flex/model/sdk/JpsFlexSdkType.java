package com.intellij.flex.model.sdk;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.library.sdk.JpsSdkType;
import org.jetbrains.jps.model.serialization.library.JpsSdkPropertiesSerializer;

public class JpsFlexSdkType extends JpsSdkType<JpsDummyElement> {
  public static final String ID = "Flex SDK Type (new)";
  public static final JpsFlexSdkType INSTANCE = new JpsFlexSdkType();

  @Override
  public String getPresentableName() {
    return "Flex/AIR SDK";
  }

  public static JpsSdkPropertiesSerializer<JpsDummyElement> createJpsSdkPropertiesSerializer() {
    return new JpsSdkPropertiesSerializer<JpsDummyElement>(ID, INSTANCE) {
      @Override
      @NotNull
      public JpsDummyElement loadProperties(@Nullable final Element propertiesElement) {
        return JpsElementFactory.getInstance().createDummyElement();
      }
    };
  }
}
