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

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.lang.PbTextFileType;
import com.intellij.protobuf.lang.psi.PbTextField;
import com.intellij.protobuf.lang.psi.PbTextStringPart;
import com.intellij.protobuf.lang.psi.PbTextStringValue;
import com.intellij.protobuf.lang.util.ProtoString;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.TokenType;
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

    // Build old decoded string and track offsets and decoded slices per part in one O(N) pass
    int numParts = parts.size();
    int[] partStartInHost = new int[numParts];
    TextRange[] partContentRangeInHost = new TextRange[numParts];
    int[] partDecodedStart = new int[numParts];
    int[] partDecodedEnd = new int[numParts];
    StringBuilder oldDecodedBuilder = new StringBuilder();

    int partIdx = 0;
    int nodeOffset = 0;
    for (ASTNode child = element.getNode().getFirstChildNode();
        child != null;
        child = child.getTreeNext()) {
      int childLen = child.getTextLength();
      if (child.getPsi() instanceof PbTextStringPart part && partIdx < numParts) {
        partStartInHost[partIdx] = nodeOffset;
        partContentRangeInHost[partIdx] =
            PbTextStringValueEscaper.getPartContentRangeInHost(part, nodeOffset);
        partDecodedStart[partIdx] = oldDecodedBuilder.length();
        oldDecodedBuilder.append(part.getParsedString().toString());
        partDecodedEnd[partIdx] = oldDecodedBuilder.length();
        partIdx++;
      }
      nodeOffset += childLen;
    }
    String oldDecoded = oldDecodedBuilder.toString();
    int oldLen = oldDecoded.length();

    int decodedRangeStart =
        hostOffsetToDecodedIndex(
            range.getStartOffset(),
            parts,
            partStartInHost,
            partContentRangeInHost,
            partDecodedStart,
            partDecodedEnd,
            oldLen);
    int decodedRangeEnd =
        hostOffsetToDecodedIndex(
            range.getEndOffset(),
            parts,
            partStartInHost,
            partContentRangeInHost,
            partDecodedStart,
            partDecodedEnd,
            oldLen);
    if (decodedRangeEnd < decodedRangeStart) {
      decodedRangeEnd = decodedRangeStart;
    }

    // If the decoded content in the target range is unchanged, return element as-is
    if (newContent.equals(oldDecoded.substring(decodedRangeStart, decodedRangeEnd))) {
      return element;
    }

    // Compute diff between the target slice of oldDecoded and newContent using common prefix and suffix
    int rangeDecodedLen = decodedRangeEnd - decodedRangeStart;
    int newLen = newContent.length();

    int localPrefix = 0;
    while (localPrefix < rangeDecodedLen
        && localPrefix < newLen
        && oldDecoded.charAt(decodedRangeStart + localPrefix) == newContent.charAt(localPrefix)) {
      localPrefix++;
    }

    int localSuffix = 0;
    while (localSuffix < (rangeDecodedLen - localPrefix)
        && localSuffix < (newLen - localPrefix)
        && oldDecoded.charAt(decodedRangeEnd - 1 - localSuffix)
            == newContent.charAt(newLen - 1 - localSuffix)) {
      localSuffix++;
    }

    // Do not split a UTF-16 surrogate pair (e.g. from a \Uxxxxxxxx escape) across prefix or suffix
    if (localPrefix > 0
        && (decodedRangeStart + localPrefix) < oldLen
        && Character.isHighSurrogate(oldDecoded.charAt(decodedRangeStart + localPrefix - 1))
        && Character.isLowSurrogate(oldDecoded.charAt(decodedRangeStart + localPrefix))) {
      localPrefix--;
    }
    if (localSuffix > 0
        && (decodedRangeEnd - localSuffix) > 0
        && Character.isLowSurrogate(oldDecoded.charAt(decodedRangeEnd - localSuffix))
        && Character.isHighSurrogate(oldDecoded.charAt(decodedRangeEnd - localSuffix - 1))) {
      localSuffix--;
    }

    int oldChangeStart = decodedRangeStart + localPrefix;
    int oldChangeEnd = decodedRangeEnd - localSuffix;
    String newReplacement = newContent.substring(localPrefix, newLen - localSuffix);

    if (oldChangeStart == oldChangeEnd && newReplacement.isEmpty()) {
      return element;
    }

    // Diff sliding: if this is a deletion, slide the diff left to align with part boundaries
    if (newReplacement.isEmpty() && oldChangeStart < oldChangeEnd) {
      int bestStart = oldChangeStart;
      int bestEnd = oldChangeEnd;
      int bestScore =
          getBoundaryAlignmentScore(
              oldChangeStart, oldChangeEnd, partDecodedStart, partDecodedEnd);
      int curStart = oldChangeStart;
      int curEnd = oldChangeEnd;

      while (bestScore < 2
          && curStart > decodedRangeStart
          && oldDecoded.charAt(curStart - 1) == oldDecoded.charAt(curEnd - 1)) {
        curStart--;
        curEnd--;
        int score =
            getBoundaryAlignmentScore(curStart, curEnd, partDecodedStart, partDecodedEnd);
        if (score > 0 && score >= bestScore) {
          bestStart = curStart;
          bestEnd = curEnd;
          bestScore = score;
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
        if (oldChangeStart < partDecodedEnd[i]
            || (oldChangeStart == 0 && partDecodedStart[i] == 0)) {
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
    boolean isFullPartDeletion =
        newReplacement.isEmpty()
            && (oldChangeStart <= partDecodedStart[firstAffected])
            && (oldChangeEnd >= partDecodedEnd[lastAffected]);

    if (isFullPartDeletion) {
      ASTNode valueNode = element.getNode();
      if (firstAffected == 0 && lastAffected == numParts - 1) {
        // All parts deleted: collapse to a single empty string
        char defaultQuote = getPartQuote(parts.getFirst());
        PbTextStringValue emptyValue =
            createStringValue(element, String.valueOf(defaultQuote) + defaultQuote);
        valueNode.replaceAllChildrenToChildrenOf(emptyValue.getNode());
        return element;
      }
      // Drop parts firstAffected..lastAffected completely without leaving empty "" parts
      if (lastAffected < numParts - 1) {
        ASTNode firstToRemove = parts.get(firstAffected).getNode();
        ASTNode firstToKeep = parts.get(lastAffected + 1).getNode();
        valueNode.removeRange(firstToRemove, firstToKeep);
      } else {
        ASTNode firstToRemove = parts.get(firstAffected - 1).getNode().getTreeNext();
        if (firstToRemove != null) {
          valueNode.removeRange(firstToRemove, null);
        }
      }
      return element;
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
    if (deltaStart > 0) {
      int prevRawStart = getRawOffsetInPart(firstPart, deltaStart - 1);
      int tokenLen = rawStart - prevRawStart;
      if ((tokenLen == 2 || tokenLen == 3)
          && prefix.charAt(prevRawStart) == '\\'
          && isOctalDigit(prefix.charAt(prevRawStart + 1))) {
        String pad = tokenLen == 2 ? "00" : "0";
        prefix =
            prefix.substring(0, prevRawStart + 1) + pad + prefix.substring(prevRawStart + 1);
      }
    }
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
    char openQuote =
        appendEncoded(
            middle, newReplacement, firstQuote, lastQuote, separator, split, suffix.length() > 1);
    if (openQuote == lastQuote) {
      middle.append(suffix);
    } else {
      String remainingDecoded = lastPart.getParsedString().toString().substring(deltaEnd);
      middle.append(escapeContent(remainingDecoded, openQuote));
      if (!lastPart.isUnterminated()) {
        middle.append(openQuote);
      }
    }
    String middleReplacement = middle.toString();

    PbTextStringValue replacementValue = createStringValue(element, middleReplacement);
    ASTNode valueNode = element.getNode();
    ASTNode firstNode = firstPart.getNode();
    ASTNode anchorAfter = lastPart.getNode().getTreeNext();
    ASTNode newFirst = replacementValue.getNode().getFirstChildNode();
    if (firstAffected == lastAffected && newFirst != null && newFirst.getTreeNext() == null) {
      valueNode.replaceChild(firstNode, newFirst);
    } else if (firstAffected == 0 && lastAffected == numParts - 1) {
      valueNode.replaceAllChildrenToChildrenOf(replacementValue.getNode());
    } else {
      if (newFirst != null) {
        valueNode.addChildren(newFirst, null, firstNode);
      }
      valueNode.removeRange(firstNode, anchorAfter);
    }
    return element;
  }

  private static int hostOffsetToDecodedIndex(
      int hostOffset,
      List<PbTextStringPart> parts,
      int[] partStartInHost,
      TextRange[] partContentRangeInHost,
      int[] partDecodedStart,
      int[] partDecodedEnd,
      int totalDecodedLen) {
    int numParts = parts.size();
    for (int i = 0; i < numParts; i++) {
      TextRange contentRange = partContentRangeInHost[i];
      if (hostOffset <= contentRange.getStartOffset()) {
        return partDecodedStart[i];
      }
      if (hostOffset < contentRange.getEndOffset()) {
        ProtoString parsed = parts.get(i).getParsedString();
        int parsedLen = partDecodedEnd[i] - partDecodedStart[i];
        int relOffset = hostOffset - partStartInHost[i];
        for (int j = 0; j < parsedLen; j++) {
          if (parsed.getOriginalOffset(j) >= relOffset) {
            return partDecodedStart[i] + j;
          }
        }
        return partDecodedEnd[i];
      }
      if (hostOffset == contentRange.getEndOffset()) {
        return partDecodedEnd[i];
      }
    }
    return totalDecodedLen;
  }

  private static int getBoundaryAlignmentScore(
      int start, int end, int[] partDecodedStart, int[] partDecodedEnd) {
    boolean startMatches = false;
    for (int partStart : partDecodedStart) {
      if (start == partStart) {
        startMatches = true;
        break;
      }
    }
    if (!startMatches) {
      return 0;
    }
    for (int partEnd : partDecodedEnd) {
      if (end == partEnd) {
        return 2;
      }
    }
    return 1;
  }

  private static boolean isOctalDigit(char c) {
    return c >= '0' && c <= '7';
  }

  private static String escapeContent(String content, char quote) {
    StringBuilder sb = new StringBuilder(content.length() + 8);
    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      switch (c) {
        case '\007' -> sb.append("\\a");
        case '\b' -> sb.append("\\b");
        case '\f' -> sb.append("\\f");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        case '\013' -> sb.append("\\v");
        case '\\' -> sb.append("\\\\");
        case '"' -> sb.append(quote == '"' ? "\\\"" : "\"");
        case '\'' -> sb.append(quote == '\'' ? "\\'" : "'");
        default -> {
          if (c < 0x20) {
            sb.append('\\')
                .append((char) ('0' + ((c >> 6) & 7)))
                .append((char) ('0' + ((c >> 3) & 7)))
                .append((char) ('0' + (c & 7)));
          } else {
            sb.append(c);
          }
        }
      }
    }
    return sb.toString();
  }

  private static String getPartSeparator(PbTextStringValue element, List<PbTextStringPart> parts) {
    if (parts.size() > 1) {
      StringBuilder sep = new StringBuilder();
      boolean hasComment = false;
      for (ASTNode node = parts.get(0).getNode().getTreeNext();
          node != null && node != parts.get(1).getNode();
          node = node.getTreeNext()) {
        if (node.getElementType() == TokenType.WHITE_SPACE) {
          sep.append(node.getChars());
        } else {
          hasComment = true;
        }
      }
      int lastNewline = sep.lastIndexOf("\n");
      if (lastNewline != -1) {
        return hasComment ? "\n" + sep.substring(lastNewline + 1) : sep.toString();
      }
    }
    int elementOffset = element.getTextRange().getStartOffset();
    CharSequence fileText = element.getContainingFile().getViewProvider().getContents();
    int lineStart = elementOffset;
    while (lineStart > 0 && fileText.charAt(lineStart - 1) != '\n') {
      lineStart--;
    }
    StringBuilder indent = new StringBuilder("\n");
    for (int i = lineStart; i < elementOffset; i++) {
      char c = fileText.charAt(i);
      indent.append(c == '\t' ? '\t' : ' ');
    }
    return indent.toString();
  }

  private static PbTextStringValue createStringValue(
      PbTextStringValue context, String replacementText) {
    PsiFile dummyFile =
        PsiFileFactory.getInstance(context.getProject())
            .createFileFromText(
                "dummy.textproto", PbTextFileType.INSTANCE, "field: " + replacementText);
    PbTextField field = PsiTreeUtil.findChildOfType(dummyFile, PbTextField.class);
    if (field != null && field.getStringValue() != null) {
      return field.getStringValue();
    }
    throw new IncorrectOperationException(
        "Failed to update prototext string value with: " + replacementText);
  }

  private static int getRawOffsetInPart(PbTextStringPart part, int parsedOffset) {
    ProtoString parsed = part.getParsedString();
    int parsedLen = parsed.toString().length();
    if (parsedOffset < parsedLen) {
      return parsed.getOriginalOffset(parsedOffset);
    }
    return PbTextStringValueEscaper.getPartContentRangeInHost(part, 0).getEndOffset();
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
   * moved into a part of its own. Returns the quote character of the last open part.
   */
  private static char appendEncoded(
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
    return quote;
  }

  @Override
  public @NotNull TextRange getRangeInElement(@NotNull PbTextStringValue element) {
    return PbTextStringValueEscaper.getContentRange(element);
  }
}
