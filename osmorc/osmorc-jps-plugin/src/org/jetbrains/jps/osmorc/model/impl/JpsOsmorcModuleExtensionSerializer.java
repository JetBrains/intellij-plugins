package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.facet.JpsFacetConfigurationSerializer;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;

/**
 * @author michael.golubev
 */
public class JpsOsmorcModuleExtensionSerializer extends JpsFacetConfigurationSerializer<JpsOsmorcModuleExtension> {

  public JpsOsmorcModuleExtensionSerializer() {
    super(JpsOsmorcModuleExtensionImpl.ROLE, "Osmorc", "OSGi");
  }

  @Override
  protected JpsOsmorcModuleExtension loadExtension(@NotNull Element facetConfigurationElement,
                                                   String name,
                                                   JpsElement parent,
                                                   JpsModule module) {
    OsmorcModuleExtensionProperties properties = XmlSerializer
      .deserialize(facetConfigurationElement, OsmorcModuleExtensionProperties.class);
    return new JpsOsmorcModuleExtensionImpl(properties != null ? properties : new OsmorcModuleExtensionProperties());
  }

  @Override
  protected void saveExtension(JpsOsmorcModuleExtension extension, Element facetConfigurationTag, JpsModule module) {
    XmlSerializer.serializeInto(((JpsOsmorcModuleExtensionImpl)extension).getProperties(), facetConfigurationTag);
  }
}
