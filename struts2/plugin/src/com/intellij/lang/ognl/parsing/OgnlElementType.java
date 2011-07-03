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

package com.intellij.lang.ognl.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.psi.OgnlElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlElementType extends IElementType {

  public OgnlElementType(@NotNull @NonNls final String debugName) {
    super(debugName, OgnlLanguage.INSTANCE);
  }

  public PsiElement createPsiElement(final ASTNode node) {
    return new OgnlElement(node);
  }

}