package org.intellij.plugins.markdown.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownElementVisitor;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownBlockQuoteImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownListImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownTableImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkdownFoldingBuilder extends CustomFoldingBuilder implements DumbAware {
  public static final Map<IElementType, String> TYPES_PRESENTATION_MAP = new HashMap<>();

  static {
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.PARAGRAPH, MarkdownBundle.message("markdown.folding.paragraph.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ORDERED_LIST, MarkdownBundle.message("markdown.folding.ordered.list.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.UNORDERED_LIST, MarkdownBundle.message("markdown.folding.unordered.list.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.BLOCK_QUOTE, MarkdownBundle.message("markdown.folding.block.quote.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.TABLE, MarkdownBundle.message("markdown.folding.table.name"));
  }

  @Override
  protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors,
                                          @NotNull PsiElement root,
                                          @NotNull Document document,
                                          boolean quick) {
    root.accept(new MarkdownElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);
        element.acceptChildren(this);
      }

      @Override
      public void visitList(@NotNull MarkdownListImpl list) {
        addDescriptors(list);
        super.visitList(list);
      }

      @Override
      public void visitTable(@NotNull MarkdownTableImpl table) {
        addDescriptors(table);
        super.visitTable(table);
      }

      @Override
      public void visitBlockQuote(@NotNull MarkdownBlockQuoteImpl blockQuote) {
        addDescriptors(blockQuote);
        super.visitBlockQuote(blockQuote);
      }

      private void addDescriptors(@NotNull MarkdownPsiElement element) {
        if (!isOneLiner(element)) {
          TextRange textRange = element.getTextRange();
          if (textRange.getLength() > 1) {
            descriptors.add(createDescriptor(element, textRange));
          }
        }
      }

      private FoldingDescriptor createDescriptor(PsiElement element, TextRange textRange) {
        final TextRange range = TextRange.create(textRange.getStartOffset(), textRange.getEndOffset());
        return new FoldingDescriptor(element, range);
      }

      private boolean isOneLiner(PsiElement element) {
        final TextRange textRange = element.getTextRange();
        int startLine = document.getLineNumber(textRange.getStartOffset());
        int endLine = document.getLineNumber(textRange.getEndOffset() - 1);
        return startLine == endLine;
      }
    });
  }

  @Override
  protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
    IElementType elementType = PsiUtilCore.getElementType(node);
    String explicitName = TYPES_PRESENTATION_MAP.get(elementType);
    return explicitName == null ? "..." : explicitName;
  }

  @Override
  protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }
}