/*
 * Copyright 2013 The authors
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
package com.intellij.lang.ognl.psi.resolve.variable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public abstract class OgnlVariableReferencesContributor {
  /**
   * @param element
   * @param containingFile
   * @param processor
   * @return {@code false} to stop processing.
   */
  public abstract boolean process(@NotNull PsiElement element,
                                  @NotNull PsiFile containingFile,
                                  @NotNull Processor<OgnlVariableReference> processor);
}
