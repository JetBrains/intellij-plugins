package org.angularjs.lang.lexer;

import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTokenType extends IElementType {
  public AngularJSTokenType(@NotNull @NonNls String debugName) {
    super(debugName, AngularJSLanguage.INSTANCE);
  }
}
