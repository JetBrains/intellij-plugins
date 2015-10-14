// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFactoryConstructorDeclaration extends DartComponent {

  @NotNull
  List<DartComponentName> getComponentNameList();

  @Nullable
  DartExpression getExpression();

  @Nullable
  DartFormalParameterList getFormalParameterList();

  @Nullable
  DartFunctionBody getFunctionBody();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartType getType();

  @Nullable
  DartComponentName getComponentName();

}
