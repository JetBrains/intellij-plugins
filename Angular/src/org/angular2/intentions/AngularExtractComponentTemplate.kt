package org.angular2.intentions

import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.Angular2DecoratorUtil.TEMPLATE_URL_PROP
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.Angular2Bundle

class AngularExtractComponentTemplate : JavaScriptIntention() {

  override fun getFamilyName(): @IntentionFamilyName String = Angular2Bundle.message("angular.intention.extract.component.template.name")

  override fun getText(): @IntentionName String = this.familyName

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
    findProperty(element) != null

  override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
    val property = findProperty(element) ?: return
    val component = Angular2EntitiesProvider.getComponent(Angular2DecoratorUtil.getClassForDecoratorElement(property)) ?: return
    val template = component.templateFile ?: return

    val templateFileName = calculateTemplateFileName(property.containingFile) ?: return

    if (!IntentionPreviewUtils.isIntentionPreviewActive()) {
      val newTemplateFile = property.containingFile.containingDirectory.createFile(templateFileName)
      val document = PsiDocumentManager.getInstance(project).getDocument(newTemplateFile)!!
      document.setText(template.text.replace("\\`", "`").trimStart('\n').trimEnd(' ', '\n').trimIndent() + "\n")
      PsiDocumentManager.getInstance(project).commitDocument(document)
    }

    val newProperty = (property.setName(TEMPLATE_URL_PROP) as JSProperty)
    newProperty
      .initializer
      ?.replace(JSChangeUtil.createExpressionWithContext("'./$templateFileName'", property)!!.psi)

    FormatFixer.create(newProperty, FormatFixer.Mode.Reformat).fixFormat()
  }

  private fun calculateTemplateFileName(componentFile: PsiFile): String? {
    val baseName = FileUtil.getNameWithoutExtension(componentFile.name)
    val templateFileName = "$baseName.html"
    val dir = componentFile.containingDirectory
              ?: return templateFileName
    if (dir.findFile(templateFileName) == null)
      return templateFileName
    var i = 1
    while (dir.findFile("$baseName.$i.html") != null) {
      i++
    }
    return "$baseName.$i.html"
  }

  private fun findProperty(element: PsiElement) =
    (InjectedLanguageManager.getInstance(element.project).getInjectionHost(element) ?: element)
      .parent.asSafely<JSProperty>()
      ?.takeIf {
        it.name == TEMPLATE_PROP
        && Angular2DecoratorUtil.isAngularEntityInitializerProperty(it, false, COMPONENT_DEC)
      }

}