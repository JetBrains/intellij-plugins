package jetbrains.plugins.yeoman.settings;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;

@State(name = "YeomanSettings", storages = @Storage("yeomanSettings.xml"), category = SettingsCategory.TOOLS)
public class YeomanGlobalSettings implements YeomanNodeFiles, PersistentStateComponent<YeomanGlobalSettings.SettingsState> {
  public static final String PACKAGES_URL_KEY = "yeoman.settings.packages.url";
  public static final String PACKAGES_URL_DEFAULT = "https://raw.githubusercontent.com/yeoman/yeoman-generator-list/cache-generators-list/cache.json";
  public static final String RELATIVE_CLI_HELPER_PATH = "runner/yeoman-simple-cli/lib";
  public static final String CLI_HELPER_NAME = "cli.js";

  @NotNull
  private SettingsState myStoreSettings = new SettingsState();

  @Nullable
  @Override
  public SettingsState getState() {
    return myStoreSettings;
  }

  @Override
  public void loadState(@NotNull SettingsState state) {
    myStoreSettings = state;
  }


  public static class SettingsState {
    public String yoPackagePath;
    public String nodeExePath;
  }

  @NotNull
  public static YeomanGlobalSettings getInstance() {
    return ApplicationManager.getApplication().getService(YeomanGlobalSettings.class);
  }

  @NotNull
  public String getGeneratorsFileUrl() {
    return PropertiesComponent.getInstance().getValue(PACKAGES_URL_KEY, PACKAGES_URL_DEFAULT);
  }

  @NotNull
  public String getGeneratorsFileDefaultEncoding() {
    return CharsetToolkit.UTF8;
  }

  @Nullable
  public String getNodeInterpreterRefName() {
    if (!StringUtil.isEmpty(myStoreSettings.nodeExePath)) {
      return myStoreSettings.nodeExePath;
    }

    return null;
  }

  @Override
  @Nullable
  public NodeJsLocalInterpreter getInterpreter() {
    Project defaultProject = ProjectManager.getInstance().getDefaultProject();
    return NodeJsLocalInterpreter.tryCast(NodeJsInterpreterRef.create(getNodeInterpreterRefName()).resolve(defaultProject));
  }

  public String getYoPackagePath() {
    if (!StringUtil.isEmpty(myStoreSettings.yoPackagePath)) {
      return myStoreSettings.yoPackagePath;
    }

    return getDefaultYoPackagePath();
  }

  public void setNodePath(String path) {
    myStoreSettings.nodeExePath = path;
  }

  public void setYoPackage(String path) {
    final String normalizedPath = trimAndNotNullize(path);
    myStoreSettings.yoPackagePath = !StringUtil.isEmpty(normalizedPath) && !StringUtil.equals(normalizedPath, getDefaultYoPackagePath()) ?
                                    normalizedPath :
                                    null;
  }

  @NotNull
  private static String trimAndNotNullize(String path) {
    return StringUtil.notNullize(path).trim();
  }


  @Nullable
  private String getDefaultYoPackagePath() {
    NodeJsLocalInterpreter interpreter = getInterpreter();

    if (interpreter != null) {

      final VirtualFile dir = interpreter.getGlobalNodeModulesVirtualDir();
      if (dir != null) {
        final VirtualFile yo = dir.findChild("yo");
        if (yo != null && yo.exists()) {
          final String yoPath = yo.getCanonicalPath();
          if (yoPath != null) {
            return FileUtil.toSystemDependentName(yoPath);
          }
        }
      }
    }

    return null;
  }



  public String getCLIHelperPath() {
    try {
      String jarPath = PathUtil.getJarPathForClass(YeomanGlobalSettings.class);
      if (!jarPath.endsWith(".jar")) {
        return getCLIHelperPathFromWorkspace();
      }
      return getCLIHelperPathFromJar(jarPath);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @NotNull
  private static String getCLIHelperPathFromJar(String jarPath) {
    File jarFile = new File(jarPath);
    if (!jarFile.isFile()) {
      throw new RuntimeException("jar file cannot be null");
    }
    File pluginBaseDir = jarFile.getParentFile().getParentFile();
    return new File(new File(pluginBaseDir, RELATIVE_CLI_HELPER_PATH), CLI_HELPER_NAME).getAbsolutePath();
  }

  @NotNull
  private static String getCLIHelperPathFromWorkspace() {
    URL resource = YeomanGlobalSettings.class.getClassLoader().getResource(RELATIVE_CLI_HELPER_PATH);
    if (resource == null) {
      throw new RuntimeException("Cannot find file compiler implementation");
    }

    return new File(URLUtil.decode(resource.getPath()), CLI_HELPER_NAME).getAbsolutePath();
  }
}
