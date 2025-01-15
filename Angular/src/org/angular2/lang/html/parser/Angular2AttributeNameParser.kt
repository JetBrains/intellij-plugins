// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.Html5TagAndAttributeNamesProvider
import com.intellij.xml.util.Html5TagAndAttributeNamesProvider.Namespace
import com.intellij.xml.util.HtmlUtil
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.html.psi.Angular2HtmlEvent.AnimationPhase
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.web.ATTR_SELECT
import org.angular2.web.ELEMENT_NG_CONTENT
import org.angular2.web.ELEMENT_NG_TEMPLATE
import org.jetbrains.annotations.Nls

object Angular2AttributeNameParser {
  val ATTR_TO_PROP_MAPPING: Map<String, String> = mapOf(
    "class" to "className",
    "for" to "htmlFor",
    "formaction" to "formAction",
    "innerHtml" to "innerHTML",
    "readonly" to "readOnly",
    "tabindex" to "tabIndex"
  )

  fun parseBound(name: String, tagName: String): AttributeInfo {
    val info = parse(name)
    return if (info.type != Angular2AttributeType.REGULAR) info
    else PropertyBindingInfo(mapToHtmlProp(info.name, tagName), 0, info.isCanonical,
                             false, PropertyBindingType.PROPERTY)
  }

  fun parse(name: String): AttributeInfo {
    return parse(name, ELEMENT_NG_TEMPLATE)
  }

  fun parse(name: String, tag: XmlTag?): AttributeInfo {
    return parse(name, tag?.localName ?: ELEMENT_NG_TEMPLATE)
  }

  fun parse(name: String, tagName: String): AttributeInfo {
    val normalizedName = normalizeAttributeName(name)
    return when {
      normalizedName.startsWith("bindon-") -> {
        parsePropertyBindingCanonical(normalizedName.substring(7), 7, true, tagName)
      }
      normalizedName.startsWith("[(") && normalizedName.endsWith(")]") -> {
        parsePropertyBindingShort(normalizedName.substring(2, normalizedName.length - 2), 2, true, tagName)
      }
      normalizedName.startsWith("bind-") -> {
        parsePropertyBindingCanonical(normalizedName.substring(5), 5, false, tagName)
      }
      normalizedName.startsWith("[") && normalizedName.endsWith("]") -> {
        parsePropertyBindingShort(normalizedName.substring(1, normalizedName.length - 1), 1, false, tagName)
      }
      normalizedName.startsWith("on-") -> {
        parseEvent(normalizedName.substring(3), 3, true)
      }
      normalizedName.startsWith("(") && normalizedName.endsWith(")") -> {
        parseEvent(normalizedName.substring(1, normalizedName.length - 1), 1, false)
      }
      normalizedName.startsWith("*") -> {
        parseTemplateBindings(normalizedName.substring(1), 1)
      }
      normalizedName.startsWith("let-") -> {
        parseLet(normalizedName, normalizedName.substring(4), 4, isTemplateTag(tagName))
      }
      normalizedName.startsWith("#") -> {
        parseReference(normalizedName, normalizedName.substring(1), 1, false)
      }
      normalizedName.startsWith("ref-") -> {
        parseReference(normalizedName, normalizedName.substring(4), 4, true)
      }
      normalizedName.startsWith("@") -> {
        PropertyBindingInfo(normalizedName.substring(1), 1, false, false, PropertyBindingType.ANIMATION)
      }
      normalizedName == ATTR_SELECT && tagName == ELEMENT_NG_CONTENT -> {
        AttributeInfo(normalizedName, 0, false, Angular2AttributeType.NG_CONTENT_SELECTOR)
      }
      normalizedName.startsWith("i18n-") -> {
        AttributeInfo(normalizedName.substring(5), 5, false, Angular2AttributeType.I18N)
      }
      else -> {
        AttributeInfo(normalizedName, 0, false, Angular2AttributeType.REGULAR)
      }
    }
  }

  fun normalizeAttributeName(name: String): String {
    return if (StringUtil.startsWithIgnoreCase(name, HtmlUtil.HTML5_DATA_ATTR_PREFIX)) {
      name.substring(5)
    }
    else name
  }

