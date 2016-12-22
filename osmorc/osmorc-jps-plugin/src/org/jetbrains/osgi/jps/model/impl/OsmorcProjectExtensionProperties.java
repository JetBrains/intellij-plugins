package org.jetbrains.osgi.jps.model.impl;

import com.intellij.util.xmlb.annotations.OptionTag;

import java.util.jar.JarFile;

/**
 * @author michael.golubev
 */
public class OsmorcProjectExtensionProperties {
  @OptionTag("bundlesOutputPath")
  public String myBundlesOutputPath = "";

  @OptionTag("defaultManifestFileLocation")
  public String myDefaultManifestFileLocation = JarFile.MANIFEST_NAME;

  @OptionTag("bndWorkspace")
  public Boolean myBndWorkspace;
}
