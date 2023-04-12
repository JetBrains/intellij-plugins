// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.editor

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.psi.impl.ES6CreateImportUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.lang.javascript.refactoring.FormatFixer
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.model.Pointer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.util.XmlTagUtil
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.context.*
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.index.findScriptTag
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.libraries.VUE_CLASS_COMPONENT
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.source.COMPONENTS_PROP
import org.jetbrains.vuejs.model.source.NAME_PROP
import org.jetbrains.vuejs.model.source.VueComponents
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.model.tryResolveSrcReference

class VueComponentSourceEdit private constructor(private val component: Pointer<VueSourceComponent>) {

  companion object {

    fun create(component: VueEntitiesContainer?): VueComponentSourceEdit? =
      if (component is VueSourceComponent)
        VueComponentSourceEdit(component.createPointer())
      else null

    fun getOrCreateScriptScope(component: VueEntitiesContainer?): JSExecutionScope? {
      return create(component)?.let {
        val script = it.getOrCreateScriptScope()
        it.reformatChanges()
        script
      }
    }

  }

  fun getOrCreateScriptScope(): JSExecutionScope? {
    val file = file
    if (file is JSFile) return file
    if (file !is XmlFile) return null
    val scriptTag = findScriptTag(file, true) ?: findScriptTag(file, false) ?: createScriptTag(file)

    if (!scriptTag.isScriptSetupTag()) {
      scriptTag.tryResolveSrcReference().asSafely<JSFile>()?.let { return it }
    }

    scriptTag
      .children
      .firstNotNullOfOrNull { it as? JSEmbeddedContent }
      ?.let { return it }

    // Script content is empty - let's add a new line
    // Keep attributes intact
    val tagText = XmlTagUtil.getStartTagRange(scriptTag)
                    ?.substring(file.text)
                    ?.trimEnd { it == '>' || it == '/' }
                  ?: "<script"
    val newScriptTag = PsiFileFactory.getInstance(file.project)
      .createFileFromText("dummy.vue", VueFileType.INSTANCE, "$tagText>\n</script>")
      .let { PsiTreeUtil.findChildOfType(it, XmlTag::class.java)!! }

    return scriptTag.replace(newScriptTag)
      .children
      .firstNotNullOfOrNull { it as? JSEmbeddedContent }
  }

  fun isScriptSetup(): Boolean =
    file.asSafely<XmlFile>()
      ?.let { findScriptTag(it, true) } != null

  fun addClassicPropertyReference(kind: String, referenceName: String): Boolean =
    getOrCreateObjectLiteralBasedComponentEdit()?.addClassicPropertyReference(kind, referenceName) == true

  fun addClassicPropertyFunction(kind: String, name: String, contents: String): Boolean =
    getOrCreateObjectLiteralBasedComponentEdit()?.addClassicPropertyFunction(kind, name, contents) == true

  fun insertComponentImport(name: String,
                            elementToImport: PsiElement) {
    val capitalizedName = toAsset(name, true)

    val info = if (elementToImport is PsiFile) {
      ES6ImportPsiUtil.CreateImportExportInfo(capitalizedName, capitalizedName, ES6ImportPsiUtil.ImportExportType.DEFAULT,
                                              ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT)
    }
    else {
      ES6ImportPsiUtil.CreateImportExportInfo(null, capitalizedName, ES6ImportPsiUtil.ImportExportType.SPECIFIER,
                                              ES6ImportExportDeclaration.ImportExportPrefixKind.IMPORT)
    }

    val scriptScope = getOrCreateScriptScope() ?: return

    if (isScriptSetup() || addClassicPropertyReference(COMPONENTS_PROP, capitalizedName)) {
      ES6ImportPsiUtil.insertJSImport(scriptScope, info, elementToImport)
    }
  }

  fun reformatChanges() {
    JSRefactoringUtil.format(formatFixers)
  }

  private val file: PsiFile? get() = component.dereference()?.source?.containingFile
  private val formatFixers = mutableListOf<FormatFixer>()

