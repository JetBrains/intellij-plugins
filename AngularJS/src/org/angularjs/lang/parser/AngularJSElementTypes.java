package org.angularjs.lang.parser;

import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.AngularJSLanguage;

/**
 * @author Dennis.Ushakov
 */
public interface AngularJSElementTypes {
  IElementType REPEAT_EXPRESSION = new IElementType("REPEAT_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType FILTER_EXPRESSION = new IElementType("FILTER_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType AS_EXPRESSION     = new IElementType("AS_EXPRESSION", AngularJSLanguage.INSTANCE);
}
