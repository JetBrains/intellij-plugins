package org.jetbrains.jps.osmorc.model;

import org.jetbrains.jps.model.JpsElement;

/**
 * @author michael.golubev
 */
public interface JpsOsmorcProjectExtension extends JpsElement {

  String getBundlesOutputPath();

  String getDefaultManifestFileLocation();
}
