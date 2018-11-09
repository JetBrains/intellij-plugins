/*
 * Copyright 2018 The authors
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


// Generated from ognl.bnf, do not modify
package com.intellij.lang.ognl.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.lang.ognl.OgnlTypes.*;
import com.intellij.lang.ognl.psi.*;

public class OgnlMapEntryElementImpl extends OgnlPsiCompositeElementBase implements OgnlMapEntryElement {

  public OgnlMapEntryElementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull OgnlVisitor visitor) {
    visitor.visitMapEntryElement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof OgnlVisitor) accept((OgnlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public OgnlExpression getKeyExpression() {
    List<OgnlExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlExpression.class);
    return p1.get(0);
  }

  @Override
  @Nullable
  public OgnlExpression getValueExpression() {
    List<OgnlExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, OgnlExpression.class);
    return p1.size() < 2 ? null : p1.get(1);
  }

}
