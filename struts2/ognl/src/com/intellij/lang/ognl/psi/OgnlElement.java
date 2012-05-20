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

package com.intellij.lang.ognl.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ognl.OgnlFile;
import com.intellij.lang.ognl.OgnlLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlElement extends ASTWrapperPsiElement {

  public OgnlElement(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  public OgnlFile getContainingFile() {
    return (OgnlFile) super.getContainingFile();
  }

  @NotNull
  @Override
  public Language getLanguage() {
    return OgnlLanguage.INSTANCE;
  }

  public String toString() {
    return getNode().getElementType().toString();
  }

}