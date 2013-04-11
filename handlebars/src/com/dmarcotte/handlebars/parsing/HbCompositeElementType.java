package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Distinct interface to distinguish the leaf elements we get from the lexer from the synthetic
 * composite elements we create in the parser
 */
class HbCompositeElementType extends IElementType {
  public HbCompositeElementType(@NotNull @NonNls String debugName) {
    super(debugName, HbLanguage.INSTANCE);
  }
}
