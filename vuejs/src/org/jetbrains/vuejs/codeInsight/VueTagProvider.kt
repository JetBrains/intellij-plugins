package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDocumentImpl
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ArrayUtil
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlElementDescriptor.CONTENT_TYPE_ANY
import com.intellij.xml.XmlTagNameProvider
import com.intellij.xml.util.HtmlUtil
import com.intellij.xml.util.XmlUtil
import icons.VuejsIcons
import org.jetbrains.vuejs.GLOBAL_BINDING_MARK
import org.jetbrains.vuejs.codeInsight.VueComponentDetailsProvider.Companion.getBoundName
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.isGlobal
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.selectComponent
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.hasVue
import org.jetbrains.vuejs.index.resolve

class VueTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
    if (tag != null && hasVue(tag.project)) {
      val name = tag.name
      // do not perform additional work for html tags
      if (XmlUtil.isTagDefinedByNamespace(tag)) {
        return null
      }

      var localComponent:JSImplicitElement? = null
      processLocalComponents(tag, { foundName, element ->
        if (foundName == name || foundName == toAsset(name) || foundName == toAsset(name).capitalize()) {
          localComponent = element
        }
        return@processLocalComponents localComponent == null
      })

      if (localComponent != null) return VueElementDescriptor(localComponent!!)

      val fromAsset = fromAsset(name)
      val variants = nameVariantsWithPossiblyGlobalMark(name)
      @Suppress("LoopToCallChain") // by performance reasons
      for (variant in variants) {
        val component = selectComponent(resolve(variant, GlobalSearchScope.allScope(tag.project), VueComponentsIndex.KEY), false)
        if (component != null &&
            (isGlobal(component) ||
             VueComponents.isGlobalLibraryComponent(variant, component) ||
             VUE_FRAMEWORK_COMPONENTS.contains(fromAsset))) {
          return VueElementDescriptor(component)
        }
      }
      // keep this last in case in future we would be able to normally resolve into these components
      if (VUE_FRAMEWORK_UNRESOLVABLE_COMPONENTS.contains(fromAsset)) {
        return VueElementDescriptor(JSImplicitElementImpl(fromAsset, tag))
      }
    }
    return null
  }

  private fun nameVariantsWithPossiblyGlobalMark(name: String): MutableSet<String> {
    val variants = mutableSetOf(name, toAsset(name), toAsset(name).capitalize())
    variants.addAll(variants.map { it + GLOBAL_BINDING_MARK })
    return variants
  }

  private fun processLocalComponents(tag: XmlTag, processor: (String?, JSImplicitElement) -> Boolean): Boolean {
    val content = findScriptContent(tag.containingFile as? HtmlFileImpl) ?: return true
    val defaultExport = ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment ?: return true
    val component = defaultExport.stubSafeElement as? JSObjectLiteralExpression ?: return true

    // recursive usage case
    val nameProperty = component.findProperty("name")
    val nameValue = nameProperty?.value as? JSLiteralExpression
    if (nameValue != null && nameValue.isQuotedLiteral) {
      val name = nameValue.value as? String
      if (name != null) {
        processor.invoke(name, JSImplicitElementImpl(name, nameProperty))
      }
    }

    val components = component.findProperty("components")?.objectLiteralExpressionInitializer ?: return true
    for (property in components.properties) {
      val obj = JSStubBasedPsiTreeUtil.calculateMeaningfulElement(property) as? JSObjectLiteralExpression
      val propName = property.name ?: continue
      if (obj != null) {
        val elements = findProperty(obj, "name")?.indexingData?.implicitElements?.filter { it.userString == VueComponentsIndex.JS_KEY }
        if (elements != null && !elements.isEmpty()) {
          if (elements.any { !processor.invoke(propName, it) }) return false
          continue
        }
        val first = obj.firstProperty
        if (first != null) {
          if (!processor.invoke(propName, JSImplicitElementImpl(propName, first))) return false
          continue
        }
      }
      if (!processor.invoke(propName, JSImplicitElementImpl(propName, property.nameIdentifier))) return false
    }
    return true
  }

  override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, prefix: String?) {
    val files:MutableList<PsiFile> = mutableListOf()
    processLocalComponents(tag, { foundName, element ->
      elements?.add(PrioritizedLookupElement.withPriority(createVueLookup(element, foundName!!, false).bold(), 100.0))
      files.add(element.containingFile)
      return@processLocalComponents true
    })
    val namePrefix = tag.name.substringBefore(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, tag.name)

    val variants = nameVariantsWithPossiblyGlobalMark(namePrefix)
    val allComponents = VueComponents.getAllComponents(tag.project, { key -> variants.any { key.startsWith(it, true) } }, false)
    val components = allComponents.map.keys
      .filter { !files.contains(allComponents.map[it]!!.first.containingFile) }
      .map {
        val value = allComponents.map[it]!!
        createVueLookup(value.first, fromAsset(it), value.second)
      }

    elements?.addAll(components)
  }

  private fun createVueLookup(element: PsiElement, name: String, isGlobal: Boolean) =
    LookupElementBuilder.create(element, fromAsset(name)).
      withInsertHandler(if (isGlobal) null else VueInsertHandler.INSTANCE).
      withIcon(VuejsIcons.Vue)

  companion object {
    private val VUE_FRAMEWORK_COMPONENTS = setOf(
      "component",
      "keep-alive",
      "slot",
      "transition",
      "transition-group"
    )
    private val VUE_FRAMEWORK_UNRESOLVABLE_COMPONENTS = setOf(
      "component",
      "slot"
    )
  }
}

