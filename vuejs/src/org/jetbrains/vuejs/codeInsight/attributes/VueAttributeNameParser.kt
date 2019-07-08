// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.psi.xml.XmlTag
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueAttributeNameParser {
  companion object {
    fun parse(attributeName: String, context: XmlTag?): VueAttributeInfo {
      if (attributeName.isEmpty()) return VueAttributeInfo("", VueAttributeKind.PLAIN)
      val name: String
      val kind: VueDirectiveKind
      var paramsPos: Int
      if (attributeName.startsWith('@')) {
        name = "on"
        kind = VueDirectiveKind.ON
        paramsPos = 1
      }
      else if (attributeName.startsWith(':')) {
        name = "bind"
        kind = VueDirectiveKind.BIND
        paramsPos = 1
      }
      else if (attributeName.startsWith("v-") && attributeName.length > 2) {
        var nameEnd = attributeName.indexOfFirst { it == '.' || it == ':' }
        if (nameEnd < 0) {
          nameEnd = attributeName.length
        }
        name = attributeName.substring(2, nameEnd)
        kind = directiveKindMap[name] ?: VueDirectiveKind.CUSTOM
        paramsPos = nameEnd
        if (paramsPos < attributeName.length && attributeName[paramsPos] == ':')
          paramsPos++
      }
      else {
        var nameEnd = attributeName.indexOf('.')
        if (nameEnd < 0) {
          nameEnd = attributeName.length
        }
        name = attributeName.substring(0, nameEnd)
        val attributeKind = attributeKindMap[name]?.let { if (it.isValidIn(context)) it else VueAttributeKind.PLAIN }
                            ?: VueAttributeKind.PLAIN
        return VueAttributeInfo(name, attributeKind, parseModifiers(attributeName, nameEnd))
      }
      if (paramsPos >= attributeName.length) {
        return VueDirectiveInfo(name, kind)
      }

      val arguments: String?
      val lastBracket = attributeName.lastIndexOf(']')
      if (attributeName[paramsPos] == '[' && lastBracket > 0) {
        arguments = attributeName.substring(paramsPos, lastBracket + 1)
        paramsPos = lastBracket + 1
      }
      else {
        val firstDot = attributeName.indexOf('.', paramsPos)
        if (firstDot > 0) {
          arguments = attributeName.substring(paramsPos, firstDot)
          paramsPos = firstDot
        }
        else {
          arguments = attributeName.substring(paramsPos)
          paramsPos = attributeName.length
        }
      }
      return VueDirectiveInfo(name, kind, if (arguments.isEmpty()) null else arguments, parseModifiers(attributeName, paramsPos))
    }

    private fun parseModifiers(modifiers: String, startPos: Int): Set<String> {
      if (startPos + 1 >= modifiers.length || modifiers[startPos] != '.') {
        return emptySet()
      }
      val result = mutableSetOf<String>()
      var currentIndex = startPos
      var prevDot = startPos
      while (++currentIndex < modifiers.length) {
        if (modifiers[currentIndex] == '.') {
          if (prevDot + 1 < currentIndex) {
            result.add(modifiers.substring(prevDot + 1, currentIndex))
          }
          prevDot = currentIndex
        }
      }
      if (prevDot + 1 < modifiers.length) {
        result.add(modifiers.substring(prevDot + 1))
      }
      return result
    }

    private val attributeKindMap = StreamEx.of(*VueAttributeKind.values())
      .mapToEntry({ it.attributeName }, { it })
      .nonNullKeys()
      .toMap()

    private val directiveKindMap = StreamEx.of(*VueDirectiveKind.values())
      .mapToEntry({ it.directiveName }, { it })
      .nonNullKeys()
      .toMap()
  }

  open class VueAttributeInfo internal constructor(val name: String,
                                                   val kind: VueAttributeKind,
                                                   val modifiers: Set<String> = emptySet()) {

    open val requiresValue: Boolean get() = kind.requiresValue && modifiers.isEmpty()

    open val injectJS: Boolean get() = kind.injectJS
  }

  class VueDirectiveInfo internal constructor(name: String,
                                              val directiveKind: VueDirectiveKind,
                                              val arguments: String? = null,
                                              modifiers: Set<String> = emptySet()) : VueAttributeInfo(name,
                                                                                                      VueAttributeKind.DIRECTIVE,
                                                                                                      modifiers) {
    override val requiresValue: Boolean
      get() {
        return directiveKind.requiresValue && modifiers.isEmpty()
      }

    override val injectJS: Boolean get() = directiveKind.injectJS

  }

  enum class VueAttributeKind(val attributeName: String?,
                              val injectJS: Boolean = false,
                              val requiresValue: Boolean = true) {
    PLAIN(null, requiresValue = true),
    DIRECTIVE(null),
    SLOT("slot"),
    REF("ref"),
    SLOT_SCOPE("slot-scope", injectJS = true),
    STYLE_SCOPED("scoped", requiresValue = false),
    STYLE_MODULE("module", requiresValue = false),
    STYLE_SRC("src"),
    TEMPLATE_FUNCTIONAL("functional", requiresValue = false);

    fun isValidIn(context: XmlTag?): Boolean {
      when {
        this === TEMPLATE_FUNCTIONAL -> return context != null && isTopLevelTemplateTag(context)
        this === STYLE_SRC || this === STYLE_SCOPED || this === STYLE_MODULE -> return context != null && isTopLevelStyleTag(context)
        else -> return true
      }
    }

    private fun isTopLevelStyleTag(tag: XmlTag): Boolean = tag.parentTag == null &&
                                                           tag.name == "style" &&
                                                           tag.containingFile?.language == VueLanguage.INSTANCE

    private fun isTopLevelTemplateTag(tag: XmlTag): Boolean = tag.parentTag == null &&
                                                              tag.name == "template" &&
                                                              tag.containingFile?.language == VueLanguage.INSTANCE
  }

  enum class VueDirectiveKind(private val hasName: Boolean = true,
                              val injectJS: Boolean = true, val requiresValue: Boolean = true) {
    CUSTOM(hasName = false, requiresValue = false),
    BIND,
    ON,
    CLOAK(injectJS = false, requiresValue = false),
    ELSE(injectJS = false, requiresValue = false),
    ELSE_IF,
    FOR,
    HTML,
    IF,
    MODEL,
    ONCE(injectJS = false, requiresValue = false),
    PRE(injectJS = false, requiresValue = false),
    SHOW,
    SLOT(requiresValue = false, injectJS = false /* until supports for slots is done */),
    TEXT;

    val directiveName get() = if (hasName) name.toLowerCase().replace('_', '-') else null
  }
}
