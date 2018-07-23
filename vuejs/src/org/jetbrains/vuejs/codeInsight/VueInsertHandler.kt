// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.editorActions.XmlTagNameSynchronizer
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.IMPORT_ELEMENT_TYPES
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil.ImportExportType
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.getElementComponentDecorator
import org.jetbrains.vuejs.index.hasVueClassComponentLibrary

class VueInsertHandler : XmlTagInsertHandler() {
  companion object {
    val INSTANCE: VueInsertHandler = VueInsertHandler()

    fun reformatElement(element: PsiElement?) {
      if (element != null && element.isValid) {
        val range = element.textRange
        FormatFixer.doReformat(element as? PsiFile ?: element.containingFile, range.startOffset, range.endOffset)
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
    val jsImplicitElement = item.`object` as JSImplicitElement
    val importedFile = jsImplicitElement.containingFile
    if (importedFile == context.file) return
    val nodeModule = NodeModuleSearchUtil.findDependencyRoot((item.`object` as PsiElement).containingFile.virtualFile)
    if (isSkippedModule(nodeModule)) return

    context.commitDocument()
    val isClass = jsImplicitElement.parent is JSClassExpression<*> || jsImplicitElement.parent is ES6Decorator
    XmlTagNameSynchronizer.runWithoutCancellingSyncTagsEditing(context.document) {
      InsertHandlerWorker().insertComponentImport(context.file, item.lookupString, importedFile, context.editor, isClass)
    }
  }

  class InsertHandlerWorker {
    private var toReformat: PsiElement? = null

    fun insertComponentImport(context: PsiFile,
                              name: String,
                              importedFile: PsiFile,
                              editor: Editor?,
                              isClass: Boolean = false) {
      val file: XmlFile = context as? XmlFile ?: context.containingFile as? XmlFile ?: return
      val defaultExport = findOrCreateDefaultExport(file, isClass)
      var obj = defaultExport.stubSafeElement as? JSObjectLiteralExpression ?: VueComponents.getExportedDescriptor(defaultExport)?.obj

      if (obj == null) {
        val decorator = VueComponents.getElementComponentDecorator(defaultExport) ?: return
        val newClass = JSChangeUtil.createStatementFromTextWithContext("@Component({}) class A {}", decorator)!!.getPsi(JSClass::class.java)
        val newDecorator = getElementComponentDecorator(newClass)!!
        val replacedDecorator = decorator.replace(newDecorator)
        forReformat(replacedDecorator)
        obj = VueComponents.getExportedDescriptor(defaultExport)?.obj ?: return
      }

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

    private fun findOrCreateDefaultExport(file: XmlFile, isClass: Boolean): JSExportAssignment {
      val scriptTag = findScriptTag(file)
      val fileName = FileUtil.getNameWithoutExtension(file.name)
      if (scriptTag == null) {
        val dummyScript = createDummyScript(file.project, null, isClass, fileName)
        val addedScript = file.addAfter(dummyScript, file.lastChild)
        forReformat(addedScript)
        val addedContent = PsiTreeUtil.findChildOfType(addedScript, JSEmbeddedContent::class.java)!!
        if (isClass) {
          addClassComponentImports(addedContent)
        }
        return ES6PsiUtil.findDefaultExport(addedContent) as JSExportAssignment
      }
      else {
        var content = PsiTreeUtil.findChildOfType(scriptTag, JSEmbeddedContent::class.java)
        if (content != null) {
          val defaultExport = ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment
          if (defaultExport != null) return defaultExport
        }

        val addedExport: JSExportAssignment
        val lang = scriptTag.getAttribute("lang")?.value
        val dummyScript = createDummyScript(file.project, lang, isClass, fileName)
        if (content != null && content.children.any { it !is PsiWhiteSpace && it !is PsiComment }) {
          val dummyContent = PsiTreeUtil.findChildOfType(dummyScript, JSEmbeddedContent::class.java)!!
          val dummyExport = ES6PsiUtil.findDefaultExport(dummyContent) as JSExportAssignment

          // add after, anchor
          val anchorPair = ES6ImportPsiUtil.findAnchorToInsert(content, IMPORT_ELEMENT_TYPES, false)
          if (anchorPair.first) {
            addedExport = JSChangeUtil.doAddAfter(content, dummyExport, anchorPair.second) as JSExportAssignment
            JSChangeUtil.addWsAfter(content, addedExport, "\n")
          } else {
            addedExport = JSChangeUtil.doAddBefore(content, dummyExport, anchorPair.second) as JSExportAssignment
          }
          forReformat(addedExport)
        } else {
          val replacedScript = scriptTag.replace(dummyScript)
          forReformat(replacedScript)
          content = PsiTreeUtil.findChildOfType(replacedScript, JSEmbeddedContent::class.java)!!
          addedExport = ES6PsiUtil.findDefaultExport(content) as JSExportAssignment
        }
        if (isClass) {
          addClassComponentImports(content)
        }

        return addedExport
      }
    }

    private fun addClassComponentImports(content: JSEmbeddedContent) {
      insertImportIfNotThere("Vue", true, "vue", content)
      insertImportIfNotThere("Component", false, "vue-class-component", content)
    }

    private fun insertImportIfNotThere(exportedName: String, isDefault: Boolean, module: String, content: PsiElement) {
      val existingImports = ES6ImportPsiUtil.getExistingImports(content, module)
      if (!isDefault && existingImports.specifiers.any { exportedName == it.key }) return
      if (isDefault && existingImports.bindings.any { exportedName == it.declaredName }) return
      val exportedExpr = if (isDefault) exportedName else "{$exportedName}"
      val quote = JSCodeStyleSettings.getQuote(content)
      val text = "import $exportedExpr from $quote$module$quote" + JSCodeStyleSettings.getSemicolon(content)
      val dummyImport = JSChangeUtil.createStatementFromTextWithContext(text, content)!!.psi
      ES6CreateImportUtil.findPlaceAndInsertES6Import(content, dummyImport, module, null)
    }

    private fun createDummyScript(project: Project,
                                  lang: String?,
                                  isClass: Boolean,
                                  fileName: String): XmlTag {
      val langText = if (lang != null) " lang=\"$lang\"" else ""
      val exportText = if (isClass && hasVueClassComponentLibrary(project)) {
        "@Component\nexport default class $fileName extends Vue"
      }
      else {
        "export default"
      }
      val text = "<script$langText>$exportText {}\n</script>"
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
      val addedProperty: JSProperty
      var lastAdded: PsiElement
      if (anchor == firstProperty) {
        // only one 'name' property
        val comma = JSChangeUtil.createCommaPsiElement(anchor!!)
        val addedComma = JSChangeUtil.doDoAddAfter(obj, comma, firstProperty)
        addedProperty = JSChangeUtil.doDoAddAfter(obj, newProperty, addedComma) as JSProperty
        lastAdded = addedProperty
      } else {
        val needsComma = anchor != null && (anchor.node.elementType == JSTokenTypes.COMMA || !obj.properties.isEmpty())
        addedProperty = JSChangeUtil.doDoAddAfter(obj, newProperty, anchor) as JSProperty
        lastAdded = addedProperty
        if (needsComma) {
          val comma = JSChangeUtil.createCommaPsiElement(anchor!!)
          lastAdded = JSChangeUtil.doDoAddAfter(obj, comma, addedProperty)
        }
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