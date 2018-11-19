/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.jps.model.impl;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry;
import org.jetbrains.osgi.jps.model.OutputPathType;

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

  @Attribute("alwaysRebuildBundleJAR")
  public boolean myAlwaysRebuildBundleJar = false;

  @XCollection(propertyElementName = "additionalJARContents")
  public List<OsmorcJarContentEntry> myAdditionalJARContents = ContainerUtil.newArrayList();

  @Attribute("extractMetaInfOsgiInfToTargetClasses")
  public boolean myExtractMetaInfOsgIInfToTargetClasses = true;

  @Attribute("ignoreFilePattern")
  public String myIgnoreFilePattern;

  @Attribute("bundlorFileLocation")
  public String myBundlorFileLocation;

  @Attribute("useProjectDefaultManifestFileLocation")
  public boolean myUseProjectDefaultManifestFileLocation = true;
}
