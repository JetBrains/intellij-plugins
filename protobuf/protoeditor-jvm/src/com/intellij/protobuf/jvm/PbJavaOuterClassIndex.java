/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.jvm;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.jvm.names.JavaNameGenerator;
import com.intellij.protobuf.jvm.names.NameGeneratorSelector;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ObjectUtils;
import com.intellij.util.indexing.*;
import com.intellij.util.indexing.FileBasedIndex.InputFilter;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Index to map from Java outer class -> proto file.
 *
 * <p>TODO(jvoung): use a StubIndex? That might be cheaper than parsing for more than stubs. Problem
 * is that we don't have the file option expressions available as stubs.
 */
public class PbJavaOuterClassIndex extends ScalarIndexExtension<String> {

  public static final ID<String, Void> INDEX_ID = ID.create("protoeditor.java.outer.class.index");
  private static final DataIndexer<String, Void, FileContent> INDEXER_INSTANCE =
      new OuterClassNameIndexer();

  public static Collection<PbFile> getFilesWithOuterClass(
      Project project, String outerClassName, GlobalSearchScope scope) {
    Collection<VirtualFile> files =
        FileBasedIndex.getInstance().getContainingFiles(INDEX_ID, outerClassName, scope);
    return files
        .stream()
        .map(f -> ObjectUtils.tryCast(PsiManager.getInstance(project).findFile(f), PbFile.class))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @NotNull
  @Override
  public InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(PbFileType.INSTANCE) {
      @Override
      public boolean acceptInput(@NotNull final VirtualFile file) {
        return file.isInLocalFileSystem();
      }
    };
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return INDEX_ID;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return INDEXER_INSTANCE;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  private static class OuterClassNameIndexer implements DataIndexer<String, Void, FileContent> {

    @Override
    @NotNull
    public Map<String, Void> map(@NotNull FileContent inputData) {
      PbFile pbFile = ObjectUtils.tryCast(inputData.getPsiFile(), PbFile.class);
      if (pbFile == null) {
        return Collections.emptyMap();
      }
      Collection<String> outerClassNames = computeOuterClassNames(pbFile);
      Map<String, Void> result = new HashMap<>();
      for (String outerClassName : outerClassNames) {
        result.put(outerClassName, null);
      }
      return result;
    }

    /** Return all of the java outer class names that can be generated from this proto file. */
    @NotNull
    private Collection<String> computeOuterClassNames(PbFile file) {
      List<JavaNameGenerator> nameGenerators = NameGeneratorSelector.selectForFile(file);
      List<String> results = new ArrayList<>();
      for (JavaNameGenerator nameGenerator : nameGenerators) {
        results.addAll(nameGenerator.outerClassNames());
      }
      return results;
    }
  }
}
