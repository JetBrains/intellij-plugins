// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.JSClassExpression
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.settings.JSApplicationSettings
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.ContainerUtil
import com.intellij.xml.*
import com.intellij.xml.XmlElementDescriptor.CONTENT_TYPE_ANY
import icons.VuejsIcons
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.VueLanguage
import org.jetbrains.vuejs.codeInsight.VueComponentDetailsProvider.Companion.getBoundName
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.getExportedDescriptor
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.isNotInLibrary
import org.jetbrains.vuejs.index.*

class VueTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
    if (tag != null && tag.containingFile.language == VueLanguage.INSTANCE && hasVue(tag.project)) {
      val name = tag.name

      val localComponents = findLocalComponents(name, tag)
      if (localComponents.isNotEmpty()) return multiDefinitionDescriptor(localComponents)

      val normalized = fromAsset(name)
      val globalComponents = findGlobalComponents(normalized, tag)
      if (!globalComponents.isEmpty()) return multiDefinitionDescriptor(globalComponents)

      // keep this last in case in future we would be able to normally resolve into these components
      if (VUE_FRAMEWORK_COMPONENTS.contains(normalized) || VUETIFY_UNRESOLVED_COMPONENTS.contains(normalized)) {
        return VueElementDescriptor(JSImplicitElementImpl(normalized, tag))
      }
    }
    return null
  }

  private fun findLocalComponents(name: String, contextElement: PsiElement): List<JSImplicitElement> {
    val localComponents = mutableListOf<JSImplicitElement>()
    val decapitalized = name.decapitalize()
    processLocalComponents(contextElement) { foundName, element ->
      if (foundName == decapitalized || foundName == toAsset(decapitalized) || foundName == toAsset(decapitalized).capitalize()) {
        localComponents.add(element)
      }
      return@processLocalComponents true
    }
    return localComponents
  }

  private fun findGlobalComponents(normalized: String, tag: XmlTag): Collection<JSImplicitElement> {
    val resolvedVariants = resolve(normalized, GlobalSearchScope.allScope(tag.project), VueComponentsIndex.KEY)
    if (resolvedVariants != null) {
      return if (VUE_FRAMEWORK_COMPONENTS.contains(normalized)) resolvedVariants
      else {
        // if global component was defined with literal name, that's the "source of true"
        val globalExact = resolvedVariants.filter { isGlobalExact(it) }
        if (globalExact.isNotEmpty()) globalExact
        else {
          // prefer library definitions of components for resolve
          // i.e. prefer the place where the name is defined, not the place where it is registered with Vue.component
          val libDef = resolvedVariants.filter { VueComponentsCache.isGlobalLibraryComponent(it) }
          if (libDef.isEmpty()) resolvedVariants.filter { isGlobal(it) }
          else libDef
        }
      }
    }
    val globalAliased = VueComponentsCache.findGlobalLibraryComponent(tag.project, normalized) ?: return emptyList()

    return setOf(
      globalAliased.second as? JSImplicitElement ?: JSLocalImplicitElementImpl(globalAliased.first, null, globalAliased.second, null))
  }

  private fun nameVariantsWithPossiblyGlobalMark(name: String): MutableSet<String> {
    val variants = mutableSetOf(name, toAsset(name), toAsset(name).capitalize())
    variants.addAll(variants.map { it + GLOBAL_BINDING_MARK })
    return variants
  }

  private fun processLocalComponents(contextElement: PsiElement, processor: (String?, JSImplicitElement) -> Boolean) {
    val content = findModule(contextElement)
    val defaultExport = if (content == null) null else ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment
    val component = if (defaultExport == null) null else getExportedDescriptor(defaultExport)?.obj

    if (component != null) {
      // recursive usage case
      val nameProperty = component.findProperty("name")
      val nameValue = nameProperty?.value as? JSLiteralExpression
      if (nameValue != null && nameValue.isQuotedLiteral) {
        val name = nameValue.stringValue
        if (name != null) {
          if (!processor.invoke(name, JSImplicitElementImpl(name, nameProperty))) return
        }
      }
    }

    VueComponentDetailsProvider.INSTANCE.processLocalComponents(component, contextElement.project) { name, element ->
      if (name != null) {
        val literalOrElement = VueComponents.meaningfulExpression(element) ?: element
        processComponentMeaningfulElement(name, literalOrElement, processor, element)
      }
      else true
    }
  }

  private fun processComponentMeaningfulElement(localName: String, meaningfulElement: PsiElement,
                                                processor: (String?, JSImplicitElement) -> Boolean,
                                                sourceElement: PsiElement?): Boolean {
    var obj = VueComponentsCalculation.getObjectLiteralFromResolve(listOf(meaningfulElement))
    var clazz: JSClassExpression<*>? = null
    if (obj == null) {
      val compDefaultExport = meaningfulElement.parent as? ES6ExportDefaultAssignment
      if (compDefaultExport != null) {
        val descriptor = getExportedDescriptor(compDefaultExport)
        obj = descriptor?.obj
        clazz = descriptor?.clazz
      }
    }

    return processComponentDescriptorVariants(obj, clazz, processor, localName, sourceElement)
  }

  private fun processComponentDescriptorVariants(obj: JSObjectLiteralExpression?,
                                                 clazz: JSClassExpression<*>?,
                                                 processor: (String?, JSImplicitElement) -> Boolean,
                                                 localName: String,
                                                 sourceElement: PsiElement?): Boolean {
    if (obj != null) {
      val elements = findProperty(obj, "name")?.indexingData?.implicitElements?.filter { it.userString == VueComponentsIndex.JS_KEY }
      if (elements != null && elements.isNotEmpty()) {
        if (elements.any { !processor.invoke(localName, it) }) return false
        return true
      }
      val first = obj.firstProperty
      if (first != null) {
        if (!processor.invoke(localName, JSImplicitElementImpl(localName, first))) return false
        return true
      }
    }
    else if (clazz != null) {
      if (!processor.invoke(localName, JSImplicitElementImpl(localName, clazz))) return false
      return true
    }
    if (!processor.invoke(localName, JSImplicitElementImpl(localName, sourceElement))) return false
    return true
  }

  override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, namespacePrefix: String?) {
    elements ?: return
    if (!StringUtil.isEmpty(namespacePrefix)) return

    val scriptLanguage = detectVueScriptLanguage(tag.containingFile)
    val files: MutableList<PsiFile> = mutableListOf()
    val localLookups = mutableListOf<LookupElement>()
    processLocalComponents(tag) { foundName, element ->
      addLookupVariants(localLookups, tag, scriptLanguage, element, foundName!!, true)
      files.add(element.containingFile)
      return@processLocalComponents true
    }
    elements.addAll(localLookups.map { PrioritizedLookupElement.withPriority((it as LookupElementBuilder).bold(), 100.0) })

    if (hasVue(tag.project)) {
      val namePrefix = tag.name.substringBefore(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, tag.name)
      val variants = nameVariantsWithPossiblyGlobalMark(namePrefix)
      val allComponents = VueComponentsCache.getAllComponentsGroupedByModules(tag.project,
                                                                              { key -> variants.any { key.contains(it, true) } }, false)
      for (entry in allComponents) {
        entry.value
          .filter { !files.contains(it.value.first.containingFile) }
          .forEach {
            val value = it.value
            addLookupVariants(elements, tag, scriptLanguage, value.first, it.key, value.second, entry.key)
          }
      }
      elements.addAll(VUE_FRAMEWORK_COMPONENTS.map {
        LookupElementBuilder.create(it).withIcon(VuejsIcons.Vue).withTypeText("vue", true)
      })
      if (!hasVuetify(allComponents)) return
      elements.addAll(VUETIFY_UNRESOLVED_COMPONENTS_WITH_PASCAL_CASE.map {
        LookupElementBuilder.create(it).withIcon(VuejsIcons.Vue).withTypeText(VUETIFY, true)
      })
    }
  }

  private fun hasVuetify(allComponents: Map<String, Map<String, Pair<PsiElement, Boolean>>>): Boolean {
    return allComponents.filter { it.key == VUETIFY }.isNotEmpty()
  }

  private fun addLookupVariants(elements: MutableList<LookupElement>,
                                contextTag: XmlTag,
                                scriptLanguage: String?,
                                element: PsiElement,
                                name: String,
                                shouldNotBeImported: Boolean,
                                comment: String = "") {
    if (VueFileType.INSTANCE == contextTag.containingFile.fileType) {
      // Pascal case is allowed (and recommended for 90%)
      elements.add(createVueLookup(element, toAsset(name).capitalize(), shouldNotBeImported, comment, scriptLanguage))
    }
    elements.add(createVueLookup(element, fromAsset(name), shouldNotBeImported, comment, scriptLanguage))
  }

  private fun createVueLookup(element: PsiElement,
                              name: String,
                              shouldNotBeImported: Boolean,
                              comment: String = "",
                              scriptLanguage: String?): LookupElement {
    val builder = LookupElementBuilder.create(element, name).withIcon(VuejsIcons.Vue).withTypeText(comment, true)
    if (shouldNotBeImported) {
      return builder
    }
    val settings = JSApplicationSettings.getInstance()
    if (scriptLanguage != null && "ts" == scriptLanguage ||
        DialectDetector.isTypeScript(element) && !JSLibraryUtil.isProbableLibraryFile(element.containingFile.viewProvider.virtualFile)) {
      if (settings.hasTSImportCompletionEffective(element.project)) {
        return builder.withInsertHandler(VueInsertHandler.INSTANCE)
      }
    }
    else {
      if (settings.isUseJavaScriptAutoImport) {
        return builder.withInsertHandler(VueInsertHandler.INSTANCE)
      }
    }
    return builder
  }

  companion object {
    private val VUE_FRAMEWORK_COMPONENTS = setOf(
      "component",
      "keep-alive",
      "slot",
      "transition",
      "transition-group"
    )
    private val VUETIFY_UNRESOLVED_COMPONENTS = setOf(
      //grid components
      "v-flex",
      "v-spacer",
      "v-container",
      "v-layout",
      //functional components
      "v-autocomplete",
      "v-bottom-sheet-transition",
      "v-breadcrumbs-divider",
      "v-carousel-reverse-transition",
      "v-carousel-transition",
      "v-dialog-bottom-transition",
      "v-dialog-transition",
      "v-expand-transition",
      "v-fab-transition",
      "v-fade-transition",
      "v-menu",
      "v-menu-transition",
      "v-row-expand-transition",
      "v-select",
      "v-scale-transition",
      "v-scroll-x-reverse-transition",
      "v-scroll-x-transition",
      "v-scroll-y-reverse-transition",
      "v-scroll-y-transition",
      "v-slide-x-reverse-transition",
      "v-slide-x-transition",
      "v-slide-y-reverse-transition",
      "v-slide-y-transition",
      "v-stepper-items",
      "v-tab-item",
      "v-tab-reverse-transition",
      "v-tab-transition",
      "v-table-overflow",
      "v-tabs",
      "v-tabs-items",
      "v-text-field",
      "v-card-actions",
      "v-card-text",
      "v-list-tile-action",
      "v-list-tile-action-text",
      "v-list-tile-content",
      "v-list-tile-sub-title",
      "v-list-tile-title",
      "v-stepper-header",
      "v-toolbar-items",
      "v-toolbar-title"
    )
    val VUETIFY_UNRESOLVED_COMPONENTS_WITH_PASCAL_CASE: MutableIterable<String> = ContainerUtil.concat(VUETIFY_UNRESOLVED_COMPONENTS,
                                                                                                       VUETIFY_UNRESOLVED_COMPONENTS.map {
                                                                                                         toAsset(it).capitalize()
                                                                                                       })
  }
}

