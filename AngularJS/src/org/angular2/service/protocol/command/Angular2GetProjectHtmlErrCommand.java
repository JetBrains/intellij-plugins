package org.angular2.service.protocol.command;


import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetProjectErrCommand;
import org.jetbrains.annotations.NotNull;

public class Angular2GetProjectHtmlErrCommand extends TypeScriptGetProjectErrCommand {

  public static final String COMMAND = "IDEGetProjectHtmlErr";

  public Angular2GetProjectHtmlErrCommand(@NotNull String filePath) {
    super(filePath);
  }

  @NotNull
  @Override
  public String getCommand() {
    return COMMAND;
  }
}
