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
package com.intellij.protobuf.ide.folding;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Collection;

/** Common utilities for building folding regions. */
public final class ProtoFoldingUtils {

  public static <T> void addIfNotNull(Collection<T> collection, T item) {
    if (item != null) {
      collection.add(item);
    }
  }

  public static boolean isOnOwnLine(PsiElement element, Document document) {
    PsiElement prevElement = PsiTreeUtil.prevVisibleLeaf(element);
    PsiElement nextElement = PsiTreeUtil.nextVisibleLeaf(element);
    TextRange prevRange = prevElement != null ? prevElement.getTextRange() : null;
    TextRange nextRange = nextElement != null ? nextElement.getTextRange() : null;
    TextRange currentRange = element.getTextRange();

    int startLine = document.getLineNumber(currentRange.getStartOffset());
    int endLine = document.getLineNumber(currentRange.getEndOffset());

    if (prevRange != null && document.getLineNumber(prevRange.getEndOffset()) == startLine) {
      return false;
    }
    if (nextRange != null && document.getLineNumber(nextRange.getStartOffset()) == endLine) {
      return false;
    }
    return true;
  }

  /**
   * A utility class that tracks consecutive elements of the same type and builds a folding region
   * containing them. Used to build folding regions around blocks of line comments.
   */
  public static class ConsecutiveElementGrouper {
    private PsiElement firstElement = null;
    private PsiElement lastElement = null;

    public FoldingDescriptor pushElement(PsiElement element) {
      FoldingDescriptor result = null;
      if (lastElement == null) {
        firstElement = element;
        lastElement = element;
      } else if (element.getPrevSibling() == lastElement
          || (element.getPrevSibling() instanceof PsiWhiteSpace
              && element.getPrevSibling().getPrevSibling() == lastElement)) {
        lastElement = element;
      } else {
        result = buildBlock();
        firstElement = element;
        lastElement = element;
      }
      return result;
    }

    public FoldingDescriptor buildBlock() {
      FoldingDescriptor result = null;
      if (firstElement != lastElement) {
        result =
            new FoldingDescriptor(
                firstElement.getNode(),
                new TextRange(
                    firstElement.getTextRange().getStartOffset(),
                    lastElement.getTextRange().getEndOffset()));
      }
      firstElement = null;
      lastElement = null;
      return result;
    }
  }
}
