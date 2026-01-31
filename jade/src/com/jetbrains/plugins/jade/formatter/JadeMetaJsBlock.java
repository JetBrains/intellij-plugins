// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.FormattingMode;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.formatter.JSBlockContext;
import com.intellij.lang.javascript.formatter.blocks.JSBlock;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JadeMetaJsBlock extends JadeBlock {
  protected JadeMetaJsBlock(@NotNull ASTNode node,
                            @Nullable Wrap wrap,
                            @Nullable Alignment alignment,
                            @NotNull TemplateLanguageBlockFactory blockFactory,
                            @NotNull CodeStyleSettings settings,
                            @Nullable List<DataLanguageBlockWrapper> foreignChildren,
                            Indent indent) {
    super(node, wrap, alignment, blockFactory, settings, foreignChildren, indent);
  }


  @Override
  protected @Nullable Block getBlockForAChild(@NotNull ASTNode child) {
    if (child.getPsi() instanceof JSElement) {
      ASTNode prev = child.getTreePrev();
      if (prev != null && prev.getElementType() == JadeTokenTypes.INDENT && !prev.textContains('\n')) {
        return super.getBlockForAChild(child);
      }
      Indent type = getChildIndentByElementType(child, child.getElementType());

      return new JSBlock(child, null, type, null, null, new JSBlockContext(mySettings, JadeLanguage.INSTANCE, null, FormattingMode.REFORMAT));
    }
    return super.getBlockForAChild(child);
  }
}
