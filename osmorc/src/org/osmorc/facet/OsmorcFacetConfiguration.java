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
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osmorc.facet.ui.OsmorcFacetGeneralEditorTab;
import org.osmorc.facet.ui.OsmorcFacetJAREditorTab;
import org.osmorc.facet.ui.OsmorcFacetManifestGenerationEditorTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The facet configuration of an osmorc facet.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetConfiguration implements FacetConfiguration
{

  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext,
                                           final FacetValidatorsManager validatorsManager)
  {
    return new FacetEditorTab[]{new OsmorcFacetGeneralEditorTab(editorContext),
        new OsmorcFacetJAREditorTab(editorContext), new OsmorcFacetManifestGenerationEditorTab(editorContext)};
  }

  public void readExternal(Element element) throws InvalidDataException
  {
    setOsmorcControlsManifest(Boolean.parseBoolean(element.getAttributeValue(OSMORC_CONTROLS_MANIFEST, "true")));
    setUseBndFile(Boolean.parseBoolean(element.getAttributeValue(USE_BND_FILE, "false")));
    setBndFileLocation(element.getAttributeValue(BND_FILE_LOCATION));
    setManifestLocation(element.getAttributeValue(MANIFEST_LOCATION));
    setJarFileLocation(element.getAttributeValue(JARFILE_LOCATION));
    setBundleActivator(element.getAttributeValue(BUNDLE_ACTIVATOR));
    setBundleSymbolicName(element.getAttributeValue(BUNDLE_SYMBOLIC_NAME));
    setBundleVersion(element.getAttributeValue(BUNDLE_VERSION));
    setIgnoreFilePattern(element.getAttributeValue(IGNORE_FILE_PATTERN));
    setUseProjectDefaultManifestFileLocation(Boolean.parseBoolean(element.getAttributeValue(
        USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION, "true")));
    setAlwaysRebuildBundleJAR(Boolean.parseBoolean(element.getAttributeValue(
        ALWAYS_REBUILD_BUNDLE_JAR, "false")));

    Element props = element.getChild(ADDITIONAL_PROPERTIES);
    if (props != null)
    {
      List children = props.getChildren();
      if (children.isEmpty())
      {
        // ok this is a legacy file
        setAdditionalProperties(props.getText());
      }
      else
      {
        StringBuilder builder = new StringBuilder();
        // new handling as fix for OSMORC-43
        for (Object child : children)
        {
          Element prop = (Element) child;
          builder.append(prop.getAttributeValue(KEY)).append(":").append(prop.getAttributeValue(VALUE)).append("\n");
        }
        setAdditionalProperties(builder.toString());
      }
    }

    List<Pair<String, String>> additionalJARContents = getAdditionalJARContents();
    Element additionalJARContentsElement = element.getChild("additionalJARContents");
    if (additionalJARContentsElement != null)
    {
      @SuppressWarnings({"unchecked"})
      List<Element> children = additionalJARContentsElement.getChildren("entry");
      for (Element entryElement : children)
      {
        additionalJARContents.add(Pair.create(
            entryElement.getAttributeValue("source"),
            entryElement.getAttributeValue("dest")));
      }
    }

  }

  public void writeExternal(Element element) throws WriteExternalException
  {
    element.setAttribute(OSMORC_CONTROLS_MANIFEST, String.valueOf(isOsmorcControlsManifest()));
    element.setAttribute(MANIFEST_LOCATION, getManifestLocation());
    element.setAttribute(JARFILE_LOCATION, getJarFileLocation());
    element.setAttribute(USE_BND_FILE, String.valueOf(isUseBndFile()));
    element.setAttribute(BND_FILE_LOCATION, getBndFileLocation());
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
    for (String line : lines)
    {
      int sep = line.indexOf(':');
      if (sep != -1)
      {
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
    for (Pair<String, String> additionalJARContent : additionalJARContents)
    {
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
  public boolean isOsmorcControlsManifest()
  {
    return _osmorcControlsManifest;
  }

  /**
   * Convenience getter.
   *
   * @return true, if the manifest is edited manually, false if osmorc creates it on build
   */
  public boolean isManifestManuallyEdited()
  {
    return !_osmorcControlsManifest;
  }

  public void setOsmorcControlsManifest(boolean osmorcControlsManifest)
  {
    this._osmorcControlsManifest = osmorcControlsManifest;
  }

  /**
   * @return the manifest location, relative to the module's content roots.
   */
  @NotNull
  public String getManifestLocation()
  {
    return _manifestLocation != null ? _manifestLocation : "";
  }

  public void setManifestLocation(String manifestLocation)
  {
    this._manifestLocation = manifestLocation;
  }

  /**
   * @return the jar file to be created for this module
   */
  @NotNull
  public String getJarFileLocation()
  {
    return _jarFileLocation != null ? _jarFileLocation : "";
  }

  public void setJarFileLocation(String jarFileLocation)
  {
    _jarFileLocation = jarFileLocation;
  }

  /**
   * @return the symbolic name of the bundle to build
   */
  @NotNull
  public String getBundleSymbolicName()
  {
    return _bundleSymbolicName != null ? _bundleSymbolicName : "";
  }

  public void setBundleSymbolicName(String bundleSymbolicName)
  {
    _bundleSymbolicName = bundleSymbolicName;
  }

  /**
   * @return the bundle activator class
   */
  public String getBundleActivator()
  {
    return _bundleActivator != null ? _bundleActivator : "";
  }

  public void setBundleActivator(String bundleActivator)
  {
    _bundleActivator = bundleActivator;
  }

  /**
   * @return the version of the bundle.
   */
  @NotNull
  public String getBundleVersion()
  {
    return _bundleVersion != null ? _bundleVersion : "1.0.0";
  }

  public void setBundleVersion(String bundleVersion)
  {
    _bundleVersion = bundleVersion;
  }

  public void setAdditionalProperties(String additionalProperties)
  {
    _additionalProperties = additionalProperties;
  }

  /**
   * @return additional properties to be added to the bundle manifest
   */
  @NotNull
  public String getAdditionalProperties()
  {
    return _additionalProperties != null ? _additionalProperties : "";
  }


  /**
   * @return the contents of this configuration as a single string that can be put into a manifest file.
   */
  public String asManifestString()
  {
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
  public Map<String, String> getAdditionalPropertiesAsMap()
  {
    Map<String, String> result = new HashMap<String, String>();
    String[] lines = getAdditionalProperties().split("\n");
    for (String line : lines)
    {
      int sep = line.indexOf(':');
      if (sep != -1)
      {
        String key = line.substring(0, sep).trim();
        String value = line.substring(sep + 1).trim();
        result.put(key, value);
      }
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
  public void importAdditionalProperties(Map<String, String> properties, boolean overwrite)
  {
    Map<String, String> existing = overwrite ? properties : getAdditionalPropertiesAsMap();
    if (!overwrite)
    {
      // merge
      existing.putAll(properties);
    }
    // now create a string.
    StringBuilder builder = new StringBuilder();
    for (String s : existing.keySet())
    {
      builder.append(s).append(": ").append(existing.get(s)).append("\n");
    }
    setAdditionalProperties(builder.toString());
  }

  public void setUseProjectDefaultManifestFileLocation(boolean useProjectDefaultManifestFileLocation)
  {
    _useProjectDefaultManifestFileLocation = useProjectDefaultManifestFileLocation;
  }

  public boolean isUseProjectDefaultManifestFileLocation()
  {
    return _useProjectDefaultManifestFileLocation;
  }

  public boolean isUseBndFile()
  {
    return _useBndFile;
  }

  public void setUseBndFile(boolean useBndFile)
  {
    _useBndFile = useBndFile;
  }

  @NotNull
  public String getBndFileLocation()
  {
    return _bndFileLocation != null ? _bndFileLocation : "";
  }

  public void setBndFileLocation(String bndFileLocation)
  {
    _bndFileLocation = bndFileLocation;
  }


  @NotNull
  public List<Pair<String, String>> getAdditionalJARContents()
  {
    if (_additionalJARContents == null)
    {
      _additionalJARContents = new ArrayList<Pair<String, String>>();
    }
    return _additionalJARContents;
  }

  public void setAdditionalJARContents(@NotNull List<Pair<String, String>> additionalJARContents)
  {
    _additionalJARContents = additionalJARContents;
  }

  public void setIgnoreFilePattern(String attributeValue)
  {
    _ignoreFilePattern = attributeValue;
  }

  public String getIgnoreFilePattern()
  {
    return _ignoreFilePattern != null ? _ignoreFilePattern : "";
  }

  public boolean isAlwaysRebuildBundleJAR()
  {
    return _alwaysRebuildBundleJAR;
  }

  public void setAlwaysRebuildBundleJAR(boolean alwaysRebuildBundleJAR)
  {
    _alwaysRebuildBundleJAR = alwaysRebuildBundleJAR;
  }


  // Important: This setting must be true by default otherwise you get some dialog when importing from
  // maven metamodel asking if the manifest file should be created.
  // XXX: this should probably be fixed in the "facetAdded" method in ModuleManifestHolderImpl or ModuleDependencySynchronizer
  private boolean _osmorcControlsManifest = true;
  private String _manifestLocation;
  private String _jarFileLocation;
  private String _bundleSymbolicName;
  private String _bundleActivator;
  private String _bundleVersion;
  private String _additionalProperties;
  private List<Pair<String, String>> _additionalJARContents;
  private boolean _useProjectDefaultManifestFileLocation = true;
  private boolean _useBndFile;
  private String _bndFileLocation;
  private String _ignoreFilePattern;
  private boolean _alwaysRebuildBundleJAR;

  // constants
  private static final String OSMORC_CONTROLS_MANIFEST = "osmorcControlsManifest";
  private static final String USE_BND_FILE = "useBndFile";
  private static final String BND_FILE_LOCATION = "bndFileLocation";
  private static final String MANIFEST_LOCATION = "manifestLocation";
  private static final String JARFILE_LOCATION = "jarfileLocation";
  private static final String BUNDLE_ACTIVATOR = "bundleActivator";
  private static final String BUNDLE_SYMBOLIC_NAME = "bundleSymbolicName";
  private static final String BUNDLE_VERSION = "bundleVersion";
  private static final String USE_PROJECT_DEFAULT_MANIFEST_FILE_LOCATION = "useProjectDefaultManifestFileLocation";
  private static final String ADDITIONAL_PROPERTIES = "additionalProperties";
  private static final String IGNORE_FILE_PATTERN = "ignoreFilePattern";
  private static final String ALWAYS_REBUILD_BUNDLE_JAR = "alwaysRebuildBundleJAR";
  private static final String PROPERTY = "property";
  private static final String KEY = "key";
  private static final String VALUE = "value";
}
