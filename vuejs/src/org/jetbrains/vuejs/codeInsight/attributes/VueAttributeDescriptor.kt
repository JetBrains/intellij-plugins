// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.meta.PsiPresentableMetaData
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ArrayUtil
import com.intellij.xml.impl.BasicXmlAttributeDescriptor
import icons.VuejsIcons
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.BOOLEAN_TYPE
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeInfo
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueModelVisitor.Proximity.*
import javax.swing.Icon

open class VueAttributeDescriptor(protected val tag: XmlTag,
                                  private val name: String,
                                  internal val element: PsiElement? = null,
                                  private val sources: List<VueDocumentedItem> = listOf(),
                                  private val acceptsNoValue: Boolean = false,
                                  private val acceptsValue: Boolean = true,
                                  val priority: AttributePriority = AttributePriority.NORMAL,
                                  isRequired: Boolean = false)
  : BasicXmlAttributeDescriptor(), PsiPresentableMetaData {

  constructor(tag: XmlTag, name: String, prop: VueInputProperty) : this(
    tag, name, prop.source, listOf(prop),
    isRequired = prop.required,
    acceptsNoValue = isBooleanProp(prop),
    priority = AttributePriority.HIGH)

  private val _isRequired: Boolean = isRequired
  private val info: NotNullLazyValue<VueAttributeInfo> = NotNullLazyValue.createValue {
    VueAttributeNameParser.parse(getName(), tag)
  }

  fun getInfo(): VueAttributeInfo = info.value

  fun getSources(): List<VueDocumentedItem> = sources.toList()

  override fun validateValue(context: XmlElement?, value: String?): String? {
    if (value != null && !acceptsValue) {
      return VueBundle.message("vue.inspection.message.attribute.does.not.accept.value", name)
    }
    return null
  }

  override fun isRequired(): Boolean = _isRequired
  override fun getName(): String = name
  override fun getDeclaration(): PsiElement? = element
  override fun init(element: PsiElement?) {}

  override fun isFixed(): Boolean = false
  override fun hasIdType(): Boolean = false
  override fun getEnumeratedValueDeclaration(xmlElement: XmlElement?, value: String?): PsiElement? =
    if (isEnumerated)
      xmlElement
    else if (value == null || value.isEmpty())
      null
    else
      super.getEnumeratedValueDeclaration(xmlElement, value)

  override fun hasIdRefType(): Boolean = false
  override fun getDefaultValue(): Nothing? = null
  override fun isEnumerated(): Boolean = acceptsNoValue

  override fun getEnumeratedValues(): Array<out String> {
    if (isEnumerated) {
      return arrayOf(name)
    }
    return ArrayUtil.EMPTY_STRING_ARRAY
  }

  override fun getValueReferences(element: XmlElement?, text: String): Array<PsiReference> =
    if (StringUtil.equalsIgnoreCase(name, text))
      super.getValueReferences(element, text)
    else
      PsiReference.EMPTY_ARRAY

  override fun getTypeName(): String? = null
  override fun getIcon(): Icon = VuejsIcons.Vue

  companion object {
    private fun isBooleanProp(prop: VueInputProperty): Boolean {
      return prop.jsType?.isDirectlyAssignableType(BOOLEAN_TYPE, null) ?: false
    }
  }

  enum class AttributePriority(val value: Double) {
    NONE(0.0),
    LOW(25.0),
    NORMAL(50.0),
    HIGH(100.0);

    companion object {
      fun of(proximity: VueModelVisitor.Proximity): AttributePriority {
        return when (proximity) {
          LOCAL -> HIGH
          PLUGIN, APP -> NORMAL
          GLOBAL -> LOW
          OUT_OF_SCOPE -> NONE
        }
      }
    }
  }
}
