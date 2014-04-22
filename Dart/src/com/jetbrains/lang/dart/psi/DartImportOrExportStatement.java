package com.jetbrains.lang.dart.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DartImportOrExportStatement extends DartPsiCompositeElement {
  @NotNull
  List<DartMetadata> getMetadataList();

  @NotNull
  DartPathOrLibraryReference getLibraryExpression();

  @NotNull
  String getUri();

  @NotNull
  List<DartShowCombinator> getShowCombinatorList();

  @NotNull
  List<DartHideCombinator> getHideCombinatorList();
}
