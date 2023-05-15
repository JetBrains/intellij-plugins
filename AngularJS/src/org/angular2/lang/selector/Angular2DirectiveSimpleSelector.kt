// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.selector

import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.xml.util.XmlUtil
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.web.Angular2WebSymbolsQueryConfigurator
import org.jetbrains.annotations.NonNls
import java.util.function.Consumer
import java.util.regex.Pattern

class Angular2DirectiveSimpleSelector private constructor(
  val elementName: String?,
  val attrsAndValues: List<String>,
  val classNames: List<String>,
  val notSelectors: List<Angular2DirectiveSimpleSelector>
) {

  constructor(
    elementName: String? = null,
    attrs: Map<String, String?> = emptyMap(),
    classNames: List<String> = emptyList(),
    notSelectors: List<Angular2DirectiveSimpleSelector> = emptyList(),
  ) : this(
    elementName,
    attrs.flatMap { attr -> listOf(attr.key, normalizeAttrValue(attr.value)) },
    classNames.map { normalizeClassName(it) },
    notSelectors
  )

  private constructor(parseInfo: ParseInfo)
    : this(parseInfo.elementName, parseInfo.attrs, parseInfo.classNames, parseInfo.notSelectors.map { Angular2DirectiveSimpleSelector(it) })

  /**
   * The selectors are encoded in pairs where:
   * - even locations are attribute names
   * - odd locations are attribute values.
   *
   *
   * Example:
   * Selector: `[key1=value1][key2]` would parse to:
   * ```
   * ['key1', 'value1', 'key2', '']
   * ```
   */
  val attrNames: List<String>
    get() {
      val result: MutableList<String> = ArrayList()
      if (!classNames.isEmpty()) {
        result.add("class")
      }
      var i = 0
      while (i < attrsAndValues.size) {
        result.add(attrsAndValues[i])
        i += 2
      }
      return result
    }

  override fun toString(): String {
    val result: @NonNls StringBuilder = StringBuilder()
    if (elementName != null) {
      result.append(elementName)
    }
    classNames.forEach(Consumer { cls: String? ->
      result.append('.')
      result.append(cls)
    })
    var i = 0
    while (i < attrsAndValues.size) {
      result.append('[')
      result.append(attrsAndValues[i])
      val value = attrsAndValues[i + 1]
      if (!value.isEmpty()) {
        result.append("=")
        result.append(value)
      }
      result.append(']')
      i += 2
    }
    notSelectors.forEach(Consumer { selector: Angular2DirectiveSimpleSelector ->
      result.append(":not(")
      result.append(selector.toString())
      result.append(')')
    })
    return result.toString()
  }

  class Angular2DirectiveSimpleSelectorWithRanges {
    var elementRange: Pair<String, Int>? = null
      private set
    private val classNames: MutableList<Pair<String, Int>> = SmartList()
    private val attrs: MutableList<Pair<String, Int>> = SmartList()
    internal val mNotSelectors: MutableList<Angular2DirectiveSimpleSelectorWithRanges> = SmartList()

    val notSelectors: List<Angular2DirectiveSimpleSelectorWithRanges>
      get() = mNotSelectors

    fun addAttribute(name: String, offset: Int) {
      attrs.add(Pair.pair(name, offset))
    }

    fun addClassName(name: String, offset: Int) {
      classNames.add(Pair.pair(name, offset))
    }

    fun setElement(name: String, offset: Int) {
      elementRange = Pair.pair(name, offset)
    }

    val classNameRanges: List<Pair<String, Int>>
      get() = classNames

    val attributeRanges: List<Pair<String, Int>>
      get() = attrs

  }

  class ParseException(s: String?, val errorRange: TextRange) : Exception(s)

  private data class ParseInfo(
    var elementName: String? = null,
    val classNames: MutableList<String> = SmartList(),
    val attrs: MutableList<String> = SmartList(),
    val notSelectors: MutableList<ParseInfo> = SmartList(),
  ) {

    fun addAttribute(name: String, value: String?) {
      attrs.add(name)
      attrs.add(normalizeAttrValue(value))
    }

    fun addClassName(name: String) {
      classNames.add(normalizeClassName(name))
    }
  }

  companion object {
    private val SELECTOR_REGEXP = Pattern.compile(
      "(:not\\()|" +  //":not("
      "([-\\w]+)|" +  // "tag"
      "(?:\\.([-\\w]+))|" +  // ".class"
      // "-" should appear first in the regexp below as FF31 parses "[.-\w]" as a range
      "(?:\\[([-.\\w*]+)(?:=([\"']?)([^]\"']*)\\5)?])|" +  // "[name]", "[name=value]",
      //                                                          "[name="value"]",
      //                                                          "[name='value']"
      "(\\))|" +  // ")"
      "(\\s*,\\s*)" // ","
    )

    @Throws(ParseException::class)
    @JvmStatic
    fun parse(selector: String): List<Angular2DirectiveSimpleSelector> {
      val results: MutableList<Angular2DirectiveSimpleSelector> = SmartList()
      val addResult = Consumer { cssSel: ParseInfo ->
        if (!cssSel.notSelectors.isEmpty() && cssSel.elementName == null && cssSel.classNames.isEmpty() &&
            cssSel.attrs.isEmpty()) {
          cssSel.elementName = "*"
        }
        results.add(Angular2DirectiveSimpleSelector(cssSel))
      }
      var cssSelector = ParseInfo()
      var current = cssSelector
      var inNot = false
      val matcher = SELECTOR_REGEXP.matcher(selector)
      while (matcher.find()) {
        if (matcher.start(1) >= 0) {
          if (inNot) {
            throw ParseException(Angular2Bundle.message("angular.parse.selector.nested-not"),
                                 TextRange(matcher.start(1), matcher.end(1)))
          }
          inNot = true
          current = ParseInfo()
          cssSelector.notSelectors.add(current)
        }
        else if (matcher.start(2) >= 0) {
          current.elementName = matcher.group(2)
        }
        if (matcher.start(3) >= 0) {
          current.addClassName(matcher.group(3))
        }
        if (matcher.start(4) >= 0) {
          current.addAttribute(matcher.group(4), matcher.group(6))
        }
        if (matcher.start(7) >= 0) {
          inNot = false
          current = cssSelector
        }
        if (matcher.start(8) >= 0) {
          if (inNot) {
            throw ParseException(Angular2Bundle.message("angular.parse.selector.multiple-not"),
                                 TextRange(matcher.start(8), matcher.end(8)))
          }
          addResult.accept(cssSelector)
          current = ParseInfo()
          cssSelector = current
        }
      }
      addResult.accept(cssSelector)
      return results
    }

    @Throws(ParseException::class)
    @JvmStatic
    fun parseRanges(selector: String): List<Angular2DirectiveSimpleSelectorWithRanges> {
      val results: MutableList<Angular2DirectiveSimpleSelectorWithRanges> = SmartList()
      var cssSelector = Angular2DirectiveSimpleSelectorWithRanges()
      var current = cssSelector
      var inNot = false
      val matcher = SELECTOR_REGEXP.matcher(selector)
      while (matcher.find()) {
        if (matcher.start(1) >= 0) {
          if (inNot) {
            throw ParseException(Angular2Bundle.message("angular.parse.selector.nested-not"),
                                 TextRange(matcher.start(1), matcher.end(1)))
          }
          inNot = true
          current = Angular2DirectiveSimpleSelectorWithRanges()
          cssSelector.mNotSelectors.add(current)
        }
        else if (matcher.start(2) >= 0) {
          current.setElement(matcher.group(2), matcher.start(2))
        }
        if (matcher.start(3) >= 0) {
          current.addClassName(matcher.group(3), matcher.start(3))
        }
        if (matcher.start(4) >= 0) {
          current.addAttribute(matcher.group(4), matcher.start(4))
        }
        if (matcher.start(7) >= 0) {
          inNot = false
          current = cssSelector
        }
        if (matcher.start(8) >= 0) {
          if (inNot) {
            throw ParseException(Angular2Bundle.message("angular.parse.selector.multiple-not"),
                                 TextRange(matcher.start(8), matcher.end(8)))
          }
          results.add(cssSelector)
          current = Angular2DirectiveSimpleSelectorWithRanges()
          cssSelector = current
        }
      }
      results.add(cssSelector)
      return results
    }

    @JvmStatic
    fun createTemplateBindingsCssSelector(bindings: Angular2TemplateBindings): Angular2DirectiveSimpleSelector {
      val attributes = mutableMapOf<String, String?>()
      attributes[bindings.templateName] = null
      for (binding in bindings.bindings) {
        if (!binding.keyIsVar()) {
          attributes[binding.key] = binding.expression?.text
        }
      }
      return Angular2DirectiveSimpleSelector(
        Angular2WebSymbolsQueryConfigurator.ELEMENT_NG_TEMPLATE,
        attributes
      )
    }

    @JvmStatic
    fun createElementCssSelector(element: XmlTag): Angular2DirectiveSimpleSelector {
      val elNameNoNs = XmlUtil.findLocalNameByQualifiedName(element.name)
      val attributes = mutableMapOf<String, String?>()
      val classNames = mutableListOf<String>()
      for (attr in element.attributes) {
        val attrNameNoNs = XmlUtil.findLocalNameByQualifiedName(attr.name)
        val info = Angular2AttributeNameParser.parse(attrNameNoNs!!, element)
        if (info.type == Angular2AttributeType.TEMPLATE_BINDINGS
            || info.type == Angular2AttributeType.LET
            || info.type == Angular2AttributeType.REFERENCE) {
          continue
        }
        val attrValue = attr.value
        attributes[info.name] = attrValue
        if (StringUtil.toLowerCase(attr.name) == "class" && attrValue != null) {
          attrValue.splitToSequence(' ').forEach { if (it.isNotEmpty()) classNames.add(it) }
        }
      }
      return Angular2DirectiveSimpleSelector(elNameNoNs, attributes, classNames)
    }

    private fun normalizeAttrValue(value: String?): String =
      if (value != null) StringUtil.toLowerCase(value) else ""

    private fun normalizeClassName(className: String): String =
      StringUtil.toLowerCase(className)
  }
}