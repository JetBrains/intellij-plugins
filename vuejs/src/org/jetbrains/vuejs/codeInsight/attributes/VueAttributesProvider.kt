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
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelProximityVisitor
import org.jetbrains.vuejs.model.VueModelVisitor

class VueAttributesProvider : XmlAttributeDescriptorsProvider {

  override fun getAttributeDescriptors(context: XmlTag?): Array<out XmlAttributeDescriptor> {
    if (context == null || !org.jetbrains.vuejs.index.isVueContext(context)) return emptyArray()
    val result = mutableListOf<XmlAttributeDescriptor>()

    StreamEx.of(*VueAttributeNameParser.VueAttributeKind.values())
      .filter { it.attributeName != null && it.isValidIn(context) }
      .map { VueAttributeDescriptor(it.attributeName!!, acceptsNoValue = !it.requiresValue, priority = if (it.deprecated) NONE else LOW) }
      .forEach { result.add(it) }

    val contributedDirectives = mutableSetOf<String>()
    StreamEx.of(*VueAttributeNameParser.VueDirectiveKind.values())
      .filter {
        // 'on' should not be proposed without colon. It is added separately in VueTagAttributeCompletionProvider
        it !== VueAttributeNameParser.VueDirectiveKind.ON
        && it.directiveName != null
        && contributedDirectives.add(it.directiveName!!)
      }
      .map { VueAttributeDescriptor("v-" + it.directiveName!!, acceptsNoValue = !it.requiresValue, priority = LOW) }
      .forEach { result.add(it) }

    VueModelManager.findEnclosingContainer(context)?.acceptEntities(object : VueModelVisitor() {
      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        if (contributedDirectives.add(name)) {
          result.add(VueAttributeDescriptor("v-" + fromAsset(name), directive.source, true, AttributePriority.of(proximity)))
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
               ?: VueAttributeDescriptor(info.name, acceptsNoValue = true, priority = LOW)
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
               ?: return VueAttributeDescriptor(attributeName, acceptsNoValue = !info.requiresValue, priority = LOW)
      }
      else -> return VueAttributeDescriptor(attributeName, acceptsNoValue = !info.requiresValue, priority = LOW)
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
      VueAttributeDescriptor("v-" + fromAsset(it.defaultName ?: searchName),
                             it.source,
                             true,
                             AttributePriority.of(minProximity))
    }
  }
}
