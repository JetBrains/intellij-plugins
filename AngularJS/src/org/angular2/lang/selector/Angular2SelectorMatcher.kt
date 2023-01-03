// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.selector;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

import static com.intellij.util.containers.ContainerUtil.concat;

public class Angular2SelectorMatcher<T> {

  public static <T> Angular2SelectorMatcher<T> createNotMatcher(List<Angular2DirectiveSimpleSelector> notSelectors) {
    Angular2SelectorMatcher<T> notMatcher = new Angular2SelectorMatcher<>();
    notMatcher.addSelectables(notSelectors, null);
    return notMatcher;
  }

  private final Map<String, List<SelectorContext<T>>> _elementMap = new HashMap<>();
  private final Map<String, Angular2SelectorMatcher<T>> _elementPartialMap = new HashMap<>();
  private final Map<String, List<SelectorContext<T>>> _classMap = new HashMap<>();
  private final Map<String, Angular2SelectorMatcher<T>> _classPartialMap = new HashMap<>();
  private final Map<String, Map<String, List<SelectorContext<T>>>> _attrValueMap = new HashMap<>();
  private final Map<String, Map<String, Angular2SelectorMatcher<T>>> _attrValuePartialMap = new HashMap<>();
  private final List<SelectorListContext> _listContexts = new ArrayList<>();

  public void addSelectables(@NotNull List<Angular2DirectiveSimpleSelector> cssSelectors, @Nullable T context) {
    SelectorListContext listContext = null;
    if (cssSelectors.size() > 1) {
      listContext = new SelectorListContext(cssSelectors);
      this._listContexts.add(listContext);
    }
    for (Angular2DirectiveSimpleSelector selector : cssSelectors) {
      _addSelectable(selector, context, listContext);
    }
  }

  /**
   * Add an object that can be found later on by calling `match`.
   *
   * @param cssSelector  A css selector
   * @param callbackCtxt An opaque object that will be given to the callback of the `match` function
   */
  private void _addSelectable(@NotNull Angular2DirectiveSimpleSelector cssSelector,
                              @Nullable T callbackCtxt,
                              @Nullable SelectorListContext listContext) {
    Angular2SelectorMatcher<T> matcher = this;
    String element = cssSelector.element;
    List<String> classNames = cssSelector.classNames;
    List<String> attrs = cssSelector.attrs;
    SelectorContext<T> selectable = new SelectorContext<>(cssSelector, callbackCtxt, listContext);

    if (StringUtil.isNotEmpty(element)) {
      boolean isTerminal = attrs.isEmpty() && classNames.isEmpty();
      if (isTerminal) {
        _addTerminal(matcher._elementMap, element, selectable);
      }
      else {
        matcher = _addPartial(matcher._elementPartialMap, element);
      }
    }

    for (int i = 0; i < classNames.size(); i++) {
      boolean isTerminal = attrs.size() == 0 && i == classNames.size() - 1;
      String className = classNames.get(i);
      if (isTerminal) {
        _addTerminal(matcher._classMap, className, selectable);
      }
      else {
        matcher = _addPartial(matcher._classPartialMap, className);
      }
    }

    for (int i = 0; i < attrs.size(); i += 2) {
      boolean isTerminal = i == attrs.size() - 2;
      String name = attrs.get(i);
      String value = attrs.get(i + 1);
      if (isTerminal) {
        Map<String, List<SelectorContext<T>>> terminalValuesMap
          = matcher._attrValueMap.computeIfAbsent(name, k -> new HashMap<>());
        _addTerminal(terminalValuesMap, value, selectable);
      }
      else {
        Map<String, Angular2SelectorMatcher<T>> partialValuesMap
          = matcher._attrValuePartialMap.computeIfAbsent(name, k -> new HashMap<>());
        matcher = _addPartial(partialValuesMap, value);
      }
    }
  }

  private void _addTerminal(@NotNull Map<String, List<SelectorContext<T>>> map,
                            @NotNull String name,
                            @NotNull SelectorContext<T> selectable) {
    List<SelectorContext<T>> terminalList = map.computeIfAbsent(name, k -> new SmartList<>());
    terminalList.add(selectable);
  }

  private Angular2SelectorMatcher<T> _addPartial(@NotNull Map<String, Angular2SelectorMatcher<T>> map,
                                                 @NotNull String name) {
    return map.computeIfAbsent(name, k -> new Angular2SelectorMatcher<>());
  }