  private fun createScriptTag(file: XmlFile): XmlTag {
    val dummyScript = createEmptyScript(file)
    return file.document!!.let { it.addAfter(dummyScript, it.lastChild) as XmlTag }
  }

  private fun createEmptyScript(context: XmlFile): XmlTag {
    val project = context.project

    val hasScriptSetup = supportsScriptSetup(file)

    val hasTypeScript = TypeScriptService.getForFile(context.project, context.virtualFile) != null
    val langText = if (hasTypeScript) " lang=\"ts\"" else ""
    val setupText = if (hasScriptSetup) " setup" else ""
    val text = "<script$setupText$langText>\n</script>"
    val dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.vue", VueFileType.INSTANCE, text)
    return PsiTreeUtil.findChildOfType(dummyFile, XmlTag::class.java)!!
      .also { reformat(it) }
  }

  private fun getOrCreateObjectLiteralBasedComponentEdit(): ObjectLiteralBasedComponentEdit? {
    val scriptScope = getOrCreateScriptScope()
    val scriptTag = scriptScope?.context.asSafely<XmlTag>()
    if (scriptScope == null || scriptTag.isScriptSetupTag())
      return null
    val defaultExport = ES6PsiUtil.findDefaultExport(scriptScope) as? JSExportAssignment
                        ?: createDefaultComponentExport(scriptScope)

    val descriptor = VueComponents.getSourceComponentDescriptor(defaultExport)
                     ?: return null
    val initializer = if (descriptor.initializer == null && descriptor.clazz != null)
      addInitializerToComponentClass(descriptor.clazz)
    else
      descriptor.initializer.asSafely<JSObjectLiteralExpression>()

    return initializer?.let { ObjectLiteralBasedComponentEdit(it) }
  }

  private fun createDefaultComponentExport(scriptScope: JSExecutionScope): JSExportAssignment {
    val fileName = FileUtil.getNameWithoutExtension(scriptScope.containingFile.name)

    val isTs = DialectDetector.isTypeScript(scriptScope)

    val useDefineComponent = supportsDefineComponent(scriptScope)
    val classComponentLibrary = getVueClassComponentLibrary(scriptScope)
    val componentDecoratorName = getVueClassComponentDecoratorName(scriptScope)
    val useVueExtend = isTs && !useDefineComponent

    val componentName = toAsset(StringUtil.capitalize(fileName).replace(Regex("[^0-9a-zA-Z-]+"), "-"))
    val exportText = when {
      classComponentLibrary != null -> "@$componentDecoratorName({})\nexport default class $componentName extends Vue {\n}"
      useDefineComponent -> "export default defineComponent({\n})"
      useVueExtend -> "export default Vue.extend({\n})"
      else -> "export default {\n}"
    }
    val defaultExport = JSPsiElementFactory.createJSSourceElement(exportText, scriptScope) as JSExportAssignment
    val anchorPair = ES6ImportPsiUtil.findAnchorToInsert(scriptScope, ES6ImportPsiUtil.IMPORT_ELEMENT_TYPES, false)
    val addedExport: JSExportAssignment
    if (anchorPair.first) {
      addedExport = JSChangeUtil.doAddAfter(scriptScope, defaultExport, anchorPair.second) as JSExportAssignment
      JSChangeUtil.addWsAfter(scriptScope, addedExport, "\n")
    }
    else {
      addedExport = JSChangeUtil.doAddBefore(scriptScope, defaultExport, anchorPair.second) as JSExportAssignment
    }
    when {
      classComponentLibrary != null -> {
        if (classComponentLibrary == VUE_CLASS_COMPONENT) {
          insertImportIfNotThere("Vue", true, VUE_MODULE, scriptScope)
          insertImportIfNotThere(componentDecoratorName, true, classComponentLibrary, scriptScope)
        } else {
          insertImportIfNotThere("Vue", false, classComponentLibrary, scriptScope)
          insertImportIfNotThere(componentDecoratorName, false, classComponentLibrary, scriptScope)
        }
      }
      useDefineComponent -> {
        insertImportIfNotThere("defineComponent", false, VUE_MODULE, scriptScope)
      }
      useVueExtend -> {
        insertImportIfNotThere("Vue", true, VUE_MODULE, scriptScope)
      }
    }
    val prevSibling = addedExport.prevSibling
    if (prevSibling != null
        && (prevSibling !is PsiWhiteSpace || (prevSibling.prevSibling != null && prevSibling.text.count { it == '\n' } < 2))) {
      JSChangeUtil.addWsAfter(scriptScope, prevSibling, "\n\n")
    }
    return addedExport.also { reformat(it) }
  }

