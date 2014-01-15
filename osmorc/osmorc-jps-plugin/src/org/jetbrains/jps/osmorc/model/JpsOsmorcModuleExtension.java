package org.jetbrains.jps.osmorc.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.module.JpsModule;

/**
 * @author michael.golubev
 */
public interface JpsOsmorcModuleExtension extends JpsElement {

  @NotNull
  String getJarFileLocation();

  JpsModule getModule();
}
