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
package com.intellij.protobuf.ide.spelling;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.protobuf.lang.psi.ProtoStringPart;
import com.intellij.protobuf.lang.util.ProtoString;
import org.jetbrains.annotations.NotNull;

/** A spellchecking {@link Tokenizer} for {@link ProtoStringPart} elements. */
public class StringPartTokenizer extends Tokenizer<ProtoStringPart> {
  public static final StringPartTokenizer INSTANCE = new StringPartTokenizer();

  @Override
  public void tokenize(@NotNull ProtoStringPart element, TokenConsumer consumer) {
    String parsed = element.getParsedString().toString();
    consumer.consumeToken(
        element, parsed, false, 0, TextRange.allOf(parsed), PlainTextSplitter.getInstance());
  }

  @NotNull
  @Override
  public TextRange getHighlightingRange(PsiElement element, int offset, TextRange range) {
    if (!(element instanceof ProtoStringPart)) {
      return super.getHighlightingRange(element, offset, range);
    }
    ProtoStringPart stringPart = (ProtoStringPart) element;
    ProtoString parsed = stringPart.getParsedString();
    // Modify the text range to account for escape sequences.
    // A TextRange's end offset is exclusive. To get the adjusted position, we need to first
    // subtract one and then add one to the result.
    return TextRange.create(
        parsed.getOriginalOffset(range.getStartOffset()),
        parsed.getOriginalOffset(range.getEndOffset() - 1) + 1);
  }
}
