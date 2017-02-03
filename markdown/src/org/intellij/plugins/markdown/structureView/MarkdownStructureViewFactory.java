package org.intellij.plugins.markdown.structureView;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.intellij.plugins.markdown.structureView.MarkdownStructureElement.PRESENTABLE_TYPES;

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

    @Nullable
    @Override
    protected Object findAcceptableElement(PsiElement element) {
      // walk up the psi-tree until we find an element from the structure view
      while (element != null && !PRESENTABLE_TYPES.contains(element.getNode().getElementType())) {
        IElementType elementType = element.getParent().getNode().getElementType();

        if (elementType.equals(MarkdownElementTypes.MARKDOWN_FILE)) {
          element = element.getPrevSibling();
        } else {
          element = element.getParent();
        }
      }

      return element;
    }
  }
}
