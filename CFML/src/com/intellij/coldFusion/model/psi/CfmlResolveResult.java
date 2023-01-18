/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CfmlResolveResult extends PsiElementResolveResult {
  public CfmlResolveResult(@NotNull PsiElement element) {
    super(element);
  }

  public static ResolveResult[] create(Collection<? extends PsiElement> from) {
    final ResolveResult[] results = from.size() > 0 ? new ResolveResult[from.size()] : EMPTY_ARRAY;
    int i = 0;
    for (PsiElement element : from) {
      results[i++] = new CfmlResolveResult(element);
    }
    return results;
  }
}
