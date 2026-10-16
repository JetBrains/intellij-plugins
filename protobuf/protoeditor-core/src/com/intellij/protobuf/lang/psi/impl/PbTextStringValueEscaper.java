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
import com.intellij.protobuf.lang.psi.PbTextStringPart;
import com.intellij.protobuf.lang.psi.PbTextStringValue;
import com.intellij.protobuf.lang.util.ProtoString;
import com.intellij.psi.LiteralTextEscaper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/** Literal text escaper for prototext string values (supporting single- and multi-part strings). */
public class PbTextStringValueEscaper extends LiteralTextEscaper<PbTextStringValue> {
  private int[] offsets;

  public PbTextStringValueEscaper(@NotNull PbTextStringValue host) {
    super(host);
  }

  @Override
  public @NotNull TextRange getRelevantTextRange() {
    return getContentRange(myHost);
  }

  static @NotNull TextRange getContentRange(@NotNull PbTextStringValue host) {
    int firstStart = -1;
    int lastEnd = -1;
    int fallbackPos = -1;
    int nodeOffset = 0;

    for (ASTNode child = host.getNode().getFirstChildNode();
        child != null;
        child = child.getTreeNext()) {
      int childLen = child.getTextLength();
      if (child.getPsi() instanceof PbTextStringPart part) {
        TextRange partRange = getPartContentRangeInHost(part, nodeOffset);
        if (fallbackPos == -1) {
          fallbackPos = partRange.getStartOffset();
        }
        if (!partRange.isEmpty()) {
          if (firstStart == -1) {
            firstStart = partRange.getStartOffset();
          }
          lastEnd = partRange.getEndOffset();
        }
      }
      nodeOffset += childLen;
    }

    if (firstStart != -1) {
      return new TextRange(firstStart, lastEnd);
    }
    if (fallbackPos != -1) {
      return new TextRange(fallbackPos, fallbackPos);
    }
    return new TextRange(0, host.getTextLength());
  }

  static @NotNull TextRange getPartContentRangeInHost(
      @NotNull PbTextStringPart part, int partStartInHost) {
    String text = part.getText();
    if (!text.startsWith("\"") && !text.startsWith("'")) {
      return new TextRange(partStartInHost, partStartInHost + text.length());
    }
    char quote = text.charAt(0);
    int endOffset =
        (text.length() > 1 && !part.isUnterminated() && text.charAt(text.length() - 1) == quote)
            ? 1
            : 0;
    int start = partStartInHost + 1;
    int end = Math.max(start, partStartInHost + text.length() - endOffset);
    return new TextRange(start, end);
  }

  @Override
  public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {
    List<PbTextStringPart> parts = myHost.getStringParts();
    if (parts.isEmpty()) {
      offsets = new int[] {rangeInsideHost.getEndOffset()};
      return true;
    }

    int totalDecodedLen = 0;
    for (PbTextStringPart part : parts) {
      totalDecodedLen += part.getParsedString().toString().length();
    }

    int[] hostOffsets = new int[totalDecodedLen + 1];
    int decodedIdx = 0;
    int nodeOffset = 0;

    for (ASTNode child = myHost.getNode().getFirstChildNode();
        child != null;
        child = child.getTreeNext()) {
      int childLen = child.getTextLength();
      if (child.getPsi() instanceof PbTextStringPart part) {
        int partStartInHost = nodeOffset;
        ProtoString parsed = part.getParsedString();
        String parsedText = parsed.toString();
        int parsedLen = parsedText.length();

        for (int j = 0; j < parsedLen; j++) {
          int originalOffsetInPart = parsed.getOriginalOffset(j);
          int charHostOffset = partStartInHost + originalOffsetInPart;
          if (charHostOffset < rangeInsideHost.getStartOffset()
              || charHostOffset >= rangeInsideHost.getEndOffset()) {
            continue;
          }
          hostOffsets[decodedIdx++] = charHostOffset;
          outChars.append(parsedText.charAt(j));
        }
      }
      nodeOffset += childLen;
    }

    hostOffsets[decodedIdx] = rangeInsideHost.getEndOffset();
    offsets =
        decodedIdx + 1 == hostOffsets.length
            ? hostOffsets
            : Arrays.copyOf(hostOffsets, decodedIdx + 1);
    return true;
  }

  @Override
  public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange rangeInsideHost) {
    if (offsets == null || offsetInDecoded < 0 || offsetInDecoded >= offsets.length) {
      return -1;
    }
    return Math.min(offsets[offsetInDecoded], rangeInsideHost.getEndOffset());
  }

  @Override
  public boolean isOneLine() {
    return true;
  }
}
