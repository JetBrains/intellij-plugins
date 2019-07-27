package org.jetbrains.vuejs.intentions.expandVModel

import com.intellij.lang.javascript.intentions.JavaScriptIntention
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlElementType
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor

class VueExpandVModelIntention : JavaScriptIntention() {
    override fun getFamilyName(): String = "Expand v-model into :value and @input"
    override fun getText(): String = this.familyName
    private val validModifiers = setOf("lazy", "number", "trim")

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return element.node.elementType == XmlElementType.XML_NAME &&
                element.parent?.node?.elementType == XmlElementType.XML_ATTRIBUTE &&
                isValid(element)
    }

    private fun isValid(element: PsiElement): Boolean {
        val text = element.text
        if (!text.startsWith("v-model")) {
            return false
        }
        val modifiers = getModifiers(text)
        return validModifiers.containsAll(modifiers)
    }

    private fun getModifiers(text: String): List<String> {
        val split = text.split('.')
        return split.subList(1, split.size)
    }

    override fun invoke(project: Project, editor: Editor?, psiElement: PsiElement) {
        editor ?: return
        val parent: PsiElement = psiElement.parent
        val parentAttr = parent as XmlAttribute
        val parentTag = parentAttr.parent
        val directiveText = parent.name
        val isComponent = parentTag.descriptor is VueElementDescriptor
        var assignedValue = if (isComponent) "\$event" else "\$event.target.value"
        var eventName = "@input"
        getModifiers(directiveText).forEach {
            when {
                it === "trim" -> assignedValue = "$assignedValue.trim()"
                it == "number" -> assignedValue =
                        "isNaN(parseFloat($assignedValue)) ? $assignedValue : parseFloat($assignedValue)"
                it == "lazy" -> eventName = "@change"
            }
        }
        val modelVariableName = parentAttr.value
        CommandProcessor.getInstance().executeCommand(project, {
            WriteAction.run<RuntimeException> {
                parentAttr.name = ":value"
                parentTag.setAttribute(eventName, "$modelVariableName = $assignedValue")
            }
        }, "Expand V-Model", "VueExpandVModel")
    }
}
