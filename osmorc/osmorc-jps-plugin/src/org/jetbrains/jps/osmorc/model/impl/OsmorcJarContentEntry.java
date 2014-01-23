package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

/**
 * @author michael.golubev
 */
@Tag("entry")
public class OsmorcJarContentEntry {

  @Attribute("source")
  public String mySource;

  @Attribute("dest")
  public String myDestination;
}
