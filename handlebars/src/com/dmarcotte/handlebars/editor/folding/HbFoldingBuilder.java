package com.dmarcotte.handlebars.editor.folding;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbBlockWrapper;
import com.dmarcotte.handlebars.psi.HbCloseBlockMustache;
import com.dmarcotte.handlebars.psi.HbOpenBlockMustache;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HbFoldingBuilder implements FoldingBuilder, DumbAware {

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
    appendDescriptors(node.getPsi(), descriptors, document);
    return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
  }

  private void appendDescriptors(PsiElement psi, List<FoldingDescriptor> descriptors, Document document) {
    if (isSingleLine(psi, document)) {
      return;
    }

    if (HbTokenTypes.COMMENT == psi.getNode().getElementType()) {
      ASTNode commentNode = psi.getNode();
      String commentText = commentNode.getText();

      // comment might be unclosed, so do a bit of sanity checking on its length and whether or not it's
      // got the requisite open/close tags before we allow folding
      if (commentText.length() > 5
          && commentText.substring(0, 3).equals("{{!")
          && commentText.substring(commentText.length() - 2, commentText.length()).equals("}}")) {
        TextRange range = new TextRange(commentNode.getTextRange().getStartOffset() + 3, commentNode.getTextRange().getEndOffset() - 2);
        descriptors.add(new FoldingDescriptor(commentNode, range));
      }
    }

    if (psi instanceof HbBlockWrapper) {

      PsiElement endOpenBlockStache = getOpenBlockCloseStacheElement(psi.getFirstChild());
      PsiElement endCloseBlockStache = getCloseBlockCloseStacheElement(psi.getLastChild());

      // if we've got a well formed block with the open and close elems we need, define a region to fold
      if (endOpenBlockStache != null && endCloseBlockStache != null) {
        int endOfFirstOpenStacheLine
          = document.getLineEndOffset(document.getLineNumber(psi.getTextRange().getStartOffset()));

        // we set the start of the text we'll fold to be just before the close braces of the open stache,
        //     or, if the open stache spans multiple lines, to the end of the first line
        int foldingRangeStartOffset = Math.min(endOpenBlockStache.getTextRange().getStartOffset(), endOfFirstOpenStacheLine);
        // we set the end of the text we'll fold to be just before the final close braces in this block
        int foldingRangeEndOffset = endCloseBlockStache.getTextRange().getStartOffset();

        TextRange range = new TextRange(foldingRangeStartOffset, foldingRangeEndOffset);

        descriptors.add(new FoldingDescriptor(psi, range));
      }
    }

    PsiElement child = psi.getFirstChild();
    while (child != null) {
      appendDescriptors(child, descriptors, document);
      child = child.getNextSibling();
    }
  }

  /**
   * If the given element is a {@link com.dmarcotte.handlebars.psi.HbOpenBlockMustache} returns the close 'stache node ("}}")
   * <p/>
   * Otherwise, returns null.
   */
  private PsiElement getOpenBlockCloseStacheElement(PsiElement psiElement) {
    if (psiElement == null
        || !(psiElement instanceof HbOpenBlockMustache)) {
      return null;
    }

    PsiElement endOpenStache = psiElement.getLastChild();

    if (endOpenStache == null || endOpenStache.getNode().getElementType() != HbTokenTypes.CLOSE) {
      return null;
    }

    return endOpenStache;
  }

  /**
   * If the given element is a {@link com.dmarcotte.handlebars.psi.HbCloseBlockMustache}, returns the close 'stache node ("}}")
   * <p/>
   * Otherwise, returns null
   */
  private PsiElement getCloseBlockCloseStacheElement(PsiElement psiElement) {
    if (psiElement == null || !(psiElement instanceof HbCloseBlockMustache)) {
      return null;
    }

    PsiElement endCloseStache = psiElement.getLastChild();
    if (endCloseStache == null || endCloseStache.getNode().getElementType() != HbTokenTypes.CLOSE) {
      return null;
    }

    return endCloseStache;
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    return "...";
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return HbConfig.isAutoCollapseBlocksEnabled();
  }

  /**
   * Return true if this psi element does not span more than one line in the given document
   */
  private static boolean isSingleLine(PsiElement element, Document document) {
    TextRange range = element.getTextRange();
    return document.getLineNumber(range.getStartOffset()) == document.getLineNumber(range.getEndOffset());
  }
}
