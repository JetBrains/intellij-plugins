// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.model.impl;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry;
import org.jetbrains.osgi.jps.model.OutputPathType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
  public Map<String, String> myAdditionalProperties = new LinkedHashMap<>();

  @Attribute("bundleSymbolicName")
  public String myBundleSymbolicName;

  @Attribute("bundleVersion")
  public String myBundleVersion;

  @Attribute("bundleActivator")
  public String myBundleActivator;

  @Attribute("manifestLocation")
  public String myManifestLocation;

  @Attribute("alwaysRebuildBundleJAR")
  public boolean myAlwaysRebuildBundleJar = false;

  @XCollection(propertyElementName = "additionalJARContents")
  public List<OsmorcJarContentEntry> myAdditionalJARContents = new ArrayList<>();

  @Attribute("ignoreFilePattern")
  public String myIgnoreFilePattern;

  @Attribute("bundlorFileLocation")
  public String myBundlorFileLocation;

  @Attribute("useProjectDefaultManifestFileLocation")
  public boolean myUseProjectDefaultManifestFileLocation = true;
}