fun multiDefinitionDescriptor(variants: Collection<JSImplicitElement>): VueElementDescriptor {
  assert(!variants.isEmpty())
  val sorted = variants.sortedBy { isNotInLibrary(it) }
  return VueElementDescriptor(sorted[0], sorted)
}

class VueElementDescriptor(val element: JSImplicitElement, val variants: List<JSImplicitElement> = listOf(element)) : XmlElementDescriptor {
  companion object {
    // it is better to use default attributes method since it is guaranteed to do not call any extension providers
    fun getDefaultHtmlAttributes(context: XmlTag?): Array<out XmlAttributeDescriptor> =
      ((HtmlNSDescriptorImpl.guessTagForCommonAttributes(context) as? HtmlElementDescriptorImpl)
         ?.getDefaultAttributeDescriptors(context) ?: emptyArray())
  }

  override fun getDeclaration(): JSImplicitElement = element
  override fun getName(context: PsiElement?): String = (context as? XmlTag)?.name ?: name
  override fun getName(): String = fromAsset(declaration.name)
  override fun init(element: PsiElement?) {}
  override fun getQualifiedName(): String = name
  override fun getDefaultName(): String = name

  override fun getElementsDescriptors(context: XmlTag): Array<XmlElementDescriptor> {
    return XmlDescriptorUtil.getElementsDescriptors(context)
  }

