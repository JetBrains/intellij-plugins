// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import org.dartlang.analysis.server.protocol.Outline;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class DartStructureViewModel extends TextEditorBasedStructureViewModel implements StructureViewModel.ElementInfoProvider {

  @NotNull private final StructureViewTreeElement myRootElement;

  private final DartServerData.OutlineListener myListener = filePath -> {
    if (filePath.equals(getPsiFile().getVirtualFile().getPath())) {
      fireModelUpdate();
    }
  };


  public DartStructureViewModel(@Nullable final Editor editor, @NotNull final PsiFile psiFile) {
    super(editor, psiFile);
    myRootElement = new DartStructureViewRootElement(psiFile);
    DartAnalysisServerService.getInstance(getPsiFile().getProject()).addOutlineListener(myListener);
  }

  @Override
  public void dispose() {
    DartAnalysisServerService.getInstance(getPsiFile().getProject()).removeOutlineListener(myListener);
    super.dispose();
  }

  @NotNull
  @Override
  public StructureViewTreeElement getRoot() {
    return myRootElement;
  }

  @Override
  @Nullable
  public Object getCurrentEditorElement() {
    if (getEditor() == null) return null;

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(getPsiFile().getProject());
    final Outline outline = service.getOutline(getPsiFile().getVirtualFile());
    if (outline == null) return null;

    final Outline result = findDeepestOutlineForOffset(getEditor().getCaretModel().getOffset(), outline.getChildren());
    return result != null ? DartStructureViewElement.getValue(result) : null;
  }

  @Nullable
  private Outline findDeepestOutlineForOffset(final int offset, @NotNull final List<Outline> outlines) {
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(getPsiFile().getProject());
    for (Outline outline : outlines) {
      final int startOffset = service.getConvertedOffset(getPsiFile().getVirtualFile(), outline.getOffset());
      final int endOffset = service.getConvertedOffset(getPsiFile().getVirtualFile(), outline.getOffset() + outline.getLength());
      if (offset >= startOffset && offset <= endOffset) {
        final Outline deeperOutline = findDeepestOutlineForOffset(offset, outline.getChildren());
        return deeperOutline != null ? deeperOutline : outline;
      }
    }
    return null;
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    return false;
  }

  @NotNull
  @Override
  public Sorter[] getSorters() {
    return new Sorter[]{Sorter.ALPHA_SORTER};
  }

  private static class DartStructureViewRootElement extends PsiTreeElementBase<PsiFile> {

    public DartStructureViewRootElement(PsiFile file) {super(file);}

    @Nullable
    @Override
    public String getPresentableText() {
      return null;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
      final DartAnalysisServerService service = DartAnalysisServerService.getInstance(getValue().getProject());
      final Outline outline = service.getOutline(getValue().getVirtualFile());
      return outline != null ? Arrays.asList(new DartStructureViewElement(getValue(), outline).getChildren())
                             : Collections.emptyList();
    }
  }
}
