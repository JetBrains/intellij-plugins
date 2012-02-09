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

import aQute.lib.osgi.Analyzer;
import aQute.libg.header.OSGiHeader;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.diagnostic.Log;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osmorc.OsmorcProjectComponent;
import org.osmorc.facet.ui.OsmorcFacetGeneralEditorTab;
import org.osmorc.facet.ui.OsmorcFacetJAREditorTab;
import org.osmorc.facet.ui.OsmorcFacetManifestGenerationEditorTab;
import org.osmorc.settings.ProjectSettings;
import org.osmorc.util.OrderedProperties;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

import static aQute.lib.osgi.Constants.*;

/**
 * The facet configuration of an osmorc facet.
 * <p/>
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetConfiguration implements FacetConfiguration {

  private OsmorcFacet myFacet;
  private String myManifestLocation;
  private String myJarFileLocation;
  private String myBundleSymbolicName;
  private String myBundleActivator;
  private String myBundleVersion;
  private String myAdditionalProperties;
  private List<Pair<String, String>> myAdditionalJARContents;
  private boolean myUseProjectDefaultManifestFileLocation = true;
  private boolean myDoNotSynchronizeWithMaven = false;
  private String myBndFileLocation;
  private String myBundlorFileLocation;
  private String myIgnoreFilePattern;
  private boolean myAlwaysRebuildBundleJAR;
  private OutputPathType myOutputPathType;
  private ManifestGenerationMode myManifestGenerationMode = ManifestGenerationMode.OsmorcControlled;

  // constants
  private static final String MANIFEST_GENERATION_MODE = "manifestGenerationMode";
  private static final String OSMORC_CONTROLS_MANIFEST = "osmorcControlsManifest";
  private static final String USE_BND_FILE = "useBndFile";
  private static final String BND_FILE_LOCATION = "bndFileLocation";
  private static final String USE_BUNDLOR_FILE = "useBundlorFile";
  private static final String BUNDLOR_FILE_LOCATION = "bundlorFileLocation";
  private static final String MANIFEST_LOCATION = "manifestLocation";
  private static final String JAR_FILE_LOCATION = "jarfileLocation";
  private static final String BUNDLE_ACTIVATOR = "bundleActivator";
  private static final String BUNDLE_SYMBOLIC_NAME = "bundleSymbolicName";
  private static final String BUNDLE_VERSION = "bundleVersion";
  private static final String USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION = "useProjectDefaultManifestFileLocation";
  private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
  private static final String IGNORE_FILE_PATTERN = "ignoreFilePattern";
  private static final String ALWAYS_REBUILD_BUNDLE_JAR = "alwaysRebuildBundleJAR";
  private static final String DO_NOT_SYNCHRONIZE_WITH_MAVEN = "doNotSynchronizeWithMaven";
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
    PathMacroManager.getInstance(ApplicationManager.getApplication()).expandPaths(element);
    
    if (element.getAttributeValue(MANIFEST_GENERATION_MODE) == null) {
        // the new attribute is not there, so we got an old file, to be converted.
      // legacy files containing boolean values
      boolean osmorcControlsManifest = Boolean.parseBoolean(element.getAttributeValue(OSMORC_CONTROLS_MANIFEST, "true"));
      boolean useBndFile = Boolean.parseBoolean(element.getAttributeValue(USE_BND_FILE, "false"));
      boolean useBundlorFile = Boolean.parseBoolean(element.getAttributeValue(USE_BUNDLOR_FILE, "false"));

      // default is that osmorc controls the manifest, so set this in case the config file has been manually edited or is otherwise invalid
      setManifestGenerationMode(ManifestGenerationMode.OsmorcControlled);

      if (osmorcControlsManifest && !useBndFile && !useBundlorFile) {
        setManifestGenerationMode(ManifestGenerationMode.OsmorcControlled);
      }
      else if ((!osmorcControlsManifest && useBndFile && !useBundlorFile) || (/* workaround */ osmorcControlsManifest && useBndFile && !useBundlorFile)) {
        setManifestGenerationMode(ManifestGenerationMode.Bnd);
      }
      else if (!osmorcControlsManifest && !useBndFile && useBundlorFile) {
        setManifestGenerationMode(ManifestGenerationMode.Bundlor);
      }
      else if (!osmorcControlsManifest && !useBndFile && !useBundlorFile) {
        setManifestGenerationMode(ManifestGenerationMode.Manually);
      }
      else {
        OsmorcProjectComponent.IMPORTANT_ERROR_NOTIFICATION.createNotification("The configuration at least one OSGi facet is invalid and has been reset. Please check your facet settings!",
                                                                               NotificationType.WARNING).notify(null);
      }
    } else {
      // attribute it there, read it.
      String manifestGenerationModeName = element.getAttributeValue(MANIFEST_GENERATION_MODE, ManifestGenerationMode.OsmorcControlled.name());
      ManifestGenerationMode manifestGenerationMode = ManifestGenerationMode.valueOf(manifestGenerationModeName);
      setManifestGenerationMode(manifestGenerationMode);
    }
    setBndFileLocation(element.getAttributeValue(BND_FILE_LOCATION));
    setBundlorFileLocation(element.getAttributeValue(BUNDLOR_FILE_LOCATION));
    setManifestLocation(element.getAttributeValue(MANIFEST_LOCATION));


    String outputPathTypeName = element.getAttributeValue(OUTPUT_PATH_TYPE, OutputPathType.SpecificOutputPath.name());
    OutputPathType outputPathType = OutputPathType.valueOf(outputPathTypeName);

    setJarFileLocation(element.getAttributeValue(JAR_FILE_LOCATION), outputPathType);
    setBundleActivator(element.getAttributeValue(BUNDLE_ACTIVATOR));
    setBundleSymbolicName(element.getAttributeValue(BUNDLE_SYMBOLIC_NAME));
    setBundleVersion(element.getAttributeValue(BUNDLE_VERSION));
    setIgnoreFilePattern(element.getAttributeValue(IGNORE_FILE_PATTERN));
    setUseProjectDefaultManifestFileLocation(Boolean.parseBoolean(element.getAttributeValue(
      USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION, "true")));
    setAlwaysRebuildBundleJAR(Boolean.parseBoolean(element.getAttributeValue(
      ALWAYS_REBUILD_BUNDLE_JAR, "false")));
    setDoNotSynchronizeWithMaven(Boolean.parseBoolean(element.getAttributeValue(
      DO_NOT_SYNCHRONIZE_WITH_MAVEN, "false")));

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
    element.setAttribute(MANIFEST_GENERATION_MODE, getManifestGenerationMode().name());
    element.setAttribute(MANIFEST_LOCATION, getManifestLocation());
    element.setAttribute(JAR_FILE_LOCATION, myJarFileLocation != null ? myJarFileLocation : "");
    element.setAttribute(OUTPUT_PATH_TYPE, getOutputPathType().name());
    element.setAttribute(BND_FILE_LOCATION, getBndFileLocation());
    element.setAttribute(BUNDLOR_FILE_LOCATION, getBundlorFileLocation());
    element.setAttribute(BUNDLE_ACTIVATOR, getBundleActivator());
    element.setAttribute(BUNDLE_SYMBOLIC_NAME, getBundleSymbolicName());
    element.setAttribute(BUNDLE_VERSION, getBundleVersion());
    element.setAttribute(IGNORE_FILE_PATTERN, getIgnoreFilePattern());
    element.setAttribute(USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION,
                         String.valueOf(isUseProjectDefaultManifestFileLocation()));
    element.setAttribute(ALWAYS_REBUILD_BUNDLE_JAR,
                         String.valueOf(isAlwaysRebuildBundleJAR()));
    element.setAttribute(DO_NOT_SYNCHRONIZE_WITH_MAVEN, String.valueOf(myDoNotSynchronizeWithMaven));

    Element props = new Element(ADDITIONAL_PROPERTIES);

    PathMacroManager macroManager = PathMacroManager.getInstance(myFacet.getModule());


    Map<String, String> additionalPropertiesAsMap = getAdditionalPropertiesAsMap();
    for (String key : additionalPropertiesAsMap.keySet()) {
      Element prop = new Element(PROPERTY);
      prop.setAttribute(KEY, key);

      String value = additionalPropertiesAsMap.get(key);
      if ( key.equals(INCLUDE_RESOURCE)) {
        // there are paths in there, collapse these so the IML files don't get mixed up on every machine. The built in macro manager
        // does not recognize these, so we have to do this manually here.
        Map<String, Map<String, String>> map = OSGiHeader.parseHeader(value);

        for (String name : map.keySet()) {
          if (StringUtil.startsWithChar(name, '{') && name.endsWith("}")) {
            name = name.substring(1, name.length() - 1).trim();
          }

          String[] parts = name.split("\\s*=\\s*");
          String source = parts[0];
          if (parts.length == 2) {
            source = parts[1];
          }

          if (StringUtil.startsWithChar(source, '@')) {
            source = source.substring(1);
          }

          // so we now got the source path and can replace it

          String collapsedSource = macroManager.collapsePath(source);
          value = value.replace(source, collapsedSource);
        }
      }
      prop.setAttribute(VALUE, value);
      props.addContent(prop);
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
   * Returns the manifest generation mode.
   *
   * @return the manifest generation mode.
   */
  public ManifestGenerationMode getManifestGenerationMode() {
    return myManifestGenerationMode;
  }


  public void setManifestGenerationMode(@NotNull ManifestGenerationMode manifestGenerationMode) {
    myManifestGenerationMode = manifestGenerationMode;
  }

  /**
   * Convenience-getter. Shortcut for
   * <pre>
   *   getManifestGenerationMode() == ManifestGenerationMode.OsmorcControlled
   * </pre>
   * @return true if Osmorc controls the manifest, false if is edited manually, created by bundlor or created by bnd
   */
  public boolean isOsmorcControlsManifest() {
    return getManifestGenerationMode() == ManifestGenerationMode.OsmorcControlled;
  }

  /**
   * Convenience getter. Shortcut for
   * <pre>
   *   getManifestGenerationMode() == ManifestGenerationMode.Manually
   * </pre>
   *
   * @return true, if the manifest is edited manually, false if osmorc creates it on build using facet settings, pre-configured bnd file or bundlor file
   */
  public boolean isManifestManuallyEdited() {
    return getManifestGenerationMode() == ManifestGenerationMode.Manually;
  }

  /**
   * Convenience getter. Shortcut for
   * <pre>
   *   getManifestGenerationMode() == ManifestGenerationMode.Bundlor
   * </pre>
   *
   * @return true, if the manifest created by bundlor, false if osmorc creates it on build using facet settings, pre-configured bnd file or manually edited
   */
  public boolean isUseBundlorFile() {
    return getManifestGenerationMode() == ManifestGenerationMode.Bundlor;
  }


  /**
   * Convenience getter. Shortcut for
   * <pre>
   *   getManifestGenerationMode() == ManifestGenerationMode.Bnd
   * </pre>
   *
   * @return true, if the manifest created by bnd, false if osmorc creates it on build using facet settings, pre-configured bundlor file or manually edited
   */
  public boolean isUseBndFile() {
    return getManifestGenerationMode() == ManifestGenerationMode.Bnd;
  }

  /**
   * @param osmorcControlsManifest
   * @deprecated use {@link #setManifestGenerationMode(OsmorcFacetConfiguration.ManifestGenerationMode)}
   */
  @Deprecated
  public void setOsmorcControlsManifest(boolean osmorcControlsManifest) {
    setManifestGenerationMode(osmorcControlsManifest ? ManifestGenerationMode.OsmorcControlled : ManifestGenerationMode.Manually);
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
   *
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
    if (myOutputPathType == null) {
      return "";
    }
    switch (myOutputPathType) {
      case CompilerOutputPath:
      case OsgiOutputPath:
      case SpecificOutputPath:
        String completeOutputPath = getJarFileLocation();
        File f = new File(completeOutputPath);
        String parent = f.getParent();
        if (parent == null) {
          return "";
        }
        return parent;
      default:
        // not initialized
        return "";
    }
  }


  /**
   * Sets the location of the jar file
   *
   * @param jarFileLocation the path to the jar file. If the output path type is {@link OutputPathType#SpecificOutputPath} this needs to
   *                        be a full path otherwise it needs to be just the jar's name.
   * @param outputPathType  the path type
   */
  public void setJarFileLocation(String jarFileLocation, OutputPathType outputPathType) {
    myJarFileLocation = jarFileLocation;
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
   * to change additional properties use the {@link #importAdditionalProperties(Map, boolean)} method to re-import the
   * map once you have changed it. The returned map is ordered and will return entries in the same order as they have been specified in the
   * settings dialog.
   *
   * @return the additional properties as a Map for convenience.
   */
  @NotNull
  public Map<String, String> getAdditionalPropertiesAsMap() {
    Map<String, String> result = new LinkedHashMap<String, String>();

    Properties p = new OrderedProperties();
    try {
      p.load(new StringReader(getAdditionalProperties()));
    }
    catch (IOException e) {
      Log.print("Error when reading properties", true);
      return result;
    }

    Set<String> propNames = p.stringPropertyNames();
    for (String propName : propNames) {
      result.put(propName, p.getProperty(propName));
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
      value = value.replace("\n", "\\\n");
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



  @NotNull
  public String getBndFileLocation() {
    return myBndFileLocation != null ? myBndFileLocation : "";
  }

  public void setBndFileLocation(String bndFileLocation) {
    myBndFileLocation = bndFileLocation;
  }


  

  @NotNull
  public String getBundlorFileLocation() {
    return myBundlorFileLocation != null ? myBundlorFileLocation : "";
  }

  public void setBundlorFileLocation(String _bundlorFileLocation) {
    this.myBundlorFileLocation = _bundlorFileLocation;
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

  @NotNull
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
   * When this is true, the facet settings will not be automatically synchronized with maven.
   * @return false if settings are synchronized with maven, true otherwise.
   */
  public boolean isDoNotSynchronizeWithMaven() {
    return myDoNotSynchronizeWithMaven;
  }

  public void setDoNotSynchronizeWithMaven(boolean doNotSynchronizeWithMaven) {
    myDoNotSynchronizeWithMaven = doNotSynchronizeWithMaven;
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

  /**
   * The type of output path, where to put the resulting jar.
   */
  public enum OutputPathType {
    /**
     * Use the default compiler output path.
     */
    CompilerOutputPath,
    /**
     * Use the OSGi output path specified in the facet settings.
     */
    OsgiOutputPath,
    /**
     * Use a specific output path for this facet.
     */
    SpecificOutputPath
  }

  /**
   * How is the manifest generated.
   */
  public enum ManifestGenerationMode {
    /**
     * No generation at all. All is manual.
     */
    Manually,
    /**
     * Osmorc will generate it using facet settings.
     */
    OsmorcControlled,
    /**
     * Bnd will generate it using a bnd file.
     */
    Bnd,
    /**
     * Bundlor will generate it, using a bundlor file.
     */
    Bundlor
  }
}
