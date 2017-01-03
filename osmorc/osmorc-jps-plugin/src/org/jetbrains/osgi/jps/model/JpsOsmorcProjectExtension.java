package org.jetbrains.osgi.jps.model;

import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

/**
 * @author michael.golubev
 */
public interface JpsOsmorcProjectExtension extends JpsElement {
  JpsElementChildRole<JpsOsmorcProjectExtension> ROLE = JpsElementChildRoleBase.create("Osmorc");

  String getBundlesOutputPath();

  String getDefaultManifestFileLocation();

  boolean isBndWorkspace();
}
