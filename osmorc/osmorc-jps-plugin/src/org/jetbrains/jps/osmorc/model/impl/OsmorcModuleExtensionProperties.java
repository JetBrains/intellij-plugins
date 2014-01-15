package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.jps.osmorc.model.OutputPathType;

/**
 * @author michael.golubev
 */
public class OsmorcModuleExtensionProperties {

  @Attribute("jarfileLocation")
  public String myJarFileLocation = "";

  @Attribute("outputPathType")
  public OutputPathType myOutputPathType = OutputPathType.CompilerOutputPath;
}
