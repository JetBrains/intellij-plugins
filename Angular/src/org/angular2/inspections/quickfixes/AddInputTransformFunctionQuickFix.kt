// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.daemon.impl.quickfix.EmptyExpression
import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.TRANSFORM_PROP
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.source.Angular2SourceDirectiveProperty
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil.remapToCopyIfNeeded
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE

class AddInputTransformFunctionQuickFix(private val kind: TransformKind,
                                        private val inputName: String,
                                        private val expressionType: String,
                                        clazz: TypeScriptClass)
  : LocalQuickFixAndIntentionActionOnPsiElement(clazz), PriorityAction {

  override fun getFamilyName(): String =
    Angular2Bundle.message("angular.quickfix.template.create-input-transformer.family")

  override fun getText(): String =
    if (kind == TransformKind.Custom)
      familyName
    else
      Angular2Bundle.message("angular.quickfix.template.create-input-transformer.std.name", StringUtil.decapitalize(kind.name))

  override fun isAvailable(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement): Boolean =
    !JSProjectUtil.isInLibrary(startElement)

  override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(startElement) { invokeInner(project, editor, startElement) }
  }

  private fun invokeInner(project: Project, editor: Editor?, startElement: PsiElement) {
    val input = Angular2EntitiesProvider.getDirective(startElement as? TypeScriptClass)
                  ?.inputs
                  ?.firstNotNullOfOrNull { property ->
                    (property as? Angular2SourceDirectiveProperty)
                      ?.takeIf { it.name == inputName && !it.virtualProperty && it.transformParameterType == null }
                  } ?: return
    val objectLiteral = Angular2FixesPsiUtil.getOrCreateInputObjectLiteral(
      input.declarationSource?.remapToCopyIfNeeded(startElement.containingFile)) ?: return
    if (kind == TransformKind.Custom) {
      val inputType = input.rawJsType
        ?.substitute(startElement)
        ?.getTypeText(JSType.TypeTextFormat.CODE)
      val property = Angular2FixesPsiUtil.insertJSObjectLiteralProperty(
        objectLiteral, TRANSFORM_PROP, "(value: $expressionType): $inputType => ", preferNewLines = false)
      if (IntentionPreviewUtils.isIntentionPreviewActive()) return
      OpenFileDescriptor(project, property.containingFile.virtualFile, property.textRange.endOffset)
        .takeIf { it.canNavigateToSource() }
        ?.navigate(true)
      val componentEditor = FileEditorManager.getInstance(project)
        .getSelectedEditor(property.containingFile.virtualFile)
        ?.asSafely<TextEditor>()
        ?.editor
      if (componentEditor != null) {
        property.value?.delete()
        val template = TemplateManager.getInstance(project)
          .createTemplate("ng_insert_input_transform", "angular", "(value: \$TYPE0\$): \$TYPE1\$ => \$END$")
        template.addVariable("TYPE0", TextExpression(expressionType), TextExpression(expressionType), true, true)
        template.addVariable("TYPE1", TextExpression(inputType), TextExpression(inputType), true, true)
        template.addVariable("END", EmptyExpression(), true)
        template.setToReformat(true)
        TemplateManager.getInstance(project).startTemplate(componentEditor, template)
      }
    }
    else {
      val functionName = StringUtil.decapitalize(kind.name)
      val function = WebJSResolveUtil.resolveSymbolFromNodeModule(startElement, ANGULAR_CORE_PACKAGE, functionName, PsiElement::class.java)
      if (function != null) {
        Angular2FixesPsiUtil.insertJSObjectLiteralProperty(objectLiteral, TRANSFORM_PROP, functionName, preferNewLines = false)
        ES6ImportPsiUtil.insertJSImport(startElement, functionName, function, editor)
      }
    }
  }

  enum class TransformKind {
    BooleanAttribute,
    NumberAttribute,
    Custom
  }

  override fun getPriority(): PriorityAction.Priority =
    if (kind == TransformKind.Custom)
      PriorityAction.Priority.NORMAL
    else
      PriorityAction.Priority.HIGH

}