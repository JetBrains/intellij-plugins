package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.meta.PsiPresentableMetaData
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ArrayUtil
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider
import com.intellij.xml.impl.BasicXmlAttributeDescriptor
import icons.VuejsIcons
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.VueLanguage
import javax.swing.Icon

class VueAttributesProvider : XmlAttributeDescriptorsProvider{
  companion object {
    val SCOPED = "scoped"
    @NonNls private val SRC_ATTR_NAME = "src"
    val DEFAULT_BINDABLE = arrayOf("key", "is")
    val DEFAULT = setOf("v-text", "v-html", "v-show", "v-if", "v-else", "v-else-if", "v-for",
                          "v-on", "v-bind", "v-model", "v-pre", "v-cloak","v-once",
                          "slot", "ref").
                  plus(DEFAULT_BINDABLE.map { "v-bind:" + it }).
                  plus(DEFAULT_BINDABLE.map { ":" + it })
    val HAVE_NO_PARAMS = setOf("v-else", "v-once", "v-pre", "v-cloak", "scoped")

    fun vueAttributeDescriptor(attributeName: String?): VueAttributeDescriptor? {
      if (DEFAULT.contains(attributeName!!)) return VueAttributeDescriptor(attributeName)
      return null
    }

    fun getDefaultVueAttributes() = DEFAULT.map { VueAttributeDescriptor(it) }.toTypedArray()
  }

  override fun getAttributeDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    if (context == null || !org.jetbrains.vuejs.index.hasVue(context.project)) return emptyArray()
    val default = getDefaultVueAttributes()
    if (insideStyle(context)) {
      return default.plus(VueAttributeDescriptor(SCOPED))
    }
    return default
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    if (context == null || !org.jetbrains.vuejs.index.hasVue(context.project) || attributeName == null) return null
    if (attributeName in arrayOf(SCOPED, SRC_ATTR_NAME) && insideStyle(context)) {
      return VueAttributeDescriptor(attributeName)
    }
    val extractedName = VueComponentDetailsProvider.getBoundName(attributeName)
    if (extractedName != null) {
      return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(extractedName, context) ?: VueAttributeDescriptor(extractedName)
    }
    return vueAttributeDescriptor(attributeName)
  }

  private fun insideStyle(context: XmlTag) = "style" == context.name && context.containingFile?.language == VueLanguage.INSTANCE
}

class VueAttributeDescriptor(private val name:String,
                             private val element:PsiElement? = null) : BasicXmlAttributeDescriptor(), PsiPresentableMetaData {
  private var suppressRequired = false
  override fun getName() = name
  override fun getDeclaration() = element
  override fun init(element: PsiElement?) {}
  override fun isRequired(): Boolean {
    if (suppressRequired) return false
    val initializer = (element as? JSProperty)?.objectLiteralExpressionInitializer ?: return false
    val literal = findProperty(initializer, "required")?.literalExpressionInitializer
    return literal != null && literal.isBooleanLiteral && "true" == literal.significantValue
  }
  override fun isFixed() = false
  override fun hasIdType() = false
  override fun getDependences(): Array<out Any> = ArrayUtil.EMPTY_OBJECT_ARRAY
  override fun getEnumeratedValueDeclaration(xmlElement: XmlElement?, value: String?): PsiElement? {
    return if (VueAttributesProvider.HAVE_NO_PARAMS.contains(name)) xmlElement else super.getEnumeratedValueDeclaration(xmlElement, value)
  }

  override fun hasIdRefType() = false
  override fun getDefaultValue() = null
  override fun isEnumerated() = VueAttributesProvider.HAVE_NO_PARAMS.contains(name)
  override fun getEnumeratedValues(): Array<out String> {
    if (VueAttributesProvider.HAVE_NO_PARAMS.contains(name)) return arrayOf(name)
    return ArrayUtil.EMPTY_STRING_ARRAY
  }
  override fun getTypeName() = null
  override fun getIcon(): Icon = VuejsIcons.Vue

  fun createNameVariant(newName: String) : VueAttributeDescriptor {
    if (newName == name) return this
    val descriptor = VueAttributeDescriptor(newName, element)
    descriptor.suppressRequired = true
    return descriptor
  }
}

fun findProperty(obj: JSObjectLiteralExpression?, name:String) = obj?.properties?.find { it.name == name }