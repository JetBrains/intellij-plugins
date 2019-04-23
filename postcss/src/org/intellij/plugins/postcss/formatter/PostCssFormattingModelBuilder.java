package org.intellij.plugins.postcss.formatter;

import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Block;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import com.intellij.psi.css.impl.util.editor.CssFormattingModelBuilder;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssFormattingModelBuilder extends CssFormattingModelBuilder {
  @Override
  protected CssFormattingExtension createExtension(CodeStyleSettings settings) {
    return new PostCssFormattingExtension(settings.getCommonSettings(CSSLanguage.INSTANCE),
                                          settings.getCustomSettings(CssCodeStyleSettings.class));
  }

  private static class PostCssFormattingExtension extends CssFormattingExtension {
    private PostCssFormattingExtension(CommonCodeStyleSettings commonSettings, CssCodeStyleSettings customSettings) {
      super(commonSettings, customSettings);
    }

    @Override
    public CssRootBlock createRootBlock(ASTNode _node, int maxPropertyLength, CssFormattingExtension extension) {
      return new PostCssRootBlock(_node, maxPropertyLength, extension);
    }

    private static class PostCssRootBlock extends CssRootBlock {
      private PostCssRootBlock(ASTNode _node, int maxPropertyLength, CssFormattingExtension extension) {
        super(_node, maxPropertyLength, extension);
      }

      @Nullable
      @Override
      public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
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
  }
}
