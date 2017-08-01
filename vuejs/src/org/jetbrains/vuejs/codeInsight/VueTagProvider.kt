package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.text.StringUtil
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
import com.intellij.util.PairProcessor
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlElementDescriptor
import com.intellij.xml.XmlElementDescriptor.CONTENT_TYPE_ANY
import com.intellij.xml.XmlTagNameProvider
import com.intellij.xml.util.HtmlUtil
import icons.VuejsIcons
import org.jetbrains.vuejs.index.VueComponentsIndex
import org.jetbrains.vuejs.index.getAllKeys

private val PROPS = "props"
private val COMPUTED = "computed"
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
    val component = defaultExport.expression as? JSObjectLiteralExpression ?: return true
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

private val FUNCTION_FILTER = PairProcessor<String, PsiElement> { name, element -> element is JSProperty && element.value is JSFunction }

fun findComponentInnerDetailInObjectLiteral(obj : JSObjectLiteralExpression, attributeName: String) : Pair<String, PsiElement>? {
  val filter = VueElementDescriptor.Companion.nameVariantsFilter(attributeName)

  var descriptor: VueAttributeDescriptor? = null
  val propsProperty = findProperty(obj, PROPS)
  if (propsProperty != null) {
    descriptor = readProps(propsProperty, true, filter).firstOrNull()
  }
  if (descriptor == null) {
    val computedProperty = findProperty(obj, COMPUTED)
    if (computedProperty != null) {
      descriptor = readProps(computedProperty, true, PairProcessor { name, element -> filter.process(name, element) &&
                                                                                      FUNCTION_FILTER.process(name, element)
      }).firstOrNull()
    }
  }

  if (descriptor?.name != attributeName) {
    descriptor = descriptor?.createNameVariant(attributeName)
  }
  return if(descriptor == null) null else Pair(descriptor.name, descriptor.declaration!!)
}

fun getComponentInnerDetailsFromObjectLiteral(obj : JSObjectLiteralExpression) : List<Pair<String, PsiElement>> {
  val result : MutableList<VueAttributeDescriptor> = mutableListOf()
  val propsProperty = findProperty(obj, PROPS)
  if (propsProperty != null) {
    @Suppress("UNCHECKED_CAST")
    result.addAll(withNameVariants(readProps(propsProperty, true, null), false))
  }
  val computedProperty = findProperty(obj, COMPUTED)
  if (computedProperty != null) {
    @Suppress("UNCHECKED_CAST")
    result.addAll(withNameVariants(readProps(computedProperty, true, FUNCTION_FILTER), false))
  }
  return result.map { Pair(it.name, it.declaration!!) }
}

private fun readProps(propsProperty : JSProperty, checkForArray: Boolean, filter : PairProcessor<String, PsiElement>?): List<VueAttributeDescriptor> {
  var propsObject = propsProperty.objectLiteralExpressionInitializer
  if (propsObject == null) {
    if (propsProperty.initializerReference != null) {
      val resolved = JSStubBasedPsiTreeUtil.resolveLocally(propsProperty.initializerReference!!, propsProperty)
      if (resolved != null) {
        propsObject = JSStubBasedPsiTreeUtil
          .findDescendants(resolved, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION).find { it.context == resolved }
        if (propsObject == null && checkForArray) {
          return readPropsFromArray(resolved, filter)
        }
      }
    }
  }
  if (propsObject != null) {
    return propsObject.properties.filter { filter == null || filter.process(it.name!!, it) }.map { VueAttributeDescriptor(it.name!!, it) }
  }
  return if (checkForArray) readPropsFromArray(propsProperty, filter) else return emptyList()
}

private fun readPropsFromArray(holder: PsiElement, filter: PairProcessor<String, PsiElement>?): List<VueAttributeDescriptor> {
  return JSStubBasedPsiTreeUtil.findDescendants(holder, JSStubElementTypes.LITERAL_EXPRESSION)
    .filter {
      var result = it.isQuotedLiteral && (filter == null || filter.process(StringUtil.unquoteString(it.text), it))
      if (result) {
        val context = it.context
        result = (context is JSArrayLiteralExpression) && (context.parent == holder) || context == holder
      }
      result
    }.map { VueAttributeDescriptor(StringUtil.unquoteString(it.text), it) }
}

private fun withNameVariants(props: List<VueAttributeDescriptor>, withKebab: Boolean) : List<VueAttributeDescriptor> {
  if (props.isEmpty()) return emptyList()
  val alternatives : MutableSet<VueAttributeDescriptor> = mutableSetOf()
  props.forEach {
    val attr = it
    getNameVariants(it.name, withKebab).minus(it.name).forEach { alternatives.add(attr.createNameVariant(it)) }
  }
  return props.plus(alternatives)
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
    var result = HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)

    val obj = (declaration.parent as? JSProperty)?.context as? JSObjectLiteralExpression
    if (obj != null) {
      val propsProperty = findProperty(obj, PROPS)
      if (propsProperty != null) {
        @Suppress("UNCHECKED_CAST")
        result = result.plus(withNameVariants(readProps(propsProperty, true, null), true))
      }
    }
    return result
  }

  companion object {
    private val BIND_VARIANTS = setOf(":", "v-bind:")
    fun nameVariantsFilter(attributeName : String) : PairProcessor<String, PsiElement> {
      val prefix = VueElementDescriptor.BIND_VARIANTS.find { attributeName.startsWith(it) }
      val normalizedName = if (prefix != null) attributeName.substring(prefix.length) else attributeName
      val nameVariants = getNameVariants(normalizedName, true)
      return PairProcessor { name, element -> name in nameVariants }
    }
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    if (attributeName == null) return null

    val obj = (declaration.parent as? JSProperty)?.context as? JSObjectLiteralExpression ?: return null

    val propsProperty = obj.findProperty(PROPS)
    if (propsProperty != null) {
      val descriptor = readProps(propsProperty, true, nameVariantsFilter(attributeName)).firstOrNull()
      return if (descriptor?.name != attributeName) descriptor?.createNameVariant(attributeName) else descriptor
    }

    return null
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