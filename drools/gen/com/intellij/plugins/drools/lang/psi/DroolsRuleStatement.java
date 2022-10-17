// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsRuleStatement extends DroolsPsiCompositeElement {

  @NotNull
  List<DroolsAnnotation> getAnnotationList();

  @Nullable
  DroolsLhs getLhs();

  @Nullable
  DroolsParentRule getParentRule();

  @NotNull
  List<DroolsRhs> getRhsList();

  @Nullable
  DroolsRuleAttributes getRuleAttributes();

  @NotNull
  DroolsRuleName getRuleName();

}
