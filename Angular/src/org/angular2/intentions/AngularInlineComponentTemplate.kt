package org.angular2.intentions

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_URL_PROP
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2Bundle

class AngularInlineComponentTemplate : JavaScriptIntention() {

  override fun getFamilyName(): @IntentionFamilyName String = Angular2Bundle.message("angular.intention.inline.component.template.name")

  override fun getText(): @IntentionName String = this.familyName

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
    findProperty(element) != null

  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    val property = findProperty(element) ?: return
    val component = Angular2EntitiesProvider.getComponent(Angular2DecoratorUtil.getClassForDecoratorElement(property)) ?: return
    val template = component.templateFile ?: return
    val newProperty = (property.setName(TEMPLATE_PROP) as JSProperty)

    // Preformat the template literal a bit, since the injection might be not reformatted at all with FormatFixer
    val lineIndent = CodeStyleManager.getInstance(project).getLineIndent(property.containingFile, property.textRange.startOffset)
                     ?: ""
    val indentSize = " ".repeat(CodeStyle.getIndentSize(property.containingFile))
    val templateLiteralText = "`\n" + template.text.replace("`", "\\`").trim('\n').prependIndent(lineIndent + indentSize) + "\n$lineIndent`"

    newProperty
      .initializer
      ?.replace(JSChangeUtil.createExpressionWithContext(templateLiteralText, property)!!.psi)
    if (!IntentionPreviewUtils.isIntentionPreviewActive()) {
      template.delete()
    }
    FormatFixer.create(newProperty, FormatFixer.Mode.Reformat).fixFormat()
  }

  private fun findProperty(element: PsiElement) =
    (InjectedLanguageManager.getInstance(element.project).getInjectionHost(element) ?: element)
      .parent.let { if (it is JSLiteralExpression && it.isQuotedLiteral) it.parent else it }
      .asSafely<JSProperty>()
      ?.takeIf {
        it.name == TEMPLATE_URL_PROP
        && Angular2DecoratorUtil.isAngularEntityInitializerProperty(it, false, COMPONENT_DEC)
      }

}