class VueElementDescriptor(val element: JSImplicitElement) : XmlElementDescriptor {
  override fun getDeclaration() = element
  override fun getName(context: PsiElement?):String = (context as? XmlTag)?.name ?: name
  override fun getName() = fromAsset(declaration.name)
  override fun init(element: PsiElement?) {}
  override fun getQualifiedName() = name
  override fun getDefaultName() = name

  override fun getElementsDescriptors(context: XmlTag?): Array<out XmlElementDescriptor> {
    val xmlDocument = PsiTreeUtil.getParentOfType(context, XmlDocumentImpl::class.java) ?: return XmlElementDescriptor.EMPTY_ARRAY
    return xmlDocument.rootTagNSDescriptor.getRootElementsDescriptors(xmlDocument)
  }

  override fun getElementDescriptor(childTag: XmlTag?, contextTag: XmlTag?): XmlElementDescriptor? {
    val parent = contextTag?.parentTag ?: return null
    val descriptor = parent.getNSDescriptor(childTag?.namespace, true)
    return descriptor?.getElementDescriptor(childTag!!)
  }

  // it is better to use default attributes method since it is guaranteed to do not call any extension providers
  private fun getDefaultHtmlAttributes(context: XmlTag?): Array<out XmlAttributeDescriptor> =
    ((HtmlNSDescriptorImpl.guessTagForCommonAttributes(context) as? HtmlElementDescriptorImpl)?.
      getDefaultAttributeDescriptors(context) ?: emptyArray())

  override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    val result = mutableListOf<XmlAttributeDescriptor>()
    val defaultHtmlAttributes = getDefaultHtmlAttributes(context)
    result.addAll(defaultHtmlAttributes)
    VueAttributesProvider.addBindingAttributes(result, defaultHtmlAttributes)
    result.addAll(VueAttributesProvider.getDefaultVueAttributes())

    val obj = VueComponents.findComponentDescriptor(declaration)
    if (obj != null) {
      result.addAll(VueComponentDetailsProvider.INSTANCE.getAttributes(obj, true, true))
    }
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

  override fun getAttributeDescriptor(attribute: XmlAttribute?) = getAttributeDescriptor(attribute?.name, attribute?.parent)

  override fun getNSDescriptor() = null
  override fun getTopGroup() = null
  override fun getContentType() = CONTENT_TYPE_ANY
  override fun getDefaultValue() = null
  override fun getDependences(): Array<out Any> = ArrayUtil.EMPTY_OBJECT_ARRAY!!
}

fun findScriptContent(file: HtmlFileImpl?): JSEmbeddedContent? {
  return PsiTreeUtil.getChildrenOfType(file?.document, XmlTag::class.java)?.
    firstOrNull { HtmlUtil.isScriptTag(it) }?.children?.
    firstOrNull { it is JSEmbeddedContent } as? JSEmbeddedContent
}