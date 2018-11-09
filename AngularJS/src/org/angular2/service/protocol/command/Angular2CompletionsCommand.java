package org.angular2.service.protocol.command;


import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptCompletionsCommand;
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptCompletionsRequestArgs;
import org.jetbrains.annotations.NotNull;

public class Angular2CompletionsCommand extends TypeScriptCompletionsCommand {

  public static final String COMMAND = "IDENgCompletions";

  public Angular2CompletionsCommand(@NotNull TypeScriptCompletionsRequestArgs args) {
    super(COMMAND, args);
  }
}
