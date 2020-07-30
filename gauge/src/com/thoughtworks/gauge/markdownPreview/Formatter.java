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

package com.thoughtworks.gauge.markdownPreview;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Formatter {
  private Formatter() {
  }

  public static final String REGEX_FOR_TABLE = "\\s*\\|(.*\\n[^*]+)";

  public static String format(String text) {
    Pattern regex = Pattern.compile(REGEX_FOR_TABLE);
    Matcher regexMatcher = regex.matcher(text);
    while (regexMatcher.find()) {
      text = text.replace(regexMatcher.group(0),
                          String.format("\n%s", regexMatcher.group(0).replaceAll("\n\\s+\\|", "\n\t|")));
    }
    return text.replace("<", "&lt;").replace(">", "&gt;");
  }
}