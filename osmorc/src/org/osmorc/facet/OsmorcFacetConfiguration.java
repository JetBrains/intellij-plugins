/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.diagnostic.Log;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osmorc.facet.ui.OsmorcFacetGeneralEditorTab;
import org.osmorc.facet.ui.OsmorcFacetJAREditorTab;
import org.osmorc.facet.ui.OsmorcFacetManifestGenerationEditorTab;
import org.osmorc.settings.ProjectSettings;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The facet configuration of an osmorc facet.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetConfiguration implements FacetConfiguration {

  private OsmorcFacet myFacet;
  // Important: This setting must be true by default otherwise you get some dialog when importing from
  // maven metamodel asking if the manifest file should be created.
  // XXX: this should probably be fixed in the "facetAdded" method in ModuleManifestHolderImpl or ModuleDependencySynchronizer
  private boolean myOsmorcControlsManifest = true;
  private String myManifestLocation;
  private String myJarFileLocation;
  private String myBundleSymbolicName;
  private String myBundleActivator;
  private String myBundleVersion;
  private String myAdditionalProperties;
  private List<Pair<String, String>> myAdditionalJARContents;
  private boolean myUseProjectDefaultManifestFileLocation = true;
  private boolean myUseBndFile;
  private String myBndFileLocation;
  private boolean myUseBundlorFile;
  private String myBundlorFileLocation;
  private String myIgnoreFilePattern;
  private boolean myAlwaysRebuildBundleJAR;
  private OutputPathType myOutputPathType;

  // constants
  private static final String OSMORC_CONTROLS_MANIFEST = "osmorcControlsManifest";
  private static final String USE_BND_FILE = "useBndFile";
  private static final String BND_FILE_LOCATION = "bndFileLocation";
  private static final String USE_BUNDLOR_FILE = "useBundlorFile";
  private static final String BUNDLOR_FILE_LOCATION = "bundlorFileLocation";
  private static final String MANIFEST_LOCATION = "manifestLocation";
  private static final String JARFILE_LOCATION = "jarfileLocation";
  private static final String BUNDLE_ACTIVATOR = "bundleActivator";
  private static final String BUNDLE_SYMBOLIC_NAME = "bundleSymbolicName";
  private static final String BUNDLE_VERSION = "bundleVersion";
  private static final String USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION = "useProjectDefaultManifestFileLocation";
  private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
  private static final String IGNORE_FILE_PATTERN = "ignoreFilePattern";
  private static final String ALWAYS_REBUILD_BUNDLE_JAR = "alwaysRebuildBundleJAR";
  private static final String OUTPUT_PATH_TYPE = "outputPathType";
  private static final String PROPERTY = "property";
  private static final String KEY = "key";
  private static final String VALUE = "value";


  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext,
                                           final FacetValidatorsManager validatorsManager) {
    return new FacetEditorTab[]{new OsmorcFacetGeneralEditorTab(editorContext),
      new OsmorcFacetJAREditorTab(editorContext, validatorsManager), new OsmorcFacetManifestGenerationEditorTab(editorContext)};
  }

  public void readExternal(Element element) throws InvalidDataException {
    setOsmorcControlsManifest(Boolean.parseBoolean(element.getAttributeValue(OSMORC_CONTROLS_MANIFEST, "true")));
    setUseBndFile(Boolean.parseBoolean(element.getAttributeValue(USE_BND_FILE, "false")));
    setBndFileLocation(element.getAttributeValue(BND_FILE_LOCATION));
    setUseBundlorFile(Boolean.parseBoolean(element.getAttributeValue(USE_BUNDLOR_FILE, "false")));
    setBundlorFileLocation(element.getAttributeValue(BUNDLOR_FILE_LOCATION));
    setManifestLocation(element.getAttributeValue(MANIFEST_LOCATION));

    // IDEADEV-40357 backwards compatibility
    //if ( !"".equals(getManifestLocation()) && !getManifestLocation().contains("/") && !getManifestLocation().toUpperCase().contains(".MF") ) {
    //     its an old directory setting.fix it by appending MANIFEST.MF
    //setManifestLocation(getManifestLocation()+"/MANIFEST.MF");
    //}
    //This actually is not a good idea if someone is using a manifest named different than manifest.mf... so  i comment it out.

    String outputPathTypeName = element.getAttributeValue(OUTPUT_PATH_TYPE, OutputPathType.SpecificOutputPath.name());
    OutputPathType outputPathType = OutputPathType.valueOf(outputPathTypeName);

    setJarFileLocation(element.getAttributeValue(JARFILE_LOCATION), outputPathType);
    setBundleActivator(element.getAttributeValue(BUNDLE_ACTIVATOR));
    setBundleSymbolicName(element.getAttributeValue(BUNDLE_SYMBOLIC_NAME));
    setBundleVersion(element.getAttributeValue(BUNDLE_VERSION));
    setIgnoreFilePattern(element.getAttributeValue(IGNORE_FILE_PATTERN));
    setUseProjectDefaultManifestFileLocation(Boolean.parseBoolean(element.getAttributeValue(
      USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION, "true")));
    setAlwaysRebuildBundleJAR(Boolean.parseBoolean(element.getAttributeValue(
      ALWAYS_REBUILD_BUNDLE_JAR, "false")));

    Element props = element.getChild(ADDITIONAL_PROPERTIES);
    if (props != null) {
      List children = props.getChildren();
      if (children.isEmpty()) {
        // ok this is a legacy file
        setAdditionalProperties(props.getText());
      }
      else {
        StringBuilder builder = new StringBuilder();
        // new handling as fix for OSMORC-43
        for (Object child : children) {
          Element prop = (Element)child;
          builder.append(prop.getAttributeValue(KEY)).append(":").append(prop.getAttributeValue(VALUE)).append("\n");
        }
        setAdditionalProperties(builder.toString());
      }
    }

    List<Pair<String, String>> additionalJARContents = getAdditionalJARContents();
    Element additionalJARContentsElement = element.getChild("additionalJARContents");
    if (additionalJARContentsElement != null) {
      @SuppressWarnings({"unchecked"})
      List<Element> children = additionalJARContentsElement.getChildren("entry");
      for (Element entryElement : children) {
        additionalJARContents.add(Pair.create(
          entryElement.getAttributeValue("source"),
          entryElement.getAttributeValue("dest")));
      }
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {
    element.setAttribute(OSMORC_CONTROLS_MANIFEST, String.valueOf(isOsmorcControlsManifest()));
    element.setAttribute(MANIFEST_LOCATION, getManifestLocation());
    element.setAttribute(JARFILE_LOCATION, myJarFileLocation != null ? myJarFileLocation : "");
    element.setAttribute(OUTPUT_PATH_TYPE, getOutputPathType().name());
    element.setAttribute(USE_BND_FILE, String.valueOf(isUseBndFile()));
    element.setAttribute(BND_FILE_LOCATION, getBndFileLocation());
    element.setAttribute(USE_BUNDLOR_FILE, String.valueOf(isUseBundlorFile()));
    element.setAttribute(BUNDLOR_FILE_LOCATION, getBundlorFileLocation());
    element.setAttribute(BUNDLE_ACTIVATOR, getBundleActivator());
    element.setAttribute(BUNDLE_SYMBOLIC_NAME, getBundleSymbolicName());
    element.setAttribute(BUNDLE_VERSION, getBundleVersion());
    element.setAttribute(IGNORE_FILE_PATTERN, getIgnoreFilePattern());
    element.setAttribute(USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION,
                         String.valueOf(isUseProjectDefaultManifestFileLocation()));
    element.setAttribute(ALWAYS_REBUILD_BUNDLE_JAR,
                         String.valueOf(isAlwaysRebuildBundleJAR()));

    Element props = new Element(ADDITIONAL_PROPERTIES);

    // more robust storage of values as fix for OSMORC-43
    // I deliberately do not use getAdditionalPropertiesAsMap to preserve the order of the entries
    String[] lines = getAdditionalProperties().split("\n");
    for (String line : lines) {
      int sep = line.indexOf(':');
      if (sep != -1) {
        String key = line.substring(0, sep).trim();
        String value = line.substring(sep + 1).trim();
        Element prop = new Element(PROPERTY);
        prop.setAttribute(KEY, key);
        prop.setAttribute(VALUE, value);
        props.addContent(prop);
      }
    }
    element.addContent(props);

    Element additionalJARContentsElement = new Element("additionalJARContents");
    List<Pair<String, String>> additionalJARContents = getAdditionalJARContents();
    for (Pair<String, String> additionalJARContent : additionalJARContents) {
      Element entry = new Element("entry");
      entry.setAttribute("source", additionalJARContent.getFirst());
      entry.setAttribute("dest", additionalJARContent.getSecond());
      additionalJARContentsElement.addContent(entry);
    }
    element.addContent(additionalJARContentsElement);
  }

  /**
   * @return true if Osmorc controls the manifest, false if is edited manually
   */
  public boolean isOsmorcControlsManifest() {
    return myOsmorcControlsManifest;
  }

  /**
   * Convenience getter.
   *
   * @return true, if the manifest is edited manually, false if osmorc creates it on build
   */
  public boolean isManifestManuallyEdited() {
    return !myOsmorcControlsManifest;
  }

  public void setOsmorcControlsManifest(boolean osmorcControlsManifest) {
    this.myOsmorcControlsManifest = osmorcControlsManifest;
  }

  /**
   * @return the manifest location, relative to the module's content roots.
   */
  @NotNull
  public String getManifestLocation() {
    return myManifestLocation != null ? myManifestLocation : "";
  }

  public void setManifestLocation(String manifestLocation) {
    this.myManifestLocation = manifestLocation;
    if ( myManifestLocation != null ) {
      myManifestLocation = myManifestLocation.replace("\\", "/");
    }
  }

  /**
   * @return the jar file to be created for this module
   */
  @NotNull
  public String getJarFileLocation() {
    String nullSafeLocation = myJarFileLocation != null ? myJarFileLocation : "";
    if (myOutputPathType == null || myFacet == null) {
      // not initialized
      return nullSafeLocation;
    }
    switch (myOutputPathType) {
      case CompilerOutputPath:
        VirtualFile moduleCompilerOutputPath = CompilerModuleExtension.getInstance(myFacet.getModule()).getCompilerOutputPath();
        if (moduleCompilerOutputPath != null) {
          return moduleCompilerOutputPath.getParent().getPath() + "/" + nullSafeLocation;
        }
        else {
          return nullSafeLocation;
        }
      case OsgiOutputPath:
        ProjectSettings projectSettings = ModuleServiceManager.getService(myFacet.getModule(), ProjectSettings.class);
        String bundlesOutputPath = projectSettings.getBundlesOutputPath();
        if (bundlesOutputPath != null && bundlesOutputPath.trim().length() != 0) {
          return bundlesOutputPath + "/" + nullSafeLocation;
        }
        else {
          return ProjectSettings.getDefaultBundlesOutputPath(myFacet.getModule().getProject()) + "/" + nullSafeLocation;
        }
      case SpecificOutputPath:
      default:
        return nullSafeLocation;
    }
  }

  /**
   * Returns the file name of the jar file.
   * @return the file name of the jar file.
   */
  @NotNull
  public String getJarFileName() {
    if (myOutputPathType == null) {
      return "";
    }
    switch (myOutputPathType) {
      case CompilerOutputPath:
      case OsgiOutputPath:
      case SpecificOutputPath:
        String completeOutputPath = getJarFileLocation();

        File f = new File(completeOutputPath);
        return f.getName();
      default:
        // not initialized
        return getJarFileLocation();
    }
  }


  /**
   * Returns the path where the jar file name should be stored (excluding the jar's name).
   *
   * @return the path name where the jar file is to be stored..
   */
  @NotNull
  public String getJarFilePath() {
    if ( myOutputPathType == null ) {
      return "";
    }
    switch (myOutputPathType) {
      case CompilerOutputPath:
      case OsgiOutputPath:
      case SpecificOutputPath:
        String completeOutputPath = getJarFileLocation();
        File f = new File(completeOutputPath);
        String parent = f.getParent();
        if ( parent == null ) {
          return "";
        }
        return parent;
      default:
        // not initialized
        return "";
    }
  }



  /**
   * Sethes the location of the jar file
   *
   * @param jarFileLocation the path to the jar file. If the output path type is {@link OutputPathType#SpecificOutputPath} this needs to
   *                        be a full path otherwise it needs to be just the jar's name.
   * @param outputPathType  the path type
   */
  public void setJarFileLocation(String jarFileLocation, OutputPathType outputPathType) {
    myJarFileLocation = jarFileLocation;
    if (myJarFileLocation != null ) {
      myJarFileLocation = myJarFileLocation.replace("\\", "/");
    }
    myOutputPathType = outputPathType;
  }

  public OutputPathType getOutputPathType() {
    return myOutputPathType != null ? myOutputPathType : OutputPathType.SpecificOutputPath;
  }

  /**
   * @return the symbolic name of the bundle to build
   */
  @NotNull
  public String getBundleSymbolicName() {
    return myBundleSymbolicName != null ? myBundleSymbolicName : "";
  }

  public void setBundleSymbolicName(String bundleSymbolicName) {
    myBundleSymbolicName = bundleSymbolicName;
  }

  /**
   * @return the bundle activator class
   */
  public String getBundleActivator() {
    return myBundleActivator != null ? myBundleActivator : "";
  }

  public void setBundleActivator(String bundleActivator) {
    myBundleActivator = bundleActivator;
  }

  /**
   * @return the version of the bundle.
   */
  @NotNull
  public String getBundleVersion() {
    return myBundleVersion != null ? myBundleVersion : "1.0.0";
  }

  public void setBundleVersion(String bundleVersion) {
    myBundleVersion = bundleVersion;
  }

  public void setAdditionalProperties(String additionalProperties) {
    myAdditionalProperties = additionalProperties;
  }

  /**
   * @return additional properties to be added to the bundle manifest
   */
  @NotNull
  public String getAdditionalProperties() {
    return myAdditionalProperties != null ? myAdditionalProperties : "";
  }


  /**
   * @return the contents of this configuration as a string that comprises a BND configuration file.
   */
  public String asBndFile() {
    return Constants.BUNDLE_SYMBOLICNAME + ":" + getBundleSymbolicName() + "\n" +
           Constants.BUNDLE_VERSION + ":" + getBundleVersion() + "\n" +
           Constants.BUNDLE_ACTIVATOR + ":" + getBundleActivator() + "\n" +
           getAdditionalProperties() + "\n";
  }

  /**
   * Returns all additional properties as a map.Changes to this map will not change the facet configuration. If you want
   * to change additional properties use the {@link #importAdditionalProperties(java.util.Map, boolean)} method to reimport the
   * map once you have changed it.
   *
   * @return the additional properties as a Map for convenciene.
   */
  @NotNull
  public Map<String, String> getAdditionalPropertiesAsMap() {
    Map<String, String> result = new HashMap<String, String>();

    Properties p = new Properties();
    try {
      p.load(new StringReader(getAdditionalProperties()));
    }
    catch (IOException e) {
      Log.print("Error when reading properties", true);
      return result;
    }

    Set<String> propNames = p.stringPropertyNames();
    for (String propName : propNames) {
      result.put(propName,  p.getProperty(propName));
    }

    return result;
  }

  /**
   * Allows to import properties into the list of additional properties.
   *
   * @param properties the properties to import
   * @param overwrite  if true, all properties in this facet configuration will be overwritten by the given properties,
   *                   otherwise a merge will be performed with the given properties having precedence before the
   *                   existing properties.
   */
  public void importAdditionalProperties(Map<String, String> properties, boolean overwrite) {
    Map<String, String> existing = overwrite ? properties : getAdditionalPropertiesAsMap();
    if (!overwrite) {
      // merge
      existing.putAll(properties);
    }
    // now create a string.
    StringBuilder builder = new StringBuilder();
    for (String key : existing.keySet()) {
      String value = existing.get(key);
      value  = value.replace("\n", "\\\n");
      builder.append(key).append(": ").append(value).append("\n");
    }
    setAdditionalProperties(builder.toString());
  }

  public void setUseProjectDefaultManifestFileLocation(boolean useProjectDefaultManifestFileLocation) {
    myUseProjectDefaultManifestFileLocation = useProjectDefaultManifestFileLocation;
  }

  public boolean isUseProjectDefaultManifestFileLocation() {
    return myUseProjectDefaultManifestFileLocation;
  }

  public boolean isUseBndFile() {
    return myUseBndFile;
  }

  public void setUseBndFile(boolean useBndFile) {
    myUseBndFile = useBndFile;
  }

  @NotNull
  public String getBndFileLocation() {
    return myBndFileLocation != null ? myBndFileLocation : "";
  }

  public void setBndFileLocation(String bndFileLocation) {
    if ( bndFileLocation != null ) {
      bndFileLocation = bndFileLocation.replace("\\", "/");
    }
    myBndFileLocation = bndFileLocation;
  }


  public boolean isUseBundlorFile() {
    return myUseBundlorFile;
  }

  public void setUseBundlorFile(boolean _useBundlorFile) {
    this.myUseBundlorFile = _useBundlorFile;
  }

  @NotNull
  public String getBundlorFileLocation() {
    return myBundlorFileLocation != null ? myBundlorFileLocation : "";
  }

  public void setBundlorFileLocation(String bundlorFileLocation) {
    if (bundlorFileLocation != null ) {
      bundlorFileLocation = bundlorFileLocation.replace("\\", "/");
    }
    this.myBundlorFileLocation = bundlorFileLocation;
  }

  @NotNull
  public List<Pair<String, String>> getAdditionalJARContents() {
    if (myAdditionalJARContents == null) {
      myAdditionalJARContents = new ArrayList<Pair<String, String>>();
    }
    return myAdditionalJARContents;
  }

  public void setAdditionalJARContents(@NotNull List<Pair<String, String>> additionalJARContents) {
    myAdditionalJARContents = additionalJARContents;
  }

  public void setIgnoreFilePattern(String attributeValue) {
    myIgnoreFilePattern = attributeValue;
  }

  public String getIgnoreFilePattern() {
    return myIgnoreFilePattern != null ? myIgnoreFilePattern : "";
  }

  public boolean isAlwaysRebuildBundleJAR() {
    return myAlwaysRebuildBundleJAR;
  }

  public void setAlwaysRebuildBundleJAR(boolean alwaysRebuildBundleJAR) {
    myAlwaysRebuildBundleJAR = alwaysRebuildBundleJAR;
  }


  public boolean isIgnorePatternValid() {
    if (myIgnoreFilePattern == null || myIgnoreFilePattern.length() == 0) {
      return true; // empty pattern is ok
    }
    try {
      Pattern.compile(myIgnoreFilePattern);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * This is filled when a configuration is added to a facet. We need this to
   * get some project wide config settings in here. I am not sure if this is really a good way to do it however
   * doing it another way would break the DMServer plugin.
   *
   * @param facet the osmorc facet which this configuration is used for.
   */
  public void setFacet(OsmorcFacet facet) {
    myFacet = facet;
  }

  public enum OutputPathType {
    CompilerOutputPath,
    OsgiOutputPath,
    SpecificOutputPath
  }
}
