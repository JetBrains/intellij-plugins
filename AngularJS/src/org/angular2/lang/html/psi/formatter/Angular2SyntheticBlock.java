// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.psi.formatter.xml.SyntheticBlock;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.tree.IElementType;

import java.util.List;

import static org.angular2.lang.html.psi.formatter.Angular2HtmlTagBlock.isAngular2AttributeElementType;

public class Angular2SyntheticBlock extends SyntheticBlock {
  public Angular2SyntheticBlock(List<Block> subBlocks,
                                Block parent,
                                Indent indent,
                                XmlFormattingPolicy policy,
                                Indent childIndent) {
    super(subBlocks, parent, indent, policy, childIndent);
  }

  @Override
  protected boolean isAttributeElementType(IElementType elementType) {
    return isAngular2AttributeElementType(elementType);
  }
}
