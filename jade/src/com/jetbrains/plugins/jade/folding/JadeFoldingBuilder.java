package com.jetbrains.plugins.jade.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.xml.XmlFoldingBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.text.StringTokenizer;
import com.intellij.xml.util.XmlTagUtil;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import com.jetbrains.plugins.jade.psi.impl.JadeAttributeImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeCommentImpl;
import com.jetbrains.plugins.jade.psi.impl.JadeMixinDeclarationImpl;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class JadeFoldingBuilder extends XmlFoldingBuilder {

  @Override
  public @Nullable TextRange getRangeToFold(PsiElement element) {
    if (element instanceof XmlTag) {
      final ASTNode tagNode = element.getNode();
      PsiElement lastSubelement = XmlTagUtil.getStartTagNameElement((XmlTag)element);
      if (lastSubelement == null) {
        lastSubelement = PsiTreeUtil.findChildOfType(element, JadeAttributeImpl.class);
      }
      if (lastSubelement == null) {
        return null;
      }

      final int nameEnd = lastSubelement.getTextRange().getEndOffset();
      final int end = tagNode.getTextRange().getEndOffset();
      return new TextRange(nameEnd, end);
    }
    else if (element instanceof JadeCommentImpl) {
      final TextRange textRange = element.getTextRange();
      final int commentStartOffset = getCommentMarkerOffset(element.getNode());

      final TextRange result = new TextRange(textRange.getStartOffset() + commentStartOffset,
                                             textRange.getEndOffset());
      if (result.isEmpty()) {
        return null;
      }
      return result;
    }
    else if (element instanceof JadeMixinDeclarationImpl) {
      final PsiElement nameIdentifier = ((JadeMixinDeclarationImpl)element).getNameIdentifier();
      if (nameIdentifier == null) {
        return null;
      }

      int nameEnd = nameIdentifier.getTextRange().getEndOffset();
      int end = element.getTextRange().getEndOffset();
      return new TextRange(nameEnd, end);
    }
    else {
      return null;
    }
  }

  @Override
  protected void doAddForChildren(XmlElement tag, List<FoldingDescriptor> foldings, Document document) {
    for (PsiElement element : tag.getChildren()) {
      final IElementType type = element.getNode().getElementType();
      if (type == JadeStubElementTypes.MIXIN_DECLARATION
        || type == JadeElementTypes.COMMENT) {
        addToFold(foldings, element, document);
      }
    }

    super.doAddForChildren(tag, foldings, document);
  }

  @Override
  public String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
    if (node.getElementType() == JadeElementTypes.COMMENT) {
      return getPlaceholderForComment(node);
    }
    else {
      return super.getPlaceholderText(node);
    }
  }

  private static @Nullable String getPlaceholderForComment(@NotNull ASTNode node) {
    final int offset = getCommentMarkerOffset(node);
    final String leafText = node.getText();

    final StringTokenizer st = new StringTokenizer(leafText.substring(offset).trim(), "\n");
    if (!st.hasMoreTokens()) {
      return null;
    }

    return ' ' + st.nextToken() + (st.hasMoreTokens() ? "..." : "");
  }

  private static int getCommentMarkerOffset(@NotNull ASTNode node) {
    final ASTNode leaf = node.getFirstChildNode();
    if (leaf == null) {
      return 0;
    }

    if (leaf.getElementType() == JadeTokenTypes.COMMENT) {
      return 2;
    }
    else {
      return 3;
    }
  }
}
