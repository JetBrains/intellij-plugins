// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.folding;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class CfmlFoldingBuilder implements FoldingBuilder, DumbAware {
  @Override
  public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    final PsiElement element = node.getPsi();
    if (element instanceof CfmlFile file) {
      final PsiElement[] children = file.getChildren();
      Collection<FoldingDescriptor> result = new LinkedList<>();
      for (PsiElement child : children) {
        if ((child instanceof CfmlCompositeElement ||
             child instanceof PsiComment)) {
          List<FoldingDescriptor> descriptors = new ArrayList<>();
          addFoldingDescriptors(descriptors, child, document);
          result.addAll(descriptors);
        }
      }
      return result.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }
    return FoldingDescriptor.EMPTY_ARRAY;
  }

  private static void addFoldingDescriptorsFromChildren(List<FoldingDescriptor> descriptors,
                                                        PsiElement tag, @NotNull Document document) {
    for (PsiElement child = tag.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof CfmlCompositeElement || child instanceof PsiComment) {
        addFoldingDescriptors(descriptors, child, document);
      }
    }
  }

  private static void addFoldingDescriptors(final List<FoldingDescriptor> descriptors,
                                            final PsiElement tag,
                                            @NotNull Document document) {

    TextRange elementRange = tag.getTextRange();
    final int start = elementRange.getStartOffset();
    final int end = elementRange.getEndOffset();

    if (start + 1 < end) {
      TextRange range = null;
      ASTNode astNode = tag.getNode();
      IElementType astType = astNode.getElementType();

      if (tag instanceof CfmlTag) {
        //if (tag instanceof CfmlTagFunctionImpl || tag instanceof CfmlTagComponentImpl || tag instanceof CfmlTagScriptImpl) {
        range = buildRangeForBraces(range, astNode, CfmlTokenTypes.R_ANGLEBRACKET, CfmlTokenTypes.LSLASH_ANGLEBRACKET);
        //}
      }
      else if (astType == CfmlElementTypes.FUNCTIONBODY ||
               astType == CfmlElementTypes.BLOCK_OF_STATEMENTS
              ) {
        range = buildRange(range, start, end);
      } else if (astType == CfmlElementTypes.SWITCHEXPRESSION) {
        ASTNode lparen = astNode.findChildByType(CfscriptTokenTypes.L_CURLYBRACKET);
        ASTNode rparen = astNode.findChildByType(CfscriptTokenTypes.R_CURLYBRACKET);
        if (lparen != null && rparen != null) {
          range = buildRange(range, lparen.getStartOffset(), rparen.getTextRange().getEndOffset());
        }
      }
      else if (tag instanceof PsiComment) {
        boolean isColdFusionComment = astNode.getElementType() == CfmlTokenTypes.COMMENT;
        int endIndex = astNode.getText().lastIndexOf(isColdFusionComment ? "--->" : "*/");
        if (endIndex != -1) {
          String commentText = astNode.getText().substring(0, endIndex);
          if (commentText.contains("\n")) {
            int startOffset = tag.getTextRange().getStartOffset();
            range = buildRange(range, startOffset + (isColdFusionComment ? "<!---" : "/*").length(), startOffset + commentText.length());
          }
        }
      }

      if (range != null) {
        descriptors.add(new FoldingDescriptor(astNode, range));
      }

      // TODO: insert condition
      addFoldingDescriptorsFromChildren(descriptors, tag, document);
    }
  }

  private static TextRange buildRangeForBraces(TextRange range,
                                               @NotNull ASTNode astNode,
                                               IElementType lbraceType,
                                               IElementType rbraceType) {
    ASTNode lBrace = astNode.findChildByType(lbraceType);
    ASTNode rBrace = astNode.findChildByType(rbraceType);
    if (lBrace != null && rBrace != null) {
      range = buildRange(range, lBrace.getStartOffset() + 1, rBrace.getStartOffset());
    }
    return range;
  }

  private static TextRange buildRange(TextRange range, int leftOffset, int rightOffset) {
    if (leftOffset + 1 < rightOffset) range = new TextRange(leftOffset, rightOffset);
    return range;
  }

  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    IElementType type = node.getElementType();
    if (type == CfmlElementTypes.FUNCTIONBODY ||
        type == CfmlElementTypes.BLOCK_OF_STATEMENTS ||
        type == CfmlElementTypes.SWITCHEXPRESSION
       ) {
      return "{...}";
    }
    return "...";
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    CodeFoldingSettings settings = CodeFoldingSettings.getInstance();
    final PsiElement element = SourceTreeToPsiMap.treeElementToPsi(node);

    if (element instanceof PsiComment) {
      // find out if file header
      final ASTNode parent = node.getTreeParent();
      ASTNode treePrev = node.getTreePrev();

      if (parent.getElementType() == CfmlStubElementTypes.CFML_FILE && treePrev == null) {
        return CodeFoldingSettings.getInstance().COLLAPSE_FILE_HEADER;
      }
      else {
        return CodeFoldingSettings.getInstance().COLLAPSE_DOC_COMMENTS;
      }
    }
    else if (element instanceof CfmlFunction || node.getElementType() == CfmlElementTypes.FUNCTIONBODY) {
      return settings.COLLAPSE_METHODS;
    }/* else if (element instanceof CfmlComponent) {
      return settings.isCollapseClasses();
    }*/

    return false;
  }
}
