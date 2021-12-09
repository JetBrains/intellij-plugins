package com.intellij.deno.run

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterType
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreterType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.Ref
import com.intellij.util.NullableConsumer
import com.intellij.util.text.SemVer

class DenoInterpreter(@NlsSafe private val myPath: String) : NodeJsInterpreter() {
  
  override fun getType(): NodeJsInterpreterType<out NodeJsInterpreter> = NodeJsLocalInterpreterType.getInstance()
  override fun getReferenceName(): String = myPath
  override fun getPresentableName(): String = myPath
  override fun getCachedVersion(): Ref<SemVer>? = null
  override fun fetchVersion(consumer: NullableConsumer<in SemVer>) = consumer.consume(null)
  override fun validate(project: Project?): String? = null
}