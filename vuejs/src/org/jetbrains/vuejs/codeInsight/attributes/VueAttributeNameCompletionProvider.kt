// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.util.HtmlUtil
import icons.VuejsIcons
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor.AttributePriority.*
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind.*
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor
import org.jetbrains.vuejs.index.isVueContext
import org.jetbrains.vuejs.model.getAvailableSlots
import javax.swing.Icon

class VueAttributeNameCompletionProvider : CompletionProvider<CompletionParameters>() {
  companion object {
    // https://vuejs.org/v2/guide/events.html#Key-Modifiers
    private val KEY_MODIFIERS = arrayOf("enter", "tab", "delete", "esc", "space", "up", "down", "left", "right")
    // KEY_MODIFIERS are applicable only for the KEY_EVENTS
    private val KEY_EVENTS = arrayOf("keydown", "keypress", "keyup")
    // https://vuejs.org/v2/guide/events.html#Mouse-Button-Modifiers
    private val MOUSE_BUTTON_MODIFIERS = arrayOf("left", "right", "middle")
    // MOUSE_BUTTON_MODIFIERS are applicable only for the MOUSE_BUTTON_EVENTS
    private val MOUSE_BUTTON_EVENTS = arrayOf("click", "dblclick", "mousedown", "mouseup")
    // https://vuejs.org/v2/guide/events.html#System-Modifier-Keys
    private val SYSTEM_MODIFIERS = arrayOf("ctrl", "alt", "shift", "meta", "exact")
    // SYSTEM_MODIFIERS are applicable only for the KEY_EVENTS and all MOUSE_EVENTS
    private val MOUSE_EVENTS = arrayOf("click", "contextmenu", "dblclick", "mousedown", "mouseenter", "mouseleave", "mousemove", "mouseout",
                                       "mouseover", "mouseup", "show", "drag", "dragend", "dragenter", "dragleave", "dragover", "dragstart",
                                       "drop")
    // https://vuejs.org/v2/guide/events.html#Event-Modifiers
    private val EVENT_MODIFIERS = arrayOf("stop", "prevent", "capture", "self", "once", "passive", "native")

    private fun getDefaultHtmlAttributes(context: XmlTag?): Array<out XmlAttributeDescriptor> =
      ((HtmlNSDescriptorImpl.guessTagForCommonAttributes(context) as? HtmlElementDescriptorImpl)
         ?.getDefaultAttributeDescriptors(context) ?: emptyArray())
  }

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    if (!isVueContext(parameters.position)) return

    val attr = parameters.position.parent as? XmlAttribute ?: return
    addAttributeDescriptorCompletions(attr, parameters, result)

    val attrInfo = VueAttributeNameParser.parse(StringUtil.trimEnd(attr.name, CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED), null)
    when ((attrInfo as? VueDirectiveInfo)?.directiveKind) {
      ON -> {
        addEventCompletions(attr, attrInfo, result)
        return
      }
      BIND -> {
        addBindCompletions(attr, result)
        return
      }
      SLOT -> {
        addSlotCompletions(attr, result)
        return
      }
      else -> {
      }
    }

