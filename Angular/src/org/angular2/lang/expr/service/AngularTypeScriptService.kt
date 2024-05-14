// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import icons.AngularIcons
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.lang.expr.service.protocol.AngularTypeScriptServiceProtocol
import org.angular2.options.AngularConfigurable
import org.angular2.options.AngularServiceSettings
import org.angular2.options.getAngularSettings
import java.util.function.Consumer

class AngularTypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "Angular Console") {

  override fun getProcessName(): String = "Angular TypeScript"

  override fun isDisabledByContext(context: VirtualFile): Boolean {
    if (super.isDisabledByContext(context)) return true

    return !isVueServiceAvailableByContext(context)
  }

  private fun isVueServiceAvailableByContext(context: VirtualFile): Boolean = isAngularTypeScriptServiceEnabled(myProject, context)

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol {
    return AngularTypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), tsServicePath)
  }

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(this, currentFile, AngularIcons.Angular2, AngularConfigurable::class.java)
}


fun isAngularTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  if (!isAngular2Context(project, context)) return false

  return when (getAngularSettings(project).serviceType) {
    AngularServiceSettings.AUTO -> true
    AngularServiceSettings.DISABLED -> false
  }
}
