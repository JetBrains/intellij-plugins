// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.selector

import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.SmartList
import com.intellij.util.containers.ContainerUtil
import java.util.function.Consumer

class Angular2SelectorMatcher<T : Any> {
  private val _elementMap: MutableMap<String, MutableList<SelectorContext<T>>> = HashMap()
  private val _elementPartialMap: MutableMap<String, Angular2SelectorMatcher<T>> = HashMap()
  private val _classMap: MutableMap<String, MutableList<SelectorContext<T>>> = HashMap()
  private val _classPartialMap: MutableMap<String, Angular2SelectorMatcher<T>> = HashMap()
  private val _attrValueMap: MutableMap<String?, MutableMap<String, MutableList<SelectorContext<T>>>> = HashMap()
  private val _attrValuePartialMap: MutableMap<String?, MutableMap<String, Angular2SelectorMatcher<T>>> = HashMap()
  private val _listContexts: MutableList<SelectorListContext> = ArrayList()
  fun addSelectables(cssSelectors: List<Angular2DirectiveSimpleSelector>, context: T?) {
    var listContext: SelectorListContext? = null
    if (cssSelectors.size > 1) {
      listContext = SelectorListContext(cssSelectors)
      _listContexts.add(listContext)
    }
    for (selector in cssSelectors) {
      addSelectable(selector, context, listContext)
    }
  }

  /**
   * Add an object that can be found later on by calling `match`.
   *
   * @param cssSelector  A css selector
   * @param callbackCtx An opaque object that will be given to the callback of the `match` function
   */
  private fun addSelectable(cssSelector: Angular2DirectiveSimpleSelector,
                            callbackCtx: T?,
                            listContext: SelectorListContext?) {
    var matcher: Angular2SelectorMatcher<T> = this
    val element = cssSelector.elementName
    val classNames = cssSelector.classNames
    val attrs = cssSelector.attrsAndValues
    val selectable = SelectorContext(cssSelector, callbackCtx, listContext)
    if (StringUtil.isNotEmpty(element)) {
      val isTerminal = attrs.isEmpty() && classNames.isEmpty()
      if (isTerminal) {
        addTerminal(matcher._elementMap, element!!, selectable)
      }
      else {
        matcher = addPartial(matcher._elementPartialMap, element!!)
      }
    }
    for (i in classNames.indices) {
      val isTerminal = attrs.size == 0 && i == classNames.size - 1
      val className = classNames[i]
      if (isTerminal) {
        addTerminal(matcher._classMap, className, selectable)
      }
      else {
        matcher = addPartial(matcher._classPartialMap, className)
      }
    }
    var i = 0
    while (i < attrs.size) {
      val isTerminal = i == attrs.size - 2
      val name = attrs[i]
      val value = attrs[i + 1]
      if (isTerminal) {
        val terminalValuesMap = matcher._attrValueMap.computeIfAbsent(name) { HashMap() }
        addTerminal(terminalValuesMap, value, selectable)
      }
      else {
        val partialValuesMap = matcher._attrValuePartialMap.computeIfAbsent(name) { HashMap() }
        matcher = addPartial(partialValuesMap, value)
      }
      i += 2
    }
  }

  private fun addTerminal(map: MutableMap<String, MutableList<SelectorContext<T>>>,
                          name: String,
                          selectable: SelectorContext<T>) {
    val terminalList = map.computeIfAbsent(name) { SmartList() }
    terminalList.add(selectable)
  }

  private fun addPartial(map: MutableMap<String, Angular2SelectorMatcher<T>>,
                         name: String): Angular2SelectorMatcher<T> {
    return map.computeIfAbsent(name) { Angular2SelectorMatcher() }
  }

