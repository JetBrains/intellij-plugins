/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.tapestry.lang.TelFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public abstract class TelCompositeElementType extends IElementType implements ICompositeElementType {

  TelCompositeElementType(@NotNull @NonNls final String debugName) {
    super(debugName, TelFileType.INSTANCE.getLanguage());
  }

  public abstract PsiElement createPsiElement(ASTNode node);

  @NotNull
  public ASTNode createCompositeNode() {
    return new CompositeElement(this);
  }

  public static final TelCompositeElementType REFERENCE_EXPRESSION = new TelCompositeElementType("ReferenceExpression") {
    public PsiElement createPsiElement(final ASTNode node) {
      return new TelCompositeElement(node);
    }
  };

  public static final TelCompositeElementType METHOD_CALL_EXPRESSION = new TelCompositeElementType("MethodCallExpression") {
    public PsiElement createPsiElement(final ASTNode node) {
      return new TelCompositeElement(node);
    }
  };

  public static final TelCompositeElementType RANGE_EXPRESSION = new TelCompositeElementType("RangeExpression") {
    public PsiElement createPsiElement(ASTNode node) {
      return new TelCompositeElement(node);
    }
  };

  public static final TelCompositeElementType NOT_OP_EXPRESSION = new TelCompositeElementType("Not_Op_Expression") {
    public PsiElement createPsiElement(ASTNode node) {
      return new TelCompositeElement(node);
    }
  };

  public static final TelCompositeElementType LIST_EXPRESSION = new TelLiteralExpressionType("ListExpression", CommonClassNames.JAVA_UTIL_LIST);
  public static final TelCompositeElementType STRING_LITERAL = new TelLiteralExpressionType("StringLiteral", CommonClassNames.JAVA_LANG_STRING);
  public static final TelCompositeElementType INTEGER_LITERAL = new TelLiteralExpressionType("IntegerLiteral", PsiType.INT);
  public static final TelCompositeElementType DECIMAL_LITERAL = new TelLiteralExpressionType("DoubleLiteral", PsiType.DOUBLE);
  public static final TelCompositeElementType BOOLEAN_LITERAL = new TelLiteralExpressionType("BooleanLiteral", PsiType.BOOLEAN);
  public static final TelCompositeElementType NULL_LITERAL = new TelLiteralExpressionType("NullLiteral", PsiType.NULL);
}
