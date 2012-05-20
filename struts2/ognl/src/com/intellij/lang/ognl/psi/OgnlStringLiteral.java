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

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlStringLiteral extends OgnlExpressionBase {

  public OgnlStringLiteral(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public PsiType getType() {
    return PsiType.getJavaLangString(getManager(), getResolveScope());
  }

  @Override
  public Object getConstantValue() {
    return StringUtil.stripQuotesAroundValue(getText());
  }

}