  override fun getElementDescriptor(childTag: XmlTag, contextTag: XmlTag): XmlElementDescriptor? {
    return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag)
  }

  override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    val result = mutableListOf<XmlAttributeDescriptor>()
    val defaultHtmlAttributes = getDefaultHtmlAttributes(context)
    result.addAll(defaultHtmlAttributes)
    result.addAll(VueAttributesProvider.getDefaultVueAttributes())

    val obj = VueComponents.findComponentDescriptor(declaration)
    result.addAll(VueComponentDetailsProvider.INSTANCE.getAttributes(obj, element.project, true, xmlContext = true))
    return result.toTypedArray()
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    if (attributeName == null) return null
    if (VueAttributesProvider.DEFAULT.contains(attributeName)) return VueAttributeDescriptor(attributeName)
    val extractedName = getBoundName(attributeName)

    val obj = VueComponents.findComponentDescriptor(declaration)
    if (obj != null) {
      val descriptor = VueComponentDetailsProvider.INSTANCE.resolveAttribute(obj, extractedName ?: attributeName, true)
      if (descriptor != null) {
        return descriptor.createNameVariant(extractedName ?: attributeName)
      }
    }

    if (extractedName != null) {
      return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(extractedName, context) ?: VueAttributeDescriptor(attributeName)
    }
    return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(attributeName, context)
           // relax attributes check: https://vuejs.org/v2/guide/components.html#Non-Prop-Attributes
           // vue allows any non-declared as props attributes to be passed to a component
           ?: VueAttributeDescriptor(attributeName, isNonProp = true)
  }

  override fun getAttributeDescriptor(attribute: XmlAttribute?): XmlAttributeDescriptor? = getAttributeDescriptor(attribute?.name,
                                                                                                                  attribute?.parent)

  override fun getNSDescriptor(): XmlNSDescriptor? = null
  override fun getTopGroup(): XmlElementsGroup? = null
  override fun getContentType(): Int = CONTENT_TYPE_ANY
  override fun getDefaultValue(): String? = null
}
