package org.angular2.lang.expr.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.openapi.project.Project
import org.angular2.lang.expr.service.protocol.commands.Angular2TranspiledTemplateCommand
import org.angular2.lang.expr.service.protocol.commands.Angular2TranspiledTemplateRequestArgs
import org.angular2.lang.expr.service.protocol.commands.CodeMapping

class Angular2LanguageServiceCache(project: Project) : TypeScriptLanguageServiceCache(project) {

  override fun updateCacheAndGetServiceObject(input: JSLanguageServiceCommand): JSLanguageServiceObject? =
    if (input is Angular2TranspiledTemplateCommand)
      computeInNonBlockingReadAction { getUpdateTemplateServiceObject(input) }
    else
      super.updateCacheAndGetServiceObject(input)

  private fun getUpdateTemplateServiceObject(input: Angular2TranspiledTemplateCommand): ServiceObjectWithCacheUpdate =
    ServiceObjectWithCacheUpdate(
      Angular2TranspiledTemplateRequestArgs.build(input.templateFile, "{{ foo }}",
                                                  """import * as i0 from './tcb-check.component';
import * as i1 from '@angular/core';

/*tcb1*/
function _tcb1(this: i0.TcbCheckComponent) { if (true) {
    "" + (((this).title /*3,8*/) /*3,8*/);
} }

export const IS_A_MODULE = true;
""".trim(), listOf(
        CodeMapping(TypeScriptCompilerConfigUtil.normalizeNameAndPath(input.templateFile)!!, listOf(3), listOf(167), listOf(5))
      )),
      emptyList()
    )

}