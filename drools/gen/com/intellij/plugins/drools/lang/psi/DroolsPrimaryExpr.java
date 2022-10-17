// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsPrimaryExpr extends DroolsExpression, DroolsPrimaryExprVar {

  @Nullable
  DroolsArguments getArguments();

  @Nullable
  DroolsCreator getCreator();

  @Nullable
  DroolsExplicitGenericInvocationSuffix getExplicitGenericInvocationSuffix();

  @NotNull
  List<DroolsExpression> getExpressionList();

  @NotNull
  List<DroolsIdentifier> getIdentifierList();

  @NotNull
  List<DroolsIdentifierSuffix> getIdentifierSuffixList();

  @Nullable
  DroolsMapExpressionList getMapExpressionList();

  @Nullable
  DroolsNonWildcardTypeArguments getNonWildcardTypeArguments();

  @Nullable
  DroolsPrimitiveType getPrimitiveType();

  @Nullable
  DroolsSuperSuffix getSuperSuffix();

}
