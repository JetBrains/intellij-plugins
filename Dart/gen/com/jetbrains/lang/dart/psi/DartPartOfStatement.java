// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.vfs.VirtualFile;

public interface DartPartOfStatement extends DartPsiCompositeElement {

  @Nullable
  DartLibraryId getLibraryId();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartUriElement getUriElement();

  @NotNull
  String getLibraryName();

  @NotNull
  List<VirtualFile> getLibraryFiles();

}