  private fun parsePropertyBindingShort(name: String, nameOffset: Int, bananaBoxBinding: Boolean, tagName: String): AttributeInfo {
    return when {
      !bananaBoxBinding && name.startsWith("@") -> {
        PropertyBindingInfo(name.substring(1), nameOffset + 1, false, false, PropertyBindingType.ANIMATION)
      }
      else -> parsePropertyBindingRest(name, nameOffset, false, bananaBoxBinding, tagName)
    }
  }

  private fun parsePropertyBindingCanonical(name: String, nameOffset: Int, bananaBoxBinding: Boolean, tagName: String): AttributeInfo {
    return when {
      !bananaBoxBinding && name.startsWith("animate-") -> {
        PropertyBindingInfo(name.substring(8), nameOffset + 8, true, false, PropertyBindingType.ANIMATION)
      }
      else -> parsePropertyBindingRest(name, nameOffset, true, bananaBoxBinding, tagName)
    }
  }

  private fun parsePropertyBindingRest(name: String, nameOffset: Int, isCanonical: Boolean, bananaBoxBinding: Boolean, tagName: String): AttributeInfo {
    return when {
      name.startsWith("attr.") -> {
        PropertyBindingInfo(name.substring(5), nameOffset + 5, isCanonical, bananaBoxBinding, PropertyBindingType.ATTRIBUTE)
      }
      name.startsWith("class.") -> {
        PropertyBindingInfo(name.substring(6), nameOffset + 6, isCanonical, bananaBoxBinding, PropertyBindingType.CLASS)
      }
      name.startsWith("style.") -> {
        PropertyBindingInfo(name.substring(6), nameOffset + 6, isCanonical, bananaBoxBinding, PropertyBindingType.STYLE)
      }
      else -> PropertyBindingInfo(mapToHtmlProp(name, tagName), nameOffset, isCanonical, bananaBoxBinding, PropertyBindingType.PROPERTY)
    }
  }

  private fun parseEvent(name: String, nameOffset: Int, isCanonical: Boolean): AttributeInfo {
    val additionalOffset: Int
    val eventName = when {
      name.startsWith("@") -> {
        additionalOffset = 1
        name.substring(1)
      }
      name.startsWith("animate-") -> {
        additionalOffset = 8
        name.substring(8)
      }
      else -> {
        return EventInfo(name, nameOffset, isCanonical)
      }
    }
    return parseAnimationEvent(eventName, nameOffset + additionalOffset, isCanonical)
  }

  private fun parseTemplateBindings(name: String, nameOffset: Int): AttributeInfo {
    return AttributeInfo(name, nameOffset, false, Angular2AttributeType.TEMPLATE_BINDINGS)
  }

  private fun parseAnimationEvent(name: String, nameOffset: Int, isCanonical: Boolean): AttributeInfo {
    val dot = name.indexOf('.')
    if (dot < 0) {
      return EventInfo(name, nameOffset, isCanonical, AnimationPhase.INVALID,
                       Angular2Bundle.message("angular.parse.template.animation-trigger-missing-phase-value",
                                              name))
    }
    val phase = StringUtil.toLowerCase(name.substring(dot + 1))
    val eventName = name.substring(0, dot)
    return when (phase) {
      "done" -> {
        EventInfo(eventName, nameOffset, isCanonical, AnimationPhase.DONE)
      }
      "start" -> {
        EventInfo(eventName, nameOffset, isCanonical, AnimationPhase.START)
      }
      else -> EventInfo(eventName, nameOffset, isCanonical, AnimationPhase.INVALID,
                        Angular2Bundle.message("angular.parse.template.animation-trigger-wrong-output-phase",
                                               phase, eventName.substring(0, dot)))
    }
  }

  private fun parseLet(attrName: String, varName: String, nameOffset: Int, isInTemplateTag: Boolean): AttributeInfo {
    return when {
      !isInTemplateTag -> {
        AttributeInfo(attrName, 0, false, Angular2AttributeType.REGULAR,
                      Angular2Bundle.message("angular.parse.template.let-only-on-ng-template"))
      }
      varName.contains("-") -> {
        AttributeInfo(attrName, 0, false, Angular2AttributeType.REGULAR,
                      Angular2Bundle.message("angular.parse.template.let-dash-not-allowed-in-name"))
      }
      varName.isEmpty() -> {
        AttributeInfo(attrName, 0, false, Angular2AttributeType.REGULAR)
      }
      else -> AttributeInfo(varName, nameOffset, false, Angular2AttributeType.LET)
    }
  }

