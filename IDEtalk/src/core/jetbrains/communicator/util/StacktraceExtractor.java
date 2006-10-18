/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kir
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class StacktraceExtractor {
  private static final Pattern EX_PREPATTERN =
      Pattern.compile("^([\\t ]+\\[\\w+\\]\\s+|)[.\\w]+(Exception|Failure|Error|Throwable)", Pattern.MULTILINE);
  private static final Pattern EX_PATTERN =
      Pattern.compile("^.*\\bat [^)\\n\\r]+\\)", Pattern.MULTILINE);

  private final String myOriginalText;

  private String myStacktraceText;
  private String myMessageText;

  public StacktraceExtractor(String originalText) {
    myOriginalText = originalText;
  }

  public boolean containsStacktrace() {
    process();
    return myStacktraceText != null;
  }

  public String getMessageText() {
    return myMessageText;
  }

  public String getStacktrace() {
    process();
    return myStacktraceText;
  }

  private void process() {
    if (myMessageText != null) return;

    Matcher matcher = EX_PATTERN.matcher(myOriginalText);
    if (matcher.find()) {
      myStacktraceText = myOriginalText.substring(matcher.start());
      myMessageText = myOriginalText.substring(0, matcher.start());

      Matcher prefixMatcher = EX_PREPATTERN.matcher(myOriginalText);
      if (prefixMatcher.find()) {
        if (prefixMatcher.start() < matcher.start()) {
          myStacktraceText = myOriginalText.substring(prefixMatcher.start());
          myMessageText = myOriginalText.substring(0, prefixMatcher.start());
        }
      }
    }
    else {
      myMessageText = myOriginalText;
    }

    normalizeStacktrace();
  }

  private void normalizeStacktrace() {
    if (myStacktraceText == null) return;
    String splitted = myStacktraceText.replaceAll("\\bat ([^)\\s]+)[ \t]*[\\r\\n]+[ \t]*([^)]*\\))", "at $1$2");
    splitted = splitted.replaceAll("\\t", "  ");
    splitted = Pattern.compile("^ +", Pattern.MULTILINE).matcher(splitted).replaceAll("  ");
    splitted = splitted.replaceAll("\r\n\r\n", "\r\n").replaceAll("\n\n", "\n");
    myStacktraceText = splitted;
  }


}
