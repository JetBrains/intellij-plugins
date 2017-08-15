package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDocumentImpl
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ArrayUtil
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlElementDescriptor.CONTENT_TYPE_ANY
import com.intellij.xml.XmlTagNameProvider
import com.intellij.xml.util.HtmlUtil
import icons.VuejsIcons
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.getAllKeys

class VueTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
    if (tag != null) {
      var localComponent:JSImplicitElement? = null
      processLocalComponents(tag, { property, element ->
        if (property.name == tag.name || property.name == toAsset(tag.name) || property.name == toAsset(tag.name).capitalize()) {
          localComponent = element
        }
        return@processLocalComponents localComponent == null
      })

      val component = localComponent ?:
                      org.jetbrains.vuejs.index.resolve(tag.name, tag.resolveScope, VueComponentsIndex.KEY) ?:
                      org.jetbrains.vuejs.index.resolve(toAsset(tag.name), tag.resolveScope, VueComponentsIndex.KEY) ?:
                      org.jetbrains.vuejs.index.resolve(toAsset(tag.name).capitalize(), tag.resolveScope, VueComponentsIndex.KEY)
      if (component != null) {
        return VueElementDescriptor(component)
      }
    }
    return null
  }

  private fun processLocalComponents(tag: XmlTag, processor: (JSProperty, JSImplicitElement) -> Boolean): Boolean {
    val content = findScriptContent(tag.containingFile as? HtmlFileImpl) ?: return true
    val defaultExport = ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment ?: return true
    val component = defaultExport.stubSafeElement as? JSObjectLiteralExpression ?: return true
    val components = component.findProperty("components")?.objectLiteralExpressionInitializer ?: return true
    for (property in components.properties) {
      val obj = JSStubBasedPsiTreeUtil.calculateMeaningfulElement(property) as? JSObjectLiteralExpression
      if (obj != null) {
        val elements = findProperty(obj, "name")?.indexingData?.implicitElements
        if (elements != null) {
          elements.forEach {
            if (it.userString == VueComponentsIndex.JS_KEY && !processor.invoke(property, it)) return false
          }
          continue
        }
        val first = obj.firstProperty
        if (first != null) {
          if (property.name != null && !processor.invoke(property, JSImplicitElementImpl(property.name!!, first))) return false
        }
      }
      if (property.name != null && !processor.invoke(property, JSImplicitElementImpl(property.name!!, property.nameIdentifier))) return false
    }
    return true
  }

  override fun addTagNameVariants(elements: MutableList<LookupElement>?, tag: XmlTag, prefix: String?) {
    val files:MutableList<PsiFile> = mutableListOf()
    processLocalComponents(tag, { property, element ->
      elements?.add(PrioritizedLookupElement.withPriority(createVueLookup(element, property.name!!).bold(), 100.0))
      files.add(element.containingFile)
      return@processLocalComponents true
    })
    elements?.addAll(getAllKeys(tag.resolveScope, VueComponentsIndex.KEY).filter { !files.contains(it.containingFile) }.
      map { createVueLookup(it, it.name) })
  }

  private fun createVueLookup(element: JSImplicitElement, name: String) =
    LookupElementBuilder.create(element, fromAsset(name)).
      withInsertHandler(VueInsertHandler.INSTANCE).
      withIcon(VuejsIcons.Vue)
}

class VueElementDescriptor(val element: JSImplicitElement) : XmlElementDescriptor {
  override fun getDeclaration() = element
  override fun getName(context: PsiElement?):String = (context as? XmlTag)?.name ?: name
  override fun getName() = fromAsset(element.name)
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

  override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    var props:Collection<XmlAttributeDescriptor> = emptyList()
    if (declaration.parent is JSProperty) {
      val obj = declaration.parent.context as JSObjectLiteralExpression
      val propsProperty = findProperty(obj, "props")
      var propsObject = propsProperty?.objectLiteralExpressionInitializer
      if (propsObject == null) {
        val initializerReference = propsProperty?.initializerReference
        if (initializerReference != null) {
          val local = JSStubBasedPsiTreeUtil.resolveLocally(initializerReference, propsProperty!!)
          propsObject = ((local as? JSVariable)?.initializerOrStub ?:
                         (local as? JSDefinitionExpression)?.initializerOrStub) as? JSObjectLiteralExpression
        }
      }
      if (propsObject != null) {
        props = propsObject.properties.map { VueAttributeDescriptor(it.name!!, it) }
      }
    }
    return HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)!!.plus(props)
  }
  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?) = getAttributesDescriptors(context).find { it.name == attributeName } ?:
                                                                                  VueAttributesProvider.vueAttributeDescriptor(attributeName)
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