package org.angularjs.service.protocol.command;

import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetErrCommand;
import org.jetbrains.annotations.NotNull;


public class Angular2GetHtmlErrorCommand extends TypeScriptGetErrCommand {
  public static final String COMMAND = "IDEGetHtmlErrors";

  public Angular2GetHtmlErrorCommand(String filePath) {
    super(filePath);
  }

  @NotNull
  @Override
  public String getCommand() {
    return COMMAND;
  }
}
