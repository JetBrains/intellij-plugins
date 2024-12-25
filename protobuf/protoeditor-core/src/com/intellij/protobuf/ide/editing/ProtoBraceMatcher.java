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
package com.intellij.protobuf.ide.editing;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A {@link PairedBraceMatcher} for protobuf and prototext files. */
public class ProtoBraceMatcher implements PairedBraceMatcher {
  private static final BracePair[] PAIRS = {
    new BracePair(ProtoTokenTypes.LBRACE, ProtoTokenTypes.RBRACE, true),
    new BracePair(ProtoTokenTypes.LBRACK, ProtoTokenTypes.RBRACK, false),
    new BracePair(ProtoTokenTypes.LPAREN, ProtoTokenTypes.RPAREN, false),
    new BracePair(ProtoTokenTypes.LT, ProtoTokenTypes.GT, true),
  };

  @Override
  public @NotNull BracePair[] getPairs() {
    return PAIRS;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(
      @NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
