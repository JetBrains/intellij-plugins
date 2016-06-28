package org.intellij.plugins.postcss.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.editor.CssFormattingModelBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlToken;
import org.intellij.plugins.postcss.psi.impl.PostCssNestSymImpl;

import java.util.List;

public class PostCssFormattingModelBuilder extends CssFormattingModelBuilder {

  @Override
  protected CssFormattingExtension createExtension() {
    return PostCssFormattingExtension.INSTANCE;
  }

  protected static class PostCssFormattingExtension extends CssFormattingExtension {
    static final PostCssFormattingExtension INSTANCE = new PostCssFormattingExtension();

    @Override
    public Indent getTokenIndent(XmlToken token, CssCodeStyleSettings settings, IElementType type) {
      if (token.getTokenType() == CssElementTypes.CSS_RBRACE && settings.ALIGN_CLOSING_BRACE_WITH_PROPERTIES) {
        return Indent.getNormalIndent();
      }

      return super.getTokenIndent(token, settings, type);
    }

    @Override
    public boolean addSubBlocksOfExtendedLanguage(PsiElement element,
                                                  CssCodeStyleSettings settings,
                                                  int maxPropertyLength,
                                                  List<Block> result) {
      if (element instanceof PostCssNestSymImpl) {
        result.add(new CssSimpleBlock(element.getNode(), settings, Indent.getNoneIndent(), element.getTextLength(), this));
        return true;
      }

      return false;
    }
  }
}
