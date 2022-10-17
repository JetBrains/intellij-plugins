// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsLhsPattern extends DroolsPsiCompositeElement {

  @NotNull
  List<DroolsConstraint> getConstraintList();

  @NotNull
  DroolsLhsPatternType getLhsPatternType();

  @Nullable
  DroolsPatternFilter getPatternFilter();

  @Nullable
  DroolsPatternSource getPatternSource();

}
