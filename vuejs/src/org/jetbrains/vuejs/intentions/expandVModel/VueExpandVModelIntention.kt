package org.jetbrains.vuejs.intentions.expandVModel

import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind.DIRECTIVE
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor
import org.jetbrains.vuejs.index.isVueContext

class VueExpandVModelIntention : JavaScriptIntention() {
    override fun getFamilyName(): String = "Expand v-model"
    override fun getText(): String = this.familyName
    private val validModifiers = setOf("lazy", "number", "trim")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return element.node.elementType == XmlElementType.XML_NAME &&
                element.parent?.node?.elementType == XmlElementType.XML_ATTRIBUTE &&
                element.parent.isValid &&
                (element.parent as XmlAttribute).parent.descriptor is VueElementDescriptor &&
                isValidVModel(element.parent as XmlAttribute) &&
                isVueContext(element)
    }

    private fun isValidVModel(attribute: XmlAttribute): Boolean {
        val info = VueAttributeNameParser.parse((attribute.name), attribute.parent)
        return info.kind === DIRECTIVE &&
                (info as? VueDirectiveInfo)?.directiveKind == VueDirectiveKind.MODEL &&
                validModifiers.containsAll(info.modifiers)
    }

    override fun invoke(project: Project, editor: Editor?, psiElement: PsiElement) {
        editor ?: return
        val parent: PsiElement = psiElement.parent
        val modelAttribute = parent as XmlAttribute
        val componentTag = modelAttribute.parent
        val componentDescriptor = componentTag.descriptor as? VueElementDescriptor ?: return

        val model = componentDescriptor.getModel()
        var event = model?.event ?: "input"
        val prop = model?.prop ?: "value"
        val info = VueAttributeNameParser.parse(parent.name, parent.parent)

        val modifiers = info.modifiers
        var eventValue = "\$event"
        if (modifiers.contains("trim")) {
            eventValue = "typeof $eventValue === 'string' ? $eventValue.trim() : $eventValue"
        }
        if (modifiers.contains("number")) {
            eventValue = "_n($eventValue)"
        }
        if (modifiers.contains("lazy")) {
            event = "change"
        }
        CommandProcessor.getInstance().executeCommand(project, {
            WriteAction.run<RuntimeException> {
                modelAttribute.name = ":$prop"
                componentTag.setAttribute("@$event", "${modelAttribute.value} = $eventValue")
            }
        }, "Expand V-Model", "VueExpandVModel")
    }
}
