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
package com.intellij.protobuf.ide.views;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.protobuf.lang.psi.PbDefinition;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbStatement;
import org.jetbrains.annotations.NotNull;

/** StructureViewModel for protobufs */
public class PbStructureViewModel extends StructureViewModelBase
    implements StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {

  PbStructureViewModel(PbFile file, Editor editor) {
    this(file, editor, new PbStructureViewElement(file));
    withSorters(Sorter.ALPHA_SORTER);
    withSuitableClasses(PbStatement.class);
  }

  private PbStructureViewModel(PbFile file, Editor editor, StructureViewTreeElement rootElement) {
    super(file, editor, rootElement);
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    Object value = element.getValue();
    return value instanceof PbFile || value instanceof PbDefinition;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    Object value = element.getValue();
    return !(value instanceof PbFile) && !(value instanceof PbDefinition);
  }

  @Override
  public boolean isAutoExpand(@NotNull StructureViewTreeElement element) {
    return element.getValue() instanceof PbFile;
  }

  @Override
  public boolean isSmartExpand() {
    return false;
  }
}
