// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
import org.jetbrains.vuejs.VueLanguage
import org.jetbrains.vuejs.codeInsight.VueAttributesProvider.Companion.isBinding
import org.jetbrains.vuejs.codeInsight.VueComponentDetailsProvider.Companion.attributeAllowsNoValue
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
    if (context == null || !org.jetbrains.vuejs.index.hasVue(context.project)) return emptyArray()
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
    result.addAll(VueDirectivesProvider.getAttributes(findLocalDescriptor(context), context.project))
    return result.toTypedArray()
  }

  private fun findLocalDescriptor(context: XmlTag): JSObjectLiteralExpression? {
    val scriptWithExport = findScriptWithExport(context.containingFile.originalFile) ?: return null
    return scriptWithExport.second.stubSafeElement as? JSObjectLiteralExpression
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    if (context == null || !org.jetbrains.vuejs.index.hasVue(context.project) || attributeName == null) return null
    if (isTopLevelTemplateTag(context) && attributeName == FUNCTIONAL_ATTR ||
        isTopLevelStyleTag(context) && attributeName in arrayOf(SCOPED_ATTR, SRC_ATTR, MODULE_ATTR)) {
      return VueAttributeDescriptor(attributeName)
    }
    val fromDirective = VueDirectivesProvider.resolveAttribute(findLocalDescriptor(context), attributeName, context.project)
    if (fromDirective != null) return fromDirective
    val extractedName = VueComponentDetailsProvider.getBoundName(attributeName)
    if (extractedName != null) {
      return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(extractedName, context) ?: VueAttributeDescriptor(attributeName)
    }
    return vueAttributeDescriptor(attributeName)
  }

  private fun isTopLevelStyleTag(tag: XmlTag): Boolean = tag.parentTag == null &&
                                                         tag.name == "style" &&
                                                         tag.containingFile?.language == VueLanguage.INSTANCE

  private fun isTopLevelTemplateTag(tag: XmlTag): Boolean = tag.parentTag == null &&
                                                            tag.name == "template" &&
                                                            tag.containingFile?.language == VueLanguage.INSTANCE
}

class VueAttributeDescriptor(private val name:String,
                             private val element:PsiElement? = null,
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

  fun createNameVariant(newName: String) : VueAttributeDescriptor {
    if (newName == name) return this
    return VueAttributeDescriptor(newName, element)
  }

  fun isDirective(): Boolean = isDirective
}

fun findProperty(obj: JSObjectLiteralExpression?, name:String): JSProperty? = obj?.properties?.find { it.name == name }