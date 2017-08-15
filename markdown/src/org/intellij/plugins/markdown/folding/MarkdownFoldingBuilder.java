package org.intellij.plugins.markdown.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownElementVisitor;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.lang.psi.MarkdownRecursiveElementVisitor;
import org.intellij.plugins.markdown.lang.psi.impl.*;
import org.intellij.plugins.markdown.util.MarkdownPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkdownFoldingBuilder extends CustomFoldingBuilder implements DumbAware {
  public static final Map<IElementType, String> TYPES_PRESENTATION_MAP = new HashMap<>();

  static {
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ATX_1, MarkdownBundle.message("markdown.folding.atx.1.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ATX_2, MarkdownBundle.message("markdown.folding.atx.2.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ATX_3, MarkdownBundle.message("markdown.folding.atx.3.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ATX_4, MarkdownBundle.message("markdown.folding.atx.4.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ATX_5, MarkdownBundle.message("markdown.folding.atx.5.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ATX_6, MarkdownBundle.message("markdown.folding.atx.6.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.ORDERED_LIST, MarkdownBundle.message("markdown.folding.ordered.list.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.UNORDERED_LIST, MarkdownBundle.message("markdown.folding.unordered.list.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.BLOCK_QUOTE, MarkdownBundle.message("markdown.folding.block.quote.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.TABLE, MarkdownBundle.message("markdown.folding.table.name"));
    TYPES_PRESENTATION_MAP.put(MarkdownElementTypes.CODE_FENCE, MarkdownBundle.message("markdown.folding.code.fence.name"));
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
      public void visitParagraph(@NotNull MarkdownParagraphImpl paragraph) {
        if (paragraph.getParent() instanceof MarkdownBlockQuoteImpl) return;

        addDescriptors(paragraph);
        super.visitParagraph(paragraph);
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

      @Override
      public void visitCodeFence(@NotNull MarkdownCodeFenceImpl codeFence) {
        addDescriptors(codeFence);
        super.visitCodeFence(codeFence);
      }

      private void addDescriptors(@NotNull MarkdownPsiElement element) {
        if (!isOneLiner(element)) {
          if (element.getTextRange().getLength() > 1) {
            descriptors.add(new FoldingDescriptor(element, element.getTextRange()));
          }
        }
      }

      private boolean isOneLiner(PsiElement element) {
        final TextRange textRange = element.getTextRange();
        int startLine = document.getLineNumber(textRange.getStartOffset());
        int endLine = document.getLineNumber(textRange.getEndOffset() - 1);
        return startLine == endLine;
      }
    });

    root.accept(new MarkdownRecursiveElementVisitor() {
      @Override
      public void visitMarkdownFile(@NotNull MarkdownFile markdownFile) {
        processHeaders(markdownFile);
        super.visitMarkdownFile(markdownFile);
      }

      @Override
      public void visitHeader(@NotNull MarkdownHeaderImpl header) {
        processHeaders(header);
        super.visitHeader(header);
      }

      private void processHeaders(@NotNull MarkdownPsiElement container) {
        MarkdownPsiUtil
          .processContainer(container, element -> descriptors.add(new FoldingDescriptor(element, element.getTextRange())));
      }
    });
  }

  @Override
  protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
    IElementType elementType = PsiUtilCore.getElementType(node);
    String explicitName = TYPES_PRESENTATION_MAP.get(elementType);
    final String prefix = explicitName != null ? explicitName + ": " : "";

    return prefix + StringUtil.shortenTextWithEllipsis(node.getText(), 30, 5);
  }

  @Override
  protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
    return false;
  }
}