// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.html.psi.Angular2HtmlEvent.AnimationPhase
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.web.Angular2WebSymbolsQueryConfigurator
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

  fun parseBound(name: String): AttributeInfo {
    val info = parse(name)
    return if (info.type != Angular2AttributeType.REGULAR) info
    else PropertyBindingInfo(info.name, info.isCanonical, false, PropertyBindingType.PROPERTY)
  }

  fun parse(name: String): AttributeInfo {
    return parse(name, Angular2WebSymbolsQueryConfigurator.ELEMENT_NG_TEMPLATE)
  }

  fun parse(name: String, tag: XmlTag?): AttributeInfo {
    return parse(name, tag?.localName ?: Angular2WebSymbolsQueryConfigurator.ELEMENT_NG_TEMPLATE)
  }

  fun parse(name: String, tagName: String): AttributeInfo {
    val normalizedName = normalizeAttributeName(name)
    return when {
      normalizedName.startsWith("bindon-") -> {
        parsePropertyBindingCanonical(normalizedName.substring(7), true)
      }
      normalizedName.startsWith("[(") && normalizedName.endsWith(")]") -> {
        parsePropertyBindingShort(normalizedName.substring(2, normalizedName.length - 2), true)
      }
      normalizedName.startsWith("bind-") -> {
        parsePropertyBindingCanonical(normalizedName.substring(5), false)
      }
      normalizedName.startsWith("[") && normalizedName.endsWith("]") -> {
        parsePropertyBindingShort(normalizedName.substring(1, normalizedName.length - 1), false)
      }
      normalizedName.startsWith("on-") -> {
        parseEvent(normalizedName.substring(3), true)
      }
      normalizedName.startsWith("(") && normalizedName.endsWith(")") -> {
        parseEvent(normalizedName.substring(1, normalizedName.length - 1), false)
      }
      normalizedName.startsWith("*") -> {
        parseTemplateBindings(normalizedName.substring(1))
      }
      normalizedName.startsWith("let-") -> {
        parseLet(normalizedName, normalizedName.substring(4), isTemplateTag(tagName))
      }
      normalizedName.startsWith("#") -> {
        parseReference(normalizedName, normalizedName.substring(1), false)
      }
      normalizedName.startsWith("ref-") -> {
        parseReference(normalizedName, normalizedName.substring(4), true)
      }
      normalizedName.startsWith("@") -> {
        PropertyBindingInfo(normalizedName.substring(1), false, false, PropertyBindingType.ANIMATION)
      }
      normalizedName == Angular2WebSymbolsQueryConfigurator.ATTR_SELECT && tagName == Angular2WebSymbolsQueryConfigurator.ELEMENT_NG_CONTENT -> {
        AttributeInfo(normalizedName, false, Angular2AttributeType.NG_CONTENT_SELECTOR)
      }
      normalizedName.startsWith("i18n-") -> {
        AttributeInfo(normalizedName.substring(5), false, Angular2AttributeType.I18N)
      }
      else -> {
        AttributeInfo(normalizedName, false, Angular2AttributeType.REGULAR)
      }
    }
  }

  fun normalizeAttributeName(name: String): String {
    return if (StringUtil.startsWithIgnoreCase(name, HtmlUtil.HTML5_DATA_ATTR_PREFIX)) {
      name.substring(5)
    }
    else name
  }

  private fun parsePropertyBindingShort(name: String, bananaBoxBinding: Boolean): AttributeInfo {
    return when {
      !bananaBoxBinding && name.startsWith("@") -> {
        PropertyBindingInfo(name.substring(1), false, false, PropertyBindingType.ANIMATION)
      }
      else -> parsePropertyBindingRest(name, false, bananaBoxBinding)
    }
  }

  private fun parsePropertyBindingCanonical(name: String, bananaBoxBinding: Boolean): AttributeInfo {
    return when {
      !bananaBoxBinding && name.startsWith("animate-") -> {
        PropertyBindingInfo(name.substring(8), true, false, PropertyBindingType.ANIMATION)
      }
      else -> parsePropertyBindingRest(name, true, bananaBoxBinding)
    }
  }

  private fun parsePropertyBindingRest(name: String, isCanonical: Boolean, bananaBoxBinding: Boolean): AttributeInfo {
    return when {
      name.startsWith("attr.") -> {
        PropertyBindingInfo(name.substring(5), isCanonical, bananaBoxBinding, PropertyBindingType.ATTRIBUTE)
      }
      name.startsWith("class.") -> {
        PropertyBindingInfo(name.substring(6), isCanonical, bananaBoxBinding, PropertyBindingType.CLASS)
      }
      name.startsWith("style.") -> {
        PropertyBindingInfo(name.substring(6), isCanonical, bananaBoxBinding, PropertyBindingType.STYLE)
      }
      else -> PropertyBindingInfo(name, isCanonical, bananaBoxBinding, PropertyBindingType.PROPERTY)
    }
  }

  private fun parseEvent(name: String, isCanonical: Boolean): AttributeInfo {
    val eventName = when {
      name.startsWith("@") -> {
        name.substring(1)
      }
      name.startsWith("animate-") -> {
        name.substring(8)
      }
      else -> {
        return EventInfo(name, isCanonical)
      }
    }
    return parseAnimationEvent(eventName, isCanonical)
  }

  private fun parseTemplateBindings(name: String): AttributeInfo {
    return AttributeInfo(name, false, Angular2AttributeType.TEMPLATE_BINDINGS)
  }

  private fun parseAnimationEvent(name: String, isCanonical: Boolean): AttributeInfo {
    val dot = name.indexOf('.')
    if (dot < 0) {
      return EventInfo(name, isCanonical, AnimationPhase.INVALID,
                       Angular2Bundle.message("angular.parse.template.animation-trigger-missing-phase-value",
                                              name))
    }
    val phase = StringUtil.toLowerCase(name.substring(dot + 1))
    val eventName = name.substring(0, dot)
    return when (phase) {
      "done" -> {
        EventInfo(eventName, isCanonical, AnimationPhase.DONE)
      }
      "start" -> {
        EventInfo(eventName, isCanonical, AnimationPhase.START)
      }
      else -> EventInfo(eventName, isCanonical, AnimationPhase.INVALID,
                        Angular2Bundle.message("angular.parse.template.animation-trigger-wrong-output-phase",
                                               phase, eventName.substring(0, dot)))
    }
  }

  private fun parseLet(attrName: String, varName: String, isInTemplateTag: Boolean): AttributeInfo {
    return when {
      !isInTemplateTag -> {
        AttributeInfo(attrName, false, Angular2AttributeType.REGULAR,
                      Angular2Bundle.message("angular.parse.template.let-only-on-ng-template"))
      }
      varName.contains("-") -> {
        AttributeInfo(attrName, false, Angular2AttributeType.REGULAR,
                      Angular2Bundle.message("angular.parse.template.let-dash-not-allowed-in-name"))
      }
      varName.isEmpty() -> {
        AttributeInfo(attrName, false, Angular2AttributeType.REGULAR)
      }
      else -> AttributeInfo(varName, false, Angular2AttributeType.LET)
    }
  }

  private fun parseReference(attrName: String, refName: String, isCanonical: Boolean): AttributeInfo {
    return when {
      refName.contains("-") -> {
        AttributeInfo(attrName, false, Angular2AttributeType.REGULAR,
                      Angular2Bundle.message("angular.parse.template.ref-var-dash-not-allowed-in-name"))
      }
      refName.isEmpty() -> {
        AttributeInfo(attrName, false, Angular2AttributeType.REGULAR)
      }
      else -> AttributeInfo(refName, isCanonical, Angular2AttributeType.REFERENCE)
    }
  }

  open class AttributeInfo @JvmOverloads constructor(val name: String,
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

  class PropertyBindingInfo(name: String,
                            isCanonical: Boolean,
                            bananaBoxBinding: Boolean,
                            val bindingType: PropertyBindingType)
    : AttributeInfo(ATTR_TO_PROP_MAPPING.getOrDefault(name, name), isCanonical,
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

    constructor(name: String, isCanonical: Boolean) : super(name, isCanonical, Angular2AttributeType.EVENT) {
      eventType = Angular2HtmlEvent.EventType.REGULAR
      animationPhase = null
    }

    @JvmOverloads
    constructor(name: String, isCanonical: Boolean, animationPhase: AnimationPhase, error: @Nls String? = null)
      : super(name, isCanonical, Angular2AttributeType.EVENT, error) {

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