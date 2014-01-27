package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.java.JpsJavaProjectExtension;
import org.jetbrains.jps.osmorc.model.JpsOsmorcProjectExtension;
import org.jetbrains.jps.util.JpsPathUtil;

/**
 * @author michael.golubev
 */
public class JpsOsmorcProjectExtensionImpl extends JpsElementBase<JpsOsmorcProjectExtensionImpl> implements JpsOsmorcProjectExtension {

  public static final JpsElementChildRole<JpsOsmorcProjectExtension> ROLE = JpsElementChildRoleBase.create("Osmorc");


  private OsmorcProjectExtensionProperties myProperties;

  public JpsOsmorcProjectExtensionImpl(OsmorcProjectExtensionProperties properties) {
    myProperties = properties;
  }

  private JpsOsmorcProjectExtensionImpl(JpsOsmorcProjectExtensionImpl original) {
    myProperties = XmlSerializerUtil.createCopy(original.myProperties);
  }

  @Override
  public void applyChanges(@NotNull JpsOsmorcProjectExtensionImpl modified) {
    XmlSerializerUtil.copyBean(modified.myProperties, myProperties);
  }

  @NotNull
  @Override
  public JpsOsmorcProjectExtensionImpl createCopy() {
    return new JpsOsmorcProjectExtensionImpl(this);
  }

  @Override
  public String getBundlesOutputPath() {
    return myProperties.myBundlesOutputPath;
  }

  @Override
  public String getDefaultManifestFileLocation() {
    return myProperties.myDefaultManifestFileLocation;
  }

  @Nullable
  public static JpsOsmorcProjectExtension getExtension(@NotNull JpsProject project) {
    return project.getContainer().getChild(ROLE);
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
