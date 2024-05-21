package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand
import com.intellij.openapi.vfs.VirtualFile

class Angular2TranspiledTemplateCommand(val templateFile: VirtualFile) : JSLanguageServiceCommand {

  override fun getCommand(): String = "ngTranspiledTemplate"

}