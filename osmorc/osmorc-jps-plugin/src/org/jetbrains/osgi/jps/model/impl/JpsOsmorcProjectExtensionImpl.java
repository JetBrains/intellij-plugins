package org.jetbrains.osgi.jps.model.impl;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.java.JpsJavaProjectExtension;
import org.jetbrains.osgi.jps.model.JpsOsmorcProjectExtension;
import org.jetbrains.jps.util.JpsPathUtil;

/**
 * @author michael.golubev
 */
public class JpsOsmorcProjectExtensionImpl extends JpsElementBase<JpsOsmorcProjectExtensionImpl> implements JpsOsmorcProjectExtension {
  private OsmorcProjectExtensionProperties myProperties;

  public JpsOsmorcProjectExtensionImpl(OsmorcProjectExtensionProperties properties) {
    myProperties = properties;
  }

  @Override
  public void applyChanges(@NotNull JpsOsmorcProjectExtensionImpl modified) {
    XmlSerializerUtil.copyBean(modified.myProperties, myProperties);
  }

  @NotNull
  @Override
  public JpsOsmorcProjectExtensionImpl createCopy() {
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

  @Override
  public boolean isBndWorkspace() {
    return Boolean.TRUE.equals(myProperties.myBndWorkspace);
  }

  @NotNull
  public static String getDefaultBundlesOutputPath(JpsProject project) {
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
