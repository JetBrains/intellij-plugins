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
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor.AttributePriority.*
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind.*
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributesProvider.Companion.findAttributeDescriptor
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueDirectiveModifier
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
      (context?.descriptor as? HtmlElementDescriptorImpl
       ?: HtmlNSDescriptorImpl.guessTagForCommonAttributes(context) as? HtmlElementDescriptorImpl)
        ?.getDefaultAttributeDescriptors(context) ?: emptyArray()

    private val SPECIAL_CHARS = setOf('[', '.', '\\', '^', '$', '(', '+')
    private val SPECIAL_CHARS_ONE_BACK = setOf('?', '*', '{')

    fun getPatternCompletablePrefix(pattern: Regex?): String {
      val patternStr = pattern?.pattern ?: return ""
      if (patternStr.contains('|')) return ""
      for (i in 0..patternStr.length) {
        val char = patternStr[i]
        if (SPECIAL_CHARS.contains(char)) {
          return patternStr.substring(0 until i)
        }
        else if (SPECIAL_CHARS_ONE_BACK.contains(char)) {
          return if (i < 1) "" else patternStr.substring(0 until i - 1)
        }
      }
      return patternStr
    }
  }

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet) {
    if (!isVueContext(parameters.position)) return

    val attr = parameters.position.parent as? XmlAttribute ?: return
    val argumentsInsertHandler = InsertHandler<LookupElement> { insertionContext, _ ->
      insertionContext.setLaterRunnable {
        CodeCompletionHandlerBase(
          CompletionType.BASIC).invokeCompletion(parameters.originalFile.project, parameters.editor)
      }
    }

    val attrInfo = VueAttributeNameParser.parse(StringUtil.trimEnd(attr.name, CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED), attr.parent)

    val providedAttributes = attr.parent?.attributes?.asSequence()
                               ?.filter { it != attr }
                               ?.flatMap { getAliases(it) }
                               ?.toMutableSet() ?: mutableSetOf()
    val collector = CompletionCollector(parameters, resultSet, providedAttributes)
    addTagDescriptorCompletions(attr, attrInfo, collector, argumentsInsertHandler)
    if (attrInfo is VueDirectiveInfo) {
      val directive: VueDirective? = when (attrInfo.directiveKind) {
        ON, BIND, SLOT -> findAttributeDescriptor(ATTR_DIRECTIVE_PREFIX + fromAsset(attrInfo.name), attrInfo.name, attr.parent)
        else -> (attr.descriptor as? VueAttributeDescriptor)
      }?.getSources()?.getOrNull(0) as? VueDirective

      if (attrInfo.modifiers.isNotEmpty()) {
        addModifierCompletions(resultSet, directive, attrInfo)
      }
      else {
        when (attrInfo.directiveKind) {
          ON -> {
            addEventCompletions(attr, collector)
            return
          }
          BIND -> {
            addBindCompletions(attr, collector)
            return
          }
          SLOT -> {
            addSlotCompletions(attr, collector)
            return
          }
          else -> {
            val argument = directive?.argument
            val argumentPrefix = getPatternCompletablePrefix(argument?.pattern)
            if (attrInfo.arguments != null && argumentPrefix.isNotBlank()) {
              val prefix = collector.prefix
              val newResult = if (prefix == ATTR_DIRECTIVE_PREFIX + attrInfo.name + ATTR_ARGUMENT_PREFIX) resultSet.withPrefixMatcher("")
              else resultSet
              newResult.addElement(lookupElement(argumentPrefix, argument,
                                                 typeText = argument!!.pattern?.toString(), insertHandler = null))
            }
          }
        }
      }
    }

    for (kind in listOf(ON, BIND, SLOT)) {
      val attrName = ATTR_DIRECTIVE_PREFIX + kind.directiveName + ATTR_ARGUMENT_PREFIX
      collector.contributeAttribute(lookupElement(attrName, null, priority = LOW, insertHandler = argumentsInsertHandler))
    }
  }

  private fun getAliases(attr: XmlAttribute): Sequence<String> {
    val info = VueAttributeNameParser.parse(attr.name, attr.parent)
    val result = mutableSetOf(attr.name)
    if (info is VueDirectiveInfo) {
      val descriptor = attr.descriptor as? VueAttributeDescriptor
      when (info.directiveKind) {
        ON -> return emptySequence()
        BIND -> {
          if (info.arguments != null) {
            result.add(ATTR_ARGUMENT_PREFIX + info.arguments)
            result.add(ATTR_DIRECTIVE_PREFIX + info.name + ATTR_ARGUMENT_PREFIX + info.arguments)
          } else {
            return emptySequence()
          }
        }
        SLOT -> {
          result.add(ATTR_SLOT_SHORTHAND.toString())
          result.add(ATTR_DIRECTIVE_PREFIX + info.name)
          result.add(ATTR_DIRECTIVE_PREFIX + info.name + ATTR_ARGUMENT_PREFIX)
        }
        else -> {
          val directive = descriptor?.getSources()?.getOrNull(0) as? VueDirective
          if (directive?.argument == null) {
            result.add(ATTR_DIRECTIVE_PREFIX + info.name)
          }
        }
      }
    }
    else {
      result.add(info.name)
    }
    return result.asSequence()
  }

  private fun addTagDescriptorCompletions(attr: XmlAttribute,
                                          attrInfo: VueAttributeNameParser.VueAttributeInfo,
                                          collector: CompletionCollector,
                                          argumentsInsertHandler: InsertHandler<LookupElement>) {
    val proposeWithArg = attrInfo.modifiers.isEmpty() && (attrInfo as? VueDirectiveInfo)?.arguments == null
    val tag = attr.parent ?: return
    (tag.descriptor ?: return).getAttributesDescriptors(tag)
      .filterIsInstance<VueAttributeDescriptor>()
      .forEach {
        collector.contributeAttribute(it.name) { addAttribute ->
          val directive = it.getSources().getOrNull(0) as? VueDirective
          if (directive?.argument != null && proposeWithArg) {
            val priority = if (directive.argument?.required != true) LOW else it.priority
            collector.contributeAttribute(lookupElement(it.name + ATTR_ARGUMENT_PREFIX, it, priority = priority,
                                                        insertHandler = argumentsInsertHandler))
          }
          if (directive?.argument?.required != true) {
            var insertHandler = XmlAttributeInsertHandler.INSTANCE
            if ((HtmlUtil.isShortNotationOfBooleanAttributePreferred() &&
                 HtmlUtil.isBooleanAttribute(it, tag))
                || directive?.modifiers?.isNotEmpty() == true) {
              insertHandler = null
            }
            addAttribute(lookupElement(it.name, it, priority = it.priority, insertHandler = insertHandler))
          }
        }
      }
    collector.runRemainingContributors()
  }

  private fun addModifierCompletions(result: CompletionResultSet, directive: VueDirective?, attrInfo: VueDirectiveInfo) {
    val prefix = result.prefixMatcher.prefix
    val lastDotIndex = prefix.lastIndexOf(ATTR_MODIFIER_PREFIX)
    if (lastDotIndex < 0) return

    val newResult = result.withPrefixMatcher(prefix.substring(lastDotIndex + 1))

    if (attrInfo.directiveKind == ON) {
      addEventModifierCompletions(newResult, directive, attrInfo)
    }
    else if (directive != null) {
      doAddModifierCompletions(newResult, attrInfo.modifiers, directive.modifiers)
    }
  }

  private fun addEventModifierCompletions(result: CompletionResultSet, directive: VueDirective?, attrInfo: VueDirectiveInfo) {
    val usedModifiers = attrInfo.modifiers
    val documentedModifiers = directive?.modifiers?.associateBy { it.name } ?: mapOf()
    if (directive != null) {
      val toSkip = KEY_MODIFIERS.toMutableSet()
      toSkip.addAll(MOUSE_BUTTON_MODIFIERS)
      toSkip.addAll(SYSTEM_MODIFIERS)
      toSkip.addAll(usedModifiers)
      doAddModifierCompletions(result, toSkip, directive.modifiers)
    }
    else {
      doAddModifierCompletions(result, usedModifiers, documentedModifiers, EVENT_MODIFIERS)
    }

    val eventName = attrInfo.arguments ?: return
    if (KEY_EVENTS.contains(eventName)) {
      doAddModifierCompletions(result, usedModifiers, documentedModifiers, KEY_MODIFIERS)
      // Do we also want to suggest the full list of https://vuejs.org/v2/guide/events.html#Automatic-Key-Modifiers?
    }

    if (MOUSE_BUTTON_EVENTS.contains(eventName)) {
      doAddModifierCompletions(result, usedModifiers, documentedModifiers, MOUSE_BUTTON_MODIFIERS)
    }

    if (KEY_EVENTS.contains(eventName) || MOUSE_EVENTS.contains(eventName)) {
      doAddModifierCompletions(result, usedModifiers, documentedModifiers, SYSTEM_MODIFIERS)
    }
  }

  private fun doAddModifierCompletions(result: CompletionResultSet, usedModifiers: Collection<String>,
                                       documentedModifiers: Map<String, VueDirectiveModifier>,
                                       modifiers: Array<String>) {
    modifiers.forEach {
      if (!usedModifiers.contains(it)) {
        result.addElement(lookupElement(it, documentedModifiers[it] ?: FakeModifier(it), priority = NORMAL, insertHandler = null))
      }
    }
  }

  private fun doAddModifierCompletions(result: CompletionResultSet,
                                       usedModifiers: Collection<String>,
                                       modifiers: List<VueDirectiveModifier>) {
    modifiers.forEach {
      if (it.pattern != null) {
        val prefix = getPatternCompletablePrefix(it.pattern!!)
        if (prefix.isNotBlank()) {
          result.addElement(lookupElement(prefix, it, insertHandler = null,
                                          presentableText = it.name, typeText = it.pattern?.toString()))
        }
      }
      else if (!usedModifiers.contains(it.name)) {
        result.addElement(lookupElement(it.name, it, insertHandler = null))
      }
    }
  }

  private fun addEventCompletions(attr: XmlAttribute, collector: CompletionCollector) {
    val originalPrefix = collector.prefix
    val newCollector = if (originalPrefix == "v-on:") collector.withPrefixMatcher("") else collector
    val tag = attr.parent
    val prefix = if (originalPrefix.startsWith(ATTR_EVENT_SHORTHAND)) ATTR_EVENT_SHORTHAND.toString() else ""

    (tag?.descriptor as? VueElementDescriptor)?.getEmitCalls()?.forEach { emit ->
      newCollector.contributeAttribute("v-on:${emit.name}") { addAttribute ->
        addAttribute(lookupElement(prefix + emit.name, emit))
      }
    }

    getDefaultHtmlAttributes(tag).asSequence()
      .filter { it.name.startsWith("on") }
      .forEach {
        val eventName = it.name.substring(2)
        newCollector.contributeAttribute("v-on:$eventName") { addAttribute ->
          addAttribute(lookupElement(prefix + eventName, it, priority = LOW, icon = null))
        }
      }
  }

  private fun addBindCompletions(attr: XmlAttribute, collector: CompletionCollector) {
    val prefix = collector.prefix
    val newCollector = if (prefix == "v-bind:") collector.withPrefixMatcher("") else collector
    val lookupItemPrefix = if (prefix.startsWith(ATTR_ARGUMENT_PREFIX)) ATTR_ARGUMENT_PREFIX.toString() else ""

    (attr.parent?.descriptor as? VueElementDescriptor)
      ?.getProps()
      ?.forEach { attribute ->
        newCollector.contributeAttribute("v-bind:${attribute.name}") { addAttribute ->
          addAttribute(lookupElement(lookupItemPrefix + attribute.name, attribute, priority = HIGH))
        }
      }

    // special binding
    newCollector.contributeAttribute("v-bind:key") { addAttribute ->
      addAttribute(lookupElement(lookupItemPrefix + "key", null, priority = LOW))
    }

    // v-bind:any-standard-attribute support
    getDefaultHtmlAttributes(attr.parent).asSequence()
      .filter { it.getName(attr).let { name -> !name.startsWith("on") && !name.contains(':') } }
      .forEach {
        val name = it.getName(attr)
        newCollector.contributeAttribute("v-bind:$name") { addAttribute ->
          addAttribute(lookupElement(lookupItemPrefix + name, it, priority = LOW, icon = null))
        }
      }
  }

  private fun addSlotCompletions(attr: XmlAttribute, collector: CompletionCollector) {
    val prefix = collector.prefix
    val newCollector = if (prefix == "v-slot:") collector.withPrefixMatcher("") else collector
    val lookupItemPrefix = if (prefix.startsWith("#")) "#" else ""
    for (slot in getAvailableSlots(attr, true)) {
      // TODO provide insert handler for scoped slots
      if (slot.pattern != null) {
        val patternPrefix = getPatternCompletablePrefix(slot.pattern!!)
        if (patternPrefix.isNotBlank()) {
          newCollector.contributeAttribute("v-slot:$patternPrefix") { addAttribute ->
            addAttribute(lookupElement(patternPrefix, slot, insertHandler = null,
                                       presentableText = slot.name, priority = HIGH,
                                       typeText = slot.pattern?.toString()))
          }
        }
      }
      else {
        newCollector.contributeAttribute("v-slot:${slot.name}") { addAttribute ->
          addAttribute(lookupElement(lookupItemPrefix + slot.name, slot, priority = HIGH, insertHandler = null))
        }
      }
    }
  }

  private fun lookupElement(name: String,
                            source: Any?,
                            priority: VueAttributeDescriptor.AttributePriority = NORMAL,
                            insertHandler: InsertHandler<LookupElement>? = XmlAttributeInsertHandler.INSTANCE,
                            icon: Icon? = VuejsIcons.Vue,
                            presentableText: String? = null,
                            typeText: String? = null): LookupElement {
    val lookupObject = when (source) {
                         is VueAttributeDescriptor -> source.getSources().getOrNull(0)?.documentation?.let { Pair(it, null) }
                         is VueDocumentedItem -> Pair(source.documentation, null)
                         else -> source
                       } ?: name
    return PrioritizedLookupElement.withPriority(LookupElementBuilder.create(lookupObject, name)
                                                   .withIcon(icon)
                                                   .withPresentableText(presentableText ?: name)
                                                   .withTypeText(typeText, true)
                                                   .withBoldness(priority === HIGH)
                                                   .withInsertHandler(insertHandler),
                                                 priority.value)
  }

  private class FakeModifier(override val name: String) : VueDirectiveModifier

  private open class PrefixedCompletionCollector(protected val resultSet: CompletionResultSet,
                                                 protected val providedAttributes: MutableSet<String>) {
    fun contributeAttribute(name: String, runnable: (consumer: (lookupElement: LookupElement) -> Unit) -> Unit) {
      if (providedAttributes.add(name)) {
        runnable(resultSet::addElement)
      }
    }

    fun withPrefixMatcher(prefix: String): PrefixedCompletionCollector =
      PrefixedCompletionCollector(resultSet.withPrefixMatcher(prefix), providedAttributes)
  }

  private class CompletionCollector(private val parameters: CompletionParameters,
                                    resultSet: CompletionResultSet,
                                    providedAttributes: MutableSet<String>) :
    PrefixedCompletionCollector(resultSet, providedAttributes) {

    val prefix get() = resultSet.prefixMatcher.prefix

    fun contributeAttribute(lookupElement: LookupElement) {
      if (providedAttributes.add(lookupElement.lookupString)) {
        resultSet.addElement(lookupElement)
      }
    }

    fun runRemainingContributors() {
      resultSet.runRemainingContributors(parameters) { toPass ->
        for (str in toPass.lookupElement.allLookupStrings) {
          if (!providedAttributes.add(str)) {
            @Suppress("LABEL_NAME_CLASH")
            return@runRemainingContributors
          }
        }
        resultSet.withPrefixMatcher(toPass.prefixMatcher)
          .withRelevanceSorter(toPass.sorter)
          .addElement(toPass.lookupElement)
      }
    }
  }
}
