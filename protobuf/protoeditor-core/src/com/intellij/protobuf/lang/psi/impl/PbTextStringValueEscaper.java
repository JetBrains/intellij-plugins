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
import com.intellij.protobuf.lang.psi.PbTextStringPart;
import com.intellij.protobuf.lang.psi.PbTextStringValue;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
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
    return new TextRange(0, myHost.getTextLength());
  }

  @Override
  public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {
    List<PbTextStringPart> parts = myHost.getStringParts();
    if (parts.isEmpty()) {
      offsets = new int[] {rangeInsideHost.getEndOffset()};
      return true;
    }

    String hostText = myHost.getText();
    int hostStartOffset = myHost.getTextRange().getStartOffset();
    int totalLen = hostText.length();

    int[] hostOffsets = new int[totalLen + 1];
    int decodedIdx = 0;

    for (PbTextStringPart part : parts) {
      int partStartInHost = part.getTextRange().getStartOffset() - hostStartOffset;
      ProtoString parsed = part.getParsedString();
      String parsedText = parsed.toString();
      int parsedLen = parsedText.length();

      for (int j = 0; j < parsedLen; j++) {
        int originalOffsetInPart = parsed.getOriginalOffset(j);
        int charHostOffset = partStartInHost + originalOffsetInPart;
        if (charHostOffset < rangeInsideHost.getStartOffset() || charHostOffset >= rangeInsideHost.getEndOffset()) {
          continue;
        }
        hostOffsets[decodedIdx++] = charHostOffset;
        outChars.append(parsedText.charAt(j));
      }
    }

    hostOffsets[decodedIdx] = rangeInsideHost.getEndOffset();
    offsets = Arrays.copyOf(hostOffsets, decodedIdx + 1);
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
