package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtilCore
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
import com.intellij.openapi.util.NullableFactory
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.impl.source.xml.XmlDocumentImpl
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.TokenSet
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
import org.jetbrains.vuejs.COMPUTED
import org.jetbrains.vuejs.METHODS
import org.jetbrains.vuejs.MIXINS
import org.jetbrains.vuejs.PROPS
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.isGlobal
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.resolveReferenceToObjectLiteral
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.selectComponent
import org.jetbrains.vuejs.index.*

class VueTagProvider : XmlElementDescriptorProvider, XmlTagNameProvider {
  override fun getDescriptor(tag: XmlTag?): XmlElementDescriptor? {
    if (tag != null && hasVue(tag.project)) {
      var localComponent:JSImplicitElement? = null
      processLocalComponents(tag, { foundName, element ->
        if (foundName == tag.name || foundName == toAsset(tag.name) || foundName == toAsset(tag.name).capitalize()) {
          localComponent = element
        }
        return@processLocalComponents localComponent == null
      })

      if (localComponent != null) return VueElementDescriptor(localComponent!!)

      val component = selectComponent(resolve(tag.name, tag.resolveScope, VueComponentsIndex.KEY), false) ?:
                      selectComponent(resolve(toAsset(tag.name), tag.resolveScope, VueComponentsIndex.KEY), false) ?:
                      selectComponent(resolve(toAsset(tag.name).capitalize(), tag.resolveScope, VueComponentsIndex.KEY), false)
      if (component != null && isGlobal(component)) {
        return VueElementDescriptor(component)
      }
    }
    return null
  }

