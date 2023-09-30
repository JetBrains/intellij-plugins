// This is a generated file. Not intended for manual editing.
package com.intellij.jhipster.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface JdlEntity extends PsiNameIdentifierOwner {

  @NotNull
  List<JdlAnnotation> getAnnotationList();

  @NotNull
  List<JdlEntityFieldMapping> getEntityFieldMappingList();

  @Nullable
  JdlEntityId getEntityId();

  @Nullable
  JdlEntityTableName getEntityTableName();

}
