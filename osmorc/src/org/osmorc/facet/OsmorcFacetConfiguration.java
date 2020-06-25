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

import aQute.bnd.header.OSGiHeader;
import aQute.bnd.header.Parameters;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.libraries.FrameworkLibraryValidator;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.jetbrains.osgi.jps.util.OrderedProperties;
import org.osmorc.facet.ui.OsmorcFacetGeneralEditorTab;
import org.osmorc.facet.ui.OsmorcFacetJAREditorTab;
import org.osmorc.facet.ui.OsmorcFacetManifestGenerationEditorTab;
import org.osmorc.settings.ProjectSettings;
import org.osmorc.util.OsgiUiUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static aQute.bnd.osgi.Constants.INCLUDE_RESOURCE;

/**
 * The facet configuration of an osmorc facet.
 * <p/>
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class OsmorcFacetConfiguration implements FacetConfiguration, ModificationTracker {
  private static final Logger LOG = Logger.getInstance(OsmorcFacetConfiguration.class);

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

  private volatile long myModificationCount = 0;

  @Override
  public FacetEditorTab[] createEditorTabs(FacetEditorContext context, FacetValidatorsManager validatorsManager) {
    FrameworkLibraryValidator validator = OsgiCoreLibraryType.getValidator(context, validatorsManager);
    validatorsManager.registerValidator(validator);
    return new FacetEditorTab[]{
      new OsmorcFacetGeneralEditorTab(context, validatorsManager),
      new OsmorcFacetJAREditorTab(context, validatorsManager),
      new OsmorcFacetManifestGenerationEditorTab(context)
    };
  }

  @Override
  public void readExternal(Element element) {
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
      else if ((!osmorcControlsManifest && useBndFile && !useBundlorFile) ||
               (/* workaround */ osmorcControlsManifest && useBndFile && !useBundlorFile)) {
        setManifestGenerationMode(ManifestGenerationMode.Bnd);
      }
      else if (!osmorcControlsManifest && !useBndFile && useBundlorFile) {
        setManifestGenerationMode(ManifestGenerationMode.Bundlor);
      }
      else if (!osmorcControlsManifest && !useBndFile) {
        setManifestGenerationMode(ManifestGenerationMode.Manually);
      }
      else {
        String message = "The configuration at least one OSGi facet is invalid and has been reset. Please check your facet settings!";
        OsgiUiUtil.IMPORTANT_NOTIFICATIONS.createNotification(message, NotificationType.WARNING).notify(myFacet.getModule().getProject());
      }
    }
    else {
      // attribute it there, read it.
      String manifestGenerationModeName =
        element.getAttributeValue(MANIFEST_GENERATION_MODE, ManifestGenerationMode.OsmorcControlled.name());
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
    setUseProjectDefaultManifestFileLocation(Boolean.parseBoolean(element.getAttributeValue(USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION, "true")));
    setAlwaysRebuildBundleJAR(Boolean.parseBoolean(element.getAttributeValue(ALWAYS_REBUILD_BUNDLE_JAR, "false")));
    setDoNotSynchronizeWithMaven(Boolean.parseBoolean(element.getAttributeValue(DO_NOT_SYNCHRONIZE_WITH_MAVEN, "false")));

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
      List<Element> children = additionalJARContentsElement.getChildren("entry");
      for (Element entryElement : children) {
        additionalJARContents.add(Pair.create(
          entryElement.getAttributeValue("source"),
          entryElement.getAttributeValue("dest")));
      }
    }

    myModificationCount++;
  }

  @Override
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
    element.setAttribute(USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION, String.valueOf(isUseProjectDefaultManifestFileLocation()));
    element.setAttribute(ALWAYS_REBUILD_BUNDLE_JAR, String.valueOf(isAlwaysRebuildBundleJAR()));
    element.setAttribute(DO_NOT_SYNCHRONIZE_WITH_MAVEN, String.valueOf(myDoNotSynchronizeWithMaven));

    Element props = new Element(ADDITIONAL_PROPERTIES);
    Map<String, String> map = getAdditionalPropertiesAsMap();
    for (String key : map.keySet()) {
      String value = map.get(key);

      if (key.equals(INCLUDE_RESOURCE)) {
        // there are paths in there, collapse these so the IML files don't get mixed up on every machine. The built in macro manager
        // does not recognize these, so we have to do this manually here.
        Parameters parameters = OSGiHeader.parseHeader(value);
        PathMacroManager macroManager = PathMacroManager.getInstance(myFacet.getModule());
        StringBuilder result = new StringBuilder(value.length());

        int last = 0;
        for (String pair : parameters.keySet()) {
          if (StringUtil.startsWithChar(pair, '{') && StringUtil.endsWithChar(pair, '}')) {
            pair = pair.substring(1, pair.length() - 1).trim();
          }

          int p = pair.indexOf('=');
          String source = (p < 0 ? pair : pair.substring(p + 1)).trim();
          if (StringUtil.startsWithChar(source, '@')) {
            source = source.substring(1);
          }

          String collapsedSource = macroManager.collapsePath(source);

          int sourceStart = value.indexOf(source, last);
          result.append(value, last, sourceStart).append(collapsedSource);
          last = sourceStart + source.length();
        }
        result.append(value, last, value.length());

        value = result.toString();
      }

      Element prop = new Element(PROPERTY);
      prop.setAttribute(KEY, key);
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

  public ManifestGenerationMode getManifestGenerationMode() {
    return myManifestGenerationMode;
  }

  public void setManifestGenerationMode(@NotNull ManifestGenerationMode manifestGenerationMode) {
    myManifestGenerationMode = manifestGenerationMode;
    myModificationCount++;
  }

  public boolean isOsmorcControlsManifest() {
    return getManifestGenerationMode() == ManifestGenerationMode.OsmorcControlled;
  }

  public boolean isManifestManuallyEdited() {
    return getManifestGenerationMode() == ManifestGenerationMode.Manually;
  }

  public boolean isUseBundlorFile() {
    return getManifestGenerationMode() == ManifestGenerationMode.Bundlor;
  }

  public boolean isUseBndFile() {
    return getManifestGenerationMode() == ManifestGenerationMode.Bnd;
  }

  /**
   * Returns the manifest location, relative to the module's content roots.
   */
  @NotNull
  public String getManifestLocation() {
    return myManifestLocation != null ? myManifestLocation : "";
  }

  public void setManifestLocation(String manifestLocation) {
    myManifestLocation = manifestLocation;
    myModificationCount++;
  }

  /**
   * Returns the .jar file to be created for this module.
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
        String moduleCompilerOutputPath = CompilerPaths.getModuleOutputPath(myFacet.getModule(), false);
        if (moduleCompilerOutputPath != null) {
          return PathUtil.getParentPath(moduleCompilerOutputPath) + '/' + nullSafeLocation;
        }
        else {
          return nullSafeLocation;
        }
      case OsgiOutputPath:
        ProjectSettings projectSettings = ProjectSettings.getInstance(myFacet.getModule().getProject());
        if (projectSettings != null) {
          String bundlesOutputPath = projectSettings.getBundlesOutputPath();
          if (bundlesOutputPath != null && bundlesOutputPath.trim().length() != 0) {
            return bundlesOutputPath + "/" + nullSafeLocation;
          }
        }
        return ProjectSettings.getDefaultBundlesOutputPath(myFacet.getModule().getProject()) + "/" + nullSafeLocation;
      case SpecificOutputPath:
      default:
        return nullSafeLocation;
    }
  }

  /**
   * Returns the file name of the .jar file.
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
   * Returns the path where the .jar file should be stored (excluding the name).
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
   * Sets the location of the jar file.
   * If the output path type is {@link OutputPathType#SpecificOutputPath} this needs to be a full path,
   * otherwise it needs to be just the jar's name.
   */
  public void setJarFileLocation(String jarFileLocation, OutputPathType outputPathType) {
    myJarFileLocation = jarFileLocation;
    myOutputPathType = outputPathType;
    myModificationCount++;
  }

  public OutputPathType getOutputPathType() {
    return myOutputPathType != null ? myOutputPathType : OutputPathType.SpecificOutputPath;
  }

  @NotNull
  public String getBundleSymbolicName() {
    return myBundleSymbolicName != null ? myBundleSymbolicName : "";
  }

  public void setBundleSymbolicName(@Nullable String bundleSymbolicName) {
    myBundleSymbolicName = bundleSymbolicName;
    myModificationCount++;
  }

  public String getBundleActivator() {
    return myBundleActivator != null ? myBundleActivator : "";
  }

  public void setBundleActivator(@Nullable String bundleActivator) {
    myBundleActivator = bundleActivator;
    myModificationCount++;
  }

  @NotNull
  public String getBundleVersion() {
    return myBundleVersion != null ? myBundleVersion : "1.0.0";
  }

  public void setBundleVersion(@Nullable String bundleVersion) {
    myBundleVersion = bundleVersion;
    myModificationCount++;
  }

  public void setAdditionalProperties(@Nullable String additionalProperties) {
    myAdditionalProperties = additionalProperties;
    myModificationCount++;
  }

  /**
   * Returns additional properties to be added to the bundle manifest.
   */
  @NotNull
  public String getAdditionalProperties() {
    return myAdditionalProperties != null ? myAdditionalProperties : "";
  }

  /**
   * Returns all additional properties as a map. Changes to this map will not change the facet configuration. If you want
   * to change additional properties use the {@link #importAdditionalProperties(Map, boolean)} method to re-import the
   * map once you have changed it. The returned map is ordered and will return entries in the same order as they have been specified in the
   * settings dialog.
   */
  @NotNull
  public Map<String, String> getAdditionalPropertiesAsMap() {
    try {
      OrderedProperties p = new OrderedProperties();
      p.load(new StringReader(getAdditionalProperties()));
      return p.toMap();
    }
    catch (IOException e) {
      LOG.warn(e);
      return Collections.emptyMap();
    }
  }

  /**
   * Allows to import properties into the list of additional properties.
   *
   * @param properties the properties to import
   * @param overwrite  if true, all properties in this facet configuration will be overwritten by the given properties,
   *                   otherwise a merge will be performed with the given properties having precedence before the
   *                   existing properties.
   */
  public void importAdditionalProperties(@NotNull Map<String, String> properties, boolean overwrite) {
    Map<String, String> existing = overwrite ? properties : getAdditionalPropertiesAsMap();
    if (!overwrite) {
      // merge
      existing.putAll(properties);
    }
    // now create a string.
    StringBuilder builder = new StringBuilder();
    for (String key : existing.keySet()) {
      String value = existing.get(key);
      value = StringUtil.replace(value, "\n", "\\\n");
      builder.append(key).append(": ").append(value).append("\n");
    }
    setAdditionalProperties(builder.toString());
  }

  public void setUseProjectDefaultManifestFileLocation(boolean useProjectDefaultManifestFileLocation) {
    myUseProjectDefaultManifestFileLocation = useProjectDefaultManifestFileLocation;
    myModificationCount++;
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
    myModificationCount++;
  }

  @NotNull
  public String getBundlorFileLocation() {
    return myBundlorFileLocation != null ? myBundlorFileLocation : "";
  }

  public void setBundlorFileLocation(String _bundlorFileLocation) {
    myBundlorFileLocation = _bundlorFileLocation;
    myModificationCount++;
  }

  @NotNull
  public List<Pair<String, String>> getAdditionalJARContents() {
    if (myAdditionalJARContents == null) {
      myAdditionalJARContents = new ArrayList<>();
    }
    return myAdditionalJARContents;
  }

  public void setAdditionalJARContents(@NotNull List<Pair<String, String>> additionalJARContents) {
    myAdditionalJARContents = additionalJARContents;
    myModificationCount++;
  }

  @NotNull
  public String getIgnoreFilePattern() {
    return myIgnoreFilePattern != null ? myIgnoreFilePattern : "";
  }

  public void setIgnoreFilePattern(String attributeValue) {
    myIgnoreFilePattern = attributeValue;
    myModificationCount++;
  }

  public boolean isAlwaysRebuildBundleJAR() {
    return myAlwaysRebuildBundleJAR;
  }

  public void setAlwaysRebuildBundleJAR(boolean alwaysRebuildBundleJAR) {
    myAlwaysRebuildBundleJAR = alwaysRebuildBundleJAR;
    myModificationCount++;
  }

  public boolean isDoNotSynchronizeWithMaven() {
    return myDoNotSynchronizeWithMaven;
  }

  public void setDoNotSynchronizeWithMaven(boolean doNotSynchronizeWithMaven) {
    myDoNotSynchronizeWithMaven = doNotSynchronizeWithMaven;
    myModificationCount++;
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

  @Override
  public long getModificationCount() {
    return myModificationCount;
  }
}
