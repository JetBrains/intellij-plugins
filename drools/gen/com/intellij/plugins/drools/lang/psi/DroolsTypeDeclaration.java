// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsTypeDeclaration extends DroolsPsiClass {

  @NotNull
  List<DroolsAnnotation> getAnnotationList();

  @NotNull
  List<DroolsField> getFieldList();

  @Nullable
  DroolsSuperType getSuperType();

  @Nullable
  DroolsTraitable getTraitable();

  @NotNull
  DroolsTypeName getTypeName();

}
