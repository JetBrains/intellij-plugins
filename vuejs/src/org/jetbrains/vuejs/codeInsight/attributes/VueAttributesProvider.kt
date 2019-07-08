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
import one.util.streamex.StreamEx
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueModelVisitor
import javax.swing.Icon

class VueAttributesProvider : XmlAttributeDescriptorsProvider {

  override fun getAttributeDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    if (context == null || !org.jetbrains.vuejs.index.isVueContext(context)) return emptyArray()
    val result = mutableListOf<XmlAttributeDescriptor>()


    StreamEx.of(*VueAttributeNameParser.VueAttributeKind.values())
      .filter { it.attributeName != null && it.isValidIn(context) }
      .map { VueAttributeDescriptor(it.attributeName!!, acceptsNoValue = !it.requiresValue) }
      .forEach { result.add(it) }

    val contributedDirectives = mutableSetOf<String>()
    StreamEx.of(*VueAttributeNameParser.VueDirectiveKind.values())
      .filter {
        // 'on' should not be proposed without colon. It is added separately in VueTagAttributeCompletionProvider
        it !== VueAttributeNameParser.VueDirectiveKind.ON
        && it.directiveName != null
        && contributedDirectives.add(it.directiveName!!)
      }
      .map { VueAttributeDescriptor("v-" + it.directiveName!!, acceptsNoValue = !it.requiresValue) }
      .forEach { result.add(it) }

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
    val info = VueAttributeNameParser.parse(attributeName, context)
    when {
      info.kind == VueAttributeNameParser.VueAttributeKind.PLAIN -> {
        if (info.modifiers.isEmpty()) {
          return null
        }
        return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(info.name, context)
               ?: VueAttributeDescriptor(info.name, acceptsNoValue = true)
      }
      info is VueAttributeNameParser.VueDirectiveInfo -> {
        return when {
                 info.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND ->
                   info.arguments?.let { HtmlNSDescriptorImpl.getCommonAttributeDescriptor(it, context) }

                 info.directiveKind == VueAttributeNameParser.VueDirectiveKind.ON ->
                   info.arguments?.let { HtmlNSDescriptorImpl.getCommonAttributeDescriptor("on$it", context) }

                 info.directiveKind == VueAttributeNameParser.VueDirectiveKind.CUSTOM ->
                   return resolveDirective(info.name, context)

                 else -> null
               }
               ?: return VueAttributeDescriptor(attributeName, acceptsNoValue = !info.requiresValue)
      }
      else -> return VueAttributeDescriptor(attributeName, acceptsNoValue = !info.requiresValue)
    }
  }

  private fun resolveDirective(directiveName: String, context: XmlTag): VueAttributeDescriptor? {
    val searchName = fromAsset(directiveName)
    val directives = mutableListOf<VueDirective>()
    VueModelManager.findEnclosingContainer(context)?.acceptEntities(object : VueModelProximityVisitor() {
      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        return acceptSameProximity(proximity, fromAsset(name) == searchName) {
          directives.add(directive)
        }
      }
    }, VueModelVisitor.Proximity.GLOBAL)

    return directives.firstOrNull()?.let {
      VueAttributeDescriptor("v-" + fromAsset(it.defaultName ?: searchName), it.source, true)
    }
  }
}

@Suppress("DEPRECATION")
open class VueAttributeDescriptor(name: String,
                                  element: PsiElement? = null,
                                  acceptsNoValue: Boolean = false) :
  org.jetbrains.vuejs.codeInsight.VueAttributeDescriptor(name, element, acceptsNoValue)

// This class is the original `VueAttributeDescriptor` class,
// but it's renamed to allow instanceof check through deprecated class from 'codeInsight' package
@Deprecated("Public for internal purpose only!")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class _VueAttributeDescriptor(private val name: String,
                                   internal val element: PsiElement? = null,
                                   private val acceptsNoValue: Boolean = false) : BasicXmlAttributeDescriptor(), PsiPresentableMetaData {
  override fun getName(): String = name
  override fun getDeclaration(): PsiElement? = element
  override fun init(element: PsiElement?) {}
  override fun isRequired(): Boolean {
    if (name.startsWith(":") || name.startsWith("v-bind:")) return false
    // TODO use input prop definition model
    val initializer = (element as? JSProperty)?.objectLiteralExpressionInitializer ?: return false
    val literal = findProperty(initializer, "required")?.literalExpressionInitializer
    return literal != null && literal.isBooleanLiteral && "true" == literal.significantValue
  }

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

fun findProperty(obj: JSObjectLiteralExpression?, name: String): JSProperty? = obj?.properties?.find { it.name == name }
