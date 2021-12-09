// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.externalizer.StringCollectionExternalizer;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartPartUriIndex extends SingleEntryFileBasedIndexExtension<List<String>> {
  public static final ID<Integer, List<String>> DART_PATH_INDEX = ID.create("DartPathIndex");

  @NotNull
  @Override
  public ID<Integer, List<String>> getName() {
    return DART_PATH_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION + 1;
  }

  @Override
  public @NotNull SingleEntryIndexer<List<String>> getIndexer() {
    return new SingleEntryIndexer<>(false) {
      @Nullable
      @Override
      protected List<String> computeValue(@NotNull FileContent inputData) {
        return DartIndexUtil.indexFile(inputData).getPartUris();
      }
    };
  }

  @NotNull
  @Override
  public DataExternalizer<List<String>> getValueExternalizer() {
    return StringCollectionExternalizer.STRING_LIST_EXTERNALIZER;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(DartFileType.INSTANCE);
  }

  @NotNull
  public static List<String> getPartUris(@NotNull final Project project, @NotNull final VirtualFile virtualFile) {
    return ContainerUtil.notNullize(FileBasedIndex.getInstance().getSingleEntryIndexData(DART_PATH_INDEX, virtualFile, project));
  }
}
