package org.intellij.plugins.markdown.structureView;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkdownStructureViewFactory implements PsiStructureViewFactory {
  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @NotNull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new MarkdownStructureViewModel(psiFile, editor);
      }
    };
  }

  private static class MarkdownStructureViewModel extends StructureViewModelBase {
    public MarkdownStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
      super(psiFile, editor, new MarkdownStructureElement(psiFile));
    }

    @Override
    protected boolean isSuitable(PsiElement element) {
      if (element == null) {
        return false;
      }
      if (!MarkdownStructureElement.PRESENTABLE_TYPES.contains(element.getNode().getElementType())) {
        return false;
      }
      final PsiElement parent = element.getParent();
      if (MarkdownStructureElement.hasTrivialChild(parent)) {
        return false;
      }
      return true;
    }
  }
}
