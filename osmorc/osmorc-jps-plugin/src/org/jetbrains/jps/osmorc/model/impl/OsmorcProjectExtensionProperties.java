package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.annotations.OptionTag;

/**
 * @author michael.golubev
 */
public class OsmorcProjectExtensionProperties {

  public static final String DEFAULT_MANIFEST_LOCATION = "META-INF/MANIFEST.MF";

  @OptionTag("bundlesOutputPath")
  public String myBundlesOutputPath = "";

  @OptionTag("defaultManifestFileLocation")
  public String myDefaultManifestFileLocation = DEFAULT_MANIFEST_LOCATION;
}
