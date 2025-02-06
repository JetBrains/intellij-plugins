// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.refactoring.extractComponent

import com.intellij.application.options.CodeStyle
import com.intellij.ide.IdeEventQueue
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.model.Pointer
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.impl.pumpEventsForHierarchy
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Conditions
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.withModalProgress
import com.intellij.platform.util.progress.SequentialProgressReporter
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.psi.util.descendantsOfType
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.util.CommonRefactoringUtil.showErrorHint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.cli.AngularCliUtil
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.Angular2LangUtil.EVENT_EMITTER
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.lang.html.parser.Angular2AttributeType

internal class Angular2ExtractComponentHandler : RefactoringActionHandler {
  override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
    // available only in editor
  }

  override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
    if (editor == null) return
    if (file == null) return

    val virtualFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(file).virtualFile
    val workingDir = virtualFile.parent
    val cliDir = AngularCliUtil.findAngularCliFolder(project, virtualFile)

    if (cliDir == null) {
      showErrorHint(project, editor, Angular2Bundle.message("angular.notify.cli.required-package-not-installed"))
      return
    }
    project.service<Angular2ExtractComponentHandlerService>().run(editor, file.createSmartPointer(), workingDir, cliDir)
  }
}

@Service(Service.Level.PROJECT)
class Angular2ExtractComponentHandlerService(
  private val project: Project,
  private val coroutineScope: CoroutineScope,
) {

  fun run(editor: Editor, sourceFilePtr: Pointer<PsiFile>, workingDir: VirtualFile, cliDir: VirtualFile) {
    var ex: Throwable? = null
    val job = coroutineScope.launch {
      withModalProgress(ModalTaskOwner.project(project), Angular2Bundle.message("angular.refactor.extractComponent.task"),
                        TaskCancellation.nonCancellable()) {
        try {
          runInsideCoroutine(editor, sourceFilePtr, workingDir, cliDir)
        }
        catch (e: Throwable) {
          if (e !is CancellationException && e !is ProcessCanceledException) {
            ex = e
          }
          else throw e
        }
      }
    }
    IdeEventQueue.getInstance().pumpEventsForHierarchy {
      job.isCompleted
    }
    if (ex != null) {
      throw ex
    }
  }

  private suspend fun runInsideCoroutine(editor: Editor, sourceFilePtr: Pointer<PsiFile>, workingDir: VirtualFile, cliDir: VirtualFile) {
    reportSequentialProgress { reporter ->
      runInsideCoroutine(reporter, editor, sourceFilePtr, workingDir, cliDir)
    }
  }

  private suspend fun runInsideCoroutine(
    reporter: SequentialProgressReporter,
    editor: Editor,
    sourceFilePtr: Pointer<PsiFile>,
    workingDir: VirtualFile,
    cliDir: VirtualFile,
  ) {
    reporter.nextStep(endFraction = 10)
    writeAction {
      PsiDocumentManager.getInstance(sourceFilePtr.dereference()!!.project).commitAllDocuments()
    }

    reporter.nextStep(endFraction = 20)
    val extractedComponent = try {
      readAction {
        if (editor.caretModel.caretCount > 1) {
          throw Angular2ExtractComponentUnsupportedException(
            Angular2Bundle.message("angular.refactor.extractComponent.unsupported-multiple-carets"))
        }

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        Angular2ExtractedComponentBuilder(sourceFilePtr.dereference()!!, selectionStart, selectionEnd).build()
      }
    }
    catch (e: Angular2ExtractComponentUnsupportedException) {
      showErrorHint(project, editor, e.message!!)
      return
    }

    reporter.nextStep(endFraction = 40)
    val rangeHighlighter = addRangeHighlighter(editor, extractedComponent)
    val postProcessCli = try {
      val arguments = project.service<Angular2CliComponentGenerator>().showDialog()
                      ?: return
      try {
        project.service<Angular2CliComponentGenerator>().generateComponent(cliDir, workingDir, arguments)
      }
      catch (e: Exception) {
        thisLogger().warn("Couldn't create component with Angular CLI", e)
        showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.cli-error"))
        return
      }
    }
    finally {
      clearRangeHighlighter(editor, rangeHighlighter)
    }

    reporter.nextStep(endFraction = 60)
    val affectedPaths = writeAction {
      var result: List<String>? = null
      CommandProcessor.getInstance().runUndoTransparentAction {
        result = postProcessCli()
      }
      result
    } ?: return

    reporter.nextStep(endFraction = 80)
    val context = run {
      var context: GeneratorContext? = null
      DumbService.getInstance(project).runReadActionInSmartMode {
        try {
          val sourceFile = sourceFilePtr.dereference()!!
          val targetComponentPath = extractComponentPath(affectedPaths)!!
          val targetComponentVirtualFile = cliDir.findFileByRelativePath(targetComponentPath)!!
          val targetComponentFile = PsiManager.getInstance(project).findFile(targetComponentVirtualFile)!!

          val targetComponentClass = targetComponentFile.descendantsOfType<TypeScriptClass>().first()
          val targetComponent = Angular2EntitiesProvider.getEntity(targetComponentClass) as Angular2Component
          val targetTemplateFile = targetComponent.templateFile!!
          val sourceComponentClass = Angular2SourceUtil.findComponentClasses(sourceFile).firstOrNull()
          context = GeneratorContext(
            sourceTemplateFile = sourceFile,
            sourceComponentClass = sourceComponentClass,
            targetTemplateFile = targetTemplateFile,
            targetComponentClass = targetComponentClass,
            targetComponentSelector = targetComponent.selector.text,
          )
        }
        catch (e: Exception) {
          thisLogger().warn("Unexpected CLI output", e)
          showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.unexpected-cli-output"))
        }
      }
      context
    } ?: return

    reporter.nextStep(endFraction = 100)
    writeAction {
      CommandProcessor.getInstance().executeCommand(
        project,
        {
          CommandProcessor.getInstance().markCurrentCommandAsGlobal(project)
          afterGenerator(project, editor, extractedComponent, context)
        },
        Angular2Bundle.message("angular.refactor.extractComponent.dialog"),
        null,
        UndoConfirmationPolicy.REQUEST_CONFIRMATION)
    }
  }

  private fun afterGenerator(
    project: Project,
    editor: Editor,
    extractedComponent: Angular2ExtractedComponent,
    generatorContext: GeneratorContext,
  ) {
    val sourceTemplateFile = generatorContext.sourceTemplateFile
    val targetTemplateFile = generatorContext.targetTemplateFile
    val targetComponentClass = generatorContext.targetComponentClass
    val targetComponentFile = targetComponentClass?.containingFile

    if (sourceTemplateFile == null || targetTemplateFile == null || targetComponentFile == null) {
      thisLogger().warn("Failed to restore pointers.")
      showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.after-generator-error"))
      return
    }

    if (!ReadonlyStatusHandler.ensureFilesWritable(project, sourceTemplateFile.virtualFile, targetTemplateFile.virtualFile,
                                                   targetComponentFile.virtualFile)) {
      thisLogger().warn("Failed to ensure files are writable.")
      showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.after-generator-error"))
      return
    }

    try {
      modifySourceTemplateFile(project, extractedComponent, generatorContext)
      modifyTargetTemplateFile(project, extractedComponent, generatorContext)
      modifyTargetComponentFile(project, extractedComponent, generatorContext)
    }
    catch (e: Exception) {
      if (ApplicationManager.getApplication().isUnitTestMode) {
        throw RuntimeException("Failed to modify source", e)
      }
      thisLogger().warn("Something went wrong during file modification", e)
      showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.after-generator-error"))
    }
  }

  private fun extractComponentPath(output: List<String>): String? {
    output.forEach { filePath ->
      val ext = JSFileReferencesUtil.findExtension(filePath,
                                                   TypeScriptUtil.TYPESCRIPT_EXTENSIONS)
      if (ext != null) {
        val trimmedName = JSFileReferencesUtil.trimExistingExtension(filePath, ext)
        if (!trimmedName.endsWith(".spec")) return filePath
      }
    }
    return null
  }

  private fun modifySourceTemplateFile(project: Project,
                                       extractedComponent: Angular2ExtractedComponent,
                                       context: GeneratorContext) {
    val sourceTemplateFile = context.sourceTemplateFile!!
    val sourceDocument = PsiDocumentManager.getInstance(project).getDocument(sourceTemplateFile)!!

    val attrs = StringBuilder()
    for (attribute in extractedComponent.attributes) {
      val attrName = attribute.attributeType.buildName(attribute.name)

      attrs.append(" $attrName=\"${attribute.assignedValue}\"")
    }
    val selector = context.targetComponentSelector
    val usage = "<$selector$attrs></$selector>"

    val sourceStartOffset = extractedComponent.sourceStartOffset
    sourceDocument.replaceString(sourceStartOffset, sourceStartOffset + extractedComponent.template.length, usage)
    PsiDocumentManager.getInstance(project).commitDocument(sourceDocument)

    // A really wierd workaround to get pointers within reformatText working with injected template
    val sourceFile = context.sourceTemplateFile!!.let { file ->
      val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
      injectedLanguageManager.getInjectionHost(file)
        ?.let { host -> injectedLanguageManager.getInjectedPsiFiles(host) }
        ?.takeIf { it.size == 1 }
        ?.get(0)
        ?.first?.containingFile
      ?: file
    }

    CodeStyleManager.getInstance(project).reformatText(sourceFile, sourceStartOffset, sourceStartOffset + usage.length)
  }

  private fun modifyTargetTemplateFile(project: Project,
                                       extractedComponent: Angular2ExtractedComponent,
                                       context: GeneratorContext) {
    val templateFile: PsiFile = context.targetTemplateFile!!
    val templateDocument = PsiDocumentManager.getInstance(project).getDocument(templateFile)!!

    var template = extractedComponent.template
    extractedComponent.replacements.sortedByDescending { it.textRange.startOffset }.forEach { replacement ->
      val textRange = replacement.textRange
      template = template.replaceRange(textRange.startOffset, textRange.endOffset, replacement.text)
    }

    if (InjectedLanguageManager.getInstance(project).isInjectedFragment(templateFile)) {
      template = "\n" + template + "\n" // nicer formatting & prevents trailing whitespaces from loose source selection
    }

    templateDocument.setText(template)
    PsiDocumentManager.getInstance(project).commitDocument(templateDocument)

    val sourceTemplateFile = context.sourceTemplateFile
    if (sourceTemplateFile != null) {
      CodeStyle.reformatWithFileContext(templateFile, sourceTemplateFile)
    }
    else {
      CodeStyleManager.getInstance(project).reformat(templateFile)
    }
  }

  private fun modifyTargetComponentFile(project: Project,
                                        extractedComponent: Angular2ExtractedComponent,
                                        context: GeneratorContext) {
    val componentClass = context.targetComponentClass!!
    val anchor = componentClass.constructors.firstOrNull() ?: componentClass.lastChild
    val semicolon = JSCodeStyleSettings.getSemicolon(componentClass)

    var seenInput = false
    var seenOutput = false

    for (attribute in extractedComponent.attributes) {
      val name = attribute.name
      val type = withTypeEvaluationLocation(componentClass) {
        attribute.jsType.getTypeText(JSType.TypeTextFormat.CODE)
      }
      val texts = when (attribute.attributeType) {
        Angular2AttributeType.PROPERTY_BINDING -> {
          seenInput = true
          listOf("@$INPUT_DEC() $name: $type$semicolon")
        }
        Angular2AttributeType.EVENT -> {
          seenOutput = true
          listOf("@$OUTPUT_DEC() $name = new $EVENT_EMITTER<$type>()$semicolon")
        }
        Angular2AttributeType.BANANA_BOX_BINDING -> {
          seenInput = true
          seenOutput = true
          listOf("@$INPUT_DEC() $name: $type$semicolon",
                 "@$OUTPUT_DEC() $name$OUTPUT_CHANGE_SUFFIX = new $EVENT_EMITTER<$type>()$semicolon")
        }
        else -> {
          emptyList()
        }
      }
      for (text in texts) {
        JSChangeUtil.createClassMemberPsiFromTextWithContext(text, componentClass, JSElement::class.java)?.let { member ->
          val inserted = componentClass.addBefore(member, anchor)
          CodeStyleManager.getInstance(project).reformatNewlyAddedElement(componentClass.node, inserted.node)
        }
      }
    }

    insertImports(extractedComponent, componentClass.containingFile, seenInput, seenOutput)

    val sourceComponentFile = context.sourceComponentClass?.containingFile
    if (sourceComponentFile != null) {
      CodeStyle.reformatWithFileContext(componentClass.containingFile, sourceComponentFile)
    }
  }

  private fun insertImports(extractedComponent: Angular2ExtractedComponent,
                            componentFile: PsiFile,
                            seenInput: Boolean,
                            seenOutput: Boolean) {
    ES6CreateImportUtil.addRequiredImports(extractedComponent.importedInfos, Conditions.alwaysFalse(), componentFile)

    for (module in JSFileReferencesUtil.resolveModuleReference(componentFile, Angular2LangUtil.ANGULAR_CORE_PACKAGE)) {
      if (module !is JSElement) continue

      if (seenInput) {
        tryInsertImport(INPUT_DEC, componentFile, module)
      }
      if (seenOutput) {
        tryInsertImport(OUTPUT_DEC, componentFile, module)
        tryInsertImport(EVENT_EMITTER, componentFile, module)
      }
    }
  }

  private fun tryInsertImport(name: String, targetFile: PsiElement, coreModule: JSElement) {
    val input = JSResolveResult.toElements(ES6PsiUtil.resolveSymbolInModule(name, targetFile, coreModule)).firstOrNull() ?: return
    val createInfo = ES6ImportPsiUtil.CreateImportExportInfo(name, ES6ImportPsiUtil.ImportExportType.SPECIFIER)

    ES6ImportPsiUtil.insertJSImport(targetFile, createInfo, input)
  }

  private suspend fun addRangeHighlighter(editor: Editor,
                                          extractedComponent: Angular2ExtractedComponent): RangeHighlighter =
    writeAction {
      editor.markupModel.addRangeHighlighter(
        EditorColors.SEARCH_RESULT_ATTRIBUTES,
        extractedComponent.sourceStartOffset, extractedComponent.sourceStartOffset + extractedComponent.template.length,
        HighlighterLayer.SELECTION + 1,
        HighlighterTargetArea.EXACT_RANGE
      )
    }

  private suspend fun clearRangeHighlighter(editor: Editor, rangeHighlighter: RangeHighlighter?) {
    if (rangeHighlighter != null) {
      writeAction {
        editor.markupModel.removeHighlighter(rangeHighlighter)
      }
    }
  }

  private class GeneratorContext(
    sourceTemplateFile: PsiFile,
    sourceComponentClass: TypeScriptClass?,
    targetTemplateFile: PsiFile,
    targetComponentClass: TypeScriptClass,
    val targetComponentSelector: String,
  ) {
    private val sourceTemplateFilePtr: Pointer<PsiFile> = sourceTemplateFile.createSmartPointer()
    private val sourceComponentClassPtr: Pointer<TypeScriptClass>? = sourceComponentClass?.createSmartPointer()
    private val targetTemplateFilePtr: Pointer<PsiFile> = targetTemplateFile.createSmartPointer()
    private val targetComponentClassPtr: Pointer<TypeScriptClass> = targetComponentClass.createSmartPointer()

    val sourceTemplateFile: PsiFile? get() = sourceTemplateFilePtr.dereference()
    val sourceComponentClass: TypeScriptClass? get() = sourceComponentClassPtr?.dereference()
    val targetTemplateFile: PsiFile? get() = targetTemplateFilePtr.dereference()
    val targetComponentClass: TypeScriptClass? get() = targetComponentClassPtr.dereference()
  }
}

private fun showErrorHint(project: Project, editor: Editor, @NlsContexts.DialogMessage message: String) {
  showErrorHint(
    project,
    InjectedLanguageEditorUtil.getTopLevelEditor(editor),
    RefactoringBundle.getCannotRefactorMessage(message),
    Angular2Bundle.message("angular.refactor.extractComponent.dialog"),
    null
  )
}