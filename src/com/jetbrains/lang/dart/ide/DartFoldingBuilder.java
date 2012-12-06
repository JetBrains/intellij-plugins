package com.jetbrains.lang.dart.ide;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 10/17/11
 * Time: 8:42 PM
 */
public class DartFoldingBuilder implements FoldingBuilder, DumbAware {
  @NotNull
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
    appendDescriptors(node, document, descriptors);
    return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
  }

  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    return "...";
  }


  private static ASTNode appendDescriptors(final ASTNode node, final Document document, final List<FoldingDescriptor> descriptors) {
    node.getPsi().getFirstChild();
    for (ASTNode n = node.getFirstChildNode(); n != null; n = n.getTreeNext()) {
      final IElementType elementType = n.getElementType();

      if (elementType == DartTokenTypesSets.SINGLE_LINE_COMMENT) {
        ASTNode treeNext = n.getTreeNext();
        if (isSingleLineWs(treeNext)) {
          treeNext = treeNext.getTreeNext();
          while (treeNext != null && treeNext.getElementType() == DartTokenTypesSets.SINGLE_LINE_COMMENT) {
            ASTNode nextNext = treeNext.getTreeNext();
            if (isSingleLineWs(nextNext)) {
              nextNext = nextNext.getTreeNext();
              if (nextNext != null && nextNext.getElementType() == DartTokenTypesSets.SINGLE_LINE_COMMENT) {
                treeNext = nextNext;
              }
              else {
                break;
              }
            }
            else {
              break;
            }
          }

          if (treeNext != null && treeNext.getElementType() == DartTokenTypesSets.SINGLE_LINE_COMMENT) {
            TextRange foldedRange = new TextRange(n.getStartOffset() + "//".length(), treeNext.getStartOffset() + treeNext.getTextLength());
            descriptors.add(new FoldingDescriptor(n, foldedRange));
            n = treeNext;
          }
        }
      }
      else if (DartTokenTypesSets.COMMENTS.contains(elementType)) {
        TextRange textRange = n.getTextRange();
        if (document.getLineNumber(textRange.getStartOffset()) != document.getLineNumber(textRange.getEndOffset() - 1)) {
          String s = "", e = "";
          if (elementType == DartTokenTypesSets.DOC_COMMENT) {
            s = "/**";
            e = "*/";
          }
          else if (elementType == DartTokenTypesSets.MULTI_LINE_COMMENT) {
            s = "/*";
            e = "*/";
          }

          textRange = new TextRange(
            textRange.getStartOffset() + s.length(),
            textRange.getEndOffset() - e.length()
          );
          descriptors.add(new FoldingDescriptor(n, textRange));
        }
      }
      if (node.getFirstChildNode() != null) appendDescriptors(n, document, descriptors);
    }
    return node;
  }

  private static boolean isSingleLineWs(ASTNode treeNext) {
    if (treeNext != null && treeNext.getElementType() == DartTokenTypesSets.WHITE_SPACE) {
      String text = treeNext.getText();
      if (text.charAt(0) == '\n' && text.indexOf('\n', 1) == -1) {
        return true;
      }
    }
    return false;
  }

  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    if (node.getTreePrev() == null &&
        node.getPsi() instanceof PsiComment) {
      return true;
    }
    return false;
  }
}
