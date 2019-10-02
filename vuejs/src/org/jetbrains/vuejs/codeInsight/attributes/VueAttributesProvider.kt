// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor.AttributePriority
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor.AttributePriority.LOW
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor.AttributePriority.NONE
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.model.*

class VueAttributesProvider : XmlAttributeDescriptorsProvider {

  override fun getAttributeDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    if (context == null || !isVueContext(context)) return emptyArray()
    val result = mutableListOf<XmlAttributeDescriptor>()

    StreamEx.of(*VueAttributeKind.values())
      .filter { it.attributeName != null && it.isValidIn(context) }
      .map {
        VueAttributeDescriptor(context, it.attributeName!!, acceptsNoValue = !it.requiresValue, priority = if (it.deprecated) NONE else LOW)
      }
      .forEach { result.add(it) }

    val contributedDirectives = mutableSetOf<String>()
    StreamEx.of(*VueDirectiveKind.values())
      .filter {
        // 'on' should not be proposed without colon. It is added separately in VueTagAttributeCompletionProvider
        it !== VueDirectiveKind.ON
        && it.directiveName != null
        && contributedDirectives.add(it.directiveName!!)
      }
      .map { VueAttributeDescriptor(context, "v-" + it.directiveName!!, acceptsNoValue = !it.requiresValue, priority = LOW) }
      .forEach { result.add(it) }

    VueModelManager.findEnclosingContainer(context)?.acceptEntities(object : VueModelVisitor() {
      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        if (contributedDirectives.add(name)) {
          result.add(VueAttributeDescriptor(context, "v-" + fromAsset(name), directive.source, true, AttributePriority.of(proximity)))
        }
        return true
      }
    }, VueModelVisitor.Proximity.GLOBAL)
    return result.toTypedArray()
  }

  override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
    if (context == null || !isVueContext(context) || attributeName == null) return null
    val info = VueAttributeNameParser.parse(attributeName, context)
    when {
      info.kind == VueAttributeKind.PLAIN -> {
        if (info.modifiers.isEmpty()) {
          return null
        }
        return HtmlNSDescriptorImpl.getCommonAttributeDescriptor(info.name, context)
               ?: VueAttributeDescriptor(context, info.name, acceptsNoValue = !info.requiresValue, priority = LOW)
      }
      info is VueAttributeNameParser.VueDirectiveInfo -> {
        return when {
                 info.isShorthand && info.arguments.isNullOrEmpty() -> return null

                 info.directiveKind == VueDirectiveKind.BIND ->
                   info.arguments?.let { HtmlNSDescriptorImpl.getCommonAttributeDescriptor(it, context) }

                 info.directiveKind == VueDirectiveKind.ON ->
                   info.arguments?.let { HtmlNSDescriptorImpl.getCommonAttributeDescriptor("on$it", context) }

                 info.directiveKind == VueDirectiveKind.SLOT -> {
                   val slotName = info.arguments ?: DEFAULT_SLOT_NAME
                   getAvailableSlots(context, true)
                     .find { it.name == slotName }
                     ?.let {
                       VueAttributeDescriptor(context, attributeName, it.source, true)
                     }
                 }

                 info.directiveKind == VueDirectiveKind.CUSTOM ->
                   return resolveDirective(info.name, context)

                 else -> null
               }
               ?: return VueAttributeDescriptor(context, attributeName, acceptsNoValue = !info.requiresValue, priority = LOW)
      }
      else -> return VueAttributeDescriptor(context, attributeName, acceptsNoValue = !info.requiresValue, priority = LOW)
    }
  }

  private fun resolveDirective(directiveName: String, context: XmlTag): VueAttributeDescriptor? {
    val searchName = fromAsset(directiveName)
    val directives = mutableListOf<VueDirective>()
    var minProximity = VueModelVisitor.Proximity.OUT_OF_SCOPE
    VueModelManager.findEnclosingContainer(context)?.acceptEntities(object : VueModelProximityVisitor() {
      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        return acceptSameProximity(proximity, fromAsset(name) == searchName) {
          directives.add(directive)
          minProximity = proximity
        }
      }
    }, VueModelVisitor.Proximity.GLOBAL)

    return directives.firstOrNull()?.let {
      VueAttributeDescriptor(context, "v-" + fromAsset(it.defaultName ?: searchName),
                             it.source,
                             true,
                             AttributePriority.of(minProximity))
    }
  }
}
