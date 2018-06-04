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
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.XmlPatterns.xmlAttribute
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import icons.VuejsIcons
import org.jetbrains.vuejs.index.hasVue

class VueAttributesCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, psiElement(XmlTokenType.XML_NAME).withParent(xmlAttribute()),
           VueEventAttrCompletionProvider())
  }
}

private class VueEventAttrCompletionProvider : CompletionProvider<CompletionParameters>() {
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
    private val EVENT_MODIFIERS = arrayOf("stop", "prevent", "capture", "self", "once", "passive")
  }

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    if (!hasVue(parameters.position.project)) return
    val attr = parameters.position.parent as? XmlAttribute ?: return
    val attrName = attr.name

    if (attrName.startsWith("v-on:") || attrName.startsWith("@")) {
      addEventCompletions(attr, result)
      return
    }

    if (attrName.startsWith("v-bind:") || attrName.startsWith(":")) {
      addBindCompletions(attr, result)
      return
    }

    val insertHandler = InsertHandler<LookupElement> { insertionContext, _ ->
      insertionContext.setLaterRunnable {
        CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(parameters.originalFile.project, parameters.editor)
      }
    }
    result.addElement(LookupElementBuilder.create("v-on:").withIcon(VuejsIcons.Vue).withInsertHandler(insertHandler))
    result.addElement(LookupElementBuilder.create("v-bind:").withIcon(VuejsIcons.Vue).withInsertHandler(insertHandler))
  }

  private fun addEventCompletions(attr: XmlAttribute, result: CompletionResultSet) {
    val prefix = result.prefixMatcher.prefix
    val lastDotIndex = prefix.lastIndexOf('.')
    if (lastDotIndex < 0) {
      val newResult = if (prefix == "v-on:") result.withPrefixMatcher("") else result
      addEventCompletions(attr.parent, newResult, if (prefix.startsWith("@")) "@" else "")
    }
    else {
      addModifierCompletions(result, prefix, lastDotIndex)
    }
  }

  private fun addEventCompletions(tag: XmlTag?, result: CompletionResultSet, prefix: String) {
    val descriptor = tag?.descriptor as? HtmlElementDescriptorImpl ?: HtmlNSDescriptorImpl.guessTagForCommonAttributes(tag) ?: return
    for (attrDescriptor in descriptor.getAttributesDescriptors(tag)) {
      val name = attrDescriptor.name
      if (name.startsWith("on")) {
        result.addElement(LookupElementBuilder
                            .create(prefix + name.substring("on".length))
                            .withInsertHandler(XmlAttributeInsertHandler.INSTANCE))
      }
    }
  }

  private fun addModifierCompletions(result: CompletionResultSet, prefix: String, lastDotIndex: Int) {
    val newResult = result.withPrefixMatcher(prefix.substring(lastDotIndex + 1))
    val usedModifiers = getUsedModifiers(prefix, lastDotIndex)

    doAddModifierCompletions(newResult, usedModifiers,
                             EVENT_MODIFIERS)

    if (isEventFromGroup(KEY_EVENTS, prefix)) {
      doAddModifierCompletions(newResult, usedModifiers,
                               KEY_MODIFIERS)
      // Do we also want to suggest the full list of https://vuejs.org/v2/guide/events.html#Automatic-Key-Modifiers?
    }

    if (isEventFromGroup(MOUSE_BUTTON_EVENTS, prefix)) {
      doAddModifierCompletions(newResult, usedModifiers,
                               MOUSE_BUTTON_MODIFIERS)
    }

    if (isEventFromGroup(KEY_EVENTS, prefix) || isEventFromGroup(
        MOUSE_EVENTS, prefix)) {
      doAddModifierCompletions(newResult, usedModifiers,
                               SYSTEM_MODIFIERS)
    }
  }

  private fun getUsedModifiers(prefix: String, lastDotIndex: Int): Collection<String> {
    val usedModifiers = SmartList<String>()
    var substring = prefix.substring(0, lastDotIndex)
    var dotIndex = substring.lastIndexOf('.')
    while (dotIndex > 0) {
      usedModifiers.add(substring.substring(dotIndex + 1))
      substring = substring.substring(0, dotIndex)
      dotIndex = substring.lastIndexOf('.')
    }
    return usedModifiers
  }

  private fun isEventFromGroup(eventGroup: Array<String>, prefix: String): Boolean {
    // in case of @click attribute prefix=="@click" but in case of v-on:click prefix=="click"
    val trimmedPrefix = StringUtil.trimStart(prefix, "@")
    return eventGroup.find { trimmedPrefix.startsWith("$it.") } != null
  }

  private fun doAddModifierCompletions(result: CompletionResultSet, usedModifiers: Collection<String>, modifiers: Array<String>) {
    modifiers.forEach {
      if (!usedModifiers.contains(it)) {
        result.addElement(LookupElementBuilder.create(it))
      }
    }
  }

  private fun addBindCompletions(attr: XmlAttribute, result: CompletionResultSet) {
    val prefix = result.prefixMatcher.prefix
    val newResult = if (prefix == "v-bind:") result.withPrefixMatcher("") else result
    val lookupItemPrefix = if (prefix.startsWith(":")) ":" else ""

    newResult.addElement(LookupElementBuilder.create(lookupItemPrefix + "is").withInsertHandler(XmlAttributeInsertHandler.INSTANCE))
    newResult.addElement(LookupElementBuilder.create(lookupItemPrefix + "key").withInsertHandler(XmlAttributeInsertHandler.INSTANCE))

    // v-bind:any-standard-attribute support
    val descriptors = (attr.parent?.descriptor as? HtmlElementDescriptorImpl)?.getDefaultAttributeDescriptors(attr.parent) ?: return
    for (attribute in descriptors) {
      newResult.addElement(LookupElementBuilder
                             .create(lookupItemPrefix + attribute.name)
                             .withInsertHandler(XmlAttributeInsertHandler.INSTANCE))
    }
  }
}

