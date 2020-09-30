/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.stub;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.*;
import com.thoughtworks.gauge.language.ConceptFileType;
import com.thoughtworks.gauge.language.SpecFileType;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class GaugeFileStubIndex extends SingleEntryFileBasedIndexExtension<Collection<Integer>> {
  @NonNls
  public static final ID<Integer, Collection<Integer>> NAME = ID.create("GaugeFileStubIndex");

  @NotNull
  @Override
  public ID<Integer, Collection<Integer>> getName() {
    return NAME;
  }

  @Override
  public @NotNull SingleEntryIndexer<Collection<Integer>> getIndexer() {
    return new SingleEntryIndexer<>(false) {
      @Override
      protected @NotNull Collection<Integer> computeValue(@NotNull FileContent fileContent) {
        Set<Integer> offsets = new HashSet<>();
        List<PsiElement> steps = new ArrayList<>();
        PsiFile psiFile = fileContent.getPsiFile();
        if (fileContent.getFileType() instanceof SpecFileType) {
          steps = new ArrayList<>(PsiTreeUtil.collectElementsOfType(psiFile, SpecStepImpl.class));
        }
        else if (fileContent.getFileType() instanceof ConceptFileType) {
          steps = new ArrayList<>(PsiTreeUtil.collectElementsOfType(psiFile, ConceptStepImpl.class));
        }
        steps.forEach((s) -> offsets.add(s.getTextOffset()));
        return offsets;
      }
    };
  }

  @NotNull
  @Override
  public DataExternalizer<Collection<Integer>> getValueExternalizer() {
    return new IntCollectionDataExternalizer();
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(SpecFileType.INSTANCE, ConceptFileType.INSTANCE) {
      @Override
      public boolean acceptInput(@NotNull VirtualFile virtualFile) {
        return virtualFile.getExtension() != null && GaugeUtil.isGaugeFile(virtualFile);
      }
    };
  }

  @Override
  public int getVersion() {
    return 2;
  }
}
