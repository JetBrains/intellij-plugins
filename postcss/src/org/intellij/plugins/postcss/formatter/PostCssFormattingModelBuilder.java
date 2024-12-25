package org.intellij.plugins.postcss.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import com.intellij.psi.css.impl.util.editor.CssFormattingModelBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssFormattingModelBuilder extends CssFormattingModelBuilder {
  @Override
  protected @NotNull CssFormattingExtension createExtension(@NotNull CodeStyleSettings settings) {
    return new PostCssFormattingExtension(settings.getCommonSettings(CSSLanguage.INSTANCE),
                                          settings.getCustomSettings(CssCodeStyleSettings.class));
  }

  private static boolean isPostCssSimpleVar(@Nullable ASTNode node) {
    if (node == null) return false;
    PsiElement leaf = PsiTreeUtil.getDeepestFirst(node.getPsi());
    return leaf.getTextLength() == node.getTextLength() &&
           leaf.getNode().getElementType() == PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN;
  }

  private static final class PostCssFormattingExtension extends CssFormattingExtension {
    private PostCssFormattingExtension(CommonCodeStyleSettings commonSettings, CssCodeStyleSettings customSettings) {
      super(commonSettings, customSettings);
    }

    @Override
    public CssRootBlock createRootBlock(ASTNode _node, CssFormattingExtension extension) {
      return new PostCssRootBlock(_node, extension);
    }

    @Override
    public CssSelectorBlock createSelectorBlock(ASTNode node, CssFormattingExtension extension) {
      return new PostCssSelectorBlock(node, extension);
    }

    @Override
    public CssPropertyBlock createPropertyBlock(ASTNode _node,
                                                Indent indent,
                                                CssFormattingExtension extension,
                                                @Nullable Alignment alignment,
                                                Alignment childAlignment) {
      return new PostCssPropertyBlock(_node, indent, extension, alignment, childAlignment);
    }

    @Override
    public boolean isComment(IElementType elementType) {
      return PostCssTokenTypes.POST_CSS_COMMENTS.contains(elementType);
    }

    @Override
    public boolean isLineComment(IElementType elementType) {
      return elementType == PostCssTokenTypes.POST_CSS_COMMENT || super.isLineComment(elementType);
    }
  }

  private static final class PostCssRootBlock extends CssRootBlock {
    private PostCssRootBlock(ASTNode _node, CssFormattingExtension extension) {
      super(_node, extension);
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
      if (child1 instanceof ASTBlock && child2 instanceof ASTBlock) {
        ASTNode node1 = ((ASTBlock)child1).getNode();
        ASTNode node2 = ((ASTBlock)child2).getNode();
        if (node1 != null && node2 != null &&
            node1.getElementType() == PostCssElementTypes.POST_CSS_SIMPLE_VARIABLE_DECLARATION &&
            node2.getElementType() == PostCssElementTypes.POST_CSS_SIMPLE_VARIABLE_DECLARATION) {
          return Spacing.createSpacing(0, 0, 1, true, getKeepBlankLines());
        }
      }
      return super.getSpacing(child1, child2);
    }
  }

  private static final class PostCssSelectorBlock extends CssSelectorBlock {
    private PostCssSelectorBlock(ASTNode node, CssFormattingExtension extension) {
      super(node, Indent.getNoneIndent(), extension);
    }

    @Override
    public @Nullable Spacing getSpacing(Block child1, @NotNull Block child2) {
      if (child1 instanceof ASTBlock && child2 instanceof ASTBlock) {
        if (isPostCssSimpleVar(((ASTBlock)child1).getNode()) || isPostCssSimpleVar(((ASTBlock)child2).getNode())) {
          return Spacing.createSpacing(0, 1, 0, true, getKeepBlankLines());
        }
      }
      return super.getSpacing(child1, child2);
    }
  }

  private static final class PostCssPropertyBlock extends CssPropertyBlock {
    private PostCssPropertyBlock(ASTNode _node,
                                 Indent indent,
                                 CssFormattingExtension extension,
                                 Alignment alignment,
                                 @Nullable Alignment childAlignment) {
      super(_node, indent, extension, alignment, childAlignment);
    }

    @Override
    public @Nullable Spacing getSpacing(Block child1, @NotNull Block child2) {
      if (child1 instanceof ASTBlock && child2 instanceof ASTBlock) {
        if (isPostCssSimpleVar(((ASTBlock)child1).getNode()) || isPostCssSimpleVar(((ASTBlock)child2).getNode())) {
          return Spacing.createSpacing(0, 1, 0, true, getKeepBlankLines());
        }
      }
      return super.getSpacing(child1, child2);
    }
  }
}
