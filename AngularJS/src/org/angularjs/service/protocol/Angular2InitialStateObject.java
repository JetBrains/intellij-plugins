package org.angularjs.service.protocol;


import com.intellij.lang.javascript.service.protocol.LocalFilePath;
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptServiceInitialStateObject;

public class Angular2InitialStateObject extends TypeScriptServiceInitialStateObject {
  public LocalFilePath typescriptPluginPath;
  public LocalFilePath ngServicePath;
}
