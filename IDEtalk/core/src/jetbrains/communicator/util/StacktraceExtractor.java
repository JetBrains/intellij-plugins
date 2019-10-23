// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
    String slitted = myStacktraceText.replaceAll("\\bat ([^)\\s]+)[ \t]*[\\r\\n]+[ \t]*([^)]*\\))", "at $1$2");
    slitted = slitted.replaceAll("\\t", "  ");
    slitted = Pattern.compile("^ +", Pattern.MULTILINE).matcher(slitted).replaceAll("  ");
    slitted = slitted.replaceAll("\r\n\r\n", "\r\n").replaceAll("\n\n", "\n");
    myStacktraceText = slitted;
  }


}
