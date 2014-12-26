package com.jetbrains.lang.dart.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DartImportOrExportStatement extends DartUriBasedDirective {
  @NotNull
  List<DartMetadata> getMetadataList();

  @NotNull
  List<DartShowCombinator> getShowCombinatorList();

  @NotNull
  List<DartHideCombinator> getHideCombinatorList();
}
