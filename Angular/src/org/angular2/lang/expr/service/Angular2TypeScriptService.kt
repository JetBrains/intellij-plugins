// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.service

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptServiceEvaluationSupport
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServiceWidgetItem
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.application.ReadAction.computeCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.indexing.SubstitutedFileType
import com.intellij.util.ui.EDT
import icons.AngularIcons
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2EntitiesProvider
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
    super.isAcceptable(file) || isAcceptableHtmlFile(file)

  override fun isAcceptableNonTsFile(project: Project, service: TypeScriptConfigService, virtualFile: VirtualFile): Boolean {
    return super.isAcceptableNonTsFile(project, service, virtualFile) || isAcceptableHtmlFile(virtualFile)
  }

  private fun isAcceptableHtmlFile(file: VirtualFile): Boolean =
    file.isInLocalFileSystem && file.fileType.let {
      it is HtmlFileType && SubstitutedFileType.substituteFileType(file, it, project).asSafely<SubstitutedFileType>()?.language is Angular2HtmlDialect
    }

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is Angular2Language || file.language is Angular2HtmlDialect
  }

  override fun isAcceptableForHighlighting(file: PsiFile): Boolean =
    if (file.language is Angular2HtmlDialect || file.language is Angular2Language)
      false // For now do not use TS server for highlighting TODO - Angular2Compiler.isStrictTemplates(file)
    else
      super.isAcceptableForHighlighting(file)

  override fun postprocessErrors(file: PsiFile, list: List<JSAnnotationError>): List<JSAnnotationError> =
    computeCancellable<List<JSAnnotationError>, Throwable> {
      // For now do not use TS server for highlighting TODO - !Angular2Compiler.isStrictTemplates(file)
      if (file.language is TypeScriptLanguageDialect) {
        val templateRanges = findTemplatesRanges(file)
        if (templateRanges.isNotEmpty()) {
          return@computeCancellable list.filter { error -> templateRanges.none { it.contains(error.line, error.column) } }
        }
      }
      super.postprocessErrors(file, list)
    }

  override val typeEvaluationSupport: TypeScriptServiceEvaluationSupport = Angular2CompilerServiceEvaluationSupport(project)

  override fun supportsTypeEvaluation(virtualFile: VirtualFile, element: PsiElement): Boolean =
    (element.language.let { it is Angular2Language || it is Angular2HtmlDialect }
     && Angular2EntitiesProvider.findTemplateComponent(element) != null
    ) || super.supportsTypeEvaluation(virtualFile, element)

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
      if (element !is JSElement && element.parent !is JSElement) null else super.getElementType(element, virtualFile)

    override fun commitDocumentsBeforeGetElementType(element: PsiElement, virtualFile: VirtualFile) {
      commitDocumentsWithNBRA(virtualFile)
      if (element.language is Angular2Language || element.language is Angular2HtmlDialect) {
        process?.executeNoBlocking(Angular2TranspiledTemplateCommand(virtualFile), null, null)
      }
    }
  }

  private fun findTemplatesRanges(file: PsiFile): List<TemplateRange> {
    val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
                   ?: return emptyList()
    val result = SmartList<TemplateRange>()
    file.acceptChildren(object : JSElementVisitor() {
      override fun visitTypeScriptClass(typeScriptClass: TypeScriptClass) {
        Angular2DecoratorUtil.findDecorator(typeScriptClass, Angular2DecoratorUtil.COMPONENT_DEC)
          ?.let { Angular2DecoratorUtil.getProperty(it, Angular2DecoratorUtil.TEMPLATE_PROP) }
          ?.value?.textRange
          ?.let {
            val startLine = document.getLineNumber(it.startOffset)
            val endLine = document.getLineNumber(it.endOffset)
            result.add(TemplateRange(startLine, it.startOffset - document.getLineStartOffset(startLine),
                                     endLine, it.endOffset - document.getLineStartOffset(endLine)))
          }
      }

      override fun visitES6ExportDefaultAssignment(node: ES6ExportDefaultAssignment) {
        node.acceptChildren(this)
      }
    })
    return result
  }

  private data class TemplateRange(
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
  ) {
    fun contains(line: Int, column: Int): Boolean {
      if (line < startLine) return false
      if (line == startLine && column < startColumn) return false
      if (line > endLine) return false
      if (line == endLine && column >= endColumn) return false
      return true
    }
  }
}

fun isAngularTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  if (EDT.isCurrentThreadEdt())
    !isAngular2Context(project, context)
  else
    computeCancellable<Boolean, Throwable> { !isAngular2Context(project, context) }

  return when (getAngularSettings(project).serviceType) {
    AngularServiceSettings.AUTO -> true
    AngularServiceSettings.DISABLED -> false
  }
}
