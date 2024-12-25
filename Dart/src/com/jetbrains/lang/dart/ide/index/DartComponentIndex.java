// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class DartComponentIndex extends FileBasedIndexExtension<String, DartComponentInfo> {
  private static final ID<String, DartComponentInfo> DART_COMPONENT_INDEX = ID.create("DartComponentIndex");

  @Override
  public @NotNull ID<String, DartComponentInfo> getName() {
    return DART_COMPONENT_INDEX;
  }

  @Override
  public @NotNull DataIndexer<String, DartComponentInfo, FileContent> getIndexer() {
    return inputData -> DartIndexUtil.indexFile(inputData).getComponentInfoMap();
  }

  @Override
  public @NotNull KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @Override
  public @NotNull DataExternalizer<DartComponentInfo> getValueExternalizer() {
    return new DartComponentInfoExternalizer();
  }

  @Override
  public @NotNull FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(DartFileType.INSTANCE);
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
  }

  public static Collection<VirtualFile> getAllFiles(final @NotNull String componentName, final @NotNull GlobalSearchScope scope) {
    return FileBasedIndex.getInstance().getContainingFiles(DART_COMPONENT_INDEX, componentName, scope);
  }
}
