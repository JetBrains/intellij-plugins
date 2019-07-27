// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.psi.PsiElement
import com.intellij.psi.meta.PsiPresentableMetaData
import com.intellij.psi.xml.XmlElement
import com.intellij.util.ArrayUtil
import com.intellij.xml.impl.BasicXmlAttributeDescriptor
import icons.VuejsIcons
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.codeInsight.BOOLEAN_TYPE
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueModelVisitor.Proximity.*
import javax.swing.Icon

@Suppress("DEPRECATION")
open class VueAttributeDescriptor(name: String,
                                  element: PsiElement? = null,
                                  acceptsNoValue: Boolean = false,
                                  val priority: AttributePriority = AttributePriority.NORMAL,
                                  isRequired: Boolean = false) :
  org.jetbrains.vuejs.codeInsight.VueAttributeDescriptor(name, element, isNonProp = acceptsNoValue) {

  constructor(name: String, prop: VueInputProperty) : this(name, prop.source,
                                                           isRequired = prop.required,
                                                           acceptsNoValue = isBooleanProp(prop),
                                                           priority = AttributePriority.HIGH)

  private val _isRequired: Boolean = isRequired

  override fun isRequired(): Boolean {
    return _isRequired
  }

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

// This class is the original `VueAttributeDescriptor` class,
// but it's renamed to allow instanceof check through deprecated class from 'codeInsight' package
@Deprecated("Public for internal purpose only!")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class _VueAttributeDescriptor(private val name: String,
                                   internal val element: PsiElement? = null,
                                   private val acceptsNoValue: Boolean = false) : BasicXmlAttributeDescriptor(), PsiPresentableMetaData {
  override fun isRequired(): Boolean {
    return false
  }

  override fun getName(): String = name
  override fun getDeclaration(): PsiElement? = element
  override fun init(element: PsiElement?) {}

  override fun isFixed(): Boolean = false
  override fun hasIdType(): Boolean = false
  override fun getEnumeratedValueDeclaration(xmlElement: XmlElement?, value: String?): PsiElement? {
    return if (isEnumerated)
      xmlElement
    else if (value == null || value.isEmpty())
      null
    else
      super.getEnumeratedValueDeclaration(xmlElement, value)
  }

  override fun hasIdRefType(): Boolean = false
  override fun getDefaultValue(): Nothing? = null
  override fun isEnumerated(): Boolean = acceptsNoValue

  override fun getEnumeratedValues(): Array<out String> {
    if (isEnumerated) {
      return arrayOf(name)
    }
    return ArrayUtil.EMPTY_STRING_ARRAY
  }

  override fun getTypeName(): String? = null
  override fun getIcon(): Icon = VuejsIcons.Vue

}