  /**
   * Find the objects that have been added via `addSelectable`
   * whose css selector is contained in the given css selector.
   *
   * @param cssSelector     A css selector
   * @param matchedCallback This callback will be called with the object handed into `addSelectable`
   * @return boolean true if a match was found
   */
  fun match(cssSelector: Angular2DirectiveSimpleSelector,
            matchedCallback: ((Angular2DirectiveSimpleSelector, T?) -> Unit)?): Boolean {
    val element = cssSelector.elementName
    val classNames = cssSelector.classNames
    val attrs = cssSelector.attrsAndValues
    _listContexts.forEach(Consumer { l: SelectorListContext -> l.alreadyMatched = false })
    var result = matchTerminal(_elementMap, element, cssSelector, matchedCallback)
    result = result or matchPartial(_elementPartialMap, element, cssSelector, matchedCallback)
    for (className in classNames) {
      result = result or matchTerminal(_classMap, className, cssSelector, matchedCallback)
      result = result or matchPartial(_classPartialMap, className, cssSelector, matchedCallback)
    }
    var i = 0
    while (i < attrs.size) {
      val name = attrs[i]
      val value = attrs[i + 1]
      val terminalValuesMap = _attrValueMap[name]
      if (StringUtil.isNotEmpty(value)) {
        result = result or matchTerminal(terminalValuesMap, "", cssSelector, matchedCallback)
      }
      result = result or matchTerminal(terminalValuesMap, value, cssSelector, matchedCallback)
      val partialValuesMap = _attrValuePartialMap[name]
      if (StringUtil.isNotEmpty(value)) {
        result = result or matchPartial(partialValuesMap, "", cssSelector, matchedCallback)
      }
      result = result or matchPartial(partialValuesMap, value, cssSelector, matchedCallback)
      i += 2
    }
    return result
  }

  private fun matchTerminal(map: Map<String, MutableList<SelectorContext<T>>>?,
                            name: String?,
                            cssSelector: Angular2DirectiveSimpleSelector,
                            matchedCallback: ((Angular2DirectiveSimpleSelector, T?) -> Unit)?): Boolean {
    if (map == null || name == null) {
      return false
    }
    val selectables = map.getOrDefault(name, emptyList())
    val starSelectables = map.getOrDefault("*", emptyList())
    if (selectables.isEmpty() && starSelectables.isEmpty()) {
      return false
    }
    var result = false
    for (selectable in ContainerUtil.concat(selectables, starSelectables)) {
      result = selectable.finalize(cssSelector, matchedCallback) || result
    }
    return result
  }

  private fun matchPartial(map: Map<String, Angular2SelectorMatcher<T>>?,
                           name: String?,
                           cssSelector: Angular2DirectiveSimpleSelector,
                           matchedCallback: ((Angular2DirectiveSimpleSelector, T?) -> Unit)?): Boolean {
    if (map == null || name == null) {
      return false
    }
    val nestedSelector = map[name] ?: return false
    // TODO(perf): get rid of recursion and measure again
    // TODO(perf): don't pass the whole selector into the recursion,
    // but only the not processed parts
    return nestedSelector.match(cssSelector, matchedCallback)
  }

  private class SelectorListContext(val selectors: List<Angular2DirectiveSimpleSelector>) {
    var alreadyMatched = false
  }

  // Store context to pass back selector and context when a selector is matched
  private class SelectorContext<T : Any>(val selector: Angular2DirectiveSimpleSelector,
                                         context: T?,
                                         listContext: SelectorListContext?) {
    val notSelectors: List<Angular2DirectiveSimpleSelector>
    val context: T?
    val listContext: SelectorListContext?

    init {
      notSelectors = selector.notSelectors
      this.context = context
      this.listContext = listContext
    }

    fun finalize(cssSelector: Angular2DirectiveSimpleSelector,
                 callback: ((Angular2DirectiveSimpleSelector, T?) -> Unit)?): Boolean {
      var result = true
      if (!notSelectors.isEmpty() && (listContext == null || !listContext.alreadyMatched)) {
        val notMatcher = createNotMatcher<T>(notSelectors)
        result = !notMatcher.match(cssSelector, null)
      }
      if (result && callback != null && (listContext == null || !listContext.alreadyMatched)) {
        if (listContext != null) {
          listContext.alreadyMatched = true
        }
        callback(selector, context)
      }
      return result
    }
  }

  companion object {
    fun <T : Any> createNotMatcher(notSelectors: List<Angular2DirectiveSimpleSelector>): Angular2SelectorMatcher<T> {
      val notMatcher = Angular2SelectorMatcher<T>()
      notMatcher.addSelectables(notSelectors, null)
      return notMatcher
    }
  }
}