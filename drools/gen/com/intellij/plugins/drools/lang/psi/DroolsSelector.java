// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsSelector extends DroolsPsiCompositeElement {

  @Nullable
  DroolsArguments getArguments();

  @Nullable
  DroolsExpression getExpression();

  @Nullable
  DroolsIdentifier getIdentifier();

  @Nullable
  DroolsInnerCreator getInnerCreator();

  @Nullable
  DroolsNonWildcardTypeArguments getNonWildcardTypeArguments();

  @Nullable
  DroolsSuperSuffix getSuperSuffix();

}