  private fun processLocalComponents(tag: XmlTag, processor: (String?, JSImplicitElement) -> Boolean): Boolean {
    val content = findScriptContent(tag.containingFile as? HtmlFileImpl) ?: return true
    val defaultExport = ES6PsiUtil.findDefaultExport(content) as? JSExportAssignment ?: return true
    val component = defaultExport.stubSafeElement as? JSObjectLiteralExpression ?: return true

    // recursive usage case
    val nameProperty = component.findProperty("name")
    val nameValue = nameProperty?.value as? JSLiteralExpression
    if (nameValue != null && nameValue.isQuotedLiteral) {
      val name = StringUtil.unquoteString(nameValue.text)
      processor.invoke(name, JSImplicitElementImpl(name, nameProperty))
    }

    val components = component.findProperty("components")?.objectLiteralExpressionInitializer ?: return true
    for (property in components.properties) {
      val obj = JSStubBasedPsiTreeUtil.calculateMeaningfulElement(property) as? JSObjectLiteralExpression
      val propName = property.name
      if (obj != null) {
        val elements = findProperty(obj, "name")?.indexingData?.implicitElements
        if (elements != null) {
          elements.forEach {
            if (it.userString == VueComponentsIndex.JS_KEY && !processor.invoke(propName, it)) return false
          }
          continue
        }
        val first = obj.firstProperty
        if (first != null) {
          if (propName != null && !processor.invoke(propName, JSImplicitElementImpl(propName, first))) return false
        }
      }
      if (propName != null && !processor.invoke(propName, JSImplicitElementImpl(propName, property.nameIdentifier))) return false
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
    val globals = mutableSetOf<String>()
    val variants = getForAllKeys(tag.resolveScope, VueComponentsIndex.KEY, { key -> key.startsWith(namePrefix, true) }).
      filter { !files.contains(it.containingFile) }
    variants.forEach { if (VueComponents.isGlobal(it)) globals.addAll(getNameVariants(it.name, true)) }

    val components = variants.filter { !globals.contains(it.name) || VueComponents.isGlobal(it) }.
      map { createVueLookup(it, it.name, VueComponents.isGlobal(it)) }
    elements?.addAll(components)
  }

  private fun createVueLookup(element: JSImplicitElement, name: String, isGlobal: Boolean) =
    LookupElementBuilder.create(element, fromAsset(name)).
      withInsertHandler(if (isGlobal) null else VueInsertHandler.INSTANCE).
      withIcon(VuejsIcons.Vue)
}

private val FUNCTION_FILTER = PairProcessor<String, PsiElement> { _, element ->
  element is JSFunctionProperty || element is JSProperty && element.value is JSFunction }

fun findComponentInnerDetailInObjectLiteral(obj : JSObjectLiteralExpression, attributeName: String) : Pair<String, PsiElement>? {
  val filter = VueElementDescriptor.Companion.nameVariantsFilter(attributeName)

  var descriptor = findInnerDetailDescriptor(obj, filter)
  if (descriptor?.name != attributeName) {
    descriptor = descriptor?.createNameVariant(attributeName)
  }
  return if(descriptor == null) null else Pair(descriptor.name, descriptor.declaration!!)
}

private fun findInnerDetailDescriptor(obj: JSObjectLiteralExpression,
                                      filter: PairProcessor<String, PsiElement>): VueAttributeDescriptor? {
  val namedFunctionFilter = PairProcessor<String, PsiElement> { name, element ->
    filter.process(name, element) &&
    FUNCTION_FILTER.process(name, element)
  }
  val searchers : List<NullableFactory<VueAttributeDescriptor>> = listOf(
    NullableFactory {
      val propsProperty = findProperty(obj, PROPS)
      if (propsProperty != null) {
        readProps(propsProperty, true, filter).firstOrNull()
      }
      else null
    },
    NullableFactory {
      val computedProperty = findProperty(obj, COMPUTED)
      if (computedProperty != null) {
        readProps(computedProperty, true, namedFunctionFilter).firstOrNull()
      }
      else null
    },
    NullableFactory {
      val methodsProperty = findProperty(obj, METHODS)
      if (methodsProperty != null) {
        readProps(methodsProperty, true, namedFunctionFilter).firstOrNull()
      }
      else null
    },
    NullableFactory {
      val dataProperty = findProperty(obj, "data")
      if (dataProperty != null) {
        readDataProps(dataProperty, filter).firstOrNull()
      }
      else null
    },
    NullableFactory {
      getComponentMixins(obj)?.mapNotNull {
        val mixinObj = resolveMixinObject(it) ?: return@mapNotNull null
        return@mapNotNull findInnerDetailDescriptor(mixinObj, filter)
      }?.firstOrNull()
    }
  )
  return searchers.mapNotNull { it.create() }.firstOrNull()
}

private fun resolveMixinObject(it: JSImplicitElement): JSObjectLiteralExpression? {
  var mixinObj = it.parent as? JSObjectLiteralExpression
  if (it.typeString != null) {
    mixinObj = resolveReferenceToObjectLiteral(it, it.typeString!!)
  }
  return mixinObj
}

private fun getComponentMixins(obj: JSObjectLiteralExpression): List<JSImplicitElement>? {
  val mixinsProperty = findProperty(obj, MIXINS) ?: return null
  val elements = resolve("", GlobalSearchScope.fileScope(mixinsProperty.containingFile), VueMixinBindingIndex.KEY) ?: return null
  return elements.filter { PsiTreeUtil.isAncestor(mixinsProperty, it.parent, false) }
}

fun readDataProps(dataProps: JSProperty, filter : PairProcessor<String, PsiElement>?) : List<VueAttributeDescriptor> {
  var dataObject = dataProps.objectLiteralExpressionInitializer
  if (dataObject == null) {
    val function = dataProps.tryGetFunctionInitializer() ?: return emptyList()
    dataObject = JSStubBasedPsiTreeUtil.findDescendants<JSObjectLiteralExpression>(function, TokenSet.create(
      JSStubElementTypes.OBJECT_LITERAL_EXPRESSION))
      .find {
        it.context == function ||
        it.context is JSParenthesizedExpression && it.context?.context == function ||
        it.context is JSReturnStatement
      } ?: return emptyList()
  }
  return filteredObjectProperties(dataObject, filter)
}

private fun getComponentInnerDetailsFromObjectLiteralBase(obj : JSObjectLiteralExpression) : MutableList<Pair<String, PsiElement>> {
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
  val methodsProperty = findProperty(obj, METHODS)
  if (methodsProperty != null) {
    @Suppress("UNCHECKED_CAST")
    result.addAll(withNameVariants(readProps(methodsProperty, true, FUNCTION_FILTER), false))
  }
  val dataProperty = findProperty(obj, "data")
  if (dataProperty != null) {
    @Suppress("UNCHECKED_CAST")
    result.addAll(withNameVariants(readDataProps(dataProperty, null), false))
  }
  return result.map { Pair(it.name, it.declaration!!) } as MutableList
}

fun getComponentInnerDetailsFromObjectLiteral(obj : JSObjectLiteralExpression) : List<Pair<String, PsiElement>> {
  val result = getComponentInnerDetailsFromObjectLiteralBase(obj)
  getComponentMixins(obj)?.forEach {
    val mixinObject = resolveMixinObject(it) ?: return@forEach
    result.addAll(getComponentInnerDetailsFromObjectLiteralBase(mixinObject))
  }
  return result
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
    return filteredObjectProperties(propsObject, filter)
  }
  return if (checkForArray) readPropsFromArray(propsProperty, filter) else return emptyList()
}

