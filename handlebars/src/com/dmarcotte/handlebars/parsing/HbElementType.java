package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

class HbElementType extends IElementType {
  private final String _parseExpectedMessageKey;

  /**
   * @param parseExpectedMessageKey Key to the {@link HbBundle} message to show the user when the parser
   *                                expected this token, but found something else.
   */
  public HbElementType(@NotNull @NonNls String debugName, @NotNull @NonNls String parseExpectedMessageKey) {
    super(debugName, HbLanguage.INSTANCE);
    _parseExpectedMessageKey = parseExpectedMessageKey;
  }

  @Override
  public String toString() {
    return "[Hb] " + super.toString();
  }

  public String parseExpectedMessage() {
    return HbBundle.message(_parseExpectedMessageKey);
  }
}
