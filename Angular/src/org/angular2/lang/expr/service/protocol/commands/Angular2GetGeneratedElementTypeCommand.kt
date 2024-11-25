package org.angular2.lang.expr.service.protocol.commands

import com.google.gson.JsonObject
import com.intellij.lang.javascript.psi.resolve.JSEvaluationStatisticsCollector
import com.intellij.lang.typescript.compiler.TypeScriptServiceQueueCommand
import com.intellij.lang.typescript.compiler.TypeScriptServiceRequest
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.openapi.components.service
import com.intellij.util.application

class Angular2GetGeneratedElementTypeCommand(args: Angular2GetGeneratedElementTypeRequestArgs)
  : TypeScriptServiceQueueCommand<Angular2GetGeneratedElementTypeRequestArgs, JsonObject>("ngGetGeneratedElementType", args) {

  override fun processResult(answer: JsonObject): JsonObject? =
    answer.getAsJsonObject(TypeScriptServerServiceImpl.BODY_FIELD)

  override fun onServiceTaskCompleted(task: TypeScriptServiceRequest<Angular2GetGeneratedElementTypeRequestArgs, JsonObject>) {
    if (JSEvaluationStatisticsCollector.State.isEnabled()) {
      application.service<JSEvaluationStatisticsCollector>().responseReady(!task.wasExecuted)
    }
  }

}