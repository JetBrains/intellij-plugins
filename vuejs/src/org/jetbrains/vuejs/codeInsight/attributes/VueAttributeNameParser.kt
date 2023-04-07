// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.MultiMap
import com.intellij.xml.util.HtmlUtil.*
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.model.DEPRECATED_SLOT_ATTRIBUTE
import org.jetbrains.vuejs.model.SLOT_NAME_ATTRIBUTE
import org.jetbrains.vuejs.model.SLOT_TAG_NAME
import java.util.*

class VueAttributeNameParser private constructor() {
  companion object {
    fun parse(attributeName: CharSequence, context: String? = null, isTopLevel: Boolean = false): VueAttributeInfo {
      return parse(attributeName) { it.isValidIn(context, isTopLevel) }
    }

    fun parse(attributeName: CharSequence, context: XmlTag): VueAttributeInfo {
      return parse(attributeName) { it.isValidIn(context) }
    }

    private fun parse(attributeName: CharSequence, isValid: (VueAttributeKind) -> Boolean): VueAttributeInfo {
      if (attributeName.length == 0) {
        return VueAttributeInfo("", VueAttributeKind.PLAIN)
      }

      val name: String
      val kind: VueDirectiveKind
      var paramsPos: Int
      var isShorthand = false
      if (attributeName.startsWith(ATTR_EVENT_SHORTHAND)) {
        name = "on"
        kind = VueDirectiveKind.ON
        paramsPos = 0
        isShorthand = true
      }
      else if (attributeName.startsWith(ATTR_ARGUMENT_PREFIX)) {
        name = "bind"
        kind = VueDirectiveKind.BIND
        paramsPos = 0
        isShorthand = true
      }
      else if (attributeName.startsWith(ATTR_SLOT_SHORTHAND)) {
        name = "slot"
        kind = VueDirectiveKind.SLOT
        paramsPos = 0
        isShorthand = true
      }
      else if (attributeName.startsWith(ATTR_DIRECTIVE_PREFIX) && attributeName.length > 2) {
        var nameEnd = attributeName.indexOfFirst { it == ATTR_MODIFIER_PREFIX || it == ATTR_ARGUMENT_PREFIX }
        if (nameEnd < 0) {
          nameEnd = attributeName.length
        }
        name = attributeName.substring(2, nameEnd)
        kind = directiveKindMap[name] ?: VueDirectiveKind.CUSTOM
        paramsPos = nameEnd
      }
      else {
        var nameEnd = attributeName.indexOf(ATTR_MODIFIER_PREFIX)
        if (nameEnd < 0) {
          nameEnd = attributeName.length
        }
        name = attributeName.substring(0, nameEnd)
        val attributeKind = attributeKindMap.get(name).find(isValid) ?: VueAttributeKind.PLAIN
        return VueAttributeInfo(name, attributeKind, parseModifiers(attributeName, nameEnd))
      }
      if (paramsPos >= attributeName.length
          || (attributeName[paramsPos] != ATTR_EVENT_SHORTHAND
              && attributeName[paramsPos] != ATTR_ARGUMENT_PREFIX
              && attributeName[paramsPos] != ATTR_SLOT_SHORTHAND
              && attributeName[paramsPos] != ATTR_MODIFIER_PREFIX)) {
        return VueDirectiveInfo(name, kind, isShorthand = isShorthand)
      }

      val arguments: String?
      if (attributeName[paramsPos] == ATTR_MODIFIER_PREFIX) {
        return VueDirectiveInfo(name, kind, null, isShorthand, parseModifiers(attributeName, paramsPos))
      }
      else {
        paramsPos++
      }
      val lastBracket = attributeName.lastIndexOf(']')
      if (lastBracket >= paramsPos && attributeName[paramsPos] == '[') {
        arguments = attributeName.substring(paramsPos, lastBracket + 1)
        paramsPos = lastBracket + 1
      }
      else {
        val firstDot = attributeName.indexOf(ATTR_MODIFIER_PREFIX, paramsPos)
        if (firstDot > 0) {
          arguments = attributeName.substring(paramsPos, firstDot)
          paramsPos = firstDot
        }
        else {
          arguments = attributeName.substring(paramsPos)
          paramsPos = attributeName.length
        }
      }
      return VueDirectiveInfo(name, kind, arguments, isShorthand, parseModifiers(attributeName, paramsPos))
    }

    private fun parseModifiers(modifiers: CharSequence, startPos: Int): Set<String> {
      if (startPos >= modifiers.length || modifiers[startPos] != ATTR_MODIFIER_PREFIX) {
        return emptySet()
      }
      val result = mutableSetOf<String>()
      var currentIndex = startPos
      var prevDot = startPos
      while (++currentIndex < modifiers.length) {
        if (modifiers[currentIndex] == ATTR_MODIFIER_PREFIX) {
          if (prevDot < currentIndex) {
            result.add(modifiers.substring(prevDot + 1, currentIndex))
          }
          prevDot = currentIndex
        }
      }
      if (prevDot < modifiers.length) {
        result.add(modifiers.substring(prevDot + 1))
      }
      return result
    }

    private val attributeKindMap: MultiMap<String, VueAttributeKind> = MultiMap()

    private val directiveKindMap = StreamEx.of(*VueDirectiveKind.values())
      .mapToEntry({ it.directiveName }, { it })
      .nonNullKeys()
      .toMap()

    init {
      VueAttributeKind.values().asSequence()
        .filter { it.attributeName != null }
        .forEach {
          attributeKindMap.putValue(it.attributeName, it)
        }
    }
  }

