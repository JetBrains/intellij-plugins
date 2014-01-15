package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;
import org.jetbrains.jps.osmorc.model.JpsOsmorcProjectExtension;

/**
 * @author michael.golubev
 */
public class JpsOsmorcProjectExtensionSerializer extends JpsProjectExtensionSerializer {

  public JpsOsmorcProjectExtensionSerializer() {
    super(null, "Osmorc");
  }

  @Override
  public void loadExtension(@NotNull JpsProject project, @NotNull Element componentTag) {
    doLoadExtension(project, XmlSerializer.deserialize(componentTag, OsmorcProjectExtensionProperties.class));
  }

  @Override
  public void loadExtensionWithDefaultSettings(@NotNull JpsProject project) {
    doLoadExtension(project, null);
  }

  private static void doLoadExtension(@NotNull JpsProject project, @Nullable OsmorcProjectExtensionProperties settings) {
    if (settings == null) settings = new OsmorcProjectExtensionProperties();
    JpsOsmorcProjectExtension component = new JpsOsmorcProjectExtensionImpl(settings);
    project.getContainer().setChild(JpsOsmorcProjectExtensionImpl.ROLE, component);
  }

  @Override
  public void saveExtension(@NotNull JpsProject project, @NotNull Element componentTag) {
    throw new UnsupportedOperationException();
  }
}
