package com.intellij.flex.model.lib;

import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.JpsLibraryType;
import org.jetbrains.jps.model.serialization.JpsLibraryPropertiesSerializer;

public class JpsFlexLibraryType extends JpsLibraryType<JpsSimpleElement<JpsFlexLibraryProperties>> {

  public static final JpsFlexLibraryType INSTANCE = new JpsFlexLibraryType();

  private static final String ID = "flex";
  private static final String ID_ATTR = "id";

  public static JpsLibraryPropertiesSerializer<JpsSimpleElement<JpsFlexLibraryProperties>> createLibraryPropertiesSerializer() {
    return new JpsLibraryPropertiesSerializer<JpsSimpleElement<JpsFlexLibraryProperties>>(INSTANCE, ID) {
      public JpsSimpleElement<JpsFlexLibraryProperties> loadProperties(@Nullable final Element propertiesElement) {
        final String libraryId = propertiesElement == null ? null : propertiesElement.getAttributeValue(ID_ATTR);
        return JpsElementFactory.getInstance().createSimpleElement(new JpsFlexLibraryProperties(libraryId));
      }

      public void saveProperties(final JpsSimpleElement<JpsFlexLibraryProperties> propertiesElement, final Element element) {
        final String libraryId = propertiesElement.getData().getLibraryId();
        if (libraryId != null) {
          element.setAttribute(ID_ATTR, libraryId);
        }
      }
    };
  }
}