  open class VueAttributeInfo internal constructor(val name: String,
                                                   val kind: VueAttributeKind,
                                                   val modifiers: Set<String> = emptySet()) {

    open val requiresValue: Boolean get() = kind.requiresValue && modifiers.isEmpty()

    open val injectJS: Boolean get() = kind.injectJS

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as VueAttributeInfo

      return name == other.name
             && kind === other.kind
             && modifiers == other.modifiers
    }

    override fun hashCode(): Int {
      var result = name.hashCode()
      result = 31 * result + kind.hashCode()
      result = 31 * result + modifiers.hashCode()
      return result
    }

    override fun toString(): String {
      return "$name [$kind, modifiers=$modifiers]"
    }
  }

  class VueDirectiveInfo internal constructor(name: String,
                                              val directiveKind: VueDirectiveKind,
                                              val arguments: String? = null,
                                              val isShorthand: Boolean = false,
                                              modifiers: Set<String> = emptySet()) : VueAttributeInfo(name,
                                                                                                      VueAttributeKind.DIRECTIVE,
                                                                                                      modifiers) {
    override val requiresValue: Boolean
      get() {
        return directiveKind.requiresValue && modifiers.isEmpty()
      }

    override val injectJS: Boolean get() = directiveKind.injectJS

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      if (!super.equals(other)) return false

      other as VueDirectiveInfo

      return directiveKind === other.directiveKind
             && arguments == other.arguments
    }

    override fun hashCode(): Int {
      var result = super.hashCode()
      result = 31 * result + directiveKind.hashCode()
      result = 31 * result + arguments.hashCode()
      return result
    }

    override fun toString(): String {
      return "$name [$kind, args=$arguments, isShorthand=$isShorthand, modifiers=$modifiers]"
    }

  }

  enum class VueAttributeKind(val attributeName: String?,
                              val injectJS: Boolean = false,
                              val requiresValue: Boolean = true,
                              val deprecated: Boolean = false,
                              val requiresTag: String? = null,
                              val onlyTopLevelTag: Boolean = true) {
    PLAIN(null),
    DIRECTIVE(null),
    SLOT(DEPRECATED_SLOT_ATTRIBUTE, deprecated = true),
    REF(REF_ATTRIBUTE_NAME),
    IS("is"),
    KEY("key"),
    SCOPE("scope", injectJS = true, deprecated = true, requiresTag = TEMPLATE_TAG_NAME, onlyTopLevelTag = false),
    SLOT_SCOPE("slot-scope", injectJS = true, deprecated = true),
    SLOT_NAME(SLOT_NAME_ATTRIBUTE, injectJS = false, requiresTag = SLOT_TAG_NAME, onlyTopLevelTag = false),
    STYLE_SCOPED("scoped", requiresValue = false, requiresTag = STYLE_TAG_NAME),
    STYLE_MODULE(MODULE_ATTRIBUTE_NAME, requiresValue = false, requiresTag = STYLE_TAG_NAME),
    STYLE_SRC(SRC_ATTRIBUTE_NAME, requiresTag = STYLE_TAG_NAME),
    STYLE_LANG(LANG_ATTRIBUTE_NAME, requiresTag = STYLE_TAG_NAME),
    TEMPLATE_FUNCTIONAL("functional", requiresValue = false, requiresTag = TEMPLATE_TAG_NAME),
    TEMPLATE_SRC(SRC_ATTRIBUTE_NAME, requiresTag = TEMPLATE_TAG_NAME),
    TEMPLATE_LANG(LANG_ATTRIBUTE_NAME, requiresTag = TEMPLATE_TAG_NAME),
    SCRIPT_ID(ID_ATTRIBUTE_NAME, requiresTag = SCRIPT_TAG_NAME, onlyTopLevelTag = false),
    SCRIPT_SRC(SRC_ATTRIBUTE_NAME, requiresTag = SCRIPT_TAG_NAME),
    SCRIPT_LANG(LANG_ATTRIBUTE_NAME, requiresTag = SCRIPT_TAG_NAME),
    SCRIPT_SETUP(SETUP_ATTRIBUTE_NAME, injectJS = false, requiresValue = false, requiresTag = SCRIPT_TAG_NAME),
    SCRIPT_GENERIC(GENERIC_ATTRIBUTE_NAME, injectJS = true, requiresValue = true, requiresTag = SCRIPT_TAG_NAME,
                   onlyTopLevelTag = false)
    ;

    fun isValidIn(context: String?, isTopLevel: Boolean): Boolean {
      return requiresTag == null
             || ((!onlyTopLevelTag || isTopLevel)
                 && context?.lowercase(Locale.US) == requiresTag)
    }

    fun isValidIn(context: XmlTag): Boolean {
      return isValidIn(context.name, context.parentTag == null)
    }
  }

  enum class VueDirectiveKind(private val hasName: Boolean = true,
                              val injectJS: Boolean = true,
                              val requiresValue: Boolean = true) {
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
    SLOT(requiresValue = false),
    TEXT;

    val directiveName get() = if (hasName) name.lowercase(Locale.US).replace('_', '-') else null
  }
}
