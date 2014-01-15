package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.annotations.OptionTag;

/**
 * @author michael.golubev
 */
public class OsmorcProjectExtensionProperties {

  @OptionTag("bundlesOutputPath")
  public String myBundlesOutputPath = "";
}
