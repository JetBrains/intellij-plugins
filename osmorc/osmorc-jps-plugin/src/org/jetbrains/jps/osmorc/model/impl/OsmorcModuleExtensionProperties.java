package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.jps.osmorc.model.ManifestGenerationMode;
import org.jetbrains.jps.osmorc.model.OsmorcJarContentEntry;
import org.jetbrains.jps.osmorc.model.OutputPathType;

import java.util.List;
import java.util.Map;

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

  @Tag("additionalProperties")
  @MapAnnotation(surroundWithTag = false, entryTagName = "property")
  public Map<String, String> myAdditionalProperties = ContainerUtil.newLinkedHashMap();

  @Attribute("bundleSymbolicName")
  public String myBundleSymbolicName;

  @Attribute("bundleVersion")
  public String myBundleVersion;

  @Attribute("bundleActivator")
  public String myBundleActivator;

  @Attribute("manifestLocation")
  public String myManifestLocation;

  @Tag("additionalJARContents")
  @AbstractCollection(surroundWithTag = false)
  public List<OsmorcJarContentEntry> myAdditionalJARContents = ContainerUtil.newArrayList();

  @Attribute("ignoreFilePattern")
  public String myIgnoreFilePattern;

  @Attribute("bundlorFileLocation")
  public String myBundlorFileLocation;

  @Attribute("useProjectDefaultManifestFileLocation")
  public boolean myUseProjectDefaultManifestFileLocation = true;
}
