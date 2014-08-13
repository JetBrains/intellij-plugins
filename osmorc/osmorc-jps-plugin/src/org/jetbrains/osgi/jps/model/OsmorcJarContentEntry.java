package org.jetbrains.osgi.jps.model;

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
