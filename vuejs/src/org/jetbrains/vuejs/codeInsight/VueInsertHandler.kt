package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ImportExportType
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.VueFileType

class VueInsertHandler : XmlTagInsertHandler() {
  companion object {
    val INSTANCE = VueInsertHandler()

    fun reformatElement(element: PsiElement?) {
      if (element != null && element.isValid) {
        WriteAction.run<RuntimeException> {
          val range = element.textRange
          FormatFixer.doReformat(element as? PsiFile ?: element.containingFile, range.startOffset, range.endOffset)
        }
      }
    }

    private fun shouldHandleXmlInsert(context: InsertionContext): Boolean {
      val file = context.file
      if (!file.language.isKindOf(XMLLanguage.INSTANCE)) {
        return false
      }
      val element = PsiTreeUtil.findElementOfClassAtOffset(file, context.startOffset, XmlTag::class.java, false)
      return element == null || element.language.isKindOf(XMLLanguage.INSTANCE)
    }

    private fun isSkippedModule(nodeModule: VirtualFile?) = "vue" == nodeModule?.name || "vue-router" == nodeModule?.name
  }

  override fun handleInsert(context: InsertionContext?, item: LookupElement?) {
    if (context == null || shouldHandleXmlInsert(context)) {
      super.handleInsert(context, item)
    }
    if (context == null || item == null) return
    val importedFile = (item.`object` as JSImplicitElement).containingFile
    if (importedFile == context.file) return
    val nodeModule = NodeModuleSearchUtil.findDependencyRoot((item.`object` as PsiElement).containingFile.virtualFile)
    if (isSkippedModule(nodeModule)) return

    context.commitDocument()
    InsertHandlerWorker().insertComponentImport(context.file, item.lookupString, importedFile, context.editor)
  }

  class InsertHandlerWorker {
    private var toReformat: PsiElement? = null

    fun insertComponentImport(context: PsiElement,
                              name: String,
                              importedFile: PsiFile,
                              editor: Editor?) {
      val file: XmlFile = context as? XmlFile ?: context.containingFile as? XmlFile ?: return
      val defaultExport = findOrCreateDefaultExport(file)
      val obj = defaultExport.stubSafeElement as? JSObjectLiteralExpression ?: return
      val components = componentProperty(obj).value as? JSObjectLiteralExpression ?: return
      val decapitalized = toAsset(name).decapitalize()
      val capitalizedName = decapitalized.capitalize()
      if (components.findProperty(decapitalized) != null || components.findProperty(capitalizedName) != null) return
      val newProperty = (JSChangeUtil.createExpressionWithContext("{ $capitalizedName }",
                                                                  obj)!!.psi as JSObjectLiteralExpression).firstProperty!!
      forReformat(addProperty(newProperty, components, false))
      ES6ImportPsiUtil.insertJSImport(defaultExport.parent, capitalizedName, ImportExportType.DEFAULT, importedFile, editor)
      if (toReformat != null) {
        reformatElement(toReformat)
      }
    }

    private fun findOrCreateDefaultExport(file: XmlFile): JSExportAssignment {
      val content = findModule(file)
      if (content == null) {
        val dummyScript = createDummyScript(file.project, null)
        val addedScript = file.addAfter(dummyScript, file.lastChild)
        forReformat(addedScript)
        val addedContent = PsiTreeUtil.findChildOfType(addedScript, JSEmbeddedContent::class.java)!!
        return ES6PsiUtil.findDefaultExport(addedContent) as JSExportAssignment
      }
      else {
        val defaultExport = ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment
        if (defaultExport != null) return defaultExport

        val lang = (content.parent as? XmlTag)?.getAttribute("lang")?.value
        val dummyScript = createDummyScript(file.project, lang)
        val dummyContent = PsiTreeUtil.findChildOfType(dummyScript, JSEmbeddedContent::class.java)!!
        val dummyExport = ES6PsiUtil.findDefaultExport(dummyContent) as JSExportAssignment

        val addedExport = JSChangeUtil.doAddBefore(content, dummyExport, content.firstChild) as JSExportAssignment
        forReformat(addedExport)
        return addedExport
      }
    }

    private fun createDummyScript(project: Project, lang: String?): XmlTag {
      val text = if (lang != null) {
        "<script lang=\"$lang\">export default {}\n</script>"
      }
      else {
        "<script>export default {}\n</script>"
      }
      val dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.vue", VueFileType.INSTANCE, text)
      return PsiTreeUtil.findChildOfType(dummyFile, XmlTag::class.java)!!
    }

    private fun componentProperty(obj: JSObjectLiteralExpression): JSProperty {
      val property = obj.findProperty("components")
      if (property != null) return property
      val newProperty = (JSChangeUtil.createExpressionWithContext("{ components: {} }",
                                                                  obj)!!.psi as JSObjectLiteralExpression).firstProperty!!
      val addedProperty: PsiElement = addProperty(newProperty, obj, true)
      forReformat(addedProperty)
      return addedProperty as JSProperty
    }

    private fun addProperty(newProperty: JSProperty,
                            obj: JSObjectLiteralExpression,
                            onTheNewLine: Boolean): JSProperty {
      val firstProperty = obj.firstProperty
      val anchor: PsiElement?
      anchor = if ("name" == firstProperty?.name) {
        PsiTreeUtil.findSiblingForward(firstProperty, JSTokenTypes.COMMA, null) ?: firstProperty
      } else {
        obj.node.findChildByType(JSTokenTypes.LBRACE)?.psi
      }
      val needsComma = anchor != null && (anchor.node.elementType == JSTokenTypes.COMMA || !obj.properties.isEmpty())
      val addedProperty = JSChangeUtil.doDoAddAfter(obj, newProperty, anchor) as JSProperty
      var lastAdded: PsiElement = addedProperty
      if (needsComma) {
        val comma = JSChangeUtil.createCommaPsiElement(anchor!!)
        lastAdded = JSChangeUtil.doDoAddAfter(obj, comma, addedProperty)
      }
      if (onTheNewLine) {
        JSChangeUtil.addWs(obj.node, addedProperty.node, "\n")
        if (addedProperty.nextSibling != null) {
          JSChangeUtil.addWs(obj.node, lastAdded.nextSibling.node, "\n")
        }
      }
      return addedProperty
    }

    private fun forReformat(el: PsiElement) {
      if (toReformat == null) {
        toReformat = el
      }
    }
  }
}