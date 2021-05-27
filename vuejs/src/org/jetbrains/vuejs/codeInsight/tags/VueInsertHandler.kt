// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.tags

import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.completion.XmlTagInsertHandler
import com.intellij.codeInsight.editorActions.XmlTagNameSynchronizer
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.web.webTypes.registry.WebTypesCodeCompletionItem
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
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
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
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.VUE_CLASS_COMPONENT_MODULE
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.hasVueClassComponentLibrary
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.source.COMPONENTS_PROP
import org.jetbrains.vuejs.model.source.NAME_PROP
import org.jetbrains.vuejs.model.source.VueComponents

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

  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    if (shouldHandleXmlInsert(context)) {
      super.handleInsert(context, item)
    }
    val element = WebTypesCodeCompletionItem.getPsiElement(item)
                  ?: return
    val importedFile = element.containingFile
    if (importedFile == context.file) return
    val nodeModule = NodeModuleSearchUtil.findDependencyRoot(element.containingFile.virtualFile)
    if (isSkippedModule(nodeModule)) return

    context.commitDocument()
    val isClass = element.context is JSClassExpression || element.context is ES6Decorator
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
      val defaultExportElement = defaultExport.stubSafeElement
      var obj = VueComponents.getComponentDescriptor(defaultExportElement)?.initializer

      if (obj !is JSObjectLiteralExpression) {
        if (defaultExportElement !is JSClass) return
        val decorator = VueComponents.getComponentDecorator(defaultExportElement) ?: return
        val newClass = JSPsiElementFactory.createJSClass("@Component({}) class A {}", decorator)
        val newDecorator = VueComponents.getComponentDecorator(newClass)!!
        val replacedDecorator = decorator.replace(newDecorator)
        forReformat(replacedDecorator)
        obj = VueComponents.getComponentDescriptor(defaultExportElement)?.initializer
      }

      val components = componentProperty(obj as? JSObjectLiteralExpression ?: return).value as? JSObjectLiteralExpression ?: return
      val decapitalized = toAsset(name).decapitalize()
      val capitalizedName = decapitalized.capitalize()
      if (components.findProperty(decapitalized) != null || components.findProperty(capitalizedName) != null) return
      val newProperty = JSPsiElementFactory.createJSExpression("{ $capitalizedName }", obj,
                                                               JSObjectLiteralExpression::class.java).firstProperty!!
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
        val lang = scriptTag.getAttribute(LANG_ATTRIBUTE_NAME)?.value
        val dummyScript = createDummyScript(file.project, lang, isClass, fileName)
        if (content != null && content.children.any { it !is PsiWhiteSpace && it !is PsiComment }) {
          val dummyContent = PsiTreeUtil.findChildOfType(dummyScript, JSEmbeddedContent::class.java)!!
          val dummyExport = ES6PsiUtil.findDefaultExport(dummyContent) as JSExportAssignment

          // add after, anchor
          val anchorPair = ES6ImportPsiUtil.findAnchorToInsert(content, IMPORT_ELEMENT_TYPES, false)
          if (anchorPair.first) {
            addedExport = JSChangeUtil.doAddAfter(content, dummyExport, anchorPair.second) as JSExportAssignment
            JSChangeUtil.addWsAfter(content, addedExport, "\n")
          }
          else {
            addedExport = JSChangeUtil.doAddBefore(content, dummyExport, anchorPair.second) as JSExportAssignment
          }
          forReformat(addedExport)
        }
        else {
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
      insertImportIfNotThere("Vue", true, VUE_MODULE, content)
      insertImportIfNotThere("Component", false, VUE_CLASS_COMPONENT_MODULE, content)
    }

    private fun insertImportIfNotThere(exportedName: String, isDefault: Boolean, module: String, content: PsiElement) {
      val existingImports = ES6ImportPsiUtil.getExistingImports(content, module)
      if (!isDefault && existingImports.specifiers.any { exportedName == it.key }) return
      if (isDefault && existingImports.bindings.any { exportedName == it.declaredName }) return
      val exportedExpr = if (isDefault) exportedName else "{$exportedName}"
      val quote = JSCodeStyleSettings.getQuote(content)
      val text = "import $exportedExpr from $quote$module$quote" + JSCodeStyleSettings.getSemicolon(content)
      val dummyImport = JSPsiElementFactory.createJSSourceElement(text, content)
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
      val property = obj.findProperty(COMPONENTS_PROP)
      if (property != null) return property
      val newProperty = JSPsiElementFactory.createJSExpression("{ components: {} }", obj,
                                                               JSObjectLiteralExpression::class.java).firstProperty!!
      val addedProperty: PsiElement = addProperty(newProperty, obj, true)
      forReformat(addedProperty)
      return addedProperty as JSProperty
    }

    private fun addProperty(newProperty: JSProperty,
                            obj: JSObjectLiteralExpression,
                            onTheNewLine: Boolean): JSProperty {
      val firstProperty = obj.firstProperty
      val anchor: PsiElement?
      anchor = if (NAME_PROP == firstProperty?.name) {
        PsiTreeUtil.findSiblingForward(firstProperty, JSTokenTypes.COMMA, null) ?: firstProperty
      }
      else {
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
      }
      else {
        val needsComma = anchor != null && (anchor.node.elementType == JSTokenTypes.COMMA || obj.properties.isNotEmpty())
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
