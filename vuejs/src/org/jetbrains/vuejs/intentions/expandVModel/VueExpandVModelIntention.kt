package org.jetbrains.vuejs.intentions.expandVModel

import com.intellij.codeInsight.intention.FileModifier.SafeFieldForPreview
import com.intellij.javascript.web.codeInsight.html.elements.WebSymbolElementDescriptor
import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElementType
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.web.getModel

class VueExpandVModelIntention : JavaScriptIntention() {
  override fun getFamilyName(): String = VueBundle.message("vue.template.intention.v-model.expand.family.name")
  override fun getText(): String = this.familyName
  @SafeFieldForPreview
  private val validModifiers = setOf("lazy", "number", "trim")

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
    element.node.elementType == XmlElementType.XML_NAME
           && element.parent
             ?.let {
               it.node.elementType == XmlElementType.XML_ATTRIBUTE
               && it.isValid  && it is XmlAttribute
               && it.parent?.descriptor is WebSymbolElementDescriptor
               && isValidVModel(it)
             } == true
           && isVueContext(element)

  private fun isValidVModel(attribute: XmlAttribute): Boolean {
    val info = VueAttributeNameParser.parse((attribute.name), attribute.parent)
    return (info as? VueDirectiveInfo)?.directiveKind == VueDirectiveKind.MODEL
           && validModifiers.containsAll(info.modifiers)
  }

  override fun invoke(project: Project, editor: Editor?, psiElement: PsiElement) {
    editor ?: return
    val parent: PsiElement = psiElement.parent
    val modelAttribute = parent as XmlAttribute
    val componentTag = modelAttribute.parent
    val componentDescriptor = componentTag.descriptor as? WebSymbolElementDescriptor ?: return

    val model = componentDescriptor.getModel()
    val defaultModel = VueModelDirectiveProperties.getDefault(psiElement)
    var event = model.event ?: defaultModel.event
    val prop = model.prop ?: defaultModel.prop
    val info = VueAttributeNameParser.parse(parent.name, parent.parent)

    val modifiers = info.modifiers
    var eventValue = "\$event"
    if (modifiers.contains("trim")) {
      eventValue = "typeof $eventValue === 'string' ? $eventValue.trim() : $eventValue"
    }
    if (modifiers.contains("number")) {
      eventValue = "isNaN(parseFloat($eventValue)) ? $eventValue : parseFloat($eventValue)"
    }
    if (modifiers.contains("lazy")) {
      event = "change"
    }
    modelAttribute.name = ":$prop"
    componentTag.setAttribute("@$event", "${modelAttribute.value} = $eventValue")
  }
}
