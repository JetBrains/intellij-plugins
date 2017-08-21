package org.angularjs.service.protocol;


import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Consumer;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Angular2LanguageServiceProtocol extends TypeScriptServiceStandardOutputProtocol {
  public static final String NG_LANGUAGE_SERVICE = "ngLanguageService";
  private final String myNgServicePath;

  public Angular2LanguageServiceProtocol(@NotNull Project project,
                                         @NotNull String ngServicePath,
                                         @NotNull TypeScriptCompilerSettings settings,
                                         @NotNull Consumer<?> readyConsumer,
                                         @NotNull Consumer<JSLanguageServiceAnswer> eventConsumer) {
    super(project, settings, readyConsumer, eventConsumer);
    myNgServicePath = ngServicePath;
  }

  @Override
  public Angular2InitialStateObject createState() {
    Angular2InitialStateObject state = new Angular2InitialStateObject();
    fillState(state);
    state.pluginName = "angular 2 language service";
    File directory = JSLanguageServiceUtil.getPluginDirectory(AngularJSLanguage.class, NG_LANGUAGE_SERVICE);
    state.typescriptPluginPath =
      TypeScriptUtil.getTypeScriptCompilerFolderFile().getAbsolutePath() + File.separator + "typescript" + File.separator;

    //override plugin path
    state.pluginPath = new File(directory, "angular-plugin.js").getAbsolutePath();
    state.ngServicePath = FileUtil.toSystemDependentName(myNgServicePath);
    return state;
  }
}
