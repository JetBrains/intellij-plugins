package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;
import org.jetbrains.jps.osmorc.model.JpsOsmorcProjectExtension;
import org.jetbrains.jps.osmorc.model.ManifestGenerationMode;
import org.jetbrains.jps.osmorc.model.OutputPathType;
import org.jetbrains.jps.osmorc.util.JpsOrderedProperties;
import org.jetbrains.jps.util.JpsPathUtil;
import org.osgi.framework.Constants;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author michael.golubev
 */
public class JpsOsmorcModuleExtensionImpl extends JpsElementBase<JpsOsmorcModuleExtensionImpl> implements JpsOsmorcModuleExtension {

  private static final Logger LOG = Logger.getInstance(JpsOsmorcModuleExtensionImpl.class);

  public static final JpsElementChildRole<JpsOsmorcModuleExtension> ROLE = JpsElementChildRoleBase.create("Osmorc");


  private OsmorcModuleExtensionProperties myProperties;

  public JpsOsmorcModuleExtensionImpl(OsmorcModuleExtensionProperties properties) {
    myProperties = properties;
  }

  private JpsOsmorcModuleExtensionImpl(JpsOsmorcModuleExtensionImpl original) {
    myProperties = XmlSerializerUtil.createCopy(original.myProperties);
  }

  public OsmorcModuleExtensionProperties getProperties() {
    return myProperties;
  }

  @NotNull
  @Override
  public JpsModule getModule() {
    return (JpsModule)getParent();
  }

  @Override
  public void applyChanges(@NotNull JpsOsmorcModuleExtensionImpl modified) {
    XmlSerializerUtil.copyBean(modified.myProperties, myProperties);
  }

  @NotNull
  @Override
  public JpsOsmorcModuleExtensionImpl createCopy() {
    return new JpsOsmorcModuleExtensionImpl(this);
  }

  @NotNull
  public String getJarFileLocation() {
    String jarFileLocation = myProperties.myJarFileLocation;
    OutputPathType outputPathType = myProperties.myOutputPathType;

    String nullSafeLocation = jarFileLocation != null ? jarFileLocation : "";
    if (outputPathType == null) {
      return nullSafeLocation;
    }
    JpsModule module = getModule();
    switch (outputPathType) {
      case CompilerOutputPath:
        File outputDir = JpsJavaExtensionService.getInstance().getOutputDirectory(module, false);
        if (outputDir != null) {
          return new File(outputDir.getParent(), nullSafeLocation).getAbsolutePath();
        }
        else {
          return nullSafeLocation;
        }
      case OsgiOutputPath:
        JpsProject project = module.getProject();
        JpsOsmorcProjectExtension projectExtension = JpsOsmorcProjectExtensionImpl.getExtension(project);
        String bundlesOutputPath = projectExtension == null ? null : projectExtension.getBundlesOutputPath();
        if (bundlesOutputPath != null && bundlesOutputPath.trim().length() != 0) {
          return bundlesOutputPath + "/" + nullSafeLocation;
        }
        else {
          return JpsOsmorcProjectExtensionImpl.getDefaultBundlesOutputPath(project) + "/" + nullSafeLocation;
        }
      case SpecificOutputPath:
      default:
        return nullSafeLocation;
    }
  }

  public boolean isUseBndFile() {
    return getManifestGenerationMode() == ManifestGenerationMode.Bnd;
  }

  public boolean isUseBundlorFile() {
    return getManifestGenerationMode() == ManifestGenerationMode.Bundlor;
  }

  public boolean isManifestManuallyEdited() {
    return getManifestGenerationMode() == ManifestGenerationMode.Manually;
  }

  public boolean isOsmorcControlsManifest() {
    return getManifestGenerationMode() == ManifestGenerationMode.OsmorcControlled;
  }

  public ManifestGenerationMode getManifestGenerationMode() {
    return myProperties.myManifestGenerationMode;
  }

  @NotNull
  public String getBndFileLocation() {
    return StringUtil.notNullize(myProperties.myBndFileLocation);
  }

