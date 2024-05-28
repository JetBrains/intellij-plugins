// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.service

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptServiceEvaluationSupport
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import com.intellij.util.indexing.SubstitutedFileType
import icons.AngularIcons
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.service.protocol.Angular2TypeScriptServiceProtocol
import org.angular2.lang.expr.service.protocol.commands.Angular2TranspiledTemplateCommand
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.options.AngularConfigurable
import org.angular2.options.AngularServiceSettings
import org.angular2.options.getAngularSettings
import java.util.function.Consumer

class AngularTypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "Angular Console") {

  override fun getProcessName(): String = "Angular TypeScript"

  override fun isAcceptable(file: VirtualFile): Boolean =
    super.isAcceptable(file)
    || file.isInLocalFileSystem && file.fileType.let {
      it is HtmlFileType && SubstitutedFileType.substituteFileType(file, it, project).asSafely<SubstitutedFileType>()?.language is Angular2HtmlDialect
    }

  override fun isAcceptableNonTsFile(project: Project, service: TypeScriptConfigService, virtualFile: VirtualFile): Boolean {
    return super.isAcceptableNonTsFile(project, service, virtualFile) || virtualFile.isInLocalFileSystem && virtualFile.fileType.let {
      it is HtmlFileType && SubstitutedFileType.substituteFileType(virtualFile, it, project).asSafely<SubstitutedFileType>()?.language is Angular2HtmlDialect
    }
  }

  override fun canHighlight(file: PsiFile): Boolean {
    return file.language is Angular2HtmlDialect
           || super.canHighlight(file)
  }

  override val typeEvaluationSupport: TypeScriptServiceEvaluationSupport = Angular2CompilerServiceEvaluationSupport(project)

  override fun supportsTypeEvaluation(virtualFile: VirtualFile, element: PsiElement): Boolean =
    element.language is Angular2Language || super.supportsTypeEvaluation(virtualFile, element)

  override fun isDisabledByContext(context: VirtualFile): Boolean =
    super.isDisabledByContext(context)
    || !isAngularServiceAvailableByContext(context)

  private fun isAngularServiceAvailableByContext(context: VirtualFile): Boolean =
    isAngularTypeScriptServiceEnabled(myProject, context)

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol =
    Angular2TypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), tsServicePath)

  override fun createWidgetItem(currentFile: VirtualFile?): LanguageServiceWidgetItem =
    TypeScriptServiceWidgetItem(this, currentFile, AngularIcons.Angular2, AngularIcons.Angular2, AngularConfigurable::class.java)

  override fun createLSCache(): TypeScriptLanguageServiceCache =
    Angular2LanguageServiceCache(myProject)

  override fun beforeGetErrors(file: VirtualFile) {
    process?.executeNoBlocking(Angular2TranspiledTemplateCommand(file), null, null)
  }

  private inner class Angular2CompilerServiceEvaluationSupport(project: Project) : TypeScriptCompilerServiceEvaluationSupport(project) {

    override val service: TypeScriptService
      get() = this@AngularTypeScriptService

    override fun getElementType(element: PsiElement, virtualFile: VirtualFile): JSType? =
      if (element !is JSElement) null else super.getElementType(element, virtualFile)

    override fun commitDocumentsBeforeGetElementType(element: PsiElement, virtualFile: VirtualFile) {
      commitDocumentsWithNBRA(virtualFile)
      if (element.language is Angular2Language || element.language is Angular2HtmlDialect) {
        process?.executeNoBlocking(Angular2TranspiledTemplateCommand(virtualFile), null, null)
      }
    }
  }
}

fun isAngularTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  if (!isAngular2Context(project, context)) return false

  return when (getAngularSettings(project).serviceType) {
    AngularServiceSettings.AUTO -> true
    AngularServiceSettings.DISABLED -> false
  }
}
