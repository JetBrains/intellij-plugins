// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface JdlEntityFieldMapping extends PsiElement {

  @NotNull
  List<JdlAnnotation> getAnnotationList();

  @NotNull
  List<JdlFieldConstraint> getFieldConstraintList();

  @NotNull
  JdlFieldName getFieldName();

  @Nullable
  JdlFieldType getFieldType();

  @NotNull String getName();

  @Nullable String getType();

  @NotNull JdlFieldName getNameElement();

  @NotNull ItemPresentation getPresentation();

  boolean isRequired();

}
