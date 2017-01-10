package org.jetbrains.vuejs.language;

import com.intellij.lang.HtmlScriptContentProvider;
import com.intellij.lexer.HtmlLexer;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

public class VueLexer extends HtmlLexer {
  @Nullable
  @Override
  protected IElementType getCurrentScriptElementType() {
    if (scriptType == null) {
      HtmlScriptContentProvider provider = findScriptContentProvider("text/ecmascript-6");
      if (provider != null) return provider.getScriptElementType();
    }
    return super.getCurrentScriptElementType();
  }
}
