/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class JpsOsmorcModelSerializerExtension extends JpsModelSerializerExtension {
  private static final String COMPONENT_NAME = "Osmorc";

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
      return new JpsOsmorcModuleExtensionImpl(XmlSerializer.deserialize(element, OsmorcModuleExtensionProperties.class));
    }

    @Override
    protected void saveExtension(JpsOsmorcModuleExtension extension, Element element, JpsModule module) {
      OsmorcModuleExtensionProperties properties = ((JpsOsmorcModuleExtensionImpl)extension).getProperties();
      XmlSerializer.serializeInto(properties, element);
    }
  }
}
