/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl.highlight;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.lang.ognl.psi.OgnlTokenTypes;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Matches all brace-types.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlBraceMatcher implements PairedBraceMatcher {

  private final BracePair[] pairs = new BracePair[]{
      new BracePair(OgnlTokenTypes.LBRACKET, OgnlTokenTypes.RBRACKET, false),
      new BracePair(OgnlTokenTypes.LPARENTH, OgnlTokenTypes.RPARENTH, false),
  };

  @Override
  public BracePair[] getPairs() {
    return pairs;
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType iElementType,
                                                 @Nullable final IElementType iElementType1) {
    return true;
  }

  @Override
  public int getCodeConstructStart(final PsiFile psiFile, final int openingBraceOffset) {
    return openingBraceOffset;
  }

}