package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsGlobal;
import org.jetbrains.jps.model.serialization.JpsGlobalExtensionSerializer;
import org.jetbrains.jps.osmorc.model.JpsOsmorcExtensionService;

/**
 * @author michael.golubev
 */
public class JpsOsmorcGlobalExtensionSerializer extends JpsGlobalExtensionSerializer {

  public JpsOsmorcGlobalExtensionSerializer() {
    super("osmorc.xml", "Osmorc");
  }

  @Override
  public void loadExtension(@NotNull JpsGlobal element, @NotNull Element componentTag) {
    OsmorcGlobalExtensionProperties properties = XmlSerializer.deserialize(componentTag, OsmorcGlobalExtensionProperties.class);
    doLoadExtension(properties);
  }

  @Override
  public void loadExtensionWithDefaultSettings(@NotNull JpsGlobal global) {
    doLoadExtension(null);
  }

  private static void doLoadExtension(@Nullable OsmorcGlobalExtensionProperties properties) {
    if (properties == null) {
      properties = new OsmorcGlobalExtensionProperties();
    }
    JpsOsmorcExtensionService.getInstance().setGlobalProperties(properties);
  }

  @Override
  public void saveExtension(@NotNull JpsGlobal element, @NotNull Element componentTag) {
    throw new UnsupportedOperationException();
  }
}
