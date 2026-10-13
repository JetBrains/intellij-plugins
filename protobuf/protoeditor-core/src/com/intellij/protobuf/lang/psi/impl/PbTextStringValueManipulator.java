/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.lang.PbTextFileType;
import com.intellij.protobuf.lang.psi.PbTextField;
import com.intellij.protobuf.lang.psi.PbTextStringPart;
import com.intellij.protobuf.lang.psi.PbTextStringValue;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.util.ProtoString;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PbTextStringValueManipulator extends AbstractElementManipulator<PbTextStringValue> {

  @Override
  public PbTextStringValue handleContentChange(
      @NotNull PbTextStringValue element, @NotNull TextRange range, String newContent)
      throws IncorrectOperationException {
    List<PbTextStringPart> parts = element.getStringParts();
    if (parts.isEmpty()) {
      return element;
    }

    String hostText = element.getText();

    // Build old decoded string and track decoded slices per part
    int numParts = parts.size();
    int[] partDecodedStart = new int[numParts];
    int[] partDecodedEnd = new int[numParts];
    StringBuilder oldDecodedBuilder = new StringBuilder();

    for (int i = 0; i < numParts; i++) {
      partDecodedStart[i] = oldDecodedBuilder.length();
      oldDecodedBuilder.append(parts.get(i).getParsedString().toString());
      partDecodedEnd[i] = oldDecodedBuilder.length();
    }
    String oldDecoded = oldDecodedBuilder.toString();

    // If the decoded content is unchanged, return element as-is (avoids re-encoding or reformatting untouched parts)
    if (newContent.equals(oldDecoded)) {
      return element;
    }

    // Compute diff between oldDecoded and newContent using common prefix and suffix
    int oldLen = oldDecoded.length();
    int newLen = newContent.length();

    int prefixLen = 0;
    while (prefixLen < oldLen && prefixLen < newLen && oldDecoded.charAt(prefixLen) == newContent.charAt(prefixLen)) {
      prefixLen++;
    }

    int suffixLen = 0;
    while (suffixLen < (oldLen - prefixLen)
        && suffixLen < (newLen - prefixLen)
        && oldDecoded.charAt(oldLen - 1 - suffixLen) == newContent.charAt(newLen - 1 - suffixLen)) {
      suffixLen++;
    }

    int oldChangeStart = prefixLen;
    int oldChangeEnd = oldLen - suffixLen;
    String newReplacement = newContent.substring(prefixLen, newLen - suffixLen);

    if (oldChangeStart == oldChangeEnd && newReplacement.isEmpty()) {
      return element;
    }

    // Diff sliding: if this is a deletion, slide the diff left to align with part boundaries
    if (newReplacement.isEmpty() && oldChangeStart < oldChangeEnd) {
      int bestStart = oldChangeStart;
      int bestEnd = oldChangeEnd;
      int curStart = oldChangeStart;
      int curEnd = oldChangeEnd;

      while (curStart > 0 && oldDecoded.charAt(curStart - 1) == oldDecoded.charAt(curEnd - 1)) {
        curStart--;
        curEnd--;
        for (int start : partDecodedStart) {
          if (curStart == start) {
            bestStart = curStart;
            bestEnd = curEnd;
            break;
          }
        }
      }
      oldChangeStart = bestStart;
      oldChangeEnd = bestEnd;
    }

    // Determine affected parts
    int firstAffected;
    int lastAffected;
    if (oldChangeStart == oldChangeEnd) {
      int targetPart = numParts - 1;
      for (int i = 0; i < numParts - 1; i++) {
        if (oldChangeStart < partDecodedEnd[i] || (oldChangeStart == 0 && partDecodedStart[i] == 0)) {
          targetPart = i;
          break;
        }
      }
      firstAffected = targetPart;
      lastAffected = targetPart;
    } else {
      firstAffected = 0;
      while (firstAffected < numParts - 1 && partDecodedEnd[firstAffected] <= oldChangeStart) {
        firstAffected++;
      }
      lastAffected = numParts - 1;
      while (lastAffected > firstAffected
          && (partDecodedStart[lastAffected] > oldChangeEnd
              || (partDecodedStart[lastAffected] == oldChangeEnd
                  && partDecodedStart[lastAffected] < partDecodedEnd[lastAffected]))) {
        lastAffected--;
      }
    }

    // Check if this is a complete deletion of one or more whole parts
    boolean isFullPartDeletion = newReplacement.isEmpty()
        && (oldChangeStart <= partDecodedStart[firstAffected])
        && (oldChangeEnd >= partDecodedEnd[lastAffected]);

    if (isFullPartDeletion) {
      if (firstAffected == 0 && lastAffected == numParts - 1) {
        // All parts deleted: collapse to a single empty string
        char defaultQuote = getPartQuote(parts.getFirst());
        return replaceWithText(element, String.valueOf(defaultQuote) + defaultQuote);
      }
      // Drop parts firstAffected..lastAffected completely without leaving empty "" parts
      StringBuilder fullText = new StringBuilder();
      boolean first = true;
      for (int i = 0; i < numParts; i++) {
        if (i >= firstAffected && i <= lastAffected) {
          continue;
        }
        if (!first) {
          if (i == lastAffected + 1 && firstAffected > 0) {
            int prevEnd = parts.get(firstAffected - 1).getStartOffsetInParent() + parts.get(firstAffected - 1).getTextLength();
            int nextStart = parts.get(firstAffected).getStartOffsetInParent();
            fullText.append(hostText, prevEnd, nextStart);
          } else {
            int prevEnd = parts.get(i - 1).getStartOffsetInParent() + parts.get(i - 1).getTextLength();
            int nextStart = parts.get(i).getStartOffsetInParent();
            fullText.append(hostText, prevEnd, nextStart);
          }
        }
        fullText.append(parts.get(i).getText());
        first = false;
      }
      return replaceWithText(element, fullText.toString());
    }

    PbTextStringPart firstPart = parts.get(firstAffected);
    PbTextStringPart lastPart = parts.get(lastAffected);
    char firstQuote = getPartQuote(firstPart);
    char lastQuote = getPartQuote(lastPart);

    int deltaStart = Math.max(0, oldChangeStart - partDecodedStart[firstAffected]);
    int deltaEnd = Math.max(0, oldChangeEnd - partDecodedStart[lastAffected]);

    int rawStart = getRawOffsetInPart(firstPart, deltaStart);
    int rawEnd = getRawOffsetInPart(lastPart, deltaEnd);

    String prefix = firstPart.getText().substring(0, rawStart);
    String suffix = lastPart.getText().substring(rawEnd);

    // Split the replacement across several string parts if the value is already multipart, or if a
    // large block (more than 2 newlines) is pasted into a single-part string. Single newlines (e.g.
    // hitting Enter) in single-part strings are kept in place as \n escapes without splitting.
    boolean split = numParts > 1 || countNewlines(newReplacement) >= 3;
    String separator = split ? getPartSeparator(element, parts) : "";

    StringBuilder middle = new StringBuilder(prefix);
    // Content appended right after a line break at the end of a part starts a part of its own. The
    // decoded text is checked rather than the raw prefix so that a literal backslash-n is not
    // mistaken for a line break.
    if (split
        && !newReplacement.isEmpty()
        && oldChangeStart == oldChangeEnd
        && oldChangeStart == partDecodedEnd[firstAffected]
        && oldChangeStart > partDecodedStart[firstAffected]
        && oldDecoded.charAt(oldChangeStart - 1) == '\n') {
      middle.append(firstQuote).append(separator).append(firstQuote);
    }
    appendEncoded(
        middle, newReplacement, firstQuote, lastQuote, separator, split, suffix.length() > 1);
    middle.append(suffix);
    String middleReplacement = middle.toString();

    StringBuilder fullText = new StringBuilder();
    for (int i = 0; i < firstAffected; i++) {
      if (i > 0) {
        int prevPartEnd = parts.get(i - 1).getStartOffsetInParent() + parts.get(i - 1).getTextLength();
        int curPartStart = parts.get(i).getStartOffsetInParent();
        fullText.append(hostText, prevPartEnd, curPartStart);
      }
      fullText.append(parts.get(i).getText());
    }
    if (firstAffected > 0) {
      int prevPartEnd = parts.get(firstAffected - 1).getStartOffsetInParent() + parts.get(firstAffected - 1).getTextLength();
      int curPartStart = parts.get(firstAffected).getStartOffsetInParent();
      fullText.append(hostText, prevPartEnd, curPartStart);
    }
    fullText.append(middleReplacement);
    for (int i = lastAffected + 1; i < numParts; i++) {
      int prevPartEnd = (i == lastAffected + 1)
          ? (parts.get(lastAffected).getStartOffsetInParent() + parts.get(lastAffected).getTextLength())
          : (parts.get(i - 1).getStartOffsetInParent() + parts.get(i - 1).getTextLength());
      int curPartStart = parts.get(i).getStartOffsetInParent();
      fullText.append(hostText, prevPartEnd, curPartStart);
      fullText.append(parts.get(i).getText());
    }

    return replaceWithText(element, fullText.toString());
  }

  private static String escapeContent(String content, char quote) {
    StringBuilder sb = new StringBuilder(content.length() + 8);
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      switch (c) {
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '"':
          if (quote == '"') {
            sb.append("\\\"");
          } else {
            sb.append('"');
          }
          break;
        case '\'':
          if (quote == '\'') {
            sb.append("\\'");
          } else {
            sb.append('\'');
          }
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  private static String getPartSeparator(PbTextStringValue element, List<PbTextStringPart> parts) {
    if (parts.size() > 1) {
      int prevEnd = parts.get(0).getStartOffsetInParent() + parts.get(0).getTextLength();
      int nextStart = parts.get(1).getStartOffsetInParent();
      String sep = element.getText().substring(prevEnd, nextStart);
      if (sep.contains("\n")) {
        return sep;
      }
    }
    int elementOffset = element.getTextRange().getStartOffset();
    CharSequence fileText = element.getContainingFile().getViewProvider().getContents();
    int lineStart = elementOffset;
    while (lineStart > 0 && fileText.charAt(lineStart - 1) != '\n') {
      lineStart--;
    }
    int indentLen = elementOffset - lineStart;
    return "\n" + " ".repeat(Math.max(0, indentLen));
  }

  private static PbTextStringValue replaceWithText(PbTextStringValue element, String replacementText) {
    PsiFile dummyFile = PsiFileFactory.getInstance(element.getProject())
        .createFileFromText("dummy.textproto", PbTextFileType.INSTANCE, "field: " + replacementText);
    PbTextField field = PsiTreeUtil.findChildOfType(dummyFile, PbTextField.class);
    if (field != null && field.getStringValue() != null) {
      return (PbTextStringValue) element.replace(field.getStringValue());
    }
    throw new IncorrectOperationException("Failed to update prototext string value with: " + replacementText);
  }

  private static int getRawOffsetInPart(PbTextStringPart part, int parsedOffset) {
    ProtoString parsed = part.getParsedString();
    int parsedLen = parsed.toString().length();
    if (parsedOffset < parsedLen) {
      return parsed.getOriginalOffset(parsedOffset);
    }
    TextRange rangeNoQuotes = PbPsiImplUtil.getTextRangeNoQuotes(part);
    int partStart = part.getTextRange().getStartOffset();
    return rangeNoQuotes.getEndOffset() - partStart;
  }

  private static char getPartQuote(PbTextStringPart part) {
    String text = part.getText();
    if (!text.isEmpty()) {
      char c = text.charAt(0);
      if (c == '\'' || c == '"') {
        return c;
      }
    }
    return '"';
  }

  /**
   * Splits {@code text} after each newline, keeping the newline at the end of the line it
   * terminates: {@code "a\nb"} -> {@code ["a\n", "b"]}, {@code "\n"} -> {@code ["\n"]},
   * {@code "a\n"} -> {@code ["a\n"]}, {@code ""} -> {@code [""]}.
   */
  private static List<String> splitAfterNewlines(String text) {
    List<String> segments = new ArrayList<>();
    int start = 0;
    int index;
    while ((index = text.indexOf('\n', start)) != -1) {
      segments.add(text.substring(start, index + 1));
      start = index + 1;
    }
    if (start < text.length() || segments.isEmpty()) {
      segments.add(text.substring(start));
    }
    return segments;
  }

  private static int countNewlines(String text) {
    int count = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        count++;
      }
    }
    return count;
  }

  /**
   * Appends {@code text} as escaped string literal content. Exactly one {@code \n} escape is
   * emitted per newline in {@code text}. When {@code split} is set, the current literal is also
   * closed and a new one opened right after each newline escape, so every line lives in its own
   * string part. {@code suffixHasContent} tells whether untouched content follows the replacement
   * inside the same part; if so and the replacement ends on a newline, that trailing content is
   * moved into a part of its own.
   */
  private static void appendEncoded(
      StringBuilder out,
      String text,
      char firstQuote,
      char lastQuote,
      String separator,
      boolean split,
      boolean suffixHasContent) {
    List<String> segments = splitAfterNewlines(text);
    if (split && suffixHasContent && segments.getLast().endsWith("\n")) {
      segments.add("");
    }
    int last = segments.size() - 1;
    char quote = firstQuote;
    for (int i = 0; i <= last; i++) {
      String segment = segments.get(i);
      boolean endsWithNewline = segment.endsWith("\n");
      String body = endsWithNewline ? segment.substring(0, segment.length() - 1) : segment;
      out.append(escapeContent(body, quote));
      if (endsWithNewline) {
        out.append("\\n");
      }
      if (split && i < last) {
        char nextQuote = (i + 1 == last) ? lastQuote : firstQuote;
        out.append(quote).append(separator).append(nextQuote);
        quote = nextQuote;
      }
    }
  }

  @Override
  public @NotNull TextRange getRangeInElement(@NotNull PbTextStringValue element) {
    return new TextRange(0, element.getTextLength());
  }
}
