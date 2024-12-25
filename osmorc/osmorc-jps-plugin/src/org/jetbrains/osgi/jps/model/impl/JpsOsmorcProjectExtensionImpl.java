// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps.model.impl;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.java.JpsJavaProjectExtension;
import org.jetbrains.jps.util.JpsPathUtil;
import org.jetbrains.osgi.jps.model.JpsOsmorcProjectExtension;

/**
 * @author michael.golubev
 */
public class JpsOsmorcProjectExtensionImpl extends JpsElementBase<JpsOsmorcProjectExtensionImpl> implements JpsOsmorcProjectExtension {
  private final OsmorcProjectExtensionProperties myProperties;

  public JpsOsmorcProjectExtensionImpl(OsmorcProjectExtensionProperties properties) {
    myProperties = properties;
  }

  @Override
  public @NotNull JpsOsmorcProjectExtensionImpl createCopy() {
    return new JpsOsmorcProjectExtensionImpl(XmlSerializerUtil.createCopy(myProperties));
  }

  @Override
  public String getBundlesOutputPath() {
    return myProperties.myBundlesOutputPath;
  }

  @Override
  public String getDefaultManifestFileLocation() {
    return myProperties.myDefaultManifestFileLocation;
  }

  public static @NotNull String getDefaultBundlesOutputPath(JpsProject project) {
    JpsJavaExtensionService service = JpsJavaExtensionService.getInstance();

    JpsJavaProjectExtension extension = service.getProjectExtension(project);
    if (extension != null) {
      String outputUrl = extension.getOutputUrl();
      if (outputUrl != null) {
        return JpsPathUtil.urlToPath(outputUrl) + "/bundles";
      }
    }
    // this actually should never happen (only in tests)
    return FileUtil.getTempDirectory();
  }
}
