// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps.model.impl;

import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.osgi.jps.model.JpsOsmorcProjectExtension;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.jetbrains.osgi.jps.util.OsgiBuildUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author michael.golubev
 */
public class JpsOsmorcModuleExtensionImpl extends JpsElementBase<JpsOsmorcModuleExtensionImpl> implements JpsOsmorcModuleExtension {
  private final OsmorcModuleExtensionProperties myProperties;
  private final NullableLazyValue<File> myDescriptorFile = new NullableLazyValue<File>() {
    @Override
    protected File compute() {
      if (myProperties.myManifestGenerationMode == ManifestGenerationMode.Bnd) {
        return OsgiBuildUtil.findFileInModuleContentRoots(getModule(), getBndFileLocation());
      }
      else if (myProperties.myManifestGenerationMode == ManifestGenerationMode.BndMavenPlugin) {
        String bnd = getBndFileLocation();
        return StringUtil.isNotEmpty(bnd) ? OsgiBuildUtil.findFileInModuleContentRoots(getModule(), bnd) : null;
      }
      else if (myProperties.myManifestGenerationMode == ManifestGenerationMode.Bundlor) {
        return OsgiBuildUtil.findFileInModuleContentRoots(getModule(), getBundlorFileLocation());
      }
      else if (myProperties.myManifestGenerationMode == ManifestGenerationMode.Manually) {
        return OsgiBuildUtil.findFileInModuleContentRoots(getModule(), getManifestLocation());
      }
      else {
        return null;
      }
    }
  };

  public JpsOsmorcModuleExtensionImpl(OsmorcModuleExtensionProperties properties) {
    myProperties = properties;
  }

  public OsmorcModuleExtensionProperties getProperties() {
    return myProperties;
  }

  @Override
  public @NotNull JpsOsmorcModuleExtensionImpl createCopy() {
    return new JpsOsmorcModuleExtensionImpl(XmlSerializerUtil.createCopy(myProperties));
  }

  @Override
  public @NotNull String getJarFileLocation() {
    String jarFileLocation = myProperties.myJarFileLocation;
    OutputPathType outputPathType = myProperties.myOutputPathType;
    String nullSafeLocation = jarFileLocation != null ? jarFileLocation : "";
    if (outputPathType == null) return nullSafeLocation;

    switch (outputPathType) {
      case CompilerOutputPath:
        File outputDir = JpsJavaExtensionService.getInstance().getOutputDirectory(getModule(), false);
        return outputDir != null ? new File(outputDir.getParent(), nullSafeLocation).getAbsolutePath() : nullSafeLocation;

      case OsgiOutputPath:
        JpsOsmorcProjectExtension projectExtension = getProjectExtension();
        if (projectExtension != null) {
          String bundlesOutputPath = projectExtension.getBundlesOutputPath();
          if (!StringUtil.isEmptyOrSpaces(bundlesOutputPath)) {
            return bundlesOutputPath + "/" + nullSafeLocation;
          }
        }
        return JpsOsmorcProjectExtensionImpl.getDefaultBundlesOutputPath(getModule().getProject()) + "/" + nullSafeLocation;

      case SpecificOutputPath:
      default:
        return nullSafeLocation;
    }
  }

  @Override
  public @Nullable File getBundleDescriptorFile() {
    return myDescriptorFile.getValue();
  }

  @Override
  public boolean isUseBndFile() {
    return myProperties.myManifestGenerationMode == ManifestGenerationMode.Bnd;
  }

  @Override
  public boolean isUseBndMavenPlugin() {
    return myProperties.myManifestGenerationMode == ManifestGenerationMode.BndMavenPlugin;
  }

  @Override
  public boolean isUseBundlorFile() {
    return myProperties.myManifestGenerationMode == ManifestGenerationMode.Bundlor;
  }

  @Override
  public boolean isManifestManuallyEdited() {
    return myProperties.myManifestGenerationMode == ManifestGenerationMode.Manually;
  }

  @Override
  public boolean isOsmorcControlsManifest() {
    return myProperties.myManifestGenerationMode == ManifestGenerationMode.OsmorcControlled;
  }

  @Override
  public @NotNull String getBndFileLocation() {
    return StringUtil.notNullize(myProperties.myBndFileLocation);
  }

  @Override
  public @NotNull Map<String, String> getAdditionalProperties() {
    return Collections.unmodifiableMap(myProperties.myAdditionalProperties);
  }

  @Override
  public @NotNull String getBundleSymbolicName() {
    return StringUtil.notNullize(myProperties.myBundleSymbolicName);
  }

  @Override
  public @NotNull String getBundleVersion() {
    return StringUtil.notNullize(myProperties.myBundleVersion, "1.0.0");
  }

  @Override
  public @Nullable String getBundleActivator() {
    return myProperties.myBundleActivator;
  }

  /**
   * Returns the manifest file for this facet. If the manifest is automatically generated, returns null.
   */
  @Override
  public @Nullable File getManifestFile() {
    if (isOsmorcControlsManifest()) {
      String pathToJar = getJarFileLocation();
      if (pathToJar.isEmpty()) {
        return null;
      }
      File jarFile = new File(pathToJar);
      if (!jarFile.exists()) {
        return null;
      }
      try (JarFile jar = new JarFile(jarFile)) {
        JarEntry manifestEntry = jar.getJarEntry("META-INF/MANIFEST.MF");
        if (manifestEntry == null) {
          return null;
        }
        return new File(jarFile, manifestEntry.getName());
      }
      catch (IOException e) {
        return null;
      }
    }
    else if (isManifestManuallyEdited()) {
      return OsgiBuildUtil.findFileInModuleContentRoots(getModule(), getManifestLocation());
    }
    else {
      return null;
    }
  }

  @Override
  public @NotNull String getManifestLocation() {
    if (myProperties.myUseProjectDefaultManifestFileLocation) {
      JpsOsmorcProjectExtension projectExtension = getProjectExtension();
      return projectExtension == null ? JarFile.MANIFEST_NAME : projectExtension.getDefaultManifestFileLocation();
    }
    else {
      return StringUtil.notNullize(myProperties.myManifestLocation);
    }
  }

  @Override
  public boolean isAlwaysRebuildBundleJar() {
    return myProperties.myAlwaysRebuildBundleJar;
  }

  @Override
  public @NotNull List<OsmorcJarContentEntry> getAdditionalJarContents() {
    return Collections.unmodifiableList(myProperties.myAdditionalJARContents);
  }

  @Override
  public @Nullable String getIgnoreFilePattern() {
    return myProperties.myIgnoreFilePattern;
  }

  @Override
  public @NotNull String getBundlorFileLocation() {
    return StringUtil.notNullize(myProperties.myBundlorFileLocation);
  }

  private JpsModule getModule() {
    return (JpsModule)getParent();
  }

  private JpsOsmorcProjectExtension getProjectExtension() {
    return JpsOsmorcExtensionService.getExtension(getModule().getProject());
  }
}
