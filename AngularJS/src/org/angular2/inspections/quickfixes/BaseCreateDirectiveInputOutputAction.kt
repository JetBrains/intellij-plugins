// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.navigation.hidePopupIfDumbModeStarts
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.ecmascript6.ES6QualifiedNamedElementRenderer
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttribute
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.lang.Angular2Bundle
import java.util.function.Consumer

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
      }
    }
    else {
      super.applyFix(project, psiElement, file, editor)
    }
  }

  override fun getPriority(): PriorityAction.Priority {
    return PriorityAction.Priority.TOP
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

  private fun getTargetClasses(context: XmlAttribute): List<TypeScriptClass> =
    context.parent.attributes
      .asSequence()
      .flatMap { it.descriptor.asSafely<Angular2AttributeDescriptor>()?.sourceDirectives ?: emptyList() }
      .plus(context.parent.descriptor.asSafely<Angular2ElementDescriptor>()?.sourceDirectives ?: emptyList())
      .mapNotNull { it.typeScriptClass }
      .filter { !JSProjectUtil.isInLibrary(it) }
      .distinct()
      .toList()

  private fun choose(project: Project, editor: Editor?, targetClasses: List<TypeScriptClass>, processor: Consumer<TypeScriptClass>) =
    JBPopupFactory.getInstance()
      .createPopupChooserBuilder(targetClasses)
      .setFont(EditorUtil.getEditorFont())
      .setRenderer(ES6QualifiedNamedElementRenderer<TypeScriptClass>())
      .setNamerForFiltering { el: TypeScriptClass -> el.name }
      .withHintUpdateSupply()
      .setTitle(Angular2Bundle.message("angular.quickfix.template.popup.choose-target-class"))
      .setItemChosenCallback { processor.accept(it) }
      .createPopup()
      .also {
        hidePopupIfDumbModeStarts(it, project)
        if (editor != null) it.showInBestPositionFor(editor)
      }



}