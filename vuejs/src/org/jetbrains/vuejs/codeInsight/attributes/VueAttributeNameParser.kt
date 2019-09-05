// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.psi.xml.XmlTag
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.lang.html.VueLanguage
import java.util.*

class VueAttributeNameParser private constructor() {
  companion object {

    fun parse(attributeName: String, context: String?): VueAttributeInfo =
      parse(attributeName) { it.isValidIn(context) }

    fun parse(attributeName: String, context: XmlTag): VueAttributeInfo =
      parse(attributeName) { it.isValidIn(context) }

    private fun parse(attributeName: String, isValid: (VueAttributeKind) -> Boolean): VueAttributeInfo {
      if (attributeName.isEmpty()) return VueAttributeInfo("", VueAttributeKind.PLAIN)
      val name: String
      val kind: VueDirectiveKind
      var paramsPos: Int
      var isShorthand = false
      if (attributeName.startsWith('@')) {
        name = "on"
        kind = VueDirectiveKind.ON
        paramsPos = 0
        isShorthand = true
      }
      else if (attributeName.startsWith(':')) {
        name = "bind"
        kind = VueDirectiveKind.BIND
        paramsPos = 0
        isShorthand = true
      }
      else if (attributeName.startsWith('#')) {
        name = "slot"
        kind = VueDirectiveKind.SLOT
        paramsPos = 0
        isShorthand = true
      }
      else if (attributeName.startsWith("v-") && attributeName.length > 2) {
        var nameEnd = attributeName.indexOfFirst { it == '.' || it == ':' }
        if (nameEnd < 0) {
          nameEnd = attributeName.length
        }
        name = attributeName.substring(2, nameEnd)
        kind = directiveKindMap[name] ?: VueDirectiveKind.CUSTOM
        paramsPos = nameEnd
      }
      else {
        var nameEnd = attributeName.indexOf('.')
        if (nameEnd < 0) {
          nameEnd = attributeName.length
        }
        name = attributeName.substring(0, nameEnd)
        val attributeKind = attributeKindMap[name]?.let { if (isValid(it)) it else VueAttributeKind.PLAIN }
                            ?: VueAttributeKind.PLAIN
        return VueAttributeInfo(name, attributeKind, parseModifiers(attributeName, nameEnd))
      }
      if (paramsPos >= attributeName.length
          || (attributeName[paramsPos] != '@'
              && attributeName[paramsPos] != ':'
              && attributeName[paramsPos] != '#'
              && attributeName[paramsPos] != '.')) {
        return VueDirectiveInfo(name, kind, isShorthand = isShorthand)
      }

      val arguments: String?
      if (attributeName[paramsPos] == '.') {
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
      return VueDirectiveInfo(name, kind, arguments, isShorthand, parseModifiers(attributeName, paramsPos))
    }

    private fun parseModifiers(modifiers: String, startPos: Int): Set<String> {
      if (startPos >= modifiers.length || modifiers[startPos] != '.') {
        return emptySet()
      }
      val result = mutableSetOf<String>()
      var currentIndex = startPos
      var prevDot = startPos
      while (++currentIndex < modifiers.length) {
        if (modifiers[currentIndex] == '.') {
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


  }

  enum class VueAttributeKind(val attributeName: String?,
                              val injectJS: Boolean = false,
                              val requiresValue: Boolean = true,
                              val deprecated: Boolean = false) {
    PLAIN(null),
    DIRECTIVE(null),
    SLOT("slot", deprecated = true),
    REF("ref"),
    IS("is"),
    SCOPE("scope", injectJS = true, deprecated = true),
    SLOT_SCOPE("slot-scope", injectJS = true, deprecated = true),
    STYLE_SCOPED("scoped", requiresValue = false),
    STYLE_MODULE("module", requiresValue = false),
    STYLE_SRC("src"),
    TEMPLATE_FUNCTIONAL("functional", requiresValue = false);

    fun isValidIn(context: String?): Boolean {
      return when {
        this === SCOPE -> context?.toLowerCase(Locale.US) == "template"
        this === TEMPLATE_FUNCTIONAL
        || this === STYLE_SRC
        || this === STYLE_SCOPED
        || this === STYLE_MODULE -> false
        else -> true
      }
    }

    fun isValidIn(context: XmlTag?): Boolean {
      return when {
        this === TEMPLATE_FUNCTIONAL ->
          context != null && isTopLevelTemplateTag(context)
        this === STYLE_SRC
        || this === STYLE_SCOPED
        || this === STYLE_MODULE ->
          context != null && isTopLevelStyleTag(context)
        else -> isValidIn(context?.name)
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

    val directiveName get() = if (hasName) name.toLowerCase(Locale.US).replace('_', '-') else null
  }
}
