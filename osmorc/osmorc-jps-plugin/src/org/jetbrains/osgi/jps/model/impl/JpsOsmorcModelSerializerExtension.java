// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps.model.impl;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;
import org.jetbrains.jps.model.serialization.facet.JpsFacetConfigurationSerializer;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.osgi.jps.model.JpsOsmorcProjectExtension;

import java.util.Collections;
import java.util.List;

/**
 * @author michael.golubev
 */
public final class JpsOsmorcModelSerializerExtension extends JpsModelSerializerExtension {
  private static final String COMPONENT_NAME = "Osmorc";

  @Override
  public @NotNull List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
    return Collections.singletonList(new JpsOsmorcProjectExtensionSerializer());
  }

  @Override
  public @NotNull List<? extends JpsFacetConfigurationSerializer<?>> getFacetConfigurationSerializers() {
    return Collections.singletonList(new JpsOsmorcModuleExtensionSerializer());
  }

  private static final class JpsOsmorcProjectExtensionSerializer extends JpsProjectExtensionSerializer {
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
  }

  private static final class JpsOsmorcModuleExtensionSerializer extends JpsFacetConfigurationSerializer<JpsOsmorcModuleExtension> {
    private JpsOsmorcModuleExtensionSerializer() {
      super(JpsOsmorcModuleExtension.ROLE, "Osmorc", "OSGi");
    }

    @Override
    protected JpsOsmorcModuleExtension loadExtension(@NotNull Element element, String name, JpsElement parent, JpsModule module) {
      return new JpsOsmorcModuleExtensionImpl(XmlSerializer.deserialize(element, OsmorcModuleExtensionProperties.class));
    }
  }
}
