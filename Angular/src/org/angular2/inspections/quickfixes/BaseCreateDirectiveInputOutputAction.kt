// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.navigation.PsiTargetNavigator
import com.intellij.codeInsight.navigation.TargetUpdaterTask
import com.intellij.codeInsight.navigation.hidePopupIfDumbModeStarts
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.xml.XmlAttribute
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.entities.Angular2ClassBasedEntity
import org.angular2.lang.Angular2Bundle

abstract class BaseCreateDirectiveInputOutputAction(context: PsiElement, fieldName: String?) : BaseCreateComponentFieldAction(fieldName) {

  init {
    myElementPointer = context.createSmartPointer()
  }

  override fun isAvailable(project: Project?, element: PsiElement?, editor: Editor?, file: PsiFile?): Boolean =
    if (element is XmlAttribute) getTargetClasses(element).isNotEmpty()
    else super.isAvailable(project, element, editor, file)

  override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
    if (psiElement is XmlAttribute) {
      val targetClasses = getTargetClasses(psiElement).ifEmpty { return }
      choose(project, editor, targetClasses) {
        doApplyFix(project, it, it.containingFile, null)
        false
      }
    }
    else {
      super.applyFix(project, psiElement, file, editor)
    }
  }

  override fun getPriority(): PriorityAction.Priority {
    return PriorityAction.Priority.NORMAL
  }

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> {
    return Pair.create(null, psiElement.lastChild)
  }

  protected fun addDecoratedField(template: Template, decorator: String) {
    if (StringUtil.isJavaIdentifier(myReferencedName)) {
      template.addTextSegment("@$decorator() $myReferencedName")
    }
    else {
      template.addTextSegment("@$decorator('$myReferencedName') ")
      template.addTextSegment(JSStringUtil.toCamelCase(myReferencedName.replace(Regex("[^a-zA-Z_0-9]"), "_"))
                                .let { if (it[0].isDigit()) "_$it" else it })
    }
  }

  protected fun addTypeSegment(template: Template) {
    myElementPointer.dereference().let { context ->
      val type = inferType(context)
        ?.getTypeText(JSType.TypeTextFormat.CODE)
      if (type == null) {
        addCompletionVar(template)
      }
      else {
        template.addVariable("__type", ConstantNode(type), ConstantNode(type), true)
      }
    }
  }

  abstract fun inferType(context: PsiElement?): JSType?

  protected open fun getTargetClasses(context: XmlAttribute): List<TypeScriptClass> {
    val scope = Angular2DeclarationsScope(context)
    return Angular2ApplicableDirectivesProvider(context.parent, scope = scope).matched
      .asSequence()
      .filterIsInstance<Angular2ClassBasedEntity>()
      .mapNotNull { it.typeScriptClass }
      .filter { !JSProjectUtil.isInLibrary(it) }
      .distinct()
      .toList()
  }

  private fun choose(
    project: Project,
    editor: Editor?,
    targetClasses: List<TypeScriptClass>,
    processor: PsiElementProcessor<TypeScriptClass>,
  ) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      if (targetClasses.size == 1) processor.execute(targetClasses[0]) else throw RuntimeException("Multiple choices")
    }
    else
      PsiTargetNavigator(targetClasses)
        // Just to avoid assertion errors in case of a single element to choose
        .updater(object : TargetUpdaterTask(project, Angular2Bundle.message("angular.description.unnamed")) {
          override fun getCaption(size: Int): String? = null
        })
        .createPopup(project, Angular2Bundle.message("angular.quickfix.template.popup.choose-target-class"), processor)
        .also {
          hidePopupIfDumbModeStarts(it, project)
          if (editor != null) it.showInBestPositionFor(editor)
        }
  }


}