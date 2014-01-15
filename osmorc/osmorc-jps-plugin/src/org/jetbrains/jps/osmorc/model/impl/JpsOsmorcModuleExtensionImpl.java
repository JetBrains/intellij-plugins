package org.jetbrains.jps.osmorc.model.impl;

import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;
import org.jetbrains.jps.osmorc.model.JpsOsmorcProjectExtension;
import org.jetbrains.jps.osmorc.model.OutputPathType;

import java.io.File;

/**
 * @author michael.golubev
 */
public class JpsOsmorcModuleExtensionImpl extends JpsElementBase<JpsOsmorcModuleExtensionImpl> implements JpsOsmorcModuleExtension {

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
        File outputDir = module != null ? JpsJavaExtensionService.getInstance().getOutputDirectory(module, false) : null;
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
}
