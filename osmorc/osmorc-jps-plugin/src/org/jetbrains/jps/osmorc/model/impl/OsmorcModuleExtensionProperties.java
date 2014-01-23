package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.jps.osmorc.model.ManifestGenerationMode;
import org.jetbrains.jps.osmorc.model.OutputPathType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michael.golubev
 */
public class OsmorcModuleExtensionProperties {

  @Attribute("jarfileLocation")
  public String myJarFileLocation = "";

  @Attribute("outputPathType")
  public OutputPathType myOutputPathType = OutputPathType.CompilerOutputPath;

  @Attribute("manifestGenerationMode")
  public ManifestGenerationMode myManifestGenerationMode = ManifestGenerationMode.OsmorcControlled;

  @Attribute("bndFileLocation")
  public String myBndFileLocation;

  @Attribute("additionalProperties")
  public String myAdditionalProperties;

  @Attribute("bundleSymbolicName")
  public String myBundleSymbolicName;

  @Attribute("bundleVersion")
  public String myBundleVersion;

  @Attribute("bundleActivator")
  public String myBundleActivator;

  @Attribute("manifestLocation")
  public String myManifestLocation;

  @Tag("additionalJARContents")
  @AbstractCollection(surroundWithTag = true)
  public List<OsmorcJarContentEntry> myAdditionalJARContents = new ArrayList<OsmorcJarContentEntry>();

  @Attribute("ignoreFilePattern")
  public String myIgnoreFilePattern;

  @Attribute("bundlorFileLocation")
  public String myBundlorFileLocation;
}