private fun filteredObjectProperties(propsObject: JSObjectLiteralExpression, filter: PairProcessor<String, PsiElement>?) =
  propsObject.properties.filter { filter == null || filter.process(it.name!!, it) }.map { VueAttributeDescriptor(it.name!!, it) }

private fun readPropsFromArray(holder: PsiElement, filter: PairProcessor<String, PsiElement>?): List<VueAttributeDescriptor> =
  getStringLiteralsFromInitializerArray(holder, filter).map { VueAttributeDescriptor(StringUtil.unquoteString(it.text), it) }

fun getStringLiteralsFromInitializerArray(holder: PsiElement, filter: PairProcessor<String, PsiElement>?): List<JSLiteralExpression> {
  return JSStubBasedPsiTreeUtil.findDescendants(holder, JSStubElementTypes.LITERAL_EXPRESSION)
    .filter({
              var result = it.significantValue != null &&
                           (filter == null || filter.process(StringUtil.unquoteString(it.significantValue!!), it))
              if (result) {
                val context = it.context
                result = (context is JSArrayLiteralExpression) && (context.parent == holder) || context == holder
              }
              result
            })
}

private fun withNameVariants(props: List<VueAttributeDescriptor>, withKebab: Boolean) : List<VueAttributeDescriptor> {
  if (props.isEmpty()) return emptyList()
  val alternatives : MutableSet<VueAttributeDescriptor> = mutableSetOf()
  props.forEach {
    @Suppress("UnnecessaryVariable")
    val attr = it
    getNameVariants(it.name, withKebab).minus(it.name).forEach { alternatives.add(attr.createNameVariant(it)) }
  }
  return props.plus(alternatives)
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

  private fun getImmediateAttributesDescriptors(obj : JSObjectLiteralExpression): List<VueAttributeDescriptor> {
    val propsProperty = findProperty(obj, PROPS) ?: return emptyList()
    return withNameVariants(readProps(propsProperty, true, null), true)
  }

  override fun getAttributesDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    val result = mutableListOf<XmlAttributeDescriptor>()
    result.addAll(HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context))
    result.addAll(VueAttributesProvider.getDefaultVueAttributes())

    val obj = VueComponents.findComponentDescriptor(declaration)
    if (obj != null) {
      result.addAll(getImmediateAttributesDescriptors(obj))
      getComponentMixins(obj)?.forEach {
        val mixinObject = resolveMixinObject(it) ?: return@forEach
        result.addAll(getImmediateAttributesDescriptors(mixinObject))
      }
    }
    return result.toTypedArray()
  }

  companion object {
    private val BIND_VARIANTS = setOf(":", "v-bind:")
    fun nameVariantsFilter(attributeName : String) : PairProcessor<String, PsiElement> {
      val prefix = VueElementDescriptor.BIND_VARIANTS.find { attributeName.startsWith(it) }
      val normalizedName = if (prefix != null) attributeName.substring(prefix.length) else attributeName
      val nameVariants = getNameVariants(normalizedName, true)
      return PairProcessor { name, _ -> name in nameVariants }
    }
  }

  private fun getImmediateAttributeDescriptor(obj : JSObjectLiteralExpression, attributeName: String): VueAttributeDescriptor? {
    val propsProperty = obj.findProperty(PROPS) ?: return null
    val descriptor = readProps(propsProperty, true, nameVariantsFilter(attributeName)).firstOrNull() ?: return null
    return if (descriptor.name != attributeName) descriptor.createNameVariant(attributeName) else descriptor
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    if (attributeName == null) return null

    val obj = VueComponents.findComponentDescriptor(declaration) ?: return null

    val descriptor = getImmediateAttributeDescriptor(obj, attributeName)
    if (descriptor != null) return descriptor
    val fromMixin = getComponentMixins(obj)?.mapNotNull {
      val mixinObject = resolveMixinObject(it) ?: return@mapNotNull null
      getImmediateAttributeDescriptor(mixinObject, attributeName)
    }?.firstOrNull()
    if (fromMixin != null) return fromMixin

    if (VueAttributesProvider.DEFAULT.contains(attributeName)) return VueAttributeDescriptor(attributeName)
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