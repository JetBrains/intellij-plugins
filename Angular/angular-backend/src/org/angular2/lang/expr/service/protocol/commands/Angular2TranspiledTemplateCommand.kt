package org.angular2.lang.expr.service.protocol.commands

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand

class Angular2TranspiledTemplateCommand(val serviceObject: Angular2TranspiledTemplateRequestArgs)
  : JSLanguageServiceSimpleCommand {

  override fun toSerializableObject(): JSLanguageServiceObject = serviceObject

  override val command: String
    get() = "ngTranspiledTemplate"

  override val isResponseExpected: Boolean
    get() = false

}