// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.externalizer.StringCollectionExternalizer;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartPartUriIndex extends FileBasedIndexExtension<String, List<String>> {
  public static final ID<String, List<String>> DART_PATH_INDEX = ID.create("DartPathIndex");
  private final DataIndexer<String, List<String>, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, List<String>> getName() {
    return DART_PATH_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, List<String>, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
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

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  public static List<String> getPartUris(@NotNull final Project project, @NotNull final VirtualFile virtualFile) {
    final List<String> result = new ArrayList<>();
    for (List<String> list : FileBasedIndex.getInstance().getValues(DART_PATH_INDEX, virtualFile.getName(),
                                                                    GlobalSearchScope.fileScope(project, virtualFile))) {
      result.addAll(list);
    }
    return result;
  }

  private static class MyDataIndexer implements DataIndexer<String, List<String>, FileContent> {
    @Override
    @NotNull
    public Map<String, List<String>> map(@NotNull final FileContent inputData) {
      return Collections.singletonMap(inputData.getFileName(), DartIndexUtil.indexFile(inputData).getPartUris());
    }
  }
}
