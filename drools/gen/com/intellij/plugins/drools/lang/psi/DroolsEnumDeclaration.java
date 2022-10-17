// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsEnumDeclaration extends DroolsPsiCompositeElement {

  @NotNull
  List<DroolsAnnotation> getAnnotationList();

  @NotNull
  List<DroolsEnumerative> getEnumerativeList();

  @NotNull
  List<DroolsField> getFieldList();

  @NotNull
  DroolsQualifiedName getQualifiedName();

}