  private fun parseReference(attrName: String, refName: String, nameOffset: Int, isCanonical: Boolean): AttributeInfo {
    return when {
      refName.contains("-") -> {
        AttributeInfo(attrName, 0, false, Angular2AttributeType.REGULAR,
                      Angular2Bundle.message("angular.parse.template.ref-var-dash-not-allowed-in-name"))
      }
      refName.isEmpty() -> {
        AttributeInfo(attrName, 0, false, Angular2AttributeType.REGULAR)
      }
      else -> AttributeInfo(refName, nameOffset, isCanonical, Angular2AttributeType.REFERENCE)
    }
  }

  private fun mapToHtmlProp(name: String, tagName: String): String =
    if (Html5TagAndAttributeNamesProvider.getTags(Namespace.HTML, false).contains(tagName))
      ATTR_TO_PROP_MAPPING.getOrDefault(name, name)
    else
      name

  open class AttributeInfo @JvmOverloads constructor(val name: String,
                                                     val nameOffset: Int,
                                                     val isCanonical: Boolean,
                                                     val type: Angular2AttributeType,
                                                     val error: @Nls String? = null) {
    open val fullName: String get() = name

    open fun isEquivalent(otherInfo: AttributeInfo?): Boolean {
      return otherInfo != null && name == otherInfo.name && type == otherInfo.type
    }

    override fun toString(): String {
      return "<$name>"
    }
  }

  class PropertyBindingInfo(
    name: String,
    nameOffset: Int,
    isCanonical: Boolean,
    bananaBoxBinding: Boolean,
    val bindingType: PropertyBindingType,
  )
    : AttributeInfo(name, nameOffset, isCanonical,
                    if (bananaBoxBinding) Angular2AttributeType.BANANA_BOX_BINDING else Angular2AttributeType.PROPERTY_BINDING) {

    override fun isEquivalent(otherInfo: AttributeInfo?): Boolean {
      return otherInfo is PropertyBindingInfo && bindingType == otherInfo.bindingType && super.isEquivalent(otherInfo)
    }

    override val fullName: String
      get() = when (bindingType) {
        PropertyBindingType.ANIMATION -> (if (isCanonical) "animate-" else "@") + name
        PropertyBindingType.ATTRIBUTE -> "attr.$name"
        PropertyBindingType.STYLE -> "style.$name"
        PropertyBindingType.CLASS -> "class.$name"
        else -> name
      }

    override fun toString(): String {
      return "<$name,$bindingType>"
    }
  }

  class EventInfo : AttributeInfo {
    val animationPhase: AnimationPhase?
    val eventType: Angular2HtmlEvent.EventType

    constructor(name: String, nameOffset: Int, isCanonical: Boolean) : super(name, nameOffset, isCanonical, Angular2AttributeType.EVENT) {
      eventType = Angular2HtmlEvent.EventType.REGULAR
      animationPhase = null
    }

    @JvmOverloads
    constructor(name: String, nameOffset: Int, isCanonical: Boolean, animationPhase: AnimationPhase, error: @Nls String? = null)
      : super(name, nameOffset, isCanonical, Angular2AttributeType.EVENT, error) {

      this.animationPhase = animationPhase
      eventType = Angular2HtmlEvent.EventType.ANIMATION
    }

    override fun isEquivalent(otherInfo: AttributeInfo?): Boolean {
      return otherInfo is EventInfo
             && eventType == otherInfo.eventType
             && animationPhase == otherInfo.animationPhase
             && super.isEquivalent(otherInfo)
    }

    override val fullName: String
      get() = if (eventType == Angular2HtmlEvent.EventType.ANIMATION) {
        if (animationPhase != null) {
          when (animationPhase) {
            AnimationPhase.DONE -> "@$name.done"
            AnimationPhase.START -> "@$name.start"
            else -> "@$name"
          }
        }
        else "@$name"
      }
      else name

    override fun toString(): String {
      return "<" + name + ", " + eventType + (if (eventType == Angular2HtmlEvent.EventType.ANIMATION) ", $animationPhase" else "") + ">"
    }
  }
}