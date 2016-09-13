// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartImportStatement extends DartImportOrExportStatement {

  @NotNull
  List<DartHideCombinator> getHideCombinatorList();

  @NotNull
  List<DartMetadata> getMetadataList();

  @NotNull
  List<DartShowCombinator> getShowCombinatorList();

  @NotNull
  List<DartStringLiteralExpression> getStringLiteralExpressionList();

  @NotNull
  DartUriElement getUriElement();

  @NotNull
  String getUriString();

  @Nullable
  DartComponentName getImportPrefix();

}
