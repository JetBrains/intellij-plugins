package org.angularjs.service.protocol.command;

import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetErrCommand;


public class Angular2GetHtmlErrorCommand extends TypeScriptGetErrCommand {
  public static final String COMMAND = "IDEGetHtmlErrors";

  public Angular2GetHtmlErrorCommand(String filePath) {
    super(filePath);
  }

  @Override
  public String getCommand() {
    return COMMAND;
  }
}