    val insertHandler = InsertHandler<LookupElement> { insertionContext, _ ->
      insertionContext.setLaterRunnable {
        CodeCompletionHandlerBase(
          CompletionType.BASIC).invokeCompletion(parameters.originalFile.project, parameters.editor)
      }
    }
    result.addElement(lookupElement("v-on:", priority = LOW, insertHandler = insertHandler))
    result.addElement(lookupElement("v-bind:", priority = LOW, insertHandler = insertHandler))
    result.addElement(lookupElement("v-slot:", priority = LOW, insertHandler = insertHandler))
  }

  private fun addAttributeDescriptorCompletions(attr: XmlAttribute, parameters: CompletionParameters, result: CompletionResultSet) {
    val tag = attr.parent ?: return
    val providedAttributes = mutableSetOf<String>()
    (tag.descriptor ?: return).getAttributesDescriptors(tag)
      .filterIsInstance<VueAttributeDescriptor>()
      .forEach {
        if (providedAttributes.add(it.name)) {
          var insertHandler = XmlAttributeInsertHandler.INSTANCE
          if (HtmlUtil.isShortNotationOfBooleanAttributePreferred() &&
              HtmlUtil.isBooleanAttribute(it, tag)) {
            insertHandler = null
          }
          result.addElement(lookupElement(it.name, it.priority, insertHandler = insertHandler))
        }
      }

    result.runRemainingContributors(parameters) { toPass ->
      for (str in toPass.lookupElement.allLookupStrings) {
        if (!providedAttributes.add(str)) {
          return@runRemainingContributors
        }
      }
      result.withPrefixMatcher(toPass.prefixMatcher)
        .withRelevanceSorter(toPass.sorter)
        .addElement(toPass.lookupElement)
    }
  }

  private fun addEventCompletions(attr: XmlAttribute, attrInfo: VueDirectiveInfo, result: CompletionResultSet) {
    if (attrInfo.modifiers.isEmpty()) {
      val prefix = result.prefixMatcher.prefix
      val newResult = if (prefix == "v-on:") result.withPrefixMatcher("") else result
      addEventCompletions(attr.parent, newResult, if (prefix.startsWith("@")) "@" else "")
    }
    else {
      addModifierCompletions(result, attrInfo)
    }
  }

  private fun addEventCompletions(tag: XmlTag?, result: CompletionResultSet, prefix: String) {
    (tag?.descriptor as? VueElementDescriptor)?.getEmitCalls()?.forEach { emit ->
      result.addElement(lookupElement(prefix + emit.name))
    }

    val descriptor = tag?.descriptor as? HtmlElementDescriptorImpl
                     ?: HtmlNSDescriptorImpl.guessTagForCommonAttributes(tag)
                     ?: return
    for (attrDescriptor in descriptor.getAttributesDescriptors(tag)) {
      val name = attrDescriptor.name
      if (name.startsWith("on")) {
        result.addElement(lookupElement(prefix + name.substring("on".length), priority = LOW, icon = null))
      }
    }
  }

  private fun addModifierCompletions(result: CompletionResultSet, attrInfo: VueDirectiveInfo) {
    val prefix = result.prefixMatcher.prefix
    val lastDotIndex = prefix.lastIndexOf('.')
    if (lastDotIndex < 0) return

    val newResult = result.withPrefixMatcher(prefix.substring(lastDotIndex + 1))
    val usedModifiers = attrInfo.modifiers

    doAddModifierCompletions(newResult, usedModifiers, EVENT_MODIFIERS)

    val eventName = attrInfo.arguments ?: return
    if (KEY_EVENTS.contains(eventName)) {
      doAddModifierCompletions(newResult, usedModifiers, KEY_MODIFIERS)
      // Do we also want to suggest the full list of https://vuejs.org/v2/guide/events.html#Automatic-Key-Modifiers?
    }

    if (MOUSE_BUTTON_EVENTS.contains(eventName)) {
      doAddModifierCompletions(newResult, usedModifiers, MOUSE_BUTTON_MODIFIERS)
    }

    if (KEY_EVENTS.contains(eventName) || MOUSE_EVENTS.contains(eventName)) {
      doAddModifierCompletions(newResult, usedModifiers, SYSTEM_MODIFIERS)
    }
  }

  private fun doAddModifierCompletions(result: CompletionResultSet, usedModifiers: Collection<String>, modifiers: Array<String>) {
    modifiers.forEach {
      if (!usedModifiers.contains(it)) {
        result.addElement(lookupElement(it, insertHandler = null))
      }
    }
  }

  private fun addBindCompletions(attr: XmlAttribute, result: CompletionResultSet) {
    val prefix = result.prefixMatcher.prefix
    val newResult = if (prefix == "v-bind:") result.withPrefixMatcher("") else result
    val lookupItemPrefix = if (prefix.startsWith(":")) ":" else ""

    // special binding
    newResult.addElement(lookupElement(lookupItemPrefix + "key", priority = LOW))

    // v-bind:any-standard-attribute support
    for (attribute in getDefaultHtmlAttributes(attr.parent)) {
      if (!attribute.name.startsWith("on")) {
        newResult.addElement(lookupElement(lookupItemPrefix + attribute.name, priority = LOW, icon = null))
      }
    }

    for (attribute in (attr.parent?.descriptor as? VueElementDescriptor)?.getProps() ?: return) {
      newResult.addElement(lookupElement(lookupItemPrefix + attribute.name, priority = HIGH))
    }
  }

  private fun addSlotCompletions(attr: XmlAttribute, result: CompletionResultSet) {
    val prefix = result.prefixMatcher.prefix
    val newResult = if (prefix == "v-slot:") result.withPrefixMatcher("") else result
    val lookupItemPrefix = if (prefix.startsWith("#")) "#" else ""
    for (slot in getAvailableSlots(attr, true)) {
      newResult.addElement(lookupElement(lookupItemPrefix + slot.name, priority = HIGH))
    }
  }

  private fun lookupElement(name: String,
                            priority: VueAttributeDescriptor.AttributePriority = NORMAL,
                            insertHandler: InsertHandler<LookupElement>? = XmlAttributeInsertHandler.INSTANCE,
                            icon: Icon? = VuejsIcons.Vue): LookupElement {
    return PrioritizedLookupElement.withPriority(LookupElementBuilder.create(name)
                                                   .withIcon(icon)
                                                   .withBoldness(priority === HIGH)
                                                   .withInsertHandler(insertHandler),
                                                 priority.value)
  }

}
