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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 28.04.2009
 * Time: 14:09:53
 * To change this template use File | Settings | File Templates.
 */
public class CfmlLiteralExpressionType extends CfmlCompositeElementType {
  private final String myTypeName;
  private final PsiPrimitiveType myPrimitiveType;

  public CfmlLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull String typeName) {
    super(debugName);
    myTypeName = typeName;
    myPrimitiveType = null;
  }

  public CfmlLiteralExpressionType(@NotNull @NonNls String debugName, @NotNull PsiType primitiveType) {
    super(debugName);
    myTypeName = null;
    assert primitiveType instanceof PsiPrimitiveType;
    myPrimitiveType = (PsiPrimitiveType)primitiveType;
  }

  @Override
  public PsiElement createPsiElement(ASTNode node) {
    return new CfmlLiteralExpression(node);
  }

  class CfmlLiteralExpression extends CfmlCompositeElement implements CfmlExpression {
    public CfmlLiteralExpression(@NotNull final ASTNode node) {
      super(node);
    }

    @Nullable
    public PsiType getPsiType() {
      if (myPrimitiveType != null) {
        return myPrimitiveType;
      }
      return CfmlPsiUtil.getTypeByName(myTypeName, getProject());
    }
  }
}
