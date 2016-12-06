package org.angularjs.service.protocol;


import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

public class Angular2LanguageServiceProtocol extends TypeScriptServiceStandardOutputProtocol {
  public static final String NG_LANGUAGE_SERVICE = "ngLanguageService";
  private final String myNgServicePath;

  public Angular2LanguageServiceProtocol(@NotNull Project project,
                                         @NotNull String ngServicePath,
                                         @NotNull TypeScriptCompilerSettings settings,
                                         @NotNull Consumer<?> readyConsumer) {
    super(project, settings, readyConsumer);
    myNgServicePath = ngServicePath;
  }

  @Override
  public Angular2InitialStateObject createState() {
    Angular2InitialStateObject state = new Angular2InitialStateObject();
    fillState(state);
    state.pluginName = "angular 2 language service";
    File directory = getServiceDirectory();
    state.typescriptPluginPath =
      TypeScriptUtil.getTypeScriptCompilerFolderFile().getAbsolutePath() + File.separator + "typescript" + File.separator;

    //override plugin path
    state.pluginPath = new File(directory, "angular-plugin.js").getAbsolutePath();
    state.ngServicePath = FileUtil.toSystemDependentName(myNgServicePath);
    return state;
  }

  public static File getServiceDirectory() {
    try {
      String jarPath = PathUtil.getJarPathForClass(AngularJSLanguage.class);
      if (!jarPath.endsWith(".jar")) {
        URL resource = AngularJSLanguage.class.getClassLoader().getResource(NG_LANGUAGE_SERVICE);
        if (resource == null) {
          throw new RuntimeException("Cannot find file compiler implementation");
        }

        return new File(URLDecoder.decode(resource.getPath(), CharsetToolkit.UTF8));
      }
      File jarFile = new File(jarPath);
      if (!jarFile.isFile()) {
        throw new RuntimeException("jar file cannot be null");
      }
      File pluginBaseDir = jarFile.getParentFile().getParentFile();
      return new File(pluginBaseDir, NG_LANGUAGE_SERVICE);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