  private fun addInitializerToComponentClass(clazz: JSClass): JSObjectLiteralExpression? {
    val decorator = VueComponents.getComponentDecorator(clazz) ?: return null
    val newClass = JSPsiElementFactory.createJSClass("@Component({}) class A {}", decorator)
    val newDecorator = VueComponents.getComponentDecorator(newClass)!!
    reformat(decorator.replace(newDecorator))
    return VueComponents.getSourceComponentDescriptor(clazz)?.initializer as? JSObjectLiteralExpression
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

  private fun reformat(element: PsiElement) {
    formatFixers.add(FormatFixer.create(element, FormatFixer.Mode.Reformat))
  }

  private inner class ObjectLiteralBasedComponentEdit(private val literal: JSObjectLiteralExpression) {

    fun addClassicPropertyFunction(kind: String, name: String, contents: String): Boolean {
      val items = getOrCreateObjectLiteralProperty(literal, kind).value as? JSObjectLiteralExpression ?: return false
      if (items.findProperty(name) != null
          || (kind == COMPONENTS_PROP && items.findProperty(StringUtil.decapitalize(name)) != null)) return false
      val newProperty = JSPsiElementFactory.createJSExpression("{ $name() {\n$contents} }", literal,
                                                               JSObjectLiteralExpression::class.java).firstProperty!!
      addProperty(newProperty, items, false)
      return true
    }

    fun addClassicPropertyReference(kind: String, referenceName: String): Boolean {
      val items = getOrCreateObjectLiteralProperty(literal, kind).value as? JSObjectLiteralExpression ?: return false

      if (items.findProperty(referenceName) != null
          || (kind == COMPONENTS_PROP && items.findProperty(StringUtil.decapitalize(referenceName)) != null)) return false

      val newProperty = JSPsiElementFactory.createJSExpression("{ $referenceName }", literal,
                                                               JSObjectLiteralExpression::class.java).firstProperty!!
      addProperty(newProperty, items, false)
      return true
    }

    private fun getOrCreateObjectLiteralProperty(obj: JSObjectLiteralExpression, propertyName: String): JSProperty {
      val property = obj.findProperty(propertyName)
      if (property != null) return property
      val newProperty = JSPsiElementFactory.createJSExpression("{ $propertyName: {} }", obj,
                                                               JSObjectLiteralExpression::class.java).firstProperty!!
      return addProperty(newProperty, obj, true)
    }

    private fun addProperty(newProperty: JSProperty,
                            obj: JSObjectLiteralExpression,
                            onTheNewLine: Boolean): JSProperty {
      val firstProperty = obj.firstProperty
      val anchor: PsiElement? =
        if (NAME_PROP == firstProperty?.name)
          PsiTreeUtil.findSiblingForward(firstProperty, JSTokenTypes.COMMA, null) ?: firstProperty
        else
          obj.node.findChildByType(JSTokenTypes.LBRACE)?.psi
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
        if (lastAdded.nextSibling.let { it != null && !(it is PsiWhiteSpace && it.text.contains('\n')) }) {
          JSChangeUtil.addWs(obj.node, lastAdded.nextSibling.node, "\n")
        }
      }
      return addedProperty.also { reformat(addedProperty) }
    }

  }


}