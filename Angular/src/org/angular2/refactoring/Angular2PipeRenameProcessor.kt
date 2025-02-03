// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenameDialog
import com.intellij.refactoring.rename.RenameUtil
import com.intellij.usageView.UsageInfo
import com.intellij.util.IncorrectOperationException
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.NAME_PROP
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.Angular2DecoratorUtil.findDecorator
import org.angular2.Angular2DecoratorUtil.getProperty
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.entities.Angular2EntitiesProvider.getPipe
import org.angular2.lang.Angular2LangUtil

class Angular2PipeRenameProcessor : JSDefaultRenameProcessor() {
  override fun canProcessElement(element: PsiElement): Boolean {
    return getPipe(element) != null
           && Angular2LangUtil.isAngular2Context(element)
  }

  override fun isInplaceRenameSupported(): Boolean {
    return false
  }

  override fun substituteElementToRename(element: PsiElement, editor: Editor?): PsiElement {
    return getPipe(element)!!.sourceElement
  }

  @Throws(IncorrectOperationException::class)
  override fun renameElement(element: PsiElement,
                             newName: String,
                             usages: Array<UsageInfo>,
                             listener: RefactoringElementListener?) {
    if (element is JSImplicitElement && element.getParent() is TypeScriptClass) {
      val decorator = findDecorator((element.getParent() as TypeScriptClass), PIPE_DEC)
      val property = getProperty(decorator, NAME_PROP)
      property?.value?.asSafely<JSLiteralExpression>()?.references?.let { refs ->
        for (ref in refs) {
          if (ref.resolve() === element) {
            ref.handleElementRename(newName)
          }
        }
      }
    }
    RenameUtil.doRenameGenericNamedElement(element, newName, usages, listener)
  }

  override fun createRenameDialog(project: Project,
                                  element: PsiElement,
                                  nameSuggestionContext: PsiElement?,
                                  editor: Editor?): RenameDialog {
    return super.createRenameDialog(project, getPipe(element)!!.sourceElement,
                                    nameSuggestionContext, editor)
  }

  override fun prepareRenaming(element: PsiElement, newName: String, allRenames: MutableMap<PsiElement, String>) {
    assert(element is JSImplicitElement)
    val pipeClass: JSClass = getPipe(element)?.asSafely<Angular2ClassBasedEntity>()?.typeScriptClass ?: return
    allRenames[pipeClass] = getDefaultPipeClassName(newName)
    if (pipeClass.containingFile.name == getDefaultPipeFileName((element as JSImplicitElement).name)) {
      allRenames[pipeClass.containingFile] = getDefaultPipeFileName(newName)
      val specFile = pipeClass.containingFile.virtualFile
        .parent.findFileByRelativePath(
          getDefaultPipeSpecFileName(element.name))
      if (specFile != null) {
        val specPsiFile = pipeClass.manager.findFile(specFile)
        if (specPsiFile != null) {
          allRenames[specPsiFile] = getDefaultPipeSpecFileName(newName)
        }
      }
    }
  }

  private fun getDefaultPipeFileName(pipeName: String): String {
    return "$pipeName.pipe.ts"
  }

  private fun getDefaultPipeSpecFileName(pipeName: String): String {
    return "$pipeName.pipe.spec.ts"
  }

  private fun getDefaultPipeClassName(pipeName: String): String {
    return StringUtil.capitalize(pipeName) + "Pipe"
  }

}