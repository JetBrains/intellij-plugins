package com.intellij.flex.model.lib;

import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.library.JpsLibraryType;
import org.jetbrains.jps.model.serialization.library.JpsLibraryPropertiesSerializer;

public class JpsFlexLibraryType extends JpsElementTypeBase<JpsSimpleElement<JpsFlexLibraryProperties>> implements JpsLibraryType<JpsSimpleElement<JpsFlexLibraryProperties>> {

  public static final JpsFlexLibraryType INSTANCE = new JpsFlexLibraryType();

  private static final String ID = "flex";
  private static final String ID_ATTR = "id";

  public static JpsLibraryPropertiesSerializer<JpsSimpleElement<JpsFlexLibraryProperties>> createLibraryPropertiesSerializer() {
    return new JpsLibraryPropertiesSerializer<JpsSimpleElement<JpsFlexLibraryProperties>>(INSTANCE, ID) {
      @Override
      public JpsSimpleElement<JpsFlexLibraryProperties> loadProperties(@Nullable final Element propertiesElement) {
        final String libraryId = propertiesElement == null ? null : propertiesElement.getAttributeValue(ID_ATTR);
        return JpsElementFactory.getInstance().createSimpleElement(new JpsFlexLibraryProperties(libraryId));
      }
    };
  }
}
