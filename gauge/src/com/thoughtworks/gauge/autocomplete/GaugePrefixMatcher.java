/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.autocomplete;

import com.intellij.codeInsight.completion.PrefixMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GaugePrefixMatcher extends PrefixMatcher {
  private final Pattern regexPattern;

  public GaugePrefixMatcher(String prefix) {
    super(prefix);
    regexPattern = Pattern.compile(toRegex(this.getPrefix()), Pattern.CASE_INSENSITIVE);
  }

  @Override
  public boolean prefixMatches(@NotNull String name) {
    Matcher matcher = regexPattern.matcher(name);
    return matcher.find();
  }

  @Override
  public @NotNull PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
    return new GaugePrefixMatcher(prefix);
  }

  private static String toRegex(String prefix) {
    Pattern pattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
    String substring = "";
    if (!prefix.isEmpty()) substring = String.valueOf(prefix.charAt(0));
    Matcher matcher = pattern.matcher(substring);
    String escape = "";
    if (matcher.matches()) escape = "\\";
    return escape + prefix.replaceAll("\"[\\w ]+\"", "<[\\\\w ]*>");
  }
}
