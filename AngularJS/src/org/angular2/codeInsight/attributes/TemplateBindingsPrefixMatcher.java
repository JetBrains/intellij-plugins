// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.completion.impl.CamelHumpMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

public class TemplateBindingsPrefixMatcher extends PrefixMatcher {

  private static final char ASTERISK = '*';
  private static final char ASTERISK_REPLACEMENT = '⭐';

  private final @NotNull PrefixMatcher myDelegate;

  public TemplateBindingsPrefixMatcher(@NotNull PrefixMatcher delegate) {
    this(delegate.getPrefix(), delegate);
  }

  private TemplateBindingsPrefixMatcher(@NotNull String prefix, @NotNull PrefixMatcher delegate) {
    super(prefix);
    myDelegate = delegate.cloneWithPrefix(convert(prefix, true));
  }

  @Override
  public boolean prefixMatches(@NotNull String name) {
    return myDelegate.prefixMatches(convert(name));
  }

  @Override
  public boolean prefixMatches(@NotNull LookupElement element) {
    return myDelegate.prefixMatches(convert(element));
  }

  @Override
  public boolean isStartMatch(String name) {
    return myDelegate.isStartMatch(convert(name));
  }

  @Override
  public boolean isStartMatch(LookupElement element) {
    return myDelegate.isStartMatch(convert(element));
  }

  @Override
  public @NotNull PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
    if (prefix.equals(myPrefix)) {
      return this;
    }
    return new TemplateBindingsPrefixMatcher(prefix, myDelegate);
  }

  @Override
  public int matchingDegree(String string) {
    return myDelegate.matchingDegree(convert(string));
  }

  private static LookupElement convert(LookupElement element) {
    return LookupElementBuilder.create(convert(element.getLookupString()))
      .withCaseSensitivity(element.isCaseSensitive())
      .withLookupStrings(ContainerUtil.map2Set(element.getAllLookupStrings(),
                                               TemplateBindingsPrefixMatcher::convert));
  }

  private static String convert(String input) {
    return convert(input, false);
  }

  private static String convert(String input, boolean forPattern) {
    if (!input.isEmpty() && input.charAt(0) == ASTERISK) {
      if (forPattern) {
        return ASTERISK_REPLACEMENT + CamelHumpMatcher.applyMiddleMatching(input.substring(1));
      }
      char[] chars = input.toCharArray();
      chars[0] = ASTERISK_REPLACEMENT;
      return new String(chars);
    }
    return input;
  }
}
