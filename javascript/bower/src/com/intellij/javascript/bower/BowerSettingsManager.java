package com.intellij.javascript.bower;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "JsBowerSettings",
  storages = {
    @Storage(StoragePathMacros.WORKSPACE_FILE)
  }
)
public class BowerSettingsManager implements PersistentStateComponent<Element> {

  public static final String BOWER_PACKAGE_NAME = "bower";
  public static final NodePackageDescriptor PKG_DESCRIPTOR = new NodePackageDescriptor(BOWER_PACKAGE_NAME);
  private static final String TAG_BOWER_PACKAGE = "bower-package";
  private static final String TAG_BOWER_JSON = "bower.json";

  private final Project myProject;
  private BowerSettings mySettings;

  public BowerSettingsManager(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public @Nullable Element getState() {
    BowerSettings settings = mySettings;
    if (settings == null) {
      return null;
    }
    Element root = new Element("js-bower-settings");
    JDOMExternalizerUtil.writeCustomField(root, TAG_BOWER_PACKAGE, settings.getBowerPackage().getSystemIndependentPath());
    JDOMExternalizerUtil.writeCustomField(root, TAG_BOWER_JSON, settings.getBowerJsonPath());
    return root;
  }

  @Override
  public void loadState(@NotNull Element state) {
    mySettings = createSettings(JDOMExternalizerUtil.readCustomField(state, TAG_BOWER_PACKAGE),
                                JDOMExternalizerUtil.readCustomField(state, TAG_BOWER_JSON));
  }

  private @NotNull BowerSettings createSettings(@Nullable String bowerPkgPath,
                                                @Nullable String bowerJsonPath) {
    BowerSettings.Builder builder = new BowerSettings.Builder(myProject);
    NodePackage bowerPkg;
    if (bowerPkgPath != null) {
      bowerPkg = new NodePackage(bowerPkgPath);
    }
    else {
      bowerPkg = guessBowerPackage();
    }
    builder.setBowerPackage(bowerPkg);
    if (bowerJsonPath == null) {
      bowerJsonPath = guessBowerConfig();
    }
    builder.setBowerJsonPath(bowerJsonPath);
    return builder.build();
  }

  private @NotNull NodePackage guessBowerPackage() {
    return PKG_DESCRIPTOR
      .findFirstDirectDependencyPackage(myProject, NodeJsInterpreterManager.getInstance(myProject).getInterpreter(), null);
  }

  private @NotNull String guessBowerConfig() {
    VirtualFile baseDir = myProject.getBaseDir();
    if (baseDir != null && baseDir.isValid()) {
      VirtualFile bowerJson = baseDir.findChild("bower.json");
      if (bowerJson != null && bowerJson.isValid() && !bowerJson.isDirectory()) {
        return FileUtil.toSystemDependentName(bowerJson.getPath());
      }
    }
    return "";
  }

  public @NotNull BowerSettings getSettings() {
    BowerSettings settings = mySettings;
    if (settings == null) {
      settings = createSettings(null, null);
      mySettings = settings;
    }
    return mySettings;
  }

  public void setSettings(@NotNull BowerSettings settings) {
    mySettings = settings;
  }

  public static @NotNull BowerSettingsManager getInstance(@NotNull Project project) {
    return project.getService(BowerSettingsManager.class);
  }
}
