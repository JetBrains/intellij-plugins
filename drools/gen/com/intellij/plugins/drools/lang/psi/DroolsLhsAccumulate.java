// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsLhsAccumulate extends DroolsPsiCompositeElement {

  @NotNull
  List<DroolsAccumulateFunctionBinding> getAccumulateFunctionBindingList();

  @NotNull
  List<DroolsConstraint> getConstraintList();

  @NotNull
  DroolsLhsAnd getLhsAnd();

}