  @NotNull
  public Map<String, String> getBndFileProperties() {
    Map<String, String> result = getAdditionalPropertiesAsMap();
    result.put(Constants.BUNDLE_SYMBOLICNAME, getBundleSymbolicName());
    result.put(Constants.BUNDLE_VERSION, getBundleVersion());
    final String bundleActivator = getBundleActivator();
    if (!bundleActivator.isEmpty()) {
      result.put(Constants.BUNDLE_ACTIVATOR, bundleActivator);
    }
    return result;
  }

  @NotNull
  public Map<String, String> getAdditionalPropertiesAsMap() {
    Map<String, String> result = new LinkedHashMap<String, String>();

    Properties p = new JpsOrderedProperties();
    try {
      p.load(new StringReader(getAdditionalProperties()));
    }
    catch (IOException e) {
      LOG.error("Error when reading properties");
      return result;
    }

    Set<String> propNames = p.stringPropertyNames();
    for (String propName : propNames) {
      result.put(propName, p.getProperty(propName));
    }

    return result;
  }

  /**
   * @return additional properties to be added to the bundle manifest
   */
  @NotNull
  public String getAdditionalProperties() {
    return StringUtil.notNullize(myProperties.myAdditionalProperties);
  }

  /**
   * @return the symbolic name of the bundle to build
   */
  @NotNull
  public String getBundleSymbolicName() {
    return StringUtil.notNullize(myProperties.myBundleSymbolicName);
  }

  /**
   * @return the version of the bundle.
   */
  @NotNull
  public String getBundleVersion() {
    return StringUtil.notNullize(myProperties.myBundleVersion, "1.0.0");
  }

  /**
   * @return the bundle activator class
   */
  public String getBundleActivator() {
    return StringUtil.notNullize(myProperties.myBundleActivator);
  }

  /**
   * Returns the manifest file for this facet.
   *
   * @return the manifest file. If the manifest is automatically generated, returns null.
   */
  @Nullable
  public File getManifestFile() {
    if (isOsmorcControlsManifest()) {
      String pathToJar = getJarFileLocation();
      if (pathToJar.isEmpty()) {
        return null;
      }
      File jarFile = new File(pathToJar);
      if (!jarFile.exists()) {
        return null;
      }
      try {
        JarFile jar = new JarFile(jarFile);
        try {
          JarEntry manifestEntry = jar.getJarEntry("META-INF/MANIFEST.MF");
          if (manifestEntry == null) {
            return null;
          }
          return new File(jarFile, manifestEntry.getName());
        }
        finally {
          jar.close();
        }
      }
      catch (IOException e) {
        return null;
      }
    }
    else {
      return findFileInModuleContentRoots(getManifestLocation());
    }
  }

  /**
   * @return the manifest location, relative to the module's content roots.
   */
  @NotNull
  public String getManifestLocation() {
    return StringUtil.notNullize(myProperties.myManifestLocation);
  }

  @Nullable
  public File findFileInModuleContentRoots(String relativePath) {
    String ioRelativePath = FileUtil.toSystemDependentName(relativePath);

    for (String rootUrl : getModule().getContentRootsList().getUrls()) {
      File root = JpsPathUtil.urlToFile(rootUrl);
      File result = new File(root, ioRelativePath);
      if (result.exists()) {
        return result;
      }
    }
    return null;
  }

  @NotNull
  public List<OsmorcJarContentEntry> getAdditionalJARContents() {
    if (myProperties.myAdditionalJARContents == null) {
      myProperties.myAdditionalJARContents = new ArrayList<OsmorcJarContentEntry>();
    }
    return myProperties.myAdditionalJARContents;
  }

  @NotNull
  public String getIgnoreFilePattern() {
    return StringUtil.notNullize(myProperties.myIgnoreFilePattern);
  }

  public boolean isIgnorePatternValid() {
    String ignoreFilePattern = myProperties.myIgnoreFilePattern;
    if (StringUtil.isEmpty(ignoreFilePattern)) {
      return true; // empty pattern is ok
    }
    try {
      Pattern.compile(ignoreFilePattern);
      return true;
    }
    catch (PatternSyntaxException e) {
      return false;
    }
  }

  @NotNull
  public String getBundlorFileLocation() {
    return StringUtil.notNullize(myProperties.myBundlorFileLocation);
  }

  public void processAffectedModules(Consumer<JpsModule> consumer) {
    JpsJavaExtensionService.dependencies(getModule()).recursively().productionOnly().processModules(consumer);
  }
}
