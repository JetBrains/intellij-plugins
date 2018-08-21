package org.angular2.service.protocol.command

import com.intellij.lang.javascript.service.JSLanguageServiceCacheableCommand
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptCommandWithArguments
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptFileWithConfigArgs

class Angular2GetHtmlErrCommand(file: String) :
  TypeScriptCommandWithArguments<TypeScriptFileWithConfigArgs>("IDEGetHtmlErrors",
                                                               TypeScriptFileWithConfigArgs(file)), JSLanguageServiceCacheableCommand