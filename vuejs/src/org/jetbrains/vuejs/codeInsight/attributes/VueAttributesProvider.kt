// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

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
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributesProvider.Companion.isBinding
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.VueComponentDetailsProvider.Companion.attributeAllowsNoValue
import org.jetbrains.vuejs.model.source.VueComponentDetailsProvider.Companion.getBoundName
import javax.swing.Icon

class VueAttributesProvider : XmlAttributeDescriptorsProvider {
  companion object {
    private const val SCOPED_ATTR: String = "scoped"
    private const val SRC_ATTR = "src"
    private const val MODULE_ATTR: String = "module"
    private const val FUNCTIONAL_ATTR: String = "functional"

    // "v-on" is not included because it can't be used as is, it must be followed by a colon and an event, this is supported separately
    val DEFAULT: Set<String> = setOf("v-text", "v-html", "v-show", "v-if", "v-else", "v-else-if", "v-for",
                                     "v-bind", "v-model", "v-pre", "v-cloak", "v-once",
                                     "slot", "ref", "slot-scope")
    val HAVE_NO_PARAMS: Set<String> = setOf("v-else", "v-once", "v-pre", "v-cloak", SCOPED_ATTR, MODULE_ATTR, FUNCTIONAL_ATTR)

    fun isInjectJS(attrName: String): Boolean {
      if (attrName == "slot" || attrName == "ref") return false
      if (DEFAULT.contains(attrName)) return true
      if (attrName.startsWith("@") || attrName.startsWith("v-on:") ||
          attrName.startsWith(":") || attrName.startsWith("v-bind:")) {
        return true
      }
      return false
    }

    fun vueAttributeDescriptor(attributeName: String?): VueAttributeDescriptor? {
      if (DEFAULT.contains(attributeName!!)) return VueAttributeDescriptor(attributeName)
      return null
    }

    fun getDefaultVueAttributes(): Array<VueAttributeDescriptor> = DEFAULT.map { VueAttributeDescriptor(it) }.toTypedArray()

    fun isBinding(name: String): Boolean = name.startsWith(":") || name.startsWith("v-bind:")
  }

  override fun getAttributeDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    if (context == null || !org.jetbrains.vuejs.index.isVueContext(context)) return emptyArray()
    val result = mutableListOf<XmlAttributeDescriptor>()
    result.addAll(getDefaultVueAttributes())

    if (isTopLevelTemplateTag(context)) {
      result.add(VueAttributeDescriptor(FUNCTIONAL_ATTR))
    }
    if (isTopLevelStyleTag(context)) {
      result.add(VueAttributeDescriptor(SCOPED_ATTR))
      result.add(VueAttributeDescriptor(SRC_ATTR))
      result.add(VueAttributeDescriptor(MODULE_ATTR))
    }
    val contributedDirectives = mutableSetOf<String>()
    VueModelManager.findEnclosingContainer(context)?.acceptEntities(object : VueModelVisitor() {
      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        if (contributedDirectives.add(name)) {
          result.add(VueAttributeDescriptor("v-" + fromAsset(name), directive.source, true))
        }
        return true
      }
    }, VueModelVisitor.Proximity.GLOBAL)
    return result.toTypedArray()
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    if (context == null || !org.jetbrains.vuejs.index.isVueContext(context) || attributeName == null) return null
    if (isTopLevelTemplateTag(context) && attributeName == FUNCTIONAL_ATTR ||
        isTopLevelStyleTag(context) && attributeName in arrayOf(SCOPED_ATTR, SRC_ATTR, MODULE_ATTR)) {
      return VueAttributeDescriptor(attributeName)
    }

    resolveDirective(attributeName, context)?.let { return it }

    val extractedName = getBoundName(attributeName)
    if (extractedName != null) {
      return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(extractedName, context) ?: VueAttributeDescriptor(attributeName)
    }
    return vueAttributeDescriptor(attributeName)
  }

  private fun resolveDirective(attrName: String, context: XmlTag): VueAttributeDescriptor? {
    val searchName = attrName.substringAfter("v-", "")
    if (searchName.isEmpty()) return null

    val directives = mutableListOf<VueDirective>()
    VueModelManager.findEnclosingContainer(context)?.acceptEntities(object : VueModelProximityVisitor() {
      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        return visitSameProximity(proximity) {
          if (fromAsset(name) == searchName) {
            directives.add(directive)
            false
          }
          else {
            true
          }
        }
      }
    }, VueModelVisitor.Proximity.GLOBAL)

    return directives.firstOrNull()?.let {
      VueAttributeDescriptor("v-" + fromAsset(it.defaultName ?: searchName), it.source, true)
    }
  }

  private fun isTopLevelStyleTag(tag: XmlTag): Boolean = tag.parentTag == null &&
                                                         tag.name == "style" &&
                                                         tag.containingFile?.language == VueLanguage.INSTANCE

  private fun isTopLevelTemplateTag(tag: XmlTag): Boolean = tag.parentTag == null &&
                                                            tag.name == "template" &&
                                                            tag.containingFile?.language == VueLanguage.INSTANCE
}

@Suppress("DEPRECATION")
open class VueAttributeDescriptor(name: String,
                                  element: PsiElement? = null,
                                  isDirective: Boolean = false,
                                  isNonProp: Boolean = false) :
  org.jetbrains.vuejs.codeInsight.VueAttributeDescriptor(name, element, isDirective, isNonProp) {

  fun createNameVariant(newName: String): VueAttributeDescriptor {
    if (newName == name) return this
    return VueAttributeDescriptor(newName, element)
  }
}

// This class is the original `VueAttributeDescriptor` class,
// but it's renamed to allow instanceof check through deprecated class from 'codeInsight' package
@Deprecated("Public for internal purpose only!")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class _VueAttributeDescriptor(private val name: String,
                                   internal val element: PsiElement? = null,
                                   private val isDirective: Boolean = false,
                                   private val isNonProp: Boolean = false) : BasicXmlAttributeDescriptor(), PsiPresentableMetaData {
  override fun getName(): String = name
  override fun getDeclaration(): PsiElement? = element
  override fun init(element: PsiElement?) {}
  override fun isRequired(): Boolean {
    if (isBinding(name)) return false
    val initializer = (element as? JSProperty)?.objectLiteralExpressionInitializer ?: return false
    val literal = findProperty(initializer, "required")?.literalExpressionInitializer
    return literal != null && literal.isBooleanLiteral && "true" == literal.significantValue
  }

  override fun isFixed(): Boolean = false
  override fun hasIdType(): Boolean = false
  override fun getEnumeratedValueDeclaration(xmlElement: XmlElement?, value: String?): PsiElement? {
    return if (isEnumerated) xmlElement else super.getEnumeratedValueDeclaration(xmlElement, value)
  }

  override fun hasIdRefType(): Boolean = false
  override fun getDefaultValue(): Nothing? = null
  override fun isEnumerated(): Boolean = isDirective || isNonProp ||
                                         VueAttributesProvider.HAVE_NO_PARAMS.contains(name) || attributeAllowsNoValue(name)

  override fun getEnumeratedValues(): Array<out String> {
    if (isEnumerated) {
      return arrayOf(name)
    }
    return ArrayUtil.EMPTY_STRING_ARRAY
  }

  override fun getTypeName(): String? = null
  override fun getIcon(): Icon = VuejsIcons.Vue

  fun isDirective(): Boolean = isDirective
}

fun findProperty(obj: JSObjectLiteralExpression?, name: String): JSProperty? = obj?.properties?.find { it.name == name }