  /**
   * Find the objects that have been added via `addSelectable`
   * whose css selector is contained in the given css selector.
   *
   * @param cssSelector     A css selector
   * @param matchedCallback This callback will be called with the object handed into `addSelectable`
   * @return boolean true if a match was found
   */
  public boolean match(@NotNull Angular2DirectiveSimpleSelector cssSelector,
                       @Nullable BiConsumer<Angular2DirectiveSimpleSelector, T> matchedCallback) {
    final String element = cssSelector.element;
    final List<String> classNames = cssSelector.classNames;
    final List<String> attrs = cssSelector.attrs;

    _listContexts.forEach(l -> l.alreadyMatched = false);

    boolean result = this._matchTerminal(this._elementMap, element, cssSelector, matchedCallback);
    result |= this._matchPartial(this._elementPartialMap, element, cssSelector, matchedCallback);

    for (String className : classNames) {
      result |= this._matchTerminal(this._classMap, className, cssSelector, matchedCallback);
      result |= this._matchPartial(this._classPartialMap, className, cssSelector, matchedCallback);
    }

    for (int i = 0; i < attrs.size(); i += 2) {
      String name = attrs.get(i);
      String value = attrs.get(i + 1);

      Map<String, List<SelectorContext<T>>> terminalValuesMap = this._attrValueMap.get(name);
      if (StringUtil.isNotEmpty(value)) {
        result |= this._matchTerminal(terminalValuesMap, "", cssSelector, matchedCallback);
      }
      result |= this._matchTerminal(terminalValuesMap, value, cssSelector, matchedCallback);

      Map<String, Angular2SelectorMatcher<T>> partialValuesMap = this._attrValuePartialMap.get(name);
      if (StringUtil.isNotEmpty(value)) {
        result |= this._matchPartial(partialValuesMap, "", cssSelector, matchedCallback);
      }
      result |= this._matchPartial(partialValuesMap, value, cssSelector, matchedCallback);
    }
    return result;
  }

  private boolean _matchTerminal(@Nullable Map<String, List<SelectorContext<T>>> map,
                                 @Nullable String name,
                                 @NotNull Angular2DirectiveSimpleSelector cssSelector,
                                 @Nullable BiConsumer<? super Angular2DirectiveSimpleSelector, ? super T> matchedCallback) {
    if (map == null || name == null) {
      return false;
    }

    List<SelectorContext<T>> selectables = map.getOrDefault(name, Collections.emptyList());
    List<SelectorContext<T>> starSelectables = map.getOrDefault("*", Collections.emptyList());
    if (selectables.isEmpty() && starSelectables.isEmpty()) {
      return false;
    }
    boolean result = false;
    for (SelectorContext<T> selectable : concat(selectables, starSelectables)) {
      result = selectable.finalize(cssSelector, matchedCallback) || result;
    }
    return result;
  }

  private boolean _matchPartial(@Nullable Map<String, Angular2SelectorMatcher<T>> map,
                                @Nullable String name,
                                @NotNull Angular2DirectiveSimpleSelector cssSelector,
                                @Nullable BiConsumer<Angular2DirectiveSimpleSelector, T> matchedCallback) {
    if (map == null || name == null) {
      return false;
    }

    Angular2SelectorMatcher<T> nestedSelector = map.get(name);
    if (nestedSelector == null) {
      return false;
    }
    // TODO(perf): get rid of recursion and measure again
    // TODO(perf): don't pass the whole selector into the recursion,
    // but only the not processed parts
    return nestedSelector.match(cssSelector, matchedCallback);
  }


  private static class SelectorListContext {
    public boolean alreadyMatched = false;
    public final List<Angular2DirectiveSimpleSelector> selectors;

    SelectorListContext(@NotNull List<Angular2DirectiveSimpleSelector> selectors) {
      this.selectors = selectors;
    }
  }

  // Store context to pass back selector and context when a selector is matched
  private static class SelectorContext<T> {
    public final List<Angular2DirectiveSimpleSelector> notSelectors;
    public final Angular2DirectiveSimpleSelector selector;
    public final T context;
    public final SelectorListContext listContext;

    SelectorContext(@NotNull Angular2DirectiveSimpleSelector selector, @Nullable T context, @Nullable SelectorListContext listContext) {
      this.notSelectors = selector.notSelectors;
      this.selector = selector;
      this.context = context;
      this.listContext = listContext;
    }

    boolean finalize(@NotNull Angular2DirectiveSimpleSelector cssSelector,
                     @Nullable BiConsumer<? super Angular2DirectiveSimpleSelector, ? super T> callback) {
      boolean result = true;
      if (!notSelectors.isEmpty() && (listContext == null || !listContext.alreadyMatched)) {
        Angular2SelectorMatcher<T> notMatcher = createNotMatcher(notSelectors);
        result = !notMatcher.match(cssSelector, null);
      }
      if (result && callback != null && (this.listContext == null || !listContext.alreadyMatched)) {
        if (listContext != null) {
          listContext.alreadyMatched = true;
        }
        callback.accept(selector, context);
      }
      return result;
    }
  }
}
