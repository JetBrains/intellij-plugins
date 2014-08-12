package org.jetbrains.osgi.jps.model.impl;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsGlobal;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsGlobalExtensionSerializer;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;
import org.jetbrains.jps.model.serialization.facet.JpsFacetConfigurationSerializer;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.osgi.jps.model.JpsOsmorcProjectExtension;

import java.util.Collections;
import java.util.List;

/**
 * @author michael.golubev
 */
public class JpsOsmorcModelSerializerExtension extends JpsModelSerializerExtension {
  private static final String COMPONENT_NAME = "Osmorc";

  @NotNull
  @Override
  public List<? extends JpsGlobalExtensionSerializer> getGlobalExtensionSerializers() {
    return Collections.singletonList(new OsmorcGlobalExtensionSerializer());
  }

  @NotNull
  @Override
  public List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
    return Collections.singletonList(new JpsOsmorcProjectExtensionSerializer());
  }

  @NotNull
  @Override
  public List<? extends JpsFacetConfigurationSerializer<?>> getFacetConfigurationSerializers() {
    return Collections.singletonList(new JpsOsmorcModuleExtensionSerializer());
  }

  private static class OsmorcGlobalExtensionSerializer extends JpsGlobalExtensionSerializer {
    private OsmorcGlobalExtensionSerializer() {
      super("osmorc.xml", COMPONENT_NAME);
    }

    @Override
    public void loadExtension(@NotNull JpsGlobal element, @NotNull Element componentTag) {
      doLoadExtension(XmlSerializer.deserialize(componentTag, OsmorcGlobalExtensionProperties.class));
    }

    @Override
    public void loadExtensionWithDefaultSettings(@NotNull JpsGlobal global) {
      doLoadExtension(null);
    }

    private static void doLoadExtension(@Nullable OsmorcGlobalExtensionProperties properties) {
      if (properties == null) properties = new OsmorcGlobalExtensionProperties();
      JpsOsmorcExtensionService.getInstance().setGlobalProperties(properties);
    }

    @Override
    public void saveExtension(@NotNull JpsGlobal element, @NotNull Element componentTag) { }
  }

  private static class JpsOsmorcProjectExtensionSerializer extends JpsProjectExtensionSerializer {
    private JpsOsmorcProjectExtensionSerializer() {
      super(null, COMPONENT_NAME);
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
      project.getContainer().setChild(JpsOsmorcProjectExtension.ROLE, new JpsOsmorcProjectExtensionImpl(settings));
    }

    @Override
    public void saveExtension(@NotNull JpsProject project, @NotNull Element componentTag) { }
  }

  private static class JpsOsmorcModuleExtensionSerializer extends JpsFacetConfigurationSerializer<JpsOsmorcModuleExtension> {
    private JpsOsmorcModuleExtensionSerializer() {
      super(JpsOsmorcModuleExtension.ROLE, "Osmorc", "OSGi");
    }

    @Override
    protected JpsOsmorcModuleExtension loadExtension(@NotNull Element element, String name, JpsElement parent, JpsModule module) {
      OsmorcModuleExtensionProperties properties = XmlSerializer.deserialize(element, OsmorcModuleExtensionProperties.class);
      return new JpsOsmorcModuleExtensionImpl(properties != null ? properties : new OsmorcModuleExtensionProperties());
    }

    @Override
    protected void saveExtension(JpsOsmorcModuleExtension extension, Element element, JpsModule module) {
      OsmorcModuleExtensionProperties properties = ((JpsOsmorcModuleExtensionImpl)extension).getProperties();
      XmlSerializer.serializeInto(properties, element);
    }
  }
}
