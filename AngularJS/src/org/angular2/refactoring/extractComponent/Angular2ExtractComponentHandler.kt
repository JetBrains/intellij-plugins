// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring.extractComponent

import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.resolve.JSResolveResult
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.Conditions
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.psi.util.descendantsOfType
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.util.CommonRefactoringUtil
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.cli.AngularCliUtil
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.Angular2LangUtil.EVENT_EMITTER
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.lang.html.parser.Angular2AttributeType

class Angular2ExtractComponentHandler : RefactoringActionHandler {
  override fun invoke(project: Project, elements: Array<out PsiElement>?, dataContext: DataContext?) {
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

    ProgressManager.getInstance().run(object : Task.Modal(project,
                                                          Angular2Bundle.message("angular.refactor.extractComponent.task"),
                                                          false) {
      override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = false
        indicator.fraction = 0.33
        val rangeHighlighterRef = Ref<RangeHighlighter>()
        try {
          val extractedComponent = try {
            ApplicationManager.getApplication().runReadAction<Angular2ExtractedComponent> {
              if (editor.caretModel.caretCount > 1) {
                throw Angular2ExtractComponentUnsupportedException(
                  Angular2Bundle.message("angular.refactor.extractComponent.unsupported-multiple-carets"))
              }

              val selectionStart = editor.selectionModel.selectionStart
              val selectionEnd = editor.selectionModel.selectionEnd
              Angular2ExtractedComponentBuilder(file, selectionStart, selectionEnd).build()
            }
          }
          catch (e: Angular2ExtractComponentUnsupportedException) {
            showErrorHint(project, editor, e.message!!)
            return
          }

          addRangeHighlighter(editor, rangeHighlighterRef, extractedComponent)

          val arguments = Angular2CliComponentGenerator.getInstance(project).showDialog()
          // Check if cancelled
          if (arguments == null) return

          val postProcessCli = try {
            Angular2CliComponentGenerator.getInstance(project).generateComponent(cliDir, workingDir, arguments)
          }
          catch (e: Exception) {
            showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.cli-error"))
            return
          }

          indicator.fraction = 0.66

          DumbService.getInstance(project).smartInvokeAndWait {
            WriteCommandAction.writeCommandAction(project)
              .withName(Angular2Bundle.message("angular.refactor.extractComponent.dialog"))
              .withGlobalUndo()
              .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
              .run<Nothing> {
                afterGenerator(project, editor, cliDir, extractedComponent, file, postProcessCli)
              }
          }

          indicator.fraction = 1.0
        }
        finally {
          clearRangeHighlighter(editor, rangeHighlighterRef)
        }
      }
    })
  }

  private fun afterGenerator(project: Project,
                             editor: Editor,
                             cliDir: VirtualFile,
                             extractedComponent: Angular2ExtractedComponent,
                             sourceFile: PsiFile,
                             postProcessCli: () -> List<String>) {
    try {
      val affectedPaths = postProcessCli()
      val componentPath = extractComponentPath(affectedPaths)!!
      val componentVirtualFile = cliDir.findFileByRelativePath(componentPath)!!
      val componentFile = PsiManager.getInstance(project).findFile(componentVirtualFile)!!

      val componentClass = componentFile.descendantsOfType<TypeScriptClass>().first()
      val component = Angular2EntitiesProvider.getEntity(componentClass) as Angular2Component
      val selector = component.selector.text
      val templateFile = component.templateFile!!

      if (!ReadonlyStatusHandler.ensureFilesWritable(project, sourceFile.virtualFile, templateFile.virtualFile, componentVirtualFile)) {
        throw IllegalStateException()
      }

      try {
        modifySourceFile(project, sourceFile, selector, extractedComponent)
        modifyTemplateFile(project, templateFile, extractedComponent)
        modifyComponentFile(project, componentFile, componentClass, extractedComponent)
      }
      catch (e: Exception) {
        showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.after-generator-error"))
      }

    }
    catch (e: Exception) {
      showErrorHint(project, editor, Angular2Bundle.message("angular.refactor.extractComponent.unexpected-cli-output"))
    }
  }

  private fun extractComponentPath(output: List<String>): String? {
    output.forEach { filePath ->
      if (filePath.endsWith(".ts") && !filePath.endsWith(".spec.ts")) {
        return filePath
      }
    }
    return null
  }

  private fun modifySourceFile(project: Project,
                               sourceFile: PsiFile,
                               selector: String,
                               extractedComponent: Angular2ExtractedComponent) {
    val sourceDocument = PsiDocumentManager.getInstance(project).getDocument(sourceFile)!!

    val attrs = StringBuilder()
    for (attribute in extractedComponent.attributes) {
      val attrName = attribute.attributeType.buildName(attribute.name)

      attrs.append(" $attrName=\"${attribute.assignedValue}\"")
    }
    val usage = "<$selector$attrs></$selector>"

    val sourceStartOffset = extractedComponent.sourceStartOffset
    sourceDocument.replaceString(sourceStartOffset, sourceStartOffset + extractedComponent.template.length, usage)
    PsiDocumentManager.getInstance(project).commitDocument(sourceDocument)
    CodeStyleManager.getInstance(project).reformatText(sourceFile, sourceStartOffset, sourceStartOffset + usage.length)
  }

  private fun modifyTemplateFile(project: Project,
                                 templateFile: PsiFile,
                                 extractedComponent: Angular2ExtractedComponent) {
    val templateDocument = PsiDocumentManager.getInstance(project).getDocument(templateFile)!!

    var template = extractedComponent.template
    extractedComponent.replacements.sortedByDescending { it.textRange.startOffset }.forEach { replacement ->
      val textRange = replacement.textRange
      template = template.replaceRange(textRange.startOffset, textRange.endOffset, replacement.text)
    }

    templateDocument.setText(template)
    PsiDocumentManager.getInstance(project).commitDocument(templateDocument)
    CodeStyleManager.getInstance(project).reformat(templateFile)
  }

  private fun modifyComponentFile(project: Project,
                                  componentFile: PsiFile,
                                  componentClass: TypeScriptClass,
                                  extractedComponent: Angular2ExtractedComponent) {
    val anchor = componentClass.constructors.first() ?: componentClass.lastChild
    val semicolon = JSCodeStyleSettings.getSemicolon(componentClass)

    var seenInput = false
    var seenOutput = false

    for (attribute in extractedComponent.attributes) {
      val name = attribute.name
      val type = attribute.jsType.getTypeText(JSType.TypeTextFormat.CODE)
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

    insertImports(extractedComponent, componentFile, seenInput, seenOutput)
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

    ES6ImportPsiUtil.insertJSImport(targetFile, createInfo, input, null)
  }

  private fun showErrorHint(project: Project, editor: Editor, @NlsContexts.DialogMessage message: String) {
    CommonRefactoringUtil.showErrorHint(
      project,
      InjectedLanguageEditorUtil.getTopLevelEditor(editor),
      RefactoringBundle.getCannotRefactorMessage(message),
      Angular2Bundle.message("angular.refactor.extractComponent.dialog"),
      null
    )
  }

  private fun addRangeHighlighter(editor: Editor,
                                  rangeHighlighterRef: Ref<RangeHighlighter>,
                                  extractedComponent: Angular2ExtractedComponent) {
    ApplicationManager.getApplication().invokeAndWait {
      ApplicationManager.getApplication().runWriteAction {
        editor.markupModel.addRangeHighlighter(
          EditorColors.SEARCH_RESULT_ATTRIBUTES,
          extractedComponent.sourceStartOffset, extractedComponent.sourceStartOffset + extractedComponent.template.length,
          HighlighterLayer.SELECTION + 1,
          HighlighterTargetArea.EXACT_RANGE
        ).let(rangeHighlighterRef::set)
      }
    }
  }

  private fun clearRangeHighlighter(editor: Editor, rangeHighlighterRef: Ref<RangeHighlighter>) {
    val highlighter = rangeHighlighterRef.get()
    if (highlighter != null) {
      ApplicationManager.getApplication().invokeAndWait {
        ApplicationManager.getApplication().runWriteAction {
          editor.markupModel.removeHighlighter(highlighter)
        }
      }
    }
  }

  private inline fun DumbService.smartInvokeAndWait(crossinline runnable: () -> Unit) {
    val actionCallback = ActionCallback()
    DumbService.getInstance(project).smartInvokeLater {
      try {
        runnable()
      }
      finally {
        actionCallback.setDone()
      }
    }
    actionCallback.waitFor(Long.MAX_VALUE)
  }
}