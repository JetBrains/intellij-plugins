/*
 * Copyright 2015 The authors
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

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.ognl.psi.OgnlFqnTypeExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class OgnlHighlightingAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof OgnlFqnTypeExpression) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(OgnlHighlighter.FQN_TYPE).create();
    }
  }
